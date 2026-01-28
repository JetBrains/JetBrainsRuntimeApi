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

/**
 * JBR service for class redefinition notifications (hotswap).
 * Typical usage:
 *   if (JBR.isHotswapSupported()) {
 *       Hotswap hotswap = JBR.getHotswap();
 *       hotswap.addListener(...);
 *   }
 */
@Service
@Provided
public interface Hotswap {

    /**
     * Registers a hotswap listener.
     *
     * @param l the listener to remove

     * Contract:
     *  - Typically called during agent startup (e.g. by HotswapAgent).
     *  - The same listener instance may be registered multiple times; the implementation
     *    is allowed to ignore duplicates.
     *  - Implementations must be thread-safe.
     */
    void addListener(Listener l);

    /**
     * Unregisters a previously registered hotswap listener.
     *
     * @param l the listener to remove
     * @return {@code true} if the listener was previously registered and has been removed,
     *         {@code false} if the listener was not registered and nothing was changed.
     * Contract:
     *  - Implementations must be thread-safe.
     */
    boolean removeListener(Listener l);

    /**
     * Defines a listener interface to handle notifications when one or more
     * classes are successfully redefined or retransformed during the runtime.
     */
    @Provides
    interface Listener {
        /**
         * Called by the runtime after one or more classes have been successfully
         * redefined or retransformed.
         * Thread model:
         *  - Invoked on the Java thread that initiated the corresponding JVMTI
         *    RedefineClasses or RetransformClasses call (never on VMThread and
         *    never while the VM is at a safepoint).
         * Restrictions:
         *  - Must return quickly and avoid long blocking operations.
         *  - Must NOT trigger another class redefinition or retransformation.
         *  - Any exception thrown by this method is ignored by the runtime.
         */
        void onClassesRedefined();
    }
}
