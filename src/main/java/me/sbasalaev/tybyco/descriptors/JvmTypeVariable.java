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
package me.sbasalaev.tybyco.descriptors;

import java.util.Objects;
import static me.sbasalaev.API.append;
import me.sbasalaev.collection.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Java type variable.
 *
 * @author Sergey Basalaev
 */
public final class JvmTypeVariable extends JvmReferenceType {

    private final String name;
    private final JvmClassType bound;

    /** Erased type of this type variable. */
    private static JvmClassType boundType(JvmClass boundClass) {
        if (boundClass instanceof JvmNestedClass nested && nested.isInstanceMember()) {
            return boundType(nested.enclosingClass()).newMemberType(nested);
        }
        return new JvmClassType(boundClass);
    }

    /** Constructs new type variable bounded by given class. */
    public JvmTypeVariable(String name, JvmClass boundClass, List<JvmAnnotation> annotations) {
        super(annotations);
        this.name = name;
        this.bound = boundType(boundClass);
    }

    /** Constructs new type variable bounded by {@code java.lang.Object}. */
    public JvmTypeVariable(String name, List<JvmAnnotation> annotations) {
        this(name, JvmClass.JVM_OBJECT, annotations);
    }

    /** Constructs new type variable bounded by given class. */
    public JvmTypeVariable(String name, JvmClass boundClass) {
        this(name, boundClass, List.empty());
    }

    /** Constructs new type variable bounded by {@code java.lang.Object}. */
    public JvmTypeVariable(String name) {
        this(name, JvmClass.JVM_OBJECT, List.empty());
    }

    /** Name of this type variable. */
    public String name() {
        return name;
    }

    /** Class name of the upper bound to this type variable. */
    @Override
    public JvmClassOrArray className() {
        return bound.className();
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    @Override
    public String genericString() {
        return 'T' + name + ';';
    }

    @Override
    public String nonGenericString() {
        return bound.nonGenericString();
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty();
    }

    @Override
    public JvmTypeVariable annotated(JvmAnnotation anno) {
        return new JvmTypeVariable(name, bound.className(),
            append(annotations(), anno));
    }

    @Override
    public JvmTypeVariable unannotated() {
        if (annotations().isEmpty()) return this;
        return new JvmTypeVariable(name, bound.className());
    }

    @Override
    public JvmReferenceType erasure() {
        return bound;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bound);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmTypeVariable typevar
            && this.name.equals(typevar.name)
            && this.bound.equals(typevar.bound);
    }
}
