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

package com.jetbrains;

/**
 * <p>Provides a description of the current desktop operating environment appertaining
 * to hi-DPI properties such as monitor resolutions, scaling, etc.
 * <p><b>Note:</b> Supported on Linux ({@code XToolkit}, {@code WLToolkit}) only.
 */
@Service
@Provided
public interface HiDPIInfo {
    /**
     * Get a description of the current desktop operating environment, particularly
     * focusing on monitor resolutions and relevant desktop settings.
     * 
     * @return a table of {@code String} objects organized in rows by the first index
     * and columns by the second index. Each row describes one property with
     * its name as the first element, value the second, and optional description 
     * as the third.
     */
    String[][] getInfo();
}

