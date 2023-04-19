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

/*
 * @test
 * @summary Verifies that jstack includes whatever the supplier
 *          provided to Jstack.includeInfoFrom() returns.
 * @run main JstackTest
 */

import com.jetbrains.JBR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;

public class JstackTest {
    final static String MAGIC_STRING1 = "Additional info:";
    final static String MAGIC_STRING2 = "this appears in jstack's output";

    public static void main(String[] args) {
        JBR.getJstack().includeInfoFrom( () -> MAGIC_STRING1 + "\n" + MAGIC_STRING2 );
        long pid = ProcessHandle.current().pid();
        Process process = runJstack(pid);
        verifyOutput(process);
    }

    static Process runJstack(long pid) {
        try {
            boolean windows = System.getProperty("os.name").toLowerCase().startsWith("win");
            String jdkPath = Objects.requireNonNull(System.getProperty("test.jdk"));
            Path jstackPath = Path.of(jdkPath, "bin", "jstack" + (windows ? ".exe" : ""));

            final String JSTACK = jstackPath.toAbsolutePath().toString();
            final ProcessBuilder pb = new ProcessBuilder(JSTACK, String.valueOf(pid));
            return pb.start();
        } catch (IOException e) {
            throw new RuntimeException("Launching jstack failed", e);
        }
    }

    static void verifyOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (MAGIC_STRING1.equals(s) && MAGIC_STRING2.equals(reader.readLine())) {
                    if (process.waitFor() == 0) {
                        return;
                    } else {
                        throw new RuntimeException("Non-zero jstack exit code");
                    }
                }
            }
            throw new RuntimeException("Magic string not found in output");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
