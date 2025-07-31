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
import java.awt.image.VolatileImage;

/**
 * The service provides functionality for working with shared textures in JetBrainsRuntime.
 *
 * This service is experemental and could be replaced with another service or deprecated.
 */
@Service
@Provided
public interface SharedTextures {
    /**
     * Metal textures are supported.
     */
    public final static int METAL_TEXTURE_TYPE = 1;

    /**
     * OpenGL textures are supported
     */
    public final static int OPENGL_TEXTURE_TYPE = 2;


    /**
     * Returns the texture type supported by the graphics configuration.
     * @param gc the GraphicsConfiguration
     * @return the type of shared texture supported.
     */
    @Extension(Extensions.SHARED_TEXTURES_OPENGL)
    int getTextureType(GraphicsConfiguration gc);

    /**
     * Returns the texture type supported by the default graphics configuration
     * of the default graphics device.
     * @deprecated The graphics environment may contain configurations of different types.
     *             Use {@link #getTextureType(GraphicsConfiguration)} instead.
     *
     * @return the type of shared texture supported.
     */
    @Deprecated
    int getTextureType();

    /**
     * Wraps the specified texture into an image that is compatible with the given graphics configuration.
     *
     * <p><b>Notes:</b></p>
     * <ul>
     *     <li>The resulting image cannot be used as a drawing destination.</li>
     *     <li>The resulting image is compatible with the provided {@link GraphicsConfiguration}.
     *         It is the responsibility of client code to track graphics configuration changes and recreate the wrapping
     *         image.</li>
     *     <li>Wrapping a texture has some overhead (allocating stencil data on Metal). It is advisable to reuse the
     *         image during the texture's lifetime unless the {@link GraphicsConfiguration} changes.</li>
     *     <li>Client code is responsible for ensuring proper synchronization. All operations involving
     *         the texture must have been completed before the resulting image is used within the JBR rendering
     *         pipeline.</li>
     *     <li>Texture lifespan:
     *          <ul>
     *              <li>Metal: This texture is retained for the wrapping image lifespan and will be released
     *                  after the image has been disposed.</li>
 *                  <li>OpenGL: The wrapping image doesn't take owernship over the texute.</li>
     *          </ul>
     *     </li>
     * </ul>
     *
     * @param gc      the target {@link GraphicsConfiguration}.
     * @param texture the texture to be wrapped.
     *                <p>Platform-specific:</p>
     *                <ul>
     *                    <li>Metal: an {@code MTLTexture} object pointer</li>
     *                    <li>OpenGL: a texture id({@code GLuint})</li>
     *               </ul>
     * @return a wrapping image compatible with the specified {@code GraphicsConfiguration}.
     * @throws UnsupportedOperationException if the current pipeline is not supported.
     * @throws IllegalArgumentException      if the texture cannot be wrapped. The details are logged in {@code J2D_TRACE_ERROR}.
     */
    Image wrapTexture(GraphicsConfiguration gc, long texture);

    /**
     * Returns the pointer to the shared OpenGL context instance.
     *
     * @param gc the graphics configuations
     * @return the OpenGL shared context instance.
     *         <p>Return type:</p>
     *         <ul>
     *           <li>Windows: {@code HGLRC}</li>
     *           <li>macOS: {@code CGLContextObj}</li>
     *           <li>Linux: {@code GLXContext}</li>
     *         </ul>
     */
    @Extension(Extensions.SHARED_TEXTURES_OPENGL)
    long getSharedOpenGLContext(GraphicsConfiguration gc) throws UnsupportedOperationException;

    /**
     * Returns the pixel format in the shared context.
     *
     * @param gc the graphics configuations
     * @return the pixel format.
     *         <p>Return type:</p>
     *         <ul>
     *           <li>Windows: {@code  int}</li>
     *           <li>macOS: {@code CGLPixelFormatObj}</li>
     *           <li>Linux: 0 (not used)</li>
     *         </ul>
     */
    @Extension(Extensions.SHARED_TEXTURES_OPENGL)
    long getSharedOpenGLPixelFormat(GraphicsConfiguration gc) throws UnsupportedOperationException;
}
