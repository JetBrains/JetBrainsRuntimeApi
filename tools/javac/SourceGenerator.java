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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates main class "JBR".
 */
public class SourceGenerator {

    private final ProcessingEnvironment processingEnv;
    private final String jbrTemplate, serviceGetterTemplate;

    public SourceGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        try {
            jbrTemplate = Files.readString(Path.of("tools/templates/JBR.java"));
            serviceGetterTemplate = Files.readString(Path.of("tools/templates/service-getter.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generate(Round round) {
        Set<? extends Element>
                serviceElements = round.getElementsAnnotatedWith(round.annotations.service),
                providedElements = round.getElementsAnnotatedWith(round.annotations.provided);
        Map<String, Set<TypeElement>> extensions = findExtensions(round);

        // Generate JBR class source code.
        List<String> serviceGetters = serviceElements.stream()
                .filter(e -> // Only top-level public interfaces are included.
                        e.getEnclosingElement().getKind() == ElementKind.PACKAGE &&
                        e.getModifiers().contains(Modifier.PUBLIC))
                .map(s -> generateServiceGetter(round, s)).toList();
        List<String> knownExtensions = extensions.entrySet().stream()
                .map(e -> "KNOWN_EXTENSIONS.put(Extensions." + e.getKey() + ", new Class[] {" +
                        e.getValue().stream().map(c -> c.getQualifiedName() + ".class")
                                .collect(Collectors.joining(", ")) + "});").toList();
        String result = replaceTemplate(
                replaceTemplate(jbrTemplate, "/*GENERATED_METHODS*/", serviceGetters, true),
                "/*KNOWN_EXTENSIONS*/", knownExtensions, false)
                .replace("/*KNOWN_PROXIES*/", joinClassNamesToList(providedElements))
                .replace("/*KNOWN_SERVICES*/", joinClassNamesToList(serviceElements));

        // Write generated content.
        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile("jetbrains.runtime.api/com.jetbrains.JBR",
                    Stream.concat(serviceElements.stream(), providedElements.stream()).toArray(Element[]::new));
            try (Writer w = file.openWriter()) {
                w.write(result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateServiceGetter(Round round, Element service) {
        String fallback = round.getFallbackName(service);
        String javadoc = processingEnv.getElementUtils().getDocComment(service);
        if (javadoc != null) javadoc = "\n *" + javadoc.replaceAll("\n", "\n *");
        else javadoc = "";
        Deprecated deprecated = service.getAnnotation(Deprecated.class);
        String deprecation;
        if (deprecated == null) deprecation = "";
        else if (!deprecated.forRemoval()) deprecation = "\n" + deprecated;
        else deprecation = "\n@SuppressWarnings(\"removal\")\n" + deprecated;
        return serviceGetterTemplate
            .replace("<FALLBACK>", fallback != null ? fallback + "::new" : "null")
            .replaceAll("\\$", service.getSimpleName().toString())
            .replace("<JAVADOC>", javadoc)
            .replaceAll("<DEPRECATED>", deprecation);
    }

    private String joinClassNamesToList(Set<? extends Element> elements) {
        return elements.stream()
                .map(e -> "\"" + processingEnv.getElementUtils().getBinaryName((TypeElement) e) + "\"")
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("SameParameterValue")
    private static String replaceTemplate(String src, String placeholder, Iterable<String> statements, boolean space) {
        int placeholderIndex = src.indexOf(placeholder);
        int indent = 0;
        while (placeholderIndex - indent >= 1 && src.charAt(placeholderIndex - indent - 1) == ' ') indent++;
        int nextLineIndex = src.indexOf('\n', placeholderIndex + placeholder.length()) + 1;
        if (nextLineIndex == 0) nextLineIndex = placeholderIndex + placeholder.length();
        String before = src.substring(0, placeholderIndex - indent), after = src.substring(nextLineIndex);
        StringBuilder sb = new StringBuilder(before);
        boolean firstStatement = true;
        for (String s : statements) {
            if (!firstStatement && space) sb.append('\n');
            sb.append(s.indent(indent));
            firstStatement = false;
        }
        sb.append(after);
        return sb.toString();
    }

    private static Map<String, Set<TypeElement>> findExtensions(Round round) {
        if (round.annotations.extension == null) return Map.of();
        Set<? extends Element> extensionElements = round.getElementsAnnotatedWith(round.annotations.extension);
        Map<String, Set<TypeElement>> map = new HashMap<>();
        for (Element method : extensionElements) {
            String extension = round.getExtensionName(method);
            if (extension != null) {
                map.computeIfAbsent(extension, s -> new HashSet<>()).add((TypeElement) method.getEnclosingElement());
            }
        }
        return map;
    }

}
