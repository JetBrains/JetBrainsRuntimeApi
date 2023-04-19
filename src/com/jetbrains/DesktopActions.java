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

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Allows desktop actions, like opening a file, or webpage to be overridden.
 * @see java.awt.Desktop
 */
@Service
@Provided
public interface DesktopActions {

    /**
     * Set global desktop action handler. All handler methods not
     * overridden explicitly are ignored and default behavior is triggered.
     * @param handler new action handler
     */
    void setHandler(Handler handler);

    /**
     * Desktop action handler.
     */
    @Provides
    interface Handler {

        /**
         * Launches the associated application to open the file.
         * @param file the file to be opened with the associated application
         * @throws IOException if the specified file has no associated
         * application or the associated application fails to be launched
         * @see java.awt.Desktop#open(java.io.File)
         */
        default void open(File file) throws IOException { throw new UnsupportedOperationException(); }

        /**
         * Launches the associated editor application and opens a file for editing.
         * @param file the file to be opened for editing
         * @throws IOException if the specified file has no associated
         * editor, or the associated application fails to be launched
         * @see java.awt.Desktop#edit(java.io.File)
         */
        default void edit(File file) throws IOException { throw new UnsupportedOperationException(); }

        /**
         * Prints a file with the native desktop printing facility, using
         * the associated application's print command.
         * @param file the file to be printed
         * @throws IOException if the specified file has no associated
         * application that can be used to print it
         * @see java.awt.Desktop#print(java.io.File)
         */
        default void print(File file) throws IOException { throw new UnsupportedOperationException(); }

        /**
         * Launches the mail composing window of the user default mail
         * client, filling the message fields specified by a {@code mailto:} URI.
         * @param mailtoURL the specified {@code mailto:} URI
         * @throws IOException if the user default mail client is not
         * found or fails to be launched
         * @see java.awt.Desktop#mail(java.net.URI)
         */
        default void mail(URI mailtoURL) throws IOException { throw new UnsupportedOperationException(); }

        /**
         * Launches the default browser to display a {@code URI}.
         * @param uri the URI to be displayed in the user default browser
         * @throws IOException if the user default browser is not found,
         * or it fails to be launched, or the default handler application
         * failed to be launched
         * @see java.awt.Desktop#browse(java.net.URI)
         */
        default void browse(URI uri) throws IOException { throw new UnsupportedOperationException(); }
    }

}
