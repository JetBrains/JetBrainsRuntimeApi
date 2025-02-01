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

import java.awt.*;

/**
 * The service provides functionality for working with shared textures in JetBrainsRuntime.
 */
@Service
@Provided
public interface SharedTextures {
    /**
     * Shared textures are not supported.
     */
    public final int NotSupported = 0;
    /**
     * Metal textures are supported.
     */
    public final int MetalTextureType = 1;

    /**
     * Checks what type of texture is supported by the current rendering pipeline.
     *
     * @return the type of shared texture supported, or {@link SharedTextures#NotSupported}
     * if shared textures are not supported.
     */
    int getTextureType();

    /**
     * Wraps the specified texture into an image that is compatible with the given graphics configuration.
     *
     * <p><b>Notes:</b></p>
     * <ul>
     *     <li>The resulting image cannot be used as a drawing destination.</li>
     *     <li>The resulting image is compatible with the provided {@link GraphicsConfiguration}.
     *         It is the responsibility of client code to track graphics configuration changes and recreate the wrapping image.</li>
     *     <li>Wrapping a texture has some overhead (allocating stencil data on Metal). It is advisable to reuse the
     *         image during the texture's lifetime unless the {@link GraphicsConfiguration} changes.</li>
     *     <li>Client code is responsible for ensuring proper synchronization. All operations involving
     *         the texture must be completed before the texture is used within the JBR rendering pipeline.</li>
     *     <li>Wrapping the texture increments the texture reference counter, which is subsequently decremented
     *         when the image is disposed.</li>
     * </ul>
     *
     * @param gc      the target {@link GraphicsConfiguration}.
     * @param texture the texture to be wrapped.
     *                <p>Platform-specific:</p>
     *                <ul>
     *                    <li>macOS (with the Metal rendering pipeline): a pointer to an {@code MTLTexture} object</li>
     *                </ul>
     * @return a wrapping image compatible with the specified {@code GraphicsConfiguration}.
     * @throws UnsupportedOperationException if the current pipeline is not supported.
     * @throws IllegalArgumentException      if the texture cannot be wrapped. The details are logged in {@code J2D_TRACE_ERROR}.
     */
    Image wrapTexture(GraphicsConfiguration gc, long texture);
}
