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
 * Extends services provided by java.lang.System and similar.
 */
@Service
@Provided
public interface SystemUtils {
    /**
     * Performs garbage collection making an additional effort to mark and collect
     * referrents of soft and weak references.
     * Implemented for G1 only; equivalent to System.gc() for other garbage collectors.
     */
    void fullGC();

    /**
     * Performs garbage collection making an additional effort to reduce the heap's committed size.
     * After GC, the JVM adjusts the committed heap size based on the amount of used memory,
     * adhering to the -XX:JbrShrinkingGcMaxHeapFreeRatio option.
     *
     * Implemented for G1 only; equivalent to System.gc() for other garbage collectors.
     *
     * Equivalent to System.gc() if -XX:JbrShrinkingGcMaxHeapFreeRatio option is not set.
     *
     * Note: This does not change the minimum (-Xms) or maximum (-Xmx) heap size limits
     */
    @Extension(Extensions.SHRINKING_GC)
    void shrinkingGC();
}
