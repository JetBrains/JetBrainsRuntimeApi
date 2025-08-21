/*
 * Copyright 2000-2025 JetBrains s.r.o.
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

import java.awt.Window;
import java.awt.image.BufferedImage;

/**
 * Provides a way to take screenshots of an individual {@link java.awt.Window} without involving the operating
 * system's window manager.
 */
@Service
@Provided
public interface Screenshoter {
    /**
     * <p>Returns a snapshot of the window's client area from its backbuffer, if any.
     * If {@code window} has no associated backbuffer, {@code null} is returned.</p>
     *
     * <blockquote>Note: the use of the backbuffer is primarily controlled by the {@code swing.bufferPerWindow}
     * system property ({@code true} by default).</blockquote>
     *
     * <p>The coordinates and size are given in the window's client area coordinate system,
     * which is the area of a window not covered by the window's decorations such as the system title bar.
     * The pixel with the coordinates {@code (x, y)} of the window's client area will be at
     * {@code (0, 0)} in the returned image.</p>
     *
     * <p>{@code width} and {@code height} are clipped to the window's client area, so {@code Integer.MAX_VALUE} can
     * be used safely.</p>
     *
     * <p>{@link java.lang.IllegalArgumentException} is thrown if either of the following holds:</p>
     * <ul>
     * <li>{@code width} or {@code height} is negative or zero.</li>
     * <li>{@code x} or {@code y} are outside the window's client area.</li>
     * </ul>
     *
     * @param window window to take a screenshot of; must not be {@code null}
     * @param x x coordinate within the window's client area
     * @param y y coordinate within the window's client area
     * @param width width of the screenshot; clipped to the window's client area
     * @param height height of the screenshot; clipped to the window's client area
     * @return a snapshot of the window's client area from the window's backbuffer, if any.
     *         {@code null} if the window has no backbuffer or if the window is not visible.
     *         The returned image is a copy of the image in the buffer strategy, so the caller can modify it.
     */
    BufferedImage getWindowBackbufferArea(Window window, int x, int y, int width, int height);

    /**
     * <p>Captures the specified area of the client region of a given {@code window} from
     * its underlying pixel representation, and returns it as an image.</p>
     *
     * <p>The coordinates and size are given in the window's client area coordinate system,
     * which is the area of a window not covered by the window's decorations such as the system title bar.
     * The pixel with the coordinates {@code (x, y)} of the window's client area will be at
     * {@code (0, 0)} in the returned image.</p>
     *
     * <p>{@link java.lang.IllegalArgumentException} is thrown if either of the following holds:</p>
     * <ul>
     * <li>{@code width} or {@code height} is negative or zero.</li>
     * <li>{@code x} or {@code y} are outside the window's client area.</li>
     * </ul>
     *
     * <blockquote><b>Note</b>: Currently available only on Linux with WLToolkit.</blockquote>
     *
     * @param window window to take a screenshot of; must not be {@code null}
     * @param x x coordinate within the window's client area
     * @param y y coordinate within the window's client area
     * @param width width of the area to screenshot; clipped to the window's client area
     * @param height height of the area to screenshot; clipped to the window's client area
     * @return a snapshot of the window's client area from its underlying graphics subsystem representation.
     *         {@code null} if the window is not visible.
     *         Since the actual pixels of the window may be scaled depending on Java or operating system settings,
     *         the size of the returned image may differ from {@code width} and {@code height}.
     *         The returned image is a copy, so the caller can modify it.
     */
    BufferedImage getWindowSurfaceArea(Window window, int x, int y, int width, int height);
}
