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

/*
 * @test
 * @summary Verifies the contract of the DisplayRefresh service: each request gets one callback
 *          on the event dispatch thread, and the frame time does not go back.
 * @key headful
 * @run main DisplayRefreshTest
 */

import com.jetbrains.DisplayRefresh;
import com.jetbrains.JBR;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DisplayRefreshTest {
    private static final long RUN_NANOS = TimeUnit.SECONDS.toNanos(1);
    private static final AtomicReference<String> failure = new AtomicReference<>();

    public static void main(String[] args) throws Exception {
        if (!JBR.isDisplayRefreshSupported()) {
            System.out.println("Service not available in this configuration. Exiting.");
            return;
        }
        DisplayRefresh refresh = JBR.getDisplayRefresh();

        Frame[] created = new Frame[1];
        EventQueue.invokeAndWait(() -> {
            created[0] = new Frame("DisplayRefreshTest");
            created[0].setSize(300, 200);
            created[0].setVisible(true);
        });
        Frame frame = created[0];
        try {
            testFrameLoop(refresh, frame);
            testFrameLoop(refresh, null);
            testSameFrame(refresh, frame);
            testNullCallback(refresh, frame);
        } finally {
            EventQueue.invokeAndWait(frame::dispose);
        }
        if (failure.get() != null) {
            throw new RuntimeException(failure.get());
        }
    }

    /** A callback that requests the next frame gets one callback per refresh, on the EDT, with a time that does not go back. */
    private static void testFrameLoop(DisplayRefresh refresh, Window window) throws Exception {
        AtomicInteger frames = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(1);
        long start = System.nanoTime();
        refresh.requestFrame(window, new DisplayRefresh.FrameCallback() {
            private long lastFrameTime = Long.MIN_VALUE;

            @Override
            public void onFrame(long frameTimeNanos, long refreshPeriodNanos) {
                if (!EventQueue.isDispatchThread()) {
                    fail("the callback is not on the event dispatch thread");
                }
                if (refreshPeriodNanos <= 0) {
                    fail("the refresh period is " + refreshPeriodNanos);
                }
                if (lastFrameTime != Long.MIN_VALUE && frameTimeNanos - lastFrameTime < 0) {
                    fail("the frame time went back by " + (lastFrameTime - frameTimeNanos) + " ns");
                }
                lastFrameTime = frameTimeNanos;
                frames.incrementAndGet();
                if (System.nanoTime() - start < RUN_NANOS) {
                    refresh.requestFrame(window, this);
                } else {
                    done.countDown();
                }
            }
        });
        if (!done.await(10, TimeUnit.SECONDS)) {
            fail("the frame loop did not end, frames: " + frames.get());
        }
        // a loaded machine gives few frames, so the test asks only for a loop
        if (frames.get() < 2) {
            fail("the frame loop made " + frames.get() + " frames in 1 s");
        }
    }

    /** The requests that wait for one refresh get the same frame time, each one exactly once, in their order. */
    private static void testSameFrame(DisplayRefresh refresh, Window window) throws Exception {
        int count = 3;
        long[] times = new long[count];
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(count);
        // the callbacks come on the event dispatch thread, so no refresh splits the requests of one event
        EventQueue.invokeAndWait(() -> {
            for (int i = 0; i < count; i++) {
                int index = i;
                refresh.requestFrame(window, (frameTimeNanos, refreshPeriodNanos) -> {
                    times[index] = frameTimeNanos;
                    if (calls.getAndIncrement() != index) {
                        fail("the callbacks did not run in the order of their requests");
                    }
                    done.countDown();
                });
            }
        });
        if (!done.await(10, TimeUnit.SECONDS)) {
            fail("not all requests of one refresh got a callback");
        }
        // a second callback for a request would come with the next refresh
        Thread.sleep(300);
        EventQueue.invokeAndWait(() -> {});
        if (calls.get() != count) {
            fail("expected " + count + " callbacks, got " + calls.get());
        }
        for (int i = 1; i < count; i++) {
            if (times[i] != times[0]) {
                fail("the requests of one refresh got different frame times");
            }
        }
    }

    private static void testNullCallback(DisplayRefresh refresh, Window window) {
        try {
            refresh.requestFrame(window, null);
            fail("a null callback did not throw");
        } catch (NullPointerException expected) {
        }
    }

    private static void fail(String message) {
        failure.compareAndSet(null, message);
    }
}
