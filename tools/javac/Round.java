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

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.Set;

/**
 * Annotation processing round context.
 */
public class Round {

    public final RoundEnvironment env;
    public final Annotations annotations = new Annotations();

    public Round(RoundEnvironment env) {
        this.env = env;
    }

    /**
     * Null-safe version of {@link RoundEnvironment#getElementsAnnotatedWith(TypeElement)}.
     */
    public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
        return a == null ? Set.of() : env.getElementsAnnotatedWith(a);
    }

    private static String getAnnotationValueAsString(TypeElement annotationType, ExecutableElement annotationValue, Element element) {
        if (annotationType == null) return null;
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            if (annotation.getAnnotationType().asElement().equals(annotationType)) {
                AnnotationValue value = annotation.getElementValues().get(annotationValue);
                return value.getValue().toString();
            }
        }
        return null;
    }

    public String getExtensionName(Element element) {
        return getAnnotationValueAsString(annotations.extension, annotations.extensionValue, element);
    }

    public String getFallbackName(Element element) {
        return getAnnotationValueAsString(annotations.fallback, annotations.fallbackValue, element);
    }

    public static class Annotations {
        public TypeElement service, provided, provides, fallback, extension;
        public ExecutableElement extensionValue, fallbackValue;
    }
}
