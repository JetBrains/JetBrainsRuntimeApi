/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jetbrains;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

/**
 * Provides convenience methods to access {@link java.awt.FontMetrics} instances, and obtain character advances from them without
 * rounding. Also provides an (unsafe) way to override character advances in those instances with arbitrary specified
 * values.
 */
@Service
@Provided
@Fallback(FontMetricsAccessor_Fallback.class)
public interface FontMetricsAccessor {
    /**
     * Returns a {@link FontMetrics} instance for the given {@link Font} and {@link FontRenderContext}. This is supposed
     * to be the same instance as returned by the public API methods ({@link Graphics#getFontMetrics()},
     * {@link Graphics#getFontMetrics(Font)} and {@link Component#getFontMetrics(Font)}) in the same context.
     * @param font    the specified font
     * @param context font rendering context
     * @return font metrics of the specified font
     */
    FontMetrics getMetrics(Font font, FontRenderContext context);

    /**
     * Returns not rounded value for the character's advance. It's not accessible directly via public
     * {@link FontMetrics} API, one can only extract it from one of the {@code getStringBounds} methods' output.
     * @param metrics   font metrics orbject
     * @param codePoint code point
     * @return advance of the specified code point
     */
    float codePointWidth(FontMetrics metrics, int codePoint);

    /**
     * Allows to override advance values returned by the specified {@link FontMetrics} instance. It's not generally
     * guaranteed the invocation of this method actually has the desired effect. One can verify whether it's the case
     * using {@link #hasOverride(FontMetrics)} method.
     * <p>
     * A subsequent invocation of this method will override any previous invocations. Passing {@code null} as the
     * {@code overrider} value will remove any override, if it was set up previously.
     * <p>
     * While this method operates on a specific {@link FontMetrics} instance, it's expected that overriding will have
     * effect regardless of the method font metrics are accessed, for all future character advance requests. This is
     * feasible, as JDK implementation generally uses the same cached {@link FontMetrics} instance in identical
     * contexts.
     * <p>
     * The method doesn't provides any concurrency guarantees, i.e. the override isn't guaranteed to be immediately
     * visible for font metrics readers in other threads.
     * <p>
     * WARNING. This method can break the consistency of a UI application, as previously calculated/returned advance
     * values can already be used/cached by some UI components. It's the calling code's responsibility to remediate such
     * consequences (e.g. re-validating all components which use the relevant font might be required).
     *
     * @param metrics   font metrics to override
     * @param overrider override handler
     */
    void setOverride(FontMetrics metrics, Overrider overrider);

    /**
     * Tells whether character advances returned by the specified {@link FontMetrics} instance are overridden using the
     * previous {@link #setOverride(FontMetrics, Overrider)} call.
     * @param metrics font metrics being checked
     * @return true if given font metrics is
     * {@linkplain #setOverride(FontMetrics, com.jetbrains.FontMetricsAccessor.Overrider)  overridden}
     */
    boolean hasOverride(FontMetrics metrics);

    /**
     * Removes all overrides set previously by {@link #setOverride(FontMetrics, Overrider)} invocations.
     */
    void removeAllOverrides();

    /**
     * Font metrics override handler.
     * @see #setOverride(FontMetrics, Overrider)
     */
    @Provides
    interface Overrider {
        /**
         * Returning {@code NaN} means the default (not overridden) value should be used.
         * @param codePoint code point
         * @return advance of the specified code point
         */
        float charWidth(int codePoint);
    }

}

final class FontMetricsAccessor_Fallback implements FontMetricsAccessor {
    private final Graphics2D g;

    FontMetricsAccessor_Fallback() {
        g = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
    }

    @Override
    public FontMetrics getMetrics(Font font, FontRenderContext context) {
        synchronized (g) {
            g.setTransform(context.getTransform());
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, context.getAntiAliasingHint());
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, context.getFractionalMetricsHint());
            return g.getFontMetrics(font);
        }
    }

    @Override
    public float codePointWidth(FontMetrics metrics, int codePoint) {
        String s = new String(new int[]{codePoint}, 0, 1);
        return (float) metrics.getFont().getStringBounds(s, metrics.getFontRenderContext()).getWidth();
    }

    @Override
    public void setOverride(FontMetrics metrics, Overrider overrider) {}

    @Override
    public boolean hasOverride(FontMetrics metrics) {
        return false;
    }

    @Override
    public void removeAllOverrides() {}
}
