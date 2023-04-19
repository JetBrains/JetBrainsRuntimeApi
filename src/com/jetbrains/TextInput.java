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

/**
 * This is a JBR API for text-input related functionality for applications that implement custom text components.
 * <p>
 * Suppose an application implements a custom text component called {@code CustomTextComponent}, that
 * doesn't inherit from {@link java.awt.TextComponent} or {@link javax.swing.text.JTextComponent}.
 * For this component to work correctly, the application needs to handle certain events that are missing from
 * the Java specification.
 * <p>
 * To do this, the application should add an event listener for the events provided by this API.
 * This is best done at application startup time, since the event listener is global, and not per-component.
 * For example, this would be a proper way to implement this event handler for {@code CustomTextComponent}:
 *
 * <pre>
 * {@code
 * var textInput = JBR.getTextInput();
 * if (textInput != null) {
 *     textInput.setGlobalEventListener(new TextInput.EventListener() {
 *         @Override
 *         public void handleSelectTextRangeEvent(TextInput.SelectTextRangeEvent event) {
 *             if (event.getSource() instanceof CustomTextComponent) {
 *                 ((CustomTextComponent)event.getSource()).select(event.getBegin(), event.getBegin() + event.getLength());
 *             }
 *         }
 *     });
 * }
 * }
 * </pre>
 * This assumes that {@code CustomTextComponent} has a method called {@code select}, that selects a text range,
 * similar to the {@link java.awt.TextComponent#select(int, int)} and {@link javax.swing.text.JTextComponent#select(int, int)}.
 * See {@link TextInput.SelectTextRangeEvent} for more information.
 */
@Service
@Provided
public interface TextInput {
    /**
     * Custom text components that do not extend {@link java.awt.TextComponent} or {@link javax.swing.text.JTextComponent}
     * should subscribe to this event. When receiving it, they should select the text range of UTF-16 code units starting
     * at index {@param begin} of length {@param length}.
     * <p>
     * It is expected, that {@link java.awt.event.KeyEvent#KEY_TYPED}, or
     * {@link java.awt.event.InputMethodEvent} events will immediately follow. They will insert new text in place of the
     * old text pointed to by this event's range.
     */
    @Provided
    interface SelectTextRangeEvent {
        /**
         * Returns an AWT component that is the target of this event
         * @return an AWT component that is the target of this event
         */
        Object getSource();

        /**
         * Returns first UTF-16 code unit index of the replacement range
         * @return first UTF-16 code unit index of the replacement range
         */
        int getBegin();

        /**
         * Returns length of the replacement range in UTF-16 code units
         * @return length of the replacement range in UTF-16 code units
         */
        int getLength();
    }

    /**
     * Event listener interface for all events supported by this API.
     */
    @Provides
    interface EventListener {
        /**
         * Handles the {@link SelectTextRangeEvent}.
         * <p>
         * Custom text components that do not extend {@link java.awt.TextComponent} or {@link javax.swing.text.JTextComponent}
         * should subscribe to this event. When receiving it, they should select the text range of UTF-16 code units starting
         * at index {@param begin} of length {@param length}.
         * <p>
         * It is expected, that {@link java.awt.event.KeyEvent#KEY_TYPED}, or
         * {@link java.awt.event.InputMethodEvent} events will immediately follow. They will insert new text in place of the
         * old text pointed to by this event's range.
         *
         * @param event the {@link SelectTextRangeEvent} object
         */
        void handleSelectTextRangeEvent(SelectTextRangeEvent event);
    }

    /**
     * Sets the global event listener for text input/IME related events.
     * This should ideally be called once on the application startup.
     * Passing null will remove the listener.
     * The listener will only be called on the event dispatch thread (EDT).
     *
     * @param listener listener
     */
    void setGlobalEventListener(EventListener listener);
}
