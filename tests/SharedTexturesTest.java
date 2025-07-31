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

/**
 * @test
 *
 * @summary Checks SharedTexture service presence on Linux
 * @requires (os.family == "linux")
 *
 * @run main/othervm -Dsun.java2d.opengl=True -DsharedTexturesSupported=True SharedTexturesTest
 */

/**
 * @test
 *
 * @summary Checks SharedTexture service presence on Windows
 * @requires (os.family == "windows")
 *
 * @run main/othervm -Dsun.java2d.opengl=True -DsharedTexturesSupported=True SharedTexturesTest
 */

/**
 * @test
 *
 * @summary Checks SharedTexture service presence on Windows
 * @requires (os.family == "mac")
 *
 * @run main/othervm -Dsun.java2d.opengl=True -DsharedTexturesSupported=True SharedTexturesTest
 * @run main/othervm -Dsun.java2d.metal=True -DsharedTexturesSupported=True SharedTexturesTest
 */


import com.jetbrains.Extensions;
import com.jetbrains.JBR;
import com.jetbrains.SharedTextures;

import java.awt.*;

public class SharedTexturesTest {
    final static GraphicsConfiguration gc = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();

    public static void main(String[] args) {
        if (!JBR.isSharedTexturesSupported()) {
            throw new RuntimeException("SharedTextures are not supported");
        }

        SharedTextures sharedTexturesService = JBR.getSharedTextures(Extensions.SHARED_TEXTURES_OPENGL);
        if (sharedTexturesService == null) {
            throw new RuntimeException("Extensions.SHARED_TEXTURES_OPENGL is not available");
        }

        int expectedTextureType = getExpectedTextureType();
        int actualTextureType = sharedTexturesService.getTextureType(gc);

        if (actualTextureType != expectedTextureType) {
            throw new RuntimeException("Expected texture type: " + expectedTextureType + ", actual: " + actualTextureType);
        }

        if (actualTextureType == SharedTextures.OPENGL_TEXTURE_TYPE) {
            if (sharedTexturesService.getSharedOpenGLContext(gc) == 0) {
                throw new RuntimeException("Expected non-zero shared OpenGL context");
            }
            sharedTexturesService.getSharedOpenGLPixelFormat(gc); // expect no throw
        }

        {
            boolean illegalArgumentExceptionThrown = false;
            try {
                sharedTexturesService.wrapTexture(gc, 0);
            } catch (IllegalArgumentException e) {
                illegalArgumentExceptionThrown = true;
            }
            if (!illegalArgumentExceptionThrown) {
                throw new RuntimeException("Expected IllegalArgumentException");
            }
        }
    }

    public static int getExpectedTextureType() {
        if ("true".equalsIgnoreCase(System.getProperty("sun.java2d.opengl"))) {
            return SharedTextures.OPENGL_TEXTURE_TYPE;
        } else if ("true".equalsIgnoreCase(System.getProperty("sun.java2d.metal"))) {
            return SharedTextures.METAL_TEXTURE_TYPE;
        }

        throw new InternalError("Unexpected rendering pipeline. The default graphics config: " + gc.toString());
    }
}
