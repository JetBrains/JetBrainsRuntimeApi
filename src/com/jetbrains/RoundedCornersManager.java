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
 * This manager allows decorate awt Window with rounded corners.
 * Appearance depends on operating system.
 */
@Service
@Provided
public interface RoundedCornersManager {
    /**
     * Setup rounded corners on window.
     *
     * Possible values for macOS:
     * <ul>
     *   <li>{@link Float} object with radius</li>
     *   <li>{@link Object} array with:<ul>
     *     <li>{@link Float} for radius</li>
     *     <li>{@link Integer} for border width</li>
     *     <li>{@link java.awt.Color} for border color</li>
     *   </ul></li>
     * </ul>
     *
     * Possible values for Windows 11 ({@link java.lang.String}):
     * <ul>
     *   <li>"default" - let the system decide whether or not to round window corners</li>
     *   <li>"none" - never round window corners</li>
     *   <li>"full" - round the corners if appropriate</li>
     *   <li>"small" - round the corners if appropriate, with a small radius</li>
     * </ul>
     *
     * @param window window to setup rounded corners on
     * @param params rounded corners hint
     */
    void setRoundedCorners(Window window, Object params);
}
