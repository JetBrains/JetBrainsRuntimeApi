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
 * @summary Verifies that heap is shrunk in size
 *          by a special GC invocation through SystemUtils.shrinkingGC() API
 * @run main/othervm -XX:JbrShrinkingGcMaxHeapFreeRatio=50 -XX:+UseG1GC -Xmx2G ShrinkingGC pass
 * @run main/othervm -XX:JbrShrinkingGcMaxHeapFreeRatio=50 -XX:+UseZGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:JbrShrinkingGcMaxHeapFreeRatio=50 -XX:+UseShenandoahGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:JbrShrinkingGcMaxHeapFreeRatio=50 -XX:+UseParallelGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:JbrShrinkingGcMaxHeapFreeRatio=50 -XX:+UseSerialGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:JbrShrinkingGcMaxHeapFreeRatio=50 -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:+UseG1GC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:+UseZGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:+UseShenandoahGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:+UseParallelGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:+UseSerialGC -Xmx2G ShrinkingGC
 * @run main/othervm -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xmx2G ShrinkingGC
 */

import com.jetbrains.Extensions;
import com.jetbrains.JBR;

import java.nio.ByteBuffer;

public class ShrinkingGC {
    private static final int ITERATIONS = 10;

    public static void main(String[] args) throws Exception {
        boolean expectTestToPass = args.length > 0 && "pass".equals(args[0]);

        ByteBuffer retained500m = ByteBuffer.allocate(512 * 1024 * 1024);

        for (int i = 1; i < ITERATIONS; i++) {
            ByteBuffer.allocate(512 * 1024 * 1024);

            System.out.println("Heap size before GC: " + Runtime.getRuntime().totalMemory());

            Thread.sleep(300);
            System.gc();
            long heapSizeAfterGc = Runtime.getRuntime().totalMemory();

            System.out.println("Heap size after System.gc(): " + heapSizeAfterGc);

            JBR.getSystemUtils(Extensions.SHRINKING_GC).shrinkingGC();
            long heapSizeAfterShrinkingGc = Runtime.getRuntime().totalMemory();

            System.out.println("Heap size after shrinkingGC(): " + heapSizeAfterShrinkingGc);
            System.out.println();

            if (expectTestToPass && ((double) heapSizeAfterShrinkingGc / (double) heapSizeAfterGc) > 0.8) {
                throw new RuntimeException("shrinkingGC() could not shrink more that gc() does");
            }
        }

        System.out.println(retained500m);
    }
}
