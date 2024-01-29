/*
 * The MIT License
 *
 * Copyright 2023 Sergey Basalaev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.sbasalaev.tybyco;

import me.sbasalaev.collection.Set;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.JvmNestedClass;
import me.sbasalaev.tybyco.descriptors.Mod;
import static org.objectweb.asm.Opcodes.*;

/**
 * Maps typesafe modifiers to JVM flags.
 *
 * @author Sergey Basalaev
 */
final class Flags {

    private Flags() { }

    private static final Set<Mod> ALLOWED_SYNTH = Set.of(
            Mod.MANDATED,
            Mod.SYNTHETIC
    );
    private static final Set<Mod> ALLOWED_REQUIRES = Set.of(
            Mod.MANDATED,
            Mod.STATIC,
            Mod.SYNTHETIC,
            Mod.TRANSITIVE
    );
    private static final Set<Mod> ALLOWED_MODULE = Set.of(
            Mod.DEPRECATED,
            Mod.MANDATED,
            Mod.OPEN,
            Mod.SYNTHETIC
    );
    private static final Set<Mod> ALLOWED_INNER_CLASS = Set.of(
            Mod.ABSTRACT,
            Mod.FINAL, 
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.SYNTHETIC
    );
    private static final Set<Mod> ALLOWED_CLASS = Set.of(
            Mod.ABSTRACT,
            Mod.DEPRECATED,
            Mod.FINAL, 
            Mod.PUBLIC, 
            Mod.SYNTHETIC
    );
    private static final Set<Mod> ALLOWED_FIELD = Set.of(
            Mod.DEPRECATED,
            Mod.FINAL,
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.SYNTHETIC,
            Mod.TRANSIENT,
            Mod.VOLATILE
    );
    private static final Set<Mod> ALLOWED_METHOD = Set.of(
            Mod.ABSTRACT,
            Mod.BRIDGE,
            Mod.FINAL,
            Mod.NATIVE,
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.STRICT,
            Mod.SYNCHRONIZED,
            Mod.SYNTHETIC,
            Mod.VARARGS
    );
    private static final Set<Mod> ALLOWED_CONSTRUCTOR = Set.of(
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STRICT,
            Mod.SYNTHETIC,
            Mod.VARARGS
    );
    private static final Set<Mod> ALLOWED_PARAMETER = Set.of(
            Mod.FINAL,
            Mod.MANDATED,
            Mod.SYNTHETIC
    );

    public static int forNestedClass(JvmNestedClass className) {
        int flags = switch (className.classKind()) {
            case ANNOTATION -> ACC_ANNOTATION | ACC_INTERFACE;
            case ENUM -> ACC_ENUM;
            case INTERFACE -> ACC_INTERFACE;
            default -> 0;
        };
        flags |= forElement(className.modifiers(), "nested class", ALLOWED_INNER_CLASS);
        return flags;
    }

    public static int forClass(JvmClass className, Set<Mod> modifiers) {
        int flags = switch (className.classKind()) {
            case ANNOTATION -> ACC_ANNOTATION | ACC_INTERFACE | ACC_ABSTRACT;
            case CLASS -> ACC_SUPER;
            case ENUM -> ACC_ENUM | ACC_SUPER;
            case INTERFACE -> ACC_INTERFACE | ACC_ABSTRACT;
            case RECORD -> ACC_RECORD | ACC_SUPER;
            case MODULE -> ACC_MODULE;
            case PACKAGE -> ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC;
        };
        flags |= forElement(modifiers, "class", ALLOWED_CLASS);
        return flags;
    }

    public static int forField(Set<Mod> modifiers) {
        return forElement(modifiers, "field", ALLOWED_FIELD);
    }

    public static int forMethod(Set<Mod> modifiers) {
        return forElement(modifiers, "method", ALLOWED_METHOD);
    }

    public static int forConstructor(Set<Mod> modifiers) {
        return forElement(modifiers, "constructor", ALLOWED_CONSTRUCTOR);
    }

    public static int forParameter(Set<Mod> modifiers) {
        return forElement(modifiers, "parameter", ALLOWED_PARAMETER);
    }

    private static int forElement(Set<Mod> modifiers, String name, Set<Mod> allowed) {
        for (var modifier : modifiers.without(allowed)) {
            invalid(modifier, name);
        }
        int flags = 0;
        for (var modifier : modifiers) {
            flags |= switch (modifier) {
                case ABSTRACT  -> ACC_ABSTRACT;
                case DEPRECATED-> ACC_DEPRECATED;
                case FINAL     -> ACC_FINAL;
                case PRIVATE   -> ACC_PRIVATE;
                case PROTECTED -> ACC_PROTECTED;
                case PUBLIC    -> ACC_PUBLIC;
                case STATIC    -> ACC_STATIC;
                case SYNTHETIC -> ACC_SYNTHETIC;
                case TRANSIENT -> ACC_TRANSIENT;
                case VOLATILE  -> ACC_VOLATILE;
                default -> 0;
            };
        }
        return flags;
    }

    public static int forModule(Set<Mod> modifiers) {
        return forModuleElement(modifiers, "module", ALLOWED_MODULE);
    }

    public static int forModuleRequires(Set<Mod> modifiers) {
        return forModuleElement(modifiers, "requires", ALLOWED_REQUIRES);
    }

    public static int forModuleExports(Set<Mod> modifiers) {
        return forModuleElement(modifiers, "exports", ALLOWED_SYNTH);
    }

    public static int forModuleOpens(Set<Mod> modifiers) {
        return forModuleElement(modifiers, "opens", ALLOWED_SYNTH);
    }

    private static int forModuleElement(Set<Mod> modifiers, String name, Set<Mod> allowed) {
        for (var modifier : modifiers.without(allowed)) {
            invalid(modifier, name);
        }
        int flags = 0;
        for (var modifier : modifiers) {
            flags |= switch (modifier) {
                case OPEN       -> ACC_OPEN;
                case MANDATED   -> ACC_MANDATED;
                case STATIC     -> ACC_STATIC_PHASE;
                case SYNTHETIC  -> ACC_SYNTHETIC;
                case TRANSITIVE -> ACC_TRANSITIVE;
                default -> 0;
            };
        }
        return flags;
    }

    private static int invalid(Mod modifier, String elementType) {
        throw new IllegalArgumentException("Modifier " + modifier + " is not allowed for " + elementType);
    }
}
