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

/**
 * Extensions to the AWT {@code FileDialog} that allow clients fully use a native file chooser
 * on supported platforms (currently macOS and Windows; the latter requires setting
 * {@code sun.awt.windows.useCommonItemDialog} property to {@code true}).
 */
@Provided
public interface JBRFileDialog {

    /**
     * Whether to select files, directories or both (used when common file dialogs are enabled on Windows, or on macOS)
     */
    int SELECT_FILES_HINT = 1, SELECT_DIRECTORIES_HINT = 2;
    /**
     * Whether to allow creating directories or not (used on macOS)
     */
    int CREATE_DIRECTORIES_HINT = 4;

    /**
     * "open" button when a file is selected in the list
     */
    String OPEN_FILE_BUTTON_KEY = "jbrFileDialogOpenFile";
    /**
     * "open" button when a directory is selected in the list
     */
    String OPEN_DIRECTORY_BUTTON_KEY = "jbrFileDialogSelectDir";
    /**
     * "all files" item in the file filter combo box
     */
    String ALL_FILES_COMBO_KEY = "jbrFileDialogAllFiles";

    /**
     * Get {@link JBRFileDialog} from {@link FileDialog}, if supported.
     * @param dialog file dialog
     * @return file dialog extension, or null
     */
    static JBRFileDialog get(FileDialog dialog) {
        if (JBRFileDialogService.INSTANCE == null) return null;
        else return JBRFileDialogService.INSTANCE.getFileDialog(dialog);
    }

    /**
     * Set file dialog hints:
     * <ul>
     *     <li>SELECT_FILES_HINT, SELECT_DIRECTORIES_HINT - whether to select files, directories, or both;
     *     if neither of the two is set, the behavior is platform-specific</li>
     *     <li>CREATE_DIRECTORIES_HINT - whether to allow creating directories or not (macOS)</li>
     * </ul>
     * @param hints bitmask of selected hints
     */
    void setHints(int hints);

    /**
     * Retrieve extended hints set on file dialog.
     * @return bitmask of selected hints
     * @see #setHints(int) 
     */
    int getHints();

    /**
     * Change text of UI elements (Windows).
     * Supported keys:
     * <ul>
     *     <li>OPEN_FILE_BUTTON_KEY - "open" button when a file is selected in the list</li>
     *     <li>OPEN_DIRECTORY_BUTTON_KEY - "open" button when a directory is selected in the list</li>
     *     <li>ALL_FILES_COMBO_KEY - "all files" item in the file filter combo box</li>
     * </ul>
     * @param key key
     * @param text localized text
     */
    void setLocalizationString(String key, String text);

    /**
     * Set file filter - a set of file extensions for files to be visible (Windows)
     * or not greyed out (macOS), and a name for the file filter combo box (Windows).
     * @param fileFilterDescription file filter description
     * @param fileFilterExtensions file filter extensions
     */
    void setFileFilterExtensions(String fileFilterDescription, String[] fileFilterExtensions);
}

@Service
@Provided
interface JBRFileDialogService {
    JBRFileDialogService INSTANCE = JBR.getService(JBRFileDialogService.class);
    JBRFileDialog getFileDialog(FileDialog dialog);
}
