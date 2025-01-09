/*
 * Copyright 2000-2025 JetBrains s.r.o.
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
         * Returns the key code for this shortcut.
         * See {@link java.awt.event.KeyEvent} for the list of key codes.
         * If this shortcut doesn't have an associated key code,
         * this method returns {@link java.awt.event.KeyEvent#VK_UNDEFINED}.
         *
         * @return The shortcut's key code
         */
        int getKeyCode();

        /**
         * Returns the key character for this shortcut.
         * If this shortcut doesn't have an associated key character,
         * this method returns {@link java.awt.event.KeyEvent#CHAR_UNDEFINED}.
         *
         * @return The shortcut's key character
         */
        char getKeyChar();

        /**
         * Returns the modifier mask for this shortcut.
         * See {@link java.awt.event.InputEvent} for the list of modifiers.
         *
         * @return The shortcut's modifiers mask
         */
        int getModifiers();

        /**
         * Returns unique identifier for this shortcut.
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
    Shortcut[] querySystemShortcuts();

    /**
     * Set an event listener which is called whenever the user changes a system shortcut.
     * The listener will be called on the EDT.
     * If the application is running with headless or custom toolkit, this may be a no-op.
     *
     * @param listener The listener
     */
    void setChangeListener(ChangeEventListener listener);
}
