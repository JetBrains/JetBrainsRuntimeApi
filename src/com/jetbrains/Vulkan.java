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
 * Access to the Vulkan rendering backend, if available.
 */
@Service
@Provided
public interface Vulkan {

    /**
     * Check whether Vulkan presentation is enabled.
     * Otherwise, only offscreen rendering is available.
     * @return true if Vulkan presentation is enabled
     */
    boolean isPresentationEnabled();

    /**
     * Get available Vulkan devices (GPUs).
     * @return array of available devices
     */
    Device[] getDevices();

    /**
     * A Vulkan device (GPU).
     */
    @Provided
    interface Device {
        /**
         * Presentation capability bit.
         */
        int CAP_PRESENTABLE_BIT = 0x80000000;
        /**
         * Logic op support (XOR drawing) capability bit.
         */
        int CAP_LOGIC_OP_BIT    = 0x40000000;
        /**
         * 4BYTE sampled format support bit.
         * This format is considered always supported, the constant is equal to 0.
         */
        int CAP_SAMPLED_4BYTE_BIT = 0;
        /**
         * 3BYTE sampled format support bit.
         */
        int CAP_SAMPLED_3BYTE_BIT = 1;
        /**
         * 565 sampled format support bit.
         */
        int CAP_SAMPLED_565_BIT   = 2;
        /**
         * 555 sampled format support bit.
         */
        int CAP_SAMPLED_555_BIT   = 4;

        /**
         * Get device name.
         * @return device name
         */
        String getName();

        /**
         * Get device type, one of OTHER, INTEGRATED_GPU, DISCRETE_GPU, VIRTUAL_GPU, CPU.
         * @return device type
         */
        String getTypeString();

        /**
         * Get device capabilities, see {@code CAP_*} constants.
         * @return device capabilities
         */
        int getCapabilities();
    }
}
