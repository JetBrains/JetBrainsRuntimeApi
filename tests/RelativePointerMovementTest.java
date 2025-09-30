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
 * @summary Verifies RelativePointerMovement.getAccumulatedMouseDeltaAndReset()
 * @requires (os.family == "linux")
 * @key headful
 * @run main RelativePointerMovementTest
 */

import com.jetbrains.JBR;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import java.awt.Robot;
import java.awt.Point;


public class RelativePointerMovementTest {
    static JFrame frame;
    static int WIDTH = 250;
    static int HEIGHT = 120;

    public static void main(String[] args) throws Exception {
        if (!JBR.isRelativePointerMovementSupported()) {
            // The test is meaningful only with -Dawt.toolkit.name=WLToolkit
            System.out.println("Service not available in this configuration. Exiting.");
            return;
        }

        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame("RelativePointerMovementTest");
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
        try {
            Robot robot = new Robot();
            robot.waitForIdle();;
            Point p = JBR.getRelativePointerMovement().getAccumulatedMouseDeltaAndReset();
            System.out.println("Mouse delta: " + p);
            if (p == null) {
                throw new RuntimeException("getRelativePointerMovement().getAccumulatedMouseDeltaAndReset() returned null");
            }
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                frame.dispose();
            });
        }
    }
}
