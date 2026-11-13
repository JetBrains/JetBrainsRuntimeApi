/*
 * Copyright 2026 JetBrains s.r.o.
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

/**
 * <p>Paces animations: gives a callback on the next refresh of the display that shows a window.</p>
 *
 * <p>The runtime takes the refreshes from the vertical synchronization (vsync) signal of the display.
 * The service works like {@code requestAnimationFrame} of a browser.
 * A client that animates requests a callback, updates its state in the callback, repaints,
 * and requests the next callback from there.
 * The runtime listens to the vsync signal only while a request is pending.</p>
 *
 * <p>A supported service always calls back. While the vsync signal is not available,
 * for example in a remote session, the runtime calls back on a timer.</p>
 *
 * <p><b>Note:</b> Supported on macOS and Windows only, and not in a headless session.</p>
 */
@Service
@Provided
public interface DisplayRefresh {
    /**
     * Requests one call of {@code callback} on the next refresh of the display that shows {@code window}.
     * <p>
     * The runtime calls the callback once, on the event dispatch thread.
     * All callbacks that wait for the same refresh of the same display get the same frame time
     * and run in the order of their requests, in one event.
     * The refreshes that occur while the event dispatch thread is busy merge into one call.
     * A request from inside a callback waits for the next refresh.
     * <p>
     * The runtime finds the display of the window at each request.
     * A client that makes a new request for each callback follows the window to another display.
     * The callback also comes for a window that is not visible.
     * The runtime then uses the display of the graphics configuration of the window.
     * <p>
     * This method can be called from any thread.
     *
     * @param window the window whose display paces the callback,
     *               or {@code null} to follow the default display
     * @param callback the callback to call once
     * @throws java.lang.NullPointerException if {@code callback} is {@code null}
     */
    void requestFrame(Window window, FrameCallback callback);

    /**
     * Receives the call that {@link #requestFrame(Window, FrameCallback)} requested.
     */
    @Provides
    interface FrameCallback {
        /**
         * Reports a refresh of the display.
         * <p>
         * The frame time is the estimated time when the content painted in this call reaches the display.
         * It is on the time line of {@link System#nanoTime()} and can be in the future.
         * When the runtime calls back on a timer, the frame time is the time of the timer tick.
         * The frame times of one display never go back.
         * <p>
         * Animations must take their progress from the frame time, not from the number of calls:
         * the frame times of two consecutive calls can be more than one refresh period apart.
         * <p>
         * The refresh period is the time between two refreshes of the display.
         * The vsync signal reports the current one, which changes on a display with a variable refresh rate.
         * On a timer it is the one of the display mode.
         *
         * @param frameTimeNanos the frame time, in nanoseconds
         * @param refreshPeriodNanos the refresh period, in nanoseconds, always positive
         */
        void onFrame(long frameTimeNanos, long refreshPeriodNanos);
    }
}
