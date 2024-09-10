/*
 * Copyright 2024 JetBrains s.r.o.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.jetbrains;

import java.awt.AWTKeyStroke;
import java.util.List;
import java.util.function.Consumer;

/**
 * Querying system shortcuts
 */
@Service
@Provided
public interface SystemShortcuts {
    /**
     * Information about a system shortcut
     */
    @Provided
    interface Shortcut {
        /**
         * Returns the keystroke for this shortcut
         *
         * @return Keystroke to activate this shortcut
         */
        AWTKeyStroke getKeyStroke();

        /**
         * Returns the unique identifier for this shortcut.
         *
         * @return Unique identifier for the shortcut action
         */
        String getId();

        /**
         * Returns a human-readable description of the shortcut action.
         *
         * @return Human-readable description of the shortcut action
         */
        String getDescription();

        /**
         * Returns the platform-dependent key code of the shortcut key without modifiers
         *
         * @return Platform-dependent key code of the shortcut key without modifiers
         */
        int getNativeKeyCode();
    }

    /**
     * Listener for shortcut change events.
     */
    @Provides
    interface ChangeEventListener {
        /**
         * Event that is fired when any of the system shortcuts change.
         */
        void handleSystemShortcutsChangeEvent();
    }

    /**
     * Query the current state of system shortcuts.
     *
     * @return The list of enabled system shortcuts, or {@code null} if the current toolkit does not support
     * querying the state of system shortcuts.
     */
    List<Shortcut> querySystemShortcuts();

    /**
     * Set an event listener which is called whenever the user changes a system shortcut.
     * The listener will be called on the EDT.
     * If the application is running with headless or custom toolkit, this may be a no-op.
     *
     * @param listener The listener
     */
    void setChangeListener(ChangeEventListener listener);
}
