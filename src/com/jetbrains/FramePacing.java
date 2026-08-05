/*
 * Copyright 2000-2026 JetBrains s.r.o.
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

import java.awt.GraphicsConfiguration;

/**
 * Display-aligned frame pacing clock.
 * <p>
 * Delivers asynchronous ticks for rate-limiting UI work, roughly once per
 * display refresh. This is not a present-completion or drawable callback:
 * a tick tells the client that now is a good moment to schedule a frame,
 * not that a previous frame has reached the glass.
 * <p>
 * Listeners are called on a non-EDT thread, must return immediately and
 * must not enter AWT locks. Painting from the listener is the client's
 * responsibility, typically by coalescing one repaint per tick onto the EDT.
 * <p>
 * Delivery is best-effort: missed ticks are skipped, never queued, so a slow
 * client never receives catch-up bursts. Listeners subscribed to the same
 * display are invoked sequentially; an exception thrown by one listener does
 * not affect the clock or other listeners.
 */
@Service
@Provided
public interface FramePacing {

    /** No pacing source is available. */
    int QUALITY_NONE = 0;
    /** Best-effort high-resolution timer at the estimated refresh rate. */
    int QUALITY_ESTIMATED = 1;
    /** OS composition / vblank-aligned clock; not glass present time. */
    int QUALITY_COMPOSITION_CLOCK = 2;
    /** True display link (e.g. CVDisplayLink). */
    int QUALITY_DISPLAY_LINK = 3;

    /**
     * Returns the quality tier of the pacing backend, one of the
     * {@code QUALITY_*} constants. Clients may refuse weak clocks and fall
     * back to their own timers.
     *
     * @return backend quality tier
     */
    int getQuality();

    /**
     * Returns an opaque stable id for the screen of the given configuration.
     *
     * @param gc graphics configuration to resolve
     * @return display id, or -1 if unknown. The id is stable while the
     * display stays connected; clients must re-resolve it after a
     * graphics configuration change.
     */
    long displayId(GraphicsConfiguration gc);

    /**
     * Returns the nominal refresh period of the given display.
     *
     * @param displayId display id from {@link #displayId(GraphicsConfiguration)}
     * @return period in nanoseconds, or 0 if unknown. Advisory only.
     */
    long refreshPeriodNanos(long displayId);

    /**
     * Subscribes the listener to ticks of the given display. The backing
     * clock is started lazily with the first subscriber of a display and
     * stopped when the last subscription is closed.
     *
     * @param displayId display id from {@link #displayId(GraphicsConfiguration)}
     * @param listener  tick callback. The implementation will hold an anchored
     *                  hard reference to the listener if it is proxied by a
     *                  {@code JBRAPI.Proxy}, or only a weak reference if it is
     *                  not proxied. In the latter case, the listener must be
     *                  kept alive by the application with a strong reference.
     * @return subscription handle, or null if that display cannot be paced
     */
    Subscription subscribe(long displayId, Listener listener);

    /**
     * Tick callback, implemented by the client.
     */
    @Provides
    interface Listener {
        /**
         * Called roughly once per refresh of the subscribed display, on a
         * non-EDT thread. Implementations must return immediately to avoid
         * any latency on the background thread, and must not enter AWT
         * locks; scheduling work with {@code EventQueue.invokeLater} is
         * the expected pattern.
         *
         * @param displayId display the tick belongs to
         * @param timeNanos monotonic timestamp for interval math, in the
         *                  {@code System.nanoTime()} time base
         */
        void onTick(long displayId, long timeNanos);
    }

    /**
     * Handle for an active tick subscription.
     */
    @Provided
    interface Subscription extends AutoCloseable {
        /**
         * Returns the display id this subscription is attached to.
         *
         * @return display id this subscription is attached to
         */
        long displayId();

        /**
         * Stops tick delivery for this subscription. Idempotent and
         * non-blocking: no new deliveries begin after this method returns,
         * but one in-flight delivery may complete. Safe to call from within
         * the listener itself.
         */
        @Override
        void close();
    }
}
