/*
 * Copyright 2025 JetBrains s.r.o.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
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
