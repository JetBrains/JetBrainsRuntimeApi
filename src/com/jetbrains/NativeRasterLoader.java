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

import java.awt.image.VolatileImage;

/**
 * Direct raster loading for VolatileImage.
 */
@Service
@Provided
public interface NativeRasterLoader {
    /**
     * Loads native image raster into VolatileImage.
     *
     * @param vi volatile image
     * @param pRaster native pointer image raster with 8-bit RGBA color components packed into integer pixels.
     * Note: The color data in this image is considered to be premultiplied with alpha.
     * @param width width of image in pixels
     * @param height height of image in pixels
     * @param pRects native pointer to array of "dirty" rects, each rect is a sequence of four 32-bit integers: x, y, width, heigth
     * Note: can be null (then whole image used)
     * @param rectsCount count of "dirty" rects (if 0 then whole image used)
     */
    void loadNativeRaster(VolatileImage vi, long pRaster, int width, int height, long pRects, int rectsCount);
}