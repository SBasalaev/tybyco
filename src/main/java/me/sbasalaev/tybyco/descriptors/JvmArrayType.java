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
package me.sbasalaev.tybyco.descriptors;

import me.sbasalaev.collection.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Type of the Java array.
 *
 * @author Sergey Basalaev
 */
public final class JvmArrayType extends JvmClassOrArrayType {

    private final JvmType componentType;

    /**
     * Constructs new array type with given component type.
     * The created type has no annotations.
     */
    public JvmArrayType(JvmType componentType) {
        this(componentType, List.empty());
    }

    /** Constructs new array type with given component type. */
    public JvmArrayType(JvmType componentType, List<JvmAnnotation> annotations) {
        super(annotations);
        this.componentType = componentType;
    }

    @Override
    public JvmArray className() {
        return switch (componentType) {
            case JvmPrimitiveType primitive -> JvmPrimitiveArray.of(primitive);
            case JvmReferenceType reference -> new JvmReferenceArray(reference.className());
        };
    }

    /** Type of the array components. */
    public JvmType componentType() {
        return componentType;
    }

    @Override
    public boolean isGeneric() {
        return componentType.isGeneric();
    }

    @Override
    public String nonGenericString() {
        return "[" + componentType.nonGenericString();
    }

    @Override
    public String genericString() {
        return "[" + componentType.genericString();
    }

    @Override
    public JvmArrayType erasure() {
        if (notGeneric() && notDeeplyAnnotated()) return this;
        return new JvmArrayType(componentType.erasure());
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty() || componentType.isDeeplyAnnotated();
    }

    @Override
    public JvmClassOrArrayType unannotated() {
        if (notDeeplyAnnotated()) return this;
        return new JvmArrayType(componentType.unannotated());
    }

    @Override
    public String toString() {
        return componentType.toString() + "[]";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmArrayType at
            && this.componentType.equals(at.componentType);
    }

    @Override
    public int hashCode() {
        return '[' ^ componentType.hashCode();
    }
}
