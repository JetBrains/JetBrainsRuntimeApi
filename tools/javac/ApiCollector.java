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
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementScanner8;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.*;

/**
 * Collects API info from parsed elements and raw source code.
 */
public class ApiCollector {

    private final ProcessingEnvironment processingEnv;
    private Round round;

    // Maps source files to hashes of qualified names of associated root elements, used to calculate total API hash.
    private final Map<JavaFileObject, Integer> compilationUnitHashMap = new HashMap<>(); // Hash map of hashes!
    private final Set<Element> encounteredSupertypes = new HashSet<>(), nonAnnotatedUnusedApiTypes = new HashSet<>();
    private final Api.Module api = new Api.Module();

    public ApiCollector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public List<String> getJava8CompilationUnitPaths() {
        Path moduleInfo = Path.of("module-info.java");
        return compilationUnitHashMap.keySet().stream().map(f -> {
            Path p = Path.of(f.toUri()).toAbsolutePath();
            return p.getFileName().equals(moduleInfo) ? null : p.toString();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Api.Module collect(Round round) {
        this.round = round;

        // Validate usage of annotations
        Set<? extends Element> serviceAnnotated = round.annotations.service == null ? Set.of() :
                round.env.getElementsAnnotatedWith(round.annotations.service);
        Set<? extends Element> providedAnnotated = round.annotations.provided == null ? Set.of() :
                round.env.getElementsAnnotatedWith(round.annotations.provided);
        Set<? extends Element> providesAnnotated = round.annotations.provides == null ? Set.of() :
                round.env.getElementsAnnotatedWith(round.annotations.provides);
        serviceAnnotated.forEach(e -> {
            if (!providedAnnotated.contains(e)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@Service also requires @Provided", e);
            }
            if (providesAnnotated.contains(e)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@Service cannot be used with @Provides", e);
            }
        });
        providedAnnotated.forEach(e -> {
            if (e.getModifiers().contains(FINAL) || e.getModifiers().contains(SEALED)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Final/sealed type marked with @Provided", e);
            }
        });
        Stream.concat(providedAnnotated.stream(), providesAnnotated.stream()).forEach(e -> {
            if (e.getKind() != ElementKind.INTERFACE && e.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Non-class/interface marked with @Provided/@Provides", e);
            }
        });

        // Collect API info from roots.
        for (Element element : round.env.getRootElements()) {
            JavaFileObject sourceFile = processingEnv.getElementUtils().getFileObjectOf(element);
            if (sourceFile != null && sourceFile.getKind() == JavaFileObject.Kind.SOURCE) {
                String name = element instanceof QualifiedNameable ?
                        ((QualifiedNameable) element).getQualifiedName().toString() : element.toString();
                // API hash should not depend on traversal order, so use XOR.
                compilationUnitHashMap.merge(sourceFile, name.hashCode(), (a, b) -> a ^ b);
            }

            element.accept(scanner, api);
        }

        if (!round.env.processingOver()) return null;

        // Check that non-annotated inheritable API types are actually used.
        nonAnnotatedUnusedApiTypes.removeAll(encounteredSupertypes);
        for (Element e : nonAnnotatedUnusedApiTypes) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "API types must ether be final, or annotated with @Service/@Provided/@Provides", e);
        }

        // Calculate hash from all source files on the last round.
        try {
            for (Map.Entry<JavaFileObject, Integer> e : compilationUnitHashMap.entrySet()) {
                // Calculate hash the same way String does it, but converting all line breaks to LF.
                int hash = 0;
                CharSequence content = e.getKey().getCharContent(true);
                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    if (c == '\r') {
                        c = '\n';
                        if (i < content.length() - 1 && content.charAt(i + 1) == '\n') i++;
                    }
                    hash = 31 * hash + c;
                }
                hash = 31 * e.getValue() + hash;
                api.hash ^= hash; // Ignore order of source files.
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return api;
    }

    private final ElementScanner8<Void, Api> scanner = new ElementScanner8<>() {
        @Override
        public Void visitUnknown(Element e, Api api) { return null; }

        @Override
        public Void visitType(TypeElement e, Api api) {
            if (api == null || isNonApi(e)) {
                collectAllSupertypes(e.asType(), null);
                return super.visitType(e, null);
            }
            Api.Type type = new Api.Type(api, processingEnv.getElementUtils().getBinaryName(e).toString());
            type.modifiers.addAll(getModifiers(e));
            type.kind = e.getKind();
            collectAllSupertypes(e.asType(), type.supertypes);
            type.typeParameters = getTypeParameters(e);
            type.deprecation = getDeprecationStatus(e);
            type.usage = getUsage(e);
            if (type.usage == Api.Usage.NONE &&
                !type.modifiers.contains(FINAL) &&
                (e.getKind() == ElementKind.CLASS || e.getKind() == ElementKind.INTERFACE)) {
                nonAnnotatedUnusedApiTypes.add(e);
            }
            super.visitType(e, type);
            api.types.put(type, type);
            return null;
        }

        @Override
        public Void visitVariable(VariableElement e, Api api) {
            if (api == null || isNonApi(e)) return null;
            Api.Type parent = (Api.Type) api;
            Api.Field field = new Api.Field(parent, e.getSimpleName().toString());
            field.modifiers.addAll(getModifiers(e));
            field.type = e.asType().toString();
            field.constantValue = (Serializable) e.getConstantValue();
            field.deprecation = getDeprecationStatus(e);
            if (field.modifiers.contains(STATIC) && !field.modifiers.contains(FINAL)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Static API fields must be final", e);
            }
            parent.fields.put(field, field);
            return null;
        }

        @Override
        public Void visitExecutable(ExecutableElement e, Api api) {
            if (api == null || isNonApi(e)) return null;
            Api.Type parent = (Api.Type) api;
            Api.Method method = new Api.Method(parent, e.getSimpleName().toString(),
                    e.getParameters().stream()
                            .map(VariableElement::asType)
                            .map(TypeMirror::toString)
                            .toArray(String[]::new));
            method.modifiers.addAll(getModifiers(e));
            method.returnType = e.getReturnType().toString();
            method.thrownTypes = collectTypesToSet(e.getThrownTypes());
            method.typeParameters = getTypeParameters(e);
            method.deprecation = getDeprecationStatus(e);
            method.extension = round.getExtensionName(e);
            if (method.extension != null && (!parent.usage.inheritableByBackend ||
                    method.modifiers.contains(STATIC) || method.modifiers.contains(FINAL))) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Only methods intended to be inherited by the backend can be marked with @Extension", e);
            }
            parent.methods.put(method, method);
            return null;
        }
    };

    private static boolean isNonApi(Element e) {
        Set<Modifier> mods = e.getModifiers();
        return !mods.contains(PUBLIC) && !mods.contains(PROTECTED);
    }

    private static Api.TypeParameter[] getTypeParameters(Parameterizable e) {
        return e.getTypeParameters().stream()
                .map(t -> new Api.TypeParameter(t.getSimpleName().toString(), collectTypesToSet(t.getBounds())))
                .toArray(Api.TypeParameter[]::new);
    }

    private static HashSet<String> collectTypesToSet(Collection<? extends TypeMirror> types) {
        return types.stream().map(TypeMirror::toString).collect(Collectors.toCollection(HashSet::new));
    }

    private void collectAllSupertypes(TypeMirror t, Set<String> result) {
        for (TypeMirror s : processingEnv.getTypeUtils().directSupertypes(t)) {
            if (s.getKind() == TypeKind.DECLARED) {
                encounteredSupertypes.add(((DeclaredType) s).asElement());
            }
            if (result != null) {
                String name = s.toString();
                if (!name.equals("java.lang.Object") && result.add(name)) {
                    collectAllSupertypes(s, result);
                }
            }
        }
    }

    private static final Set<Modifier> ALLOWED_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED, ABSTRACT, DEFAULT, STATIC, FINAL);
    private Set<Modifier> getModifiers(Element e) {
        Set<Modifier> set = e.getModifiers();
        if (!ALLOWED_MODIFIERS.containsAll(set)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Only some modifiers are allowed in public API: " + ALLOWED_MODIFIERS, e);
        }
        return set;
    }

    private Api.Deprecation getDeprecationStatus(Element e) {
        Deprecated a = e.getAnnotation(Deprecated.class);
        return a == null ? Api.Deprecation.NONE : a.forRemoval() ? Api.Deprecation.FOR_REMOVAL : Api.Deprecation.DEPRECATED;
    }

    private Api.Usage getUsage(TypeElement e) {
        AnnotationMirror service = null, provided = null, provides = null;
        for (AnnotationMirror am : e.getAnnotationMirrors()) {
            DeclaredType t = am.getAnnotationType();
            if (round.annotations.service != null && t.equals(round.annotations.service.asType())) {
                service = am;
            } else if (round.annotations.provided != null && t.equals(round.annotations.provided.asType())) {
                provided = am;
            } else if (round.annotations.provides != null && t.equals(round.annotations.provides.asType())) {
                provides = am;
            }
        }
        if (service != null) {
            return Api.Usage.SERVICE;
        } else if (provided != null) {
            if (provides != null) return Api.Usage.TWO_WAY;
            else return Api.Usage.PROVIDED;
        }
        if (provides != null) return Api.Usage.PROVIDES;
        return Api.Usage.NONE;
    }
}
