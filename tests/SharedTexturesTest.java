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


import com.jetbrains.JBR;
import com.jetbrains.SharedTextures;

public class SharedTexturesTest {
    public static void main(String[] args) {
        if (!JBR.isSharedTexturesSupported()) {
            throw new RuntimeException("SharedTextures are not supported");
        }
    }

    public static int getExpectedTextureType() {
        if ("true".equalsIgnoreCase(System.getProperty("sun.java2d.opengl"))) {
            return SharedTextures.METAL_TEXTURE_TYPE;
        }
    }
}
