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
 * @summary Verifies rounded corners on Linux/Wayland
 * @requires (os.family == "linux")
 * @run main/manual TestSetRoundedCornersWayland
 */

import com.jetbrains.JBR;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

public class TestSetRoundedCornersWayland {
    private static final int DELAY = 1000;

    private static TestSetRoundedCornersWayland theTest;

    private final Robot robot;
    private JFrame frame;
    private JFrame testFrame;

    public TestSetRoundedCornersWayland() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void performTest(Object roundParams) {
        if (!JBR.isRoundedCornersManagerSupported()) {
            throw new RuntimeException("JBR Rounded API is not available");
        }

        runSwing(() -> {
            testFrame = new JFrame("Rounded Corners Test");
            testFrame.setUndecorated(true);
            JPanel testPanel = new JPanel();
            testPanel.setOpaque(true);
            testPanel.setBackground(Color.GREEN);
            testFrame.setContentPane(testPanel);
            testFrame.setBounds(0, 0, 100, 100);
            testFrame.setVisible(true);
        });

        robot.delay(DELAY);

        System.out.println("Validating the window WITHOUT rounded corners...");
        // check the window without rounded corners
        validatePixelColor(0, 0, Color.GREEN);
        validatePixelColor(99, 0, Color.GREEN);
        validatePixelColor(0, 99, Color.GREEN);
        validatePixelColor(99, 99, Color.GREEN);
        System.out.println("...done");
        runSwing(() -> testFrame.setVisible(false));
        robot.delay(DELAY);

        runSwing(() -> {
            JBR.getRoundedCornersManager().setRoundedCorners(testFrame, roundParams);
            testFrame.setVisible(true);
        });
        robot.delay(DELAY);

        // check the window with rounded corners
        // NB: can't really check for the corners to be transparent, so have to settle for them
        // having no color info (black)
        System.out.printf("Validating the window WITH \"%s\" rounded corners...", roundParams);
        validatePixelColor(0, 0, Color.BLACK);
        validatePixelColor(99, 0, Color.BLACK);
        validatePixelColor(0, 99, Color.BLACK);
        validatePixelColor(99, 99, Color.BLACK);
        System.out.println("...done");

        runSwing(() -> dispose());
    }

    private void validatePixelColor(int x, int y, Color expected) {
        var actual = robot.getPixelColor(x, y);
        if (!actual.equals(expected)) {
            throw new RuntimeException(String.format("The color at (%d, %d) is incorrect. Expected %s, but got %s", x, y, expected, actual));
        }
    }

    private static void runSwing(Runnable r) {
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void dispose() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
    }

    public static void runTest(Object roundParams) {
        try {
            runSwing(() -> theTest = new TestSetRoundedCornersWayland());
            if (!Toolkit.getDefaultToolkit().getClass().getName().equals("sun.awt.wl.WLToolkit")) {
                System.out.println("This test requires a functional WLToolkit to run; skipping for " + Toolkit.getDefaultToolkit());
                return;
            }
            theTest.performTest(roundParams);
        } finally {
            if (theTest != null) {
                runSwing(() -> theTest.dispose());
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.toolkit.name", "WLToolkit");
        runTest("full");
        runTest("small");
    }
}
