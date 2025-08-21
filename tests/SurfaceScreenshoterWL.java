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
 * @summary Verifies Screenshoter.getWindowSurfaceArea()
 * @requires os.family == "linux"
 * @key headful
 * @run main/othervm -Dawt.toolkit.name=WLToolkit -Dsun.java2d.vulkan=false SurfaceScreenshoterWL
 * @run main/othervm -Dawt.toolkit.name=WLToolkit -Dsun.java2d.vulkan=false -Dsun.java2d.uiScale.enabled=true -Dsun.java2d.uiScale=0.7 SurfaceScreenshoterWL
 * @run main/othervm -Dawt.toolkit.name=WLToolkit -Dsun.java2d.vulkan=true -Dsun.java2d.vulkan.accelsd=true SurfaceScreenshoterWL
 * @run main/othervm -Dawt.toolkit.name=WLToolkit -Dsun.java2d.vulkan=true -Dsun.java2d.vulkan.accelsd=false SurfaceScreenshoterWL
 */

import com.jetbrains.JBR;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Robot;
import java.io.File;
import java.awt.Color;
import java.io.IOException;

public class SurfaceScreenshoterWL {
    static JFrame frame;
    static int WIDTH = 250;
    static int HEIGHT = 120;

    public static void main(String[] args) throws Exception {
        testInvalidArgs();
        testScreenshoter();
    }

    static void testInvalidArgs() throws Exception {
        boolean ok = false;

        System.out.println("=== Testing getWindowSurfaceArea() with a null Window");
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(null, 0, 0, 1, 1);
        } catch (NullPointerException npe) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with null Window didn't throw NPE");
        }

        Robot robot = new Robot();
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("SurfaceScreenshoterWL test 1");
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(Color.GREEN);
            frame.setVisible(true);
        });

        robot.waitForIdle();;
        robot.delay(1000);

        try {
            SwingUtilities.invokeAndWait(SurfaceScreenshoterWL::checkForExceptions);
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    static void checkForExceptions() {
        boolean ok = false;

        System.out.println("=== Testing getWindowSurfaceArea() with negative coordinates");
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(frame, -1, 0, 1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with negative coordinates didn't throw IAE");
        }

        ok = false;
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(frame, 1, -100, 1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with negative coordinates didn't throw IAE");
        }

        System.out.println("=== Testing getWindowSurfaceArea() with a bad size");
        ok = false;
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(frame, 0, 0, 1, -2);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with a negative size didn't throw IAE");
        }

        ok = false;
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(frame, 0, 0, 0, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with a zero size didn't throw IAE");
        }

        System.out.println("=== Testing getWindowSurfaceArea() with large coordinates");
        ok = false;
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(frame, WIDTH, 0, 1, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with coordinates outside the window didn't throw IAE");
        }

        ok = false;
        try {
            JBR.getScreenshoter().getWindowSurfaceArea(frame, 1, HEIGHT, 2, 1);
        } catch (IllegalArgumentException e) {
            ok = true;
        }
        if (!ok) {
            throw new RuntimeException("getWindowSurfaceArea() with coordinates outside the window didn't throw IAE");
        }

        System.out.println("=== Testing getWindowSurfaceArea() with a large size");
        JBR.getScreenshoter().getWindowSurfaceArea(frame, 0, 0, 101, Integer.MAX_VALUE); // OK
    }

    static void testScreenshoter() throws Exception {
        Robot robot = new Robot();
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("getWindowSurfaceArea test 2");
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            javax.swing.JPanel panel = new javax.swing.JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    super.paintComponent(g);
                    // Paint green background
                    g.setColor(Color.GREEN);
                    g.fillRect(0, 0, getWidth(), getHeight());

                    // Paint 10x10 red rectangles at each side (top, bottom, left, right centers)
                    g.setColor(Color.RED);
                    int w = getWidth();
                    int h = getHeight();
                    int s = 10;
                    g.fillRect(0, 0, s, s);
                    g.fillRect(0, h - s, s, s);
                    g.fillRect(w - s, 0, s, s);
                    g.fillRect(w - s, h - s, s, s);
                }
            };
            frame.setContentPane(panel);
        });

        System.out.println("=== Testing getWindowSurfaceArea() with a normal Window");

        try {
            BufferedImage img = JBR.getScreenshoter().getWindowSurfaceArea(frame, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (img != null) {
                throw new RuntimeException("getWindowSurfaceArea() returned a non-null image for an invisible window");
            }

            SwingUtilities.invokeAndWait(() -> {
                frame.setVisible(true);
            });

            robot.waitForIdle();;
            robot.delay(1000);

            img = JBR.getScreenshoter().getWindowSurfaceArea(frame, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            if (img == null) {
                throw new RuntimeException("getWindowSurfaceArea() returned a null image for a visible window");
            }

            // Note that the returned image size may be scaled relative to the window's client area size
            Rectangle clientBounds = new Rectangle(img.getWidth(), img.getHeight());
            int centerPixel = img.getRGB(clientBounds.width / 2, clientBounds.height / 2);
            validatePixel(centerPixel, img, Color.GREEN.getRGB());

            int topLeftPixel = img.getRGB(5, 5);
            validatePixel(topLeftPixel, img, Color.RED.getRGB());

            int topRightPixel = img.getRGB(clientBounds.width - 5, 5);
            validatePixel(topRightPixel, img, Color.RED.getRGB());

            int bottomLeftPixel = img.getRGB(5, clientBounds.height - 5);
            validatePixel(bottomLeftPixel, img, Color.RED.getRGB());

            int bottomRightPixel = img.getRGB(clientBounds.width - 5, clientBounds.height - 5);
            validatePixel(bottomRightPixel, img, Color.RED.getRGB());
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                frame.dispose();
            });
        }
    }

    private static void validatePixel(int color, BufferedImage img, int expectedColor) throws IOException {
        if (color != expectedColor) {
            ImageIO.write(img, "png", new File("SurfaceScreenshoterWL-screenshot.png"));
            throw new RuntimeException("getWindowSurfaceArea() returned an image with a wrong color: " + Integer.toHexString(color)
                    + ", expected: " + Integer.toHexString(Color.GREEN.getRGB()));
        }
    }
}
