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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Serialized version of the module API.
 */
public class Api implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public final HashMap<Type, Type> types = new HashMap<>();

    public static class Module extends Api implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public Version version;
        public int hash;
    }

    public static class Type extends Api implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public final Api parent;
        public final String qualifiedName;

        public final EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        public ElementKind kind;
        public final HashSet<String> supertypes = new HashSet<>(); // Recursive
        public TypeParameter[] typeParameters;
        public Deprecation deprecation;
        public Usage usage;
        public final HashMap<Field, Field> fields = new HashMap<>();
        public final HashMap<Method, Method> methods = new HashMap<>();

        public Type(Api parent, String qualifiedName) {
            this.parent = parent;
            this.qualifiedName = qualifiedName;
        }

        // Type is distinguishable by its name, so use it in equals and hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return qualifiedName.equals(((Type) o).qualifiedName);
        }
        @Override
        public int hashCode() {
            return qualifiedName.hashCode();
        }
        @Override
        public String toString() {
            return qualifiedName;
        }
    }

    public static class Field implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public final Type parent;
        public final String name;

        public final EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        public String type;
        public Serializable constantValue;
        public Deprecation deprecation;

        public Field(Type parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        // Field is distinguishable by its name, so use it in equals and hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return name.equals(((Field) o).name);
        }
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        @Override
        public String toString() {
            return type + " " + name;
        }
    }

    public static class Method implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public final Type parent;
        public final String name;
        public final String[] parameterTypes;

        public final EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        public String returnType;
        public HashSet<String> thrownTypes;
        public TypeParameter[] typeParameters;
        public Deprecation deprecation;
        public String extension;

        public Method(Type parent, String name, String[] parameterTypes) {
            this.parent = parent;
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        // Method is distinguishable by its signature, so use it in equals and hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Method methodApi = (Method) o;
            if (!name.equals(methodApi.name)) return false;
            return Arrays.equals(parameterTypes, methodApi.parameterTypes);
        }
        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }
        @Override
        public String toString() {
            return name + "(" + String.join(", ", parameterTypes) + ")";
        }
    }

    public record TypeParameter(String name, HashSet<String> bounds) implements Serializable {}

    enum Deprecation {
        NONE,
        DEPRECATED,
        FOR_REMOVAL
    }

    enum Usage {
        NONE(false, false), // No annotations. Non-annotated API types must either be final, or be inherited by some other type.
        SERVICE(true, false), // @Service & @Provided
        PROVIDED(true, false), // @Provided
        PROVIDES(false, true), // @Provides
        TWO_WAY(true, true); // @Provided & @Provides

        public final boolean inheritableByBackend, inheritableByClient;

        Usage(boolean inheritableByBackend, boolean inheritableByClient) {
            this.inheritableByBackend = inheritableByBackend;
            this.inheritableByClient = inheritableByClient;
        }
    }

    public record Version(int major, int minor, int patch) implements Serializable {

        public static Version parse(String value) {
            String[] c = value.split("\\.");
            if (c.length != 3) throw new IllegalArgumentException("Invalid version format");
            return new Version(parseComponent(c[0]), parseComponent(c[1]), parseComponent(c[2]));
        }
        private static int parseComponent(String value) {
            try {
                if (value.length() > 0 && value.charAt(0) != '+') return Integer.parseUnsignedInt(value);
            } catch (NumberFormatException ignore) {}
            throw new IllegalArgumentException("Invalid version component: " + value);
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
