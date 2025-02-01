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
 * This service provides utilities for using shared textures in JetBrainsRuntime
 */
@Service
@Provided
public interface SharedTextures {
    /** Shared textures are not supported */
    public final int NotSupported = 0;
    /** Metal textures are supported */
    public final int MetalTextureType = 1;

    /**
     * Check what type of the texture is supported
     * @param gc graphics configuration
     * @return the texture type or {@link SharedTextures#NotSupported} if shared textures are not supported
     */
    int getTextureType(GraphicsConfiguration gc);

    /**
     * Creates an image with the specified graphics configuration and texture.
     * The image acts as a wrapper for the native texture, allowing it to be
     * used similarly to a normal {@link java.awt.Image} object. Note that it cannot be used
     * as a drawing destination.
     *
     * Platform-specific details:
     * Only Metal textures are supported at the momemnt.
     *
     * @param gc the graphics configuration
     * @param texture the texture that will be wrapped by this instance.
     *                Platform-specific details:
     *                macOS (with the Metal rendering pipeline) - a pointer to an MTLTexture object is expected
     * @return the image
     *
     * @throws UnsupportedOperationException if the current pipeline is not supported
     * @throws IllegalArgumentException if the texture in a wrong format
     */
    Image wrapTexture(GraphicsConfiguration gc, long texture);
}