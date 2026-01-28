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

import java.lang.instrument.Instrumentation;

/**
 * Javaagent used by the child JVM to obtain Instrumentation.
 */
public final class HotswapTestAgent {
  private static volatile Instrumentation inst;

  public static void premain(String agentArgs, Instrumentation instrumentation) {
    inst = instrumentation;
  }

  public static Instrumentation instrumentation() {
    Instrumentation i = inst;
    if (i == null) {
      throw new IllegalStateException("Instrumentation is null (missing -javaagent?)");
    }
    return i;
  }

  private HotswapTestAgent() {
  }
}
