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

/*
 * @test
 * @modules java.instrument jdk.compiler
 * @build HotswapTestAgent HotswapTestMakeAgentJar HotswapTest
 * @run driver HotswapTestMakeAgentJar
 * @run main/othervm -javaagent:HotswapTestAgent.jar HotswapTest
 */

import com.jetbrains.JBR;

import com.jetbrains.Hotswap;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class HotswapTest {

  public static class A {
    public static int value() {
      return 1;
    }
  }

  public static void main(String[] args) throws Exception {
    if (!JBR.isHotswapSupported()) {
      System.out.println("SKIPPED: JBR Hotswap API is not available");
      return;
    }

    Instrumentation inst = HotswapTestAgent.instrumentation();
    if (inst == null) {
      throw new RuntimeException("Instrumentation is not available!");
    }

    Hotswap hotswap = JBR.getHotswap();

    final AtomicReference<Throwable> asyncFailure = new AtomicReference<>(null);
    final AtomicInteger calls = new AtomicInteger();
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean allowCallbacks = new AtomicBoolean(true);

    Hotswap.Listener listener = new Hotswap.Listener() {
      @Override
      public void onClassesRedefined() {
        if (!allowCallbacks.get()) {
          asyncFailure.compareAndSet(null, new RuntimeException("Listener invoked even after removeListener()"));
          return;
        }
        calls.incrementAndGet();
        latch.countDown();
      }
    };

    hotswap.addListener(listener);

    redefine_A(inst, 2);

    if (!latch.await(10, TimeUnit.SECONDS)) {
      throw new RuntimeException("Hotswap listener was not invoked after redefineClasses()");
    }
    if (calls.get() < 1) {
      throw new RuntimeException("Listener call counter mismatch");
    }

    int v = A.value();
    if (v != 2) {
      throw new RuntimeException("Redefinition likely did not apply (A.value()=" + v + ", expected 2)");
    }

    boolean removed = hotswap.removeListener(listener);
    if (!removed) {
      throw new RuntimeException("removeListener() returned false, expected true");
    }
    allowCallbacks.set(false);

    int before = calls.get();
    redefine_A(inst, 3);

    Throwable t = asyncFailure.get();
    if (t != null) {
      throw new RuntimeException(t);
    }
    if (calls.get() != before) {
      throw new RuntimeException("Call counter changed after removeListener()");
    }

    v = A.value();
    if (v != 3) {
      throw new RuntimeException("Redefinition likely did not apply (A.value()=" + v + ", expected 3)");
    }

    System.out.println("OK");
  }

  private static void redefine_A(Instrumentation inst, int newValue) {
    try {
      byte[] bytes = getReplacementBytes_A(newValue);
      inst.redefineClasses(new ClassDefinition(A.class, bytes));
    } catch (Exception e) {
      throw new RuntimeException("redefineClasses failed", e);
    }
  }

  private static byte[] getReplacementBytes_A(int newValue) {
    String src =
        "public class HotswapTest {\n" +
            "  public static class A {\n" +
            "    public static int value() { return " + newValue + "; }\n" +
            "  }\n" +
            "}\n";
    Map<String, byte[]> compiled = compileInMemory("HotswapTest", src);

    byte[] aBytes = compiled.get("HotswapTest$A");
    if (aBytes == null) {
      throw new IllegalStateException("Compiler did not produce HotswapTest$A.class");
    }
    return aBytes;
  }

  private static Map<String, byte[]> compileInMemory(String primaryClassName, String source) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException(
          "No system Java compiler available. Run tests on a JDK (or ensure jdk.compiler module is present)."
      );
    }

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager std = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null);

    InMemoryFileManager fm = new InMemoryFileManager(std);

    JavaFileObject srcObj = new StringSource(primaryClassName, source);

    List<String> options = List.of("-g:none");

    JavaCompiler.CompilationTask task = compiler.getTask(
        null, fm, diagnostics, options, null, List.of(srcObj)
    );

    Boolean ok = task.call();
    if (!Boolean.TRUE.equals(ok)) {
      StringBuilder sb = new StringBuilder("In-memory compilation failed:\n");
      for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
        sb.append(d.getKind()).append(": ").append(d.getMessage(Locale.ROOT)).append("\n");
      }
      throw new IllegalStateException(sb.toString());
    }

    return fm.getAllClassBytes();
  }

  private static final class StringSource extends SimpleJavaFileObject {
    private final String code;

    StringSource(String className, String code) {
      super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }

  private static final class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final Map<String, ByteArrayOutputStream> outputs = new HashMap<>();

    InMemoryFileManager(StandardJavaFileManager fileManager) {
      super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               JavaFileObject.Kind kind,
                                               FileObject sibling) {
      return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {
        @Override
        public OutputStream openOutputStream() {
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          outputs.put(className, bos);
          return bos;
        }
      };
    }

    Map<String, byte[]> getAllClassBytes() {
      Map<String, byte[]> res = new HashMap<>();
      for (Map.Entry<String, ByteArrayOutputStream> e : outputs.entrySet()) {
        res.put(e.getKey(), e.getValue().toByteArray());
      }
      return res;
    }
  }
}
