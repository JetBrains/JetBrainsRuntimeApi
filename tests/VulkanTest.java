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


/*
 * @test
 * @summary Verifies that Vulkan service works on Linux.
 * @requires os.family == "linux"
 * @key headful
 * @run main/othervm -Dawt.toolkit.name=WLToolkit -Dsun.java2d.vulkan=True VulkanTest
 */

import com.jetbrains.JBR;
import com.jetbrains.Vulkan;

public class VulkanTest {

    public static void main(String[] args) throws Exception {
        Vulkan vulkan = JBR.getVulkan();
        if (vulkan == null) throw new Error("Vulkan is not available");
        if (vulkan.getDevices() == null) throw new Error("Could not retrieve devices");
        System.out.println("Presentation enabled: " + vulkan.isPresentationEnabled());
    }
}
