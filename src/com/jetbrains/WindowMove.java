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

import java.awt.Window;

/**
 * X11 WM-assisted window moving facility.
 */
@Service
@Provided
public interface WindowMove {
    /**
     * Starts moving the top-level parent window of the given window together with the mouse pointer.
     * The intended use is to facilitate the implementation of window management similar to the way
     * it is done natively on the platform.
     *
     * Preconditions for calling this method:
     * <ul>
     * <li>WM supports _NET_WM_MOVE_RESIZE (this is checked automatically when an implementation
     *     of this interface is obtained).</li>
     * <li>Mouse pointer is within this window's bounds.</li>
     * <li>The mouse button specified by {@code mouseButton} is pressed.</li>
     * </ul>
     *
     * Calling this method will make the window start moving together with the mouse pointer until
     * the specified mouse button is released or Esc is pressed. The conditions for cancelling
     * the move may differ between WMs.
     *
     * @param window window to start moving
     * @param mouseButton indicates the mouse button that was pressed to start moving the window;
     *                   must be one of {@code MouseEvent.BUTTON1}, {@code MouseEvent.BUTTON2},
     *                   or {@code MouseEvent.BUTTON3}.
     */
    void startMovingTogetherWithMouse(Window window, int mouseButton);
}
