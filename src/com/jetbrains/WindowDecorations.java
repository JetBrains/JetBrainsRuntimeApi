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
import java.util.Map;

/**
 * Window decorations consist of title bar, window controls and border.
 * @see WindowDecorations.CustomTitleBar
 */
@Service
@Provided
public interface WindowDecorations {

    /**
     * If {@code customTitleBar} is not null, system-provided title bar is removed and client area is extended to the
     * top of the frame with window controls painted over the client area.
     * {@code customTitleBar=null} resets to the default appearance with system-provided title bar.
     * @param frame frame to setup custom title bar on
     * @param customTitleBar new title bar instance, or null
     * @see CustomTitleBar
     * @see #createCustomTitleBar()
     */
    void setCustomTitleBar(Frame frame, CustomTitleBar customTitleBar);

    /**
     * If {@code customTitleBar} is not null, system-provided title bar is removed and client area is extended to the
     * top of the dialog with window controls painted over the client area.
     * {@code customTitleBar=null} resets to the default appearance with system-provided title bar.
     * @param dialog dialog to setup custom title bar on
     * @param customTitleBar new title bar instance, or null
     * @see CustomTitleBar
     * @see #createCustomTitleBar()
     */
    void setCustomTitleBar(Dialog dialog, CustomTitleBar customTitleBar);

    /**
     * You must {@linkplain CustomTitleBar#setHeight(float) set title bar height} before adding it to a window.
     * @return new CustomTitleBar instance
     * @see CustomTitleBar
     * @see #setCustomTitleBar(Frame, CustomTitleBar)
     * @see #setCustomTitleBar(Dialog, CustomTitleBar)
     */
    CustomTitleBar createCustomTitleBar();

    /**
     * Custom title bar allows merging of window content with native title bar,
     * which is done by treating title bar as part of client area, but with some
     * special behavior like dragging or maximizing on double click.
     * Custom title bar has {@linkplain CustomTitleBar#getHeight() height} and controls.
     * Behavior is platform-dependent, only macOS and Windows are supported.
     * @see #setCustomTitleBar(Frame, CustomTitleBar)
     */
    @Provided
    interface CustomTitleBar {

        /**
         * Get title bar height, measured in pixels from the top of client area, i.e. excluding top frame border.
         * @return title bar height
         */
        float getHeight();

        /**
         * Set title bar height, measured in pixels from the top of client area,
         * i.e. excluding top frame border. Must be > 0.
         * @param height new title bar height
         */
        void setHeight(float height);

        /**
         * Get all properties set for the title bar.
         * @return map of properties
         * @see #putProperty(String, Object)
         */
        Map<String, Object> getProperties();

        /**
         * Put all properties from the map.
         * @param m map of properties
         * @see #putProperty(String, Object)
         */
        void putProperties(Map<String, ?> m);

        /**
         * Windows and macOS properties:
         * <ul>
         *     <li>{@code controls.visible} : {@link Boolean} - whether title bar controls
         *         (minimize/maximize/close buttons) are visible, default = true.</li>
         * </ul>
         * Windows properties:
         * <ul>
         *     <li>{@code controls.width} : {@link Number} - width of block of buttons (not individual buttons).
         *         Note that dialogs have only one button, while frames usually have 3 of them.</li>
         *     <li>{@code controls.dark} : {@link Boolean} - whether to use dark or light color theme
         *         (light or dark icons respectively).</li>
         *     <li>{@code controls.<layer>.<state>} : {@link Color} - precise control over button colors,
         *         where {@code <layer>} is one of:
         *         <ul><li>{@code foreground}</li><li>{@code background}</li></ul>
         *         and {@code <state>} is one of:
         *         <ul>
         *             <li>{@code normal}</li>
         *             <li>{@code hovered}</li>
         *             <li>{@code pressed}</li>
         *             <li>{@code disabled}</li>
         *             <li>{@code inactive}</li>
         *         </ul>
         * </ul>
         *
         * @param key property key
         * @param value property value
         */
        void putProperty(String key, Object value);

        /**
         * Get space occupied by title bar controls on the left (px).
         * @return left inset
         */
        float getLeftInset();
        /**
         * Get space occupied by title bar controls on the right (px).
         * @return right inset
         */
        float getRightInset();

        /**
         * By default, any component which has no cursor or mouse event listeners set is considered transparent for
         * native title bar actions. That is, dragging simple JPanel in title bar area will drag the
         * window, but dragging a JButton will not. Adding mouse listener to a component will prevent any native actions
         * inside bounds of that component.
         * <p>
         * This method gives you precise control of whether to allow native title bar actions or not.
         * <ul>
         *     <li>{@code client=true} means that mouse is currently over a client area. Native title bar behavior is disabled.</li>
         *     <li>{@code client=false} means that mouse is currently over a non-client area. Native title bar behavior is enabled.</li>
         * </ul>
         * <em>Intended usage:</em>
         * <ul>
         *     <li><em>This method must be called in response to all {@linkplain java.awt.event.MouseEvent mouse events}
         *         except {@link java.awt.event.MouseEvent#MOUSE_EXITED} and {@link java.awt.event.MouseEvent#MOUSE_WHEEL}.</em></li>
         *     <li><em>This method is called per-event, i.e. when component has multiple listeners, you only need to call it once.</em></li>
         *     <li><em>If this method hadn't been called, title bar behavior is reverted back to default upon processing the event.</em></li>
         * </ul>
         * Note that hit test value is relevant only for title bar area, e.g. calling
         * {@code forceHitTest(false)} will not make window draggable via non-title bar area.
         *
         * <h4>Example:</h4>
         * Suppose you have a {@code JPanel} in the title bar area. You want it to respond to right-click for
         * some popup menu, but also retain native drag and double-click behavior.
         * <pre>
         *     CustomTitleBar titlebar = ...;
         *     JPanel panel = ...;
         *     MouseAdapter adapter = new MouseAdapter() {
         *         private void hit() { titlebar.forceHitTest(false); }
         *         public void mouseClicked(MouseEvent e) {
         *             hit();
         *             if (e.getButton() == MouseEvent.BUTTON3) ...;
         *         }
         *         public void mousePressed(MouseEvent e) { hit(); }
         *         public void mouseReleased(MouseEvent e) { hit(); }
         *         public void mouseEntered(MouseEvent e) { hit(); }
         *         public void mouseDragged(MouseEvent e) { hit(); }
         *         public void mouseMoved(MouseEvent e) { hit(); }
         *     };
         *     panel.addMouseListener(adapter);
         *     panel.addMouseMotionListener(adapter);
         * </pre>
         *
         * @param client whether to force client, or non-client area response
         */
        void forceHitTest(boolean client);

        /**
         * Get window, title bar is attached to.
         * @return parent window
         */
        Window getContainingWindow();
    }
}
