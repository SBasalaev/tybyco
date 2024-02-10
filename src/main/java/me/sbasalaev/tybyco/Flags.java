/*
 * The MIT License
 *
 * Copyright 2023-2024 Sergey Basalaev
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

import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.JvmNestedClass;
import me.sbasalaev.tybyco.descriptors.Mod;
import static org.objectweb.asm.Opcodes.*;

/**
 * Maps modifiers to JVM access flags.
 *
 * @author Sergey Basalaev
 */
class Flags {

    Flags() { }

    /** Flags for {@code InnerClasses} structure. */
    public int forNestedClass(JvmNestedClass className) {
        int flags = switch (className.classKind()) {
            case ANNOTATION -> ACC_ANNOTATION | ACC_INTERFACE;
            case ENUM -> ACC_ENUM;
            case INTERFACE -> ACC_INTERFACE;
            default -> 0;
        };
        flags |= forClassElement(className.modifiers());
        return flags;
    }

    public int forClass(JvmClass className, Traversable<Mod> modifiers) {
        int flags = switch (className.classKind()) {
            case ANNOTATION -> ACC_ANNOTATION | ACC_INTERFACE | ACC_ABSTRACT;
            case CLASS -> ACC_SUPER;
            case ENUM -> ACC_ENUM | ACC_SUPER;
            case INTERFACE -> ACC_INTERFACE | ACC_ABSTRACT;
            case RECORD -> ACC_RECORD | ACC_SUPER;
            case MODULE -> ACC_MODULE;
            case PACKAGE -> ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC;
        };
        return flags | forClassElement(modifiers);
    }

    public int forClassField(Traversable<Mod> modifiers) {
        return forClassElement(modifiers);
    }

    public int forInterfaceField(Traversable<Mod> modifiers) {
        return forClassElement(modifiers);
    }

    public int forClassMethod(Traversable<Mod> modifiers) {
        return forClassElement(modifiers);
    }

    public int forInterfaceMethod(Traversable<Mod> modifiers) {
        return forClassElement(modifiers);
    }

    public int forConstructor(Traversable<Mod> modifiers) {
        return forClassElement(modifiers);
    }

    public int forParameter(Traversable<Mod> modifiers) {
        return forClassElement(modifiers);
    }

    private static int forClassElement(Traversable<Mod> modifiers) {
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


    public int forModule(Traversable<Mod> modifiers) {
        return forModuleElement(modifiers);
    }

    public int forModuleRequires(Traversable<Mod> modifiers) {
        return forModuleElement(modifiers);
    }

    public int forModuleExports(Traversable<Mod> modifiers) {
        return forModuleElement(modifiers);
    }

    public int forModuleOpens(Traversable<Mod> modifiers) {
        return forModuleElement(modifiers);
    }

    private static int forModuleElement(Traversable<Mod> modifiers) {
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
}
