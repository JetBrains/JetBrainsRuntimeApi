/*
 * Copyright 2025 JetBrains s.r.o.
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
 * @summary Verifies Screenshoter.getWindowBackbufferArea()
 * @run main/othervm -Dswing.bufferPerWindow=true BackbufferScreenshoter
 * @run main/othervm -Dswing.bufferPerWindow=false BackbufferScreenshoter
 */

import com.jetbrains.JBR;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import java.awt.Robot;
import java.io.File;
import java.awt.Color;

public class BackbufferScreenshoter {
    static JFrame frame;
    static int WIDTH = 250;
    static int HEIGHT = 120;

    public static void main(String[] args) throws Exception {
        var bufferPerWindow = Boolean.getBoolean("swing.bufferPerWindow");
        if (bufferPerWindow) {
            testInvalidArgs();
            testScreenshoter();
        } else {
            testServiceUnavailable();
        }
    }

    static void testInvalidArgs() throws Exception {
        boolean ok = false;

        System.out.println("=== Testing getWindowBackbufferArea() with a null Window");
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(null, 0, 0, 1, 1);
        } catch (NullPointerException npe) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with null Window didn't throw NPE");
        }

        Robot robot = new Robot();
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("BackbufferScreenshoter test 1");
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(Color.GREEN);
            frame.setVisible(true);
        });

        robot.waitForIdle();;
        robot.delay(1000);

        try {
            SwingUtilities.invokeAndWait(BackbufferScreenshoter::checkForExceptions);
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    static void checkForExceptions() {
        boolean ok = false;

        System.out.println("=== Testing getWindowBackbufferArea() with negative coordinates");
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(frame, -1, 0, 1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with negative coordinates didn't throw IAE");
        }

        ok = false;
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(frame, 1, -100, 1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with negative coordinates didn't throw IAE");
        }

        System.out.println("=== Testing getWindowBackbufferArea() with a bad size");
        ok = false;
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(frame, 0, 0, -1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with a negative size didn't throw IAE");
        }

        ok = false;
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(frame, 0, 0, 1, 0);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with a zero size didn't throw IAE");
        }

        System.out.println("=== Testing getWindowBackbufferArea() with large coordinates");
        ok = false;
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(frame, WIDTH, 0, 1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with coordinates outside the window didn't throw IAE");
        }

        ok = false;
        try {
            JBR.getScreenshoter().getWindowBackbufferArea(frame, 1, HEIGHT, 2, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowBackbufferArea() with coordinates outside the window didn't throw IAE");
        }

        System.out.println("=== Testing getWindowBackbufferArea() with a large size");
        JBR.getScreenshoter().getWindowBackbufferArea(frame, 0, 0, 101, Integer.MAX_VALUE); // OK
    }

    static void testScreenshoter() throws Exception {
        Robot robot = new Robot();
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("BackbufferScreenshoter test 2");
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(Color.GREEN);
        });

        System.out.println("=== Testing getWindowBackbufferArea() with a normal Window");
        try {
            robot.waitForIdle();;
            robot.delay(1000);

            BufferedImage img = JBR.getScreenshoter().getWindowBackbufferArea(frame, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (img != null) {
                throw new RuntimeException("getWindowBackbufferArea() returned a non-null image for an invisible window");
            }

            SwingUtilities.invokeAndWait(() -> {
                frame.setVisible(true);
            });

            robot.waitForIdle();;
            robot.delay(1000);

            img = JBR.getScreenshoter().getWindowBackbufferArea(frame, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (img == null) {
                throw new RuntimeException("getWindowBackbufferArea() returned a null image for a visible window");
            }
            var clientBounds = frame.getContentPane().getBounds();
            if (img.getWidth() != clientBounds.width) {
                throw new RuntimeException("getWindowBackbufferArea() returned an image with wrong width: " + img.getWidth() + ", expected: " + clientBounds.width);
            }
            if (img.getHeight() != clientBounds.height) {
                throw new RuntimeException("getWindowBackbufferArea() returned an image with wrong height: " + img.getHeight() + ", expected: " + clientBounds.height);
            }

            var centerPixel = img.getRGB(clientBounds.width / 2, clientBounds.height / 2);
            if (centerPixel != Color.GREEN.getRGB()) {
                ImageIO.write(img, "png", new File("BackbufferScreenshoter-screenshot.png"));
                throw new RuntimeException("getWindowBackbufferArea() returned an image with a wrong color: " + Integer.toHexString(centerPixel)
                        + ", expected: " + Integer.toHexString(Color.GREEN.getRGB()));
            }
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                frame.dispose();
            });
        }
    }

    static void testServiceUnavailable() throws Exception {
        Robot robot = new Robot();
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("BackbufferScreenshoter test 3");
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(Color.GREEN);
            frame.setVisible(true);
        });

        robot.waitForIdle();;
        robot.delay(1000);

        try {
            System.out.println("=== Testing getWindowBackbufferArea() with a window that is not visible");
            JBR.getScreenshoter().getWindowBackbufferArea(frame, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            // Ignore the results; it's OK as long as the service didn't crash
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                frame.dispose();
            });
        }
    }
}
