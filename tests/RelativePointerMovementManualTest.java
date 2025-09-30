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
 * @summary Demo implementation for RelativePointerMovement.getAccumulatedMouseDeltaAndReset()
 * @requires (os.family == "linux")
 * @modules java.desktop/sun.awt
 * @key headful
 * @run main/manual RelativePointerMovementManualTest
 */
import com.jetbrains.JBR;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CompletableFuture;

public class RelativePointerMovementManualTest {
    static final CompletableFuture<RuntimeException> swingError = new CompletableFuture<>();
    static JFrame frame;

    private static final String INSTRUCTIONS = "<html><h>INSTRUCTIONS</h>" +
            "<p>Click the button to show a draggable popup window.<br>" +
            "Drag the popup window around the screen.</p>" +
            "<p>If the popup moves smoothly, press Pass; otherwise press Fail.</p>" +
            "<p>Note: Popups cannot go completely outside their parent window.</p></html>";

    public static void main(String[] args) throws Exception {
        if (!JBR.isRelativePointerMovementSupported()) {
            // The test is meaningful only with -Dawt.toolkit.name=WLToolkit
            throw new RuntimeException("Service not available in this configuration. Exiting.");
        }

        try {
            SwingUtilities.invokeLater(RelativePointerMovementManualTest::createAndShowUI);
            swingError.get();
        } finally {
            if (frame != null) {
                frame.dispose();
            }
        }
    }

    private static void createAndShowUI() {
        frame = new JFrame("Popup drag test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel content = new JPanel();
        var layout = new GridLayout(4, 1, 10, 10);
        content.setLayout(layout);
        content.add(new JLabel(INSTRUCTIONS));
        JButton showPopupButton = new JButton("Show draggable popup");
        showPopupButton.addActionListener(e -> showDraggablePopup(frame));
        content.add(showPopupButton);
        JButton passButton = new JButton("Pass");
        passButton.addActionListener(e -> {
            swingError.complete(null);
        });
        JButton failButton = new JButton("Fail");
        failButton.addActionListener(e -> {
            swingError.completeExceptionally(new RuntimeException("The tester has pressed FAILED"));
        });
        content.add(failButton);
        content.add(passButton);

        frame.setContentPane(content);
        frame.pack();
        frame.setVisible(true);
    }

    private static void showDraggablePopup(JFrame owner) {
        JWindow popup = new JWindow(owner);
        popup.setType(Window.Type.POPUP);
        sun.awt.AWTAccessor.getWindowAccessor().setPopupParent(popup, owner);
        popup.setSize(220, 120);
        popup.setLocation(100, 35);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 210));
        JLabel label = new JLabel("Drag me around");
        panel.add(label, BorderLayout.CENTER);

        DragMover.install(popup);

        popup.setContentPane(panel);
        popup.setVisible(true);
    }

    /**
     * Utility that allows dragging a Window by mouse-dragging its content component.
     */
    private static class DragMover extends MouseAdapter {
        private final Window targetWindow;

        private DragMover(Window targetWindow) {
            this.targetWindow = targetWindow;
            targetWindow.addMouseListener(this);
            targetWindow.addMouseMotionListener(this);
        }

        static void install(Window window) {
            DragMover mover = new DragMover(window);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Reset the delta so we can start tracking relative motion
            JBR.getRelativePointerMovement().getAccumulatedMouseDeltaAndReset();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point delta = JBR.getRelativePointerMovement().getAccumulatedMouseDeltaAndReset();
            System.out.printf("dx=%d dy=%d  %n", delta.x, delta.y);

            var loc = targetWindow.getLocation();
            loc.translate(delta.x, delta.y);
            targetWindow.setLocation(loc);
            targetWindow.repaint();
        }
    }
}
