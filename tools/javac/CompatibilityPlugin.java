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

import com.sun.source.tree.*;
import com.sun.source.util.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Magically makes JBR API written in Java 9 compile for Java 8.
 * Also removes Java 9 classes which are duplicating existing Java 8 classes.
 */
public class CompatibilityPlugin implements Plugin {

    private TreeMaker treeMaker;

    @Override
    public String getName() {
        return "CompatibilityPlugin";
    }

    @Override
    public void init(JavacTask task, String... args) {
        Context context = ((BasicJavacTask) task).getContext();
        treeMaker = TreeMaker.instance(context);
        task.addTaskListener(new TaskListener() {
            public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.PARSE) {
                    e.getCompilationUnit().accept(scanner, null);
                } else if (e.getKind() == TaskEvent.Kind.COMPILATION) {
                    if (args.length >= 2) removeDuplicates(args[0], args[1]);
                }
            }
        });
    }

    private final TreeScanner<Void, String> scanner = new TreeScanner<>() {
        @Override
        public Void visitAnnotation(AnnotationTree node, String qualifiedName) {
            switch (node.getAnnotationType().toString()) {
                case "Deprecated", "java.lang.Deprecated" -> {
                    // @Deprecated didn't have any members in Java 8, so clear it.
                    var jca = (JCTree.JCAnnotation) node;
                    jca.args = List.nil();
                }
            }
            return super.visitAnnotation(node, qualifiedName);
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, String qualifiedName) {
            ExpressionTree pkg = node.getPackageName();
            return super.visitCompilationUnit(node, pkg == null ? null : pkg.toString());
        }
        @Override
        public Void visitClass(ClassTree node, String qualifiedName) {
            return super.visitClass(node, qualifiedName == null ? node.getSimpleName().toString() :
                    qualifiedName + "." + node.getSimpleName().toString());
        }
        @Override
        public Void visitMethod(MethodTree node, String qualifiedName) {
            // getApiVersionFromModule works, well, by asking version from module,
            // which is not available in Java 8, so for that case we return UNKNOWN.
            if (qualifiedName.equals("com.jetbrains.JBR") &&
                    node.getName().toString().equals("getApiVersionFromModule")) {
                var body = (JCTree.JCBlock) node.getBody();
                body.stats = List.of(treeMaker.Return(treeMaker.Literal("UNKNOWN")));
            }
            return super.visitMethod(node, qualifiedName);
        }
    };

    // Can we really simply compare class files byte by byte?
    // I doubt it, but jar utility does exactly that, well...
    private static void removeDuplicates(String out8, String out9) {
        byte[] cafeBabe = {(byte) 0xca, (byte) 0xfe, (byte) 0xba, (byte) 0xbe};
        Path path8 = Path.of(out8), path9 = Path.of(out9);
        try (Stream<Path> stream8 = Files.walk(path8)) {
            stream8.filter(Files::isRegularFile).forEach(f8 -> {
                Path f9 = path9.resolve(path8.relativize(f8));
                try {
                    byte[] b9 = Files.readAllBytes(f9);
                    byte[] b8 = Files.readAllBytes(f8);
                    // First 4 bytes are a magic number, next 4 bytes are version which we are ignoring.
                    if (Arrays.equals(b8, 0, 4, cafeBabe, 0, 4) && Arrays.equals(b9, 0, 4, cafeBabe, 0, 4) &&
                            Arrays.equals(b8, 8, b8.length, b9, 8, b9.length)) {
                        // Duplicate! Remove it.
                        Files.delete(f9);
                    }
                } catch (NoSuchFileException | IllegalArgumentException | ArrayIndexOutOfBoundsException ignore) {
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
