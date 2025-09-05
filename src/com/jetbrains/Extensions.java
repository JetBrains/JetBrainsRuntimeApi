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
 * List of JBR API extensions.
 * @see Extension
 * @see JBR#isExtensionSupported(Extensions)
 */
public enum Extensions {
    /**
     * Checks if a display is builtin
     */
    BUILTIN_DISPLAY_CHECKER,

    /**
     * Opts-in {@link com.jetbrains.SystemUtils#shrinkingGC}
     */
    SHRINKING_GC,
    /**
     * Extends SharedTextures service with OpenGL support
     */
    SHARED_TEXTURES_OPENGL,
}
