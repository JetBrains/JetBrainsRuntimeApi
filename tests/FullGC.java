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
 * @summary Verifies that most soft and weak references are collected
 *          by a special GC invocation through SystemUtils.fullGC() API
 * @run main/othervm -XX:+UseG1GC -Xmx1G FullGC pass
 * @run main/othervm -XX:+UseG1GC -Xmx2G FullGC pass
 * @run main/othervm -XX:+UseZGC -Xmx2G FullGC
 * @run main/othervm -XX:+UseShenandoahGC -Xmx2G FullGC
 * @run main/othervm -XX:+UseParallelGC -Xmx2G FullGC
 * @run main/othervm -XX:+UseSerialGC -Xmx2G FullGC
 * @run main/othervm -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xmx2G FullGC
 */
import com.jetbrains.JBR;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FullGC {
    private static final List<SoftReference<Object>> softReferences = new ArrayList<>();
    private static final List<WeakReference<Object>> weakReferences = new ArrayList<>();
    private static final int ALLOCATION_SIZE = 1024 * 1024; // 1MB
    private static final int ITERATIONS = 1000;
    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {
        boolean expectTestToPass = args.length > 0 && "pass".equals(args[0]);

        System.out.println("Starting memory allocation...");

        for (int allocations = 1; allocations < ITERATIONS; allocations++) {
            if (random.nextBoolean()) {
                weakReferences.add(new WeakReference<>(allocateMemory(ALLOCATION_SIZE)));
            } else {
                softReferences.add(new SoftReference<>(allocateMemory(ALLOCATION_SIZE)));
            }

            if (allocations % 100 == 0) {
                System.out.println("\n\nAllocation cycle " + allocations);
                System.out.print("Before GC:\t");
                long softRefsBefore = countLiveSoftRefs();
                long weakRefsBefore = countLiveWeakRefs();
                System.out.printf("Soft %d total vs. %d live\t", softReferences.size(), softRefsBefore);
                System.out.printf("Weak %d total vs. %d live\n", weakReferences.size(), weakRefsBefore);

                JBR.getSystemUtils().fullGC();

                System.out.print("After GC:\t");
                long softRefsAfter = countLiveSoftRefs();
                long weakRefsAfter = countLiveWeakRefs();
                System.out.printf("Soft %d total vs. %d live\t", softReferences.size(), softRefsAfter);
                System.out.printf("Weak %d total vs. %d live\n", weakReferences.size(), weakRefsAfter);

                double softRefsRemain = 100.0 * softRefsAfter / softRefsBefore;
                double weakRefsRemain = 100.0 * weakRefsAfter / weakRefsBefore;

                System.out.printf("Refs survived GC: soft %.2f%%, weak %.2f%%\n", softRefsRemain, weakRefsRemain);
                if (expectTestToPass && softRefsRemain > 10.0) {
                    throw new RuntimeException("More than 10% of soft references remain after GC");
                }

                if (expectTestToPass && weakRefsRemain > 10.0) {
                    throw new RuntimeException("More than 10% of weak references remain after GC");
                }
            }
        }
    }

    private static long countLiveSoftRefs() {
        List<SoftReference<Object>> snapshot;
        snapshot = new ArrayList<>(softReferences);
        return snapshot.stream()
                .filter(ref -> ref.get() != null)
                .count();
    }

    private static long countLiveWeakRefs() {
        List<WeakReference<Object>> snapshot;
        snapshot = new ArrayList<>(weakReferences);
        return snapshot.stream()
                .filter(ref -> ref.get() != null)
                .count();
    }

    private static Object allocateMemory(int bytes) {
        int nArrays = bytes / 1024;
        List<byte[]> list = new ArrayList<>(nArrays);
        for (int i = 0; i < nArrays; i++) {
            list.add(new byte[1024]);
        }
        return list;
    }
}
