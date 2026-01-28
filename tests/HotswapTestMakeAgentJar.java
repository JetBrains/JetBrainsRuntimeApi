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

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class HotswapTestMakeAgentJar {
  public static void main(String[] args) throws Exception {
    Path testClasses = Path.of(System.getProperty("test.classes"));
    if (!Files.isDirectory(testClasses)) {
      throw new RuntimeException("test.classes is not a directory: " + testClasses);
    }

    Path agentClass = testClasses.resolve("HotswapTestAgent.class");
    if (!Files.isRegularFile(agentClass)) {
      throw new RuntimeException("Agent class not found: " + agentClass);
    }

    Path outJar = Path.of("HotswapTestAgent.jar");
    Files.deleteIfExists(outJar);

    Manifest mf = new Manifest();
    Attributes a = mf.getMainAttributes();
    a.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    a.putValue("Premain-Class", "HotswapTestAgent");
    a.putValue("Agent-Class", "HotswapTestAgent");
    a.putValue("Can-Redefine-Classes", "true");
    a.putValue("Can-Retransform-Classes", "true");

    try (OutputStream fos = Files.newOutputStream(outJar);
         JarOutputStream jos = new JarOutputStream(fos, mf)) {

      JarEntry e = new JarEntry("HotswapTestAgent.class");
      jos.putNextEntry(e);
      jos.write(Files.readAllBytes(agentClass));
      jos.closeEntry();
    }
  }
}
