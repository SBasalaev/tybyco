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
package me.sbasalaev.tybyco.builders;

import static me.sbasalaev.API.list;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.CompiledClass;
import me.sbasalaev.tybyco.descriptors.*;

/**
 * Builder of a class or interface.
 *
 * @author Sergey Basalaev
 */
public interface ClassOrInterfaceBuilder<Self extends ClassOrInterfaceBuilder<Self>>
    extends GenericElementBuilder<Self, CompiledClass>, ClassfileBuilder<Self> {

    /**
     * Adds implemented interface to this class.
     * 
     * @param interfaceType the (possibly annotated) parameterized interface type.
     */
    Self superInterface(JvmClassType interfaceType);

    /** Adds permitted subclass to this class. */
    Self permittedSubclass(JvmClass className);

    /** Writes the nest host of the class. */
    Self nestHost(JvmClass nestHost);

    /** Writes the nest member of the class. */
    Self nestMember(JvmClass nestMember);

    /**
     * Writes the immediately enclosing class of this class.
     * This method should be called only if the class being built is local or
     * anonymous and appears directly in the body of the class, not in a method
     * or constructor.
     */
    Self enclosingClass(JvmClass enclosingClass);

    /**
     * Writes the immediately enclosing method of this class.
     * This method should be called only if the class being built is local or
     * anonymous.
     */
    Self enclosingMethod(JvmClass enclosingClass, String name, JvmMethodDescriptor descriptor);

    /**
     * Writes the immediately enclosing constructor of this class.
     * This method should be called only if the class being built is local or
     * anonymous.
     */
    Self enclosingConstructor(JvmClass enclosingClass, JvmMethodDescriptor descriptor);

    /**
     * Returns a builder for a field of this class.
     *
     * @param name the field name.
     * @param type the (possibly annotated) field type.
     * @param modifiers field modifiers among {@link Mod#DEPRECATED},
     *     {@link Mod#FINAL}, {@link Mod#PRIVATE}, {@link Mod#PROTECTED},
     *     {@link Mod#PUBLIC}, {@link Mod#STATIC}, {@link Mod#SYNTHETIC},
     *     {@link Mod#TRANSIENT} and {@link Mod#VOLATILE}.
     */
    FieldBuilder<Self> field(String name, JvmType type, Traversable<Mod> modifiers);

    /**
     * Returns a builder for a field of this class.
     *
     * @param name the field name.
     * @param type the (possibly annotated) field type.
     * @param modifiers field modifiers among {@link Mod#DEPRECATED},
     *     {@link Mod#FINAL}, {@link Mod#PRIVATE}, {@link Mod#PROTECTED},
     *     {@link Mod#PUBLIC}, {@link Mod#STATIC}, {@link Mod#SYNTHETIC},
     *     {@link Mod#TRANSIENT} and {@link Mod#VOLATILE}.
     */
    default FieldBuilder<Self> field(String name, JvmType type, Mod... modifiers) {
        return field(name, type, list(modifiers));
    }

    /**
     * Returns a builder for a compile-type constant field of this class.
     * The modifiers {@link Mod#STATIC} and {@link Mod#FINAL} are added
     * automatically and may be omitted. The {@code value} must be one of
     * the types {@link Boolean}, {@link Byte}, {@link Short}, {@link Integer},
     * {@link Long}, {@link Float}, {@link Double}, {@link Character} or {@link String}.
     * The type of the field is determined by the value. Type annotations may
     * be put on the field type by
     * {@link FieldBuilder#typeAnnotation(me.sbasalaev.tybyco.descriptors.JvmAnnotation) typeAnnotation()}
     * method.
     *
     * @param name the field name.
     * @param value the value of this constant.
     * @param modifiers field modifiers among {@link Mod#DEPRECATED},
     *     {@link Mod#PRIVATE}, {@link Mod#PROTECTED}, {@link Mod#PUBLIC},
     *     {@link Mod#SYNTHETIC}, {@link Mod#TRANSIENT} and {@link Mod#VOLATILE}.
     */
    FieldBuilder<Self> constant(String name, Object value, Traversable<Mod> modifiers);

    /**
     * Returns a builder for a compile-type constant field of this class.
     * The modifiers {@link Mod#STATIC} and {@link Mod#FINAL} are added
     * automatically and may be omitted. The {@code value} must be one of
     * the types {@link Boolean}, {@link Byte}, {@link Short}, {@link Integer},
     * {@link Long}, {@link Float}, {@link Double}, {@link Character} or {@link String}.
     * The type of the field is determined by the value. Type annotations may
     * be put on the field type by
     * {@link FieldBuilder#typeAnnotation(me.sbasalaev.tybyco.descriptors.JvmAnnotation) typeAnnotation()}
     * method.
     *
     * @param name the field name.
     * @param value the value of this constant.
     * @param modifiers field modifiers among {@link Mod#DEPRECATED},
     *     {@link Mod#PRIVATE}, {@link Mod#PROTECTED}, {@link Mod#PUBLIC},
     *     {@link Mod#SYNTHETIC}, {@link Mod#TRANSIENT} and {@link Mod#VOLATILE}.
     */
    default FieldBuilder<Self> constant(String name, Object value, Mod... modifiers) {
        return constant(name, value, list(modifiers));
    }

    /** Returns builder for the method of this class. */
    MethodBuilder<Self> method(String name, JvmMethodDescriptor descriptor, Traversable<Mod> modifiers);

    /** Returns builder for the method of this class. */
    default MethodBuilder<Self> method(String name, JvmMethodDescriptor descriptor, Mod... modifiers) {
        return method(name, descriptor, list(modifiers));
    }

    /** Returns builder of the code for the static class initializer. */
    CodeBlockBuilder<Self> staticInitializer();
}
