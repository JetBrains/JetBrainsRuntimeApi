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

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;

/**
 * Compares API computing compatibility status and textual description of API changes.
 * <a href="https://github.com/eclipse-platform/eclipse.platform/blob/master/docs/Evolving-Java-based-APIs-2.md">Useful link.</a>
 */
@SuppressWarnings("UnnecessaryUnicodeEscape")
public class ApiComparator {

    public enum Message {
        BREAKING_CHANGES("\u2757 There are breaking changes which require extra attention.", "\u2757", "!!!"),
        NON_EXTENSION_METHOD_ADDED("\u2755 Non-extension methods added to existing types. " +
                "It is generally advised to add methods to existing types as @Extension methods.", "\u2755", "!!");

        Message(String text, String mark, String simpleMark) {
            this.text = text;
            this.mark = mark;
            this.simpleMark = simpleMark;
        }
        public final String text, mark, simpleMark;
    }

    public record Digest(Compatibility compatibility, String diff, Set<Message> messages) {}

    public static class Node {

        public final String name;
        public final Set<Message> messages = EnumSet.noneOf(Message.class);
        public Diff diff;
        public Compatibility compatibility = Compatibility.SAME;
        public String note;
        public Node next, child;

        private Node(String name, Diff diff) {
            this.name = name;
            this.diff = diff;
        }
        private Node(Object a, Object b) {
            this((b == null ? a : b).toString(), b == null ? Diff.REMOVED : a == null ? Diff.ADDED : Diff.NONE);
        }

        private void check(boolean change, String note) {
            if (change) {
                if (diff == Diff.NONE) diff = Diff.MODIFIED;
                if (note != null) {
                    this.note = this.note == null ? note : this.note + ", " + note;
                }
            }
        }

        /**
         * @return digest information with total compatibility of API changes.
         */
        public Digest digest() {
            StringBuilder diff = new StringBuilder();
            Set<Message> messages = EnumSet.noneOf(Message.class);
            Compatibility compatibility = traverse(diff, messages);
            return new Digest(compatibility, diff.toString(), messages);
        }

        public Compatibility traverse(StringBuilder out, Set<Message> messages) {
            if (name == null) {
                if (child == null) return compatibility;
                return Compatibility.max(compatibility, child.traverse(out, new StringBuilder(), messages));
            } else {
                return traverse(out, new StringBuilder(), messages);
            }
        }

        private Compatibility traverse(StringBuilder out, StringBuilder indent, Set<Message> messages) {
            messages.addAll(this.messages);
            int indentDepth = indent.length();
            Compatibility nextComp = Compatibility.SAME, comp = compatibility;
            if (next != null) nextComp = next.traverse(out, indent, messages);
            if (child != null) {
                indent.append("  ");
                comp = Compatibility.max(comp, child.traverse(out, indent, messages));
                indent.setLength(indentDepth);
            }
            String marks = this.messages.stream().map(m -> m.mark != null ? " " + m.mark : "").collect(Collectors.joining());
            if (comp != Compatibility.SAME || note != null || !marks.isEmpty()) {
                indent.append(diff.ch).append(' ').append(name);
                if (note != null) indent.append(" - ").append(note);
                indent.append(marks);
                out.insert(0, indent.append('\n'));
                indent.setLength(indentDepth);
            }
            return Compatibility.max(comp, nextComp);
        }
    }

    public enum Compatibility {
        SAME(v -> v),
        PATCH(v -> new Api.Version(v.major(), v.minor(), v.patch() + 1)),
        MINOR(v -> new Api.Version(v.major(), v.minor() + 1, 0)),
        MAJOR(v -> new Api.Version(v.major() + 1, 0, 0));

        private final Function<Api.Version, Api.Version> versionIncrement;
        Compatibility(Function<Api.Version, Api.Version> versionIncrement) {
            this.versionIncrement = versionIncrement;
        }

        public Api.Version incrementVersion(Api.Version v) {
            return versionIncrement.apply(v);
        }

        public static Compatibility max(Compatibility a, Compatibility b) {
            return a.ordinal() >= b.ordinal() ? a : b;
        }
    }

    public enum Diff {
        NONE(' '),
        MODIFIED('*'),
        ADDED('+'),
        REMOVED('-');

        public final char ch;

        Diff(char ch) {
            this.ch = ch;
        }
    }

    public static Node compare(Api.Module a, Api.Module b) {
        Node node;
        if (a == null || b == null) {
            node = new Node(null, Diff.MODIFIED);
            node.compatibility = Compatibility.MAJOR;
            node.messages.add(Message.BREAKING_CHANGES);
        } else {
            node = new Node(null, Diff.NONE);
            node.child = compare(a.types, b.types, ApiComparator::compare, node.child);
            if (a.hash != b.hash) node.compatibility = Compatibility.PATCH;
        }
        return node;
    }

    public static Node compare(Api.Type a, Api.Type b) {
        Node node = new Node(a, b);
        if (node.diff == Diff.ADDED) {
            node.compatibility = Compatibility.MINOR;
        } else if (node.diff == Diff.REMOVED) {
            node.compatibility = Compatibility.MAJOR;
            node.messages.add(Message.BREAKING_CHANGES);
        } else {
            node.child = compare(a.types, b.types, ApiComparator::compare, node.child);
            node.child = compare(a.methods, b.methods, ApiComparator::compare, node.child);
            node.child = compare(a.fields, b.fields, ApiComparator::compare, node.child);

            // Breaking changes
            node.check(a.kind != b.kind, "changed kind");
            node.check(!b.supertypes.containsAll(a.supertypes), "contracted supertype set");
            node.check(!Arrays.equals(a.typeParameters, b.typeParameters), "changed type parameters");
            node.check(a.usage.inheritableByBackend && !b.usage.inheritableByBackend, "prohibited inheritance by backend");
            node.check(a.usage.inheritableByClient && !b.usage.inheritableByClient, "prohibited inheritance by client");
            if (node.diff != Diff.NONE) {
                node.compatibility = Compatibility.MAJOR;
                node.messages.add(Message.BREAKING_CHANGES);
                return node;
            }

            if (compareModifiers(node, a.modifiers, b.modifiers)) return node;

            // Compatible changes
            node.check(a.supertypes.size() != b.supertypes.size(), "expanded supertype set");
            node.check(a.deprecation != b.deprecation, "changed deprecation state");
            node.check(!a.usage.inheritableByBackend && b.usage.inheritableByBackend, "allowed inheritance by backend");
            node.check(!a.usage.inheritableByClient && b.usage.inheritableByClient, "allowed inheritance by client");
            if (node.diff != Diff.NONE) {
                node.compatibility = Compatibility.MINOR;
                return node;
            }
        }
        return node;
    }

    public static Node compare(Api.Field a, Api.Field b) {
        Node node = new Node(a, b);
        if (node.diff == Diff.ADDED) {
            node.compatibility = Compatibility.MINOR;
        } else if (node.diff == Diff.REMOVED) {
            node.compatibility = Compatibility.MAJOR;
            node.messages.add(Message.BREAKING_CHANGES);
        } else {

            // Breaking changes
            node.check(!Objects.equals(a.type, b.type), "changed type");
            node.check(!Objects.equals(a.constantValue, b.constantValue), "changed value");
            if (node.diff != Diff.NONE) {
                node.compatibility = Compatibility.MAJOR;
                node.messages.add(Message.BREAKING_CHANGES);
                return node;
            }

            if (compareModifiers(node, a.modifiers, b.modifiers)) return node;

            // Compatible changes
            node.check(a.deprecation != b.deprecation, "changed deprecation state");
            if (node.diff != Diff.NONE) {
                node.compatibility = Compatibility.MINOR;
                return node;
            }
        }
        return node;
    }

    public static Node compare(Api.Method a, Api.Method b) {
        Node node = new Node(a, b);
        if (node.diff == Diff.ADDED) {
            if (b.parent.usage.inheritableByClient && b.modifiers.contains(ABSTRACT)) {
                node.compatibility = Compatibility.MAJOR;
                node.messages.add(Message.BREAKING_CHANGES);
            } else {
                // Adding a non-abstract method to a type inheritable by a client still may
                // break compatibility in some cases, but we consider this risk low enough.
                node.compatibility = Compatibility.MINOR;
                if (b.extension == null && b.parent.usage.inheritableByBackend && b.modifiers.contains(ABSTRACT) &&
                        !b.modifiers.contains(STATIC) && !b.modifiers.contains(FINAL)) {
                    node.messages.add(Message.NON_EXTENSION_METHOD_ADDED);
                }
            }
        } else if (node.diff == Diff.REMOVED) {
            node.compatibility = Compatibility.MAJOR;
            node.messages.add(Message.BREAKING_CHANGES);
        } else {

            // Breaking changes
            node.check(!Objects.equals(a.returnType, b.returnType), "changed return type");
            node.check(!Objects.equals(a.thrownTypes, b.thrownTypes), "changed thrown types");
            node.check(!Arrays.equals(a.typeParameters, b.typeParameters), "changed type parameters");
            if (node.diff != Diff.NONE) {
                node.compatibility = Compatibility.MAJOR;
                node.messages.add(Message.BREAKING_CHANGES);
                return node;
            }

            if (compareModifiers(node, a.modifiers, b.modifiers)) return node;

            // Compatible changes
            node.check(a.deprecation != b.deprecation, "changed deprecation state");
            node.check(!Objects.equals(a.extension, b.extension), "changed extension");
            if (node.diff != Diff.NONE) {
                node.compatibility = Compatibility.MINOR;
                return node;
            }
        }
        return node;
    }

    private static boolean compareModifiers(Node node, Set<Modifier> a, Set<Modifier> b) {
        if (node.diff != Diff.NONE) return true;

        // Breaking changes
        node.check(a.contains(PUBLIC) && !b.contains(PUBLIC), "decreased visibility");
        node.check((!a.contains(ABSTRACT) && b.contains(ABSTRACT)) ||
                (a.contains(DEFAULT) && !b.contains(DEFAULT)), "made abstract");
        node.check(!a.contains(FINAL) && b.contains(FINAL), "made final");
        node.check(a.contains(STATIC) != b.contains(STATIC), "changed static");
        if (node.diff != Diff.NONE) {
            node.compatibility = Compatibility.MAJOR;
            node.messages.add(Message.BREAKING_CHANGES);
            return true;
        }

        // Compatible changes
        node.check(!a.contains(PUBLIC) && b.contains(PUBLIC), "increased visibility");
        node.check((a.contains(ABSTRACT) && !b.contains(ABSTRACT)) ||
                (!a.contains(DEFAULT) && b.contains(DEFAULT)), "made non-abstract");
        node.check(a.contains(FINAL) && !b.contains(FINAL), "made non-final");
        if (node.diff != Diff.NONE) {
            node.compatibility = Compatibility.MINOR;
            return true;
        }
        return false;
    }

    private static <T> Node compare(Map<T, T> a, Map<T, T> b, BiFunction<T, T, Node> comparator, Node next) {
        Node node = next;
        if (a == null) {
            for (T bt : b.values()) {
                Node n = comparator.apply(null, bt);
                n.next = node;
                node = n;
            }
        } else if (b == null) {
            for (T at : a.values()) {
                Node n = comparator.apply(at, null);
                n.next = node;
                node = n;
            }
        } else {
            Map<T, T> aTypes = new HashMap<>(a);
            for (T bt : b.values()) {
                T at = aTypes.remove(bt);
                Node n = comparator.apply(at, bt);
                n.next = node;
                node = n;
            }
            for (T at : aTypes.values()) {
                Node n = comparator.apply(at, null);
                n.next = node;
                node = n;
            }
        }
        return node;
    }
}
