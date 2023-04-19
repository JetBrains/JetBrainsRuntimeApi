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

import java.util.function.Supplier;

/**
 * Jstack-related utilities.
 */
@Service
@Provided
public interface Jstack {
    /**
     * Specifies a supplier of additional information to be included into
     * the output of {@code jstack}. The String supplied will be included
     * as-is with no header surrounded only with line breaks.
     *
     * {@code infoSupplier} will be invoked on an unspecified thread that
     * must not be left blocked for a long time.
     *
     * Only one supplier is allowed, so subsequent calls to
     * {@code includeInfoFrom} will overwrite the previously specified supplier.
     *
     * @param infoSupplier a supplier of {@code String} values to be
     *                     included into jstack's output. If {@code null},
     *                     then the previously registered supplier is removed
     *                     (if any) and no extra info will be included.
     */
    void includeInfoFrom(Supplier<String> infoSupplier);
}
