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
import java.awt.geom.Rectangle2D;

/**
 * Graphics2D utilities.
 */
@Service
@Provided
public interface GraphicsUtils {

    /**
     * Constructs {@link Graphics2D} instance, delegating all calls
     * to given {@code graphics2D} and combining it with given
     * {@code constrainable} handler.
     * @param graphics2D Graphics2D delegate
     * @param constrainable ConstrainableGraphics2D handler
     * @return combined Graphics2D instance
     */
    Graphics2D createConstrainableGraphics(Graphics2D graphics2D,
                                           ConstrainableGraphics2D constrainable);

    /**
     * Allows to permanently install a rectangular maximum clip that
     * cannot be extended with setClip.
     * This is similar to {@code sun.awt.ConstrainableGraphics},
     * but allows floating-point coordinates.
     */
    @Provides
    public interface ConstrainableGraphics2D {
        /**
         * Destination that this Graphics renders to.
         * Similar to {@code sun.java2d.SunGraphics2D#getDestination()}.
         * @return rendering destination
         */
        Object getDestination();

        /**
         * Constrain this graphics object to have a permanent device space
         * origin of (x, y) and a permanent maximum clip of (x,y,w,h).
         * This overload allows floating-point coordinates.
         * @param region constraint rectangle
         * @see #constrain(int, int, int, int)
         */
        void constrain(Rectangle2D region);

        /**
         * Constrain this graphics object to have a permanent device space
         * origin of (x, y) and a permanent maximum clip of (x,y,w,h).
         * Similar to {@code sun.awt.ConstrainableGraphics#constrain(int, int, int, int)}.
         * @param x x coordinate of the constraint rectangle
         * @param y y coordinate of the constraint rectangle
         * @param w width of the constraint rectangle
         * @param h height of the constraint rectangle
         */
        void constrain(int x, int y, int w, int h);
    }

    /**
     * Checks whether the display is built-in.
     * Supported only on macOS (can be implemented in other OS if necessary).
     * @param display display to check
     * @return true when display is built-in.
     */
    boolean isBuiltinDisplay(GraphicsDevice display);
}
