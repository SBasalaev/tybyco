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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Array of reference types.
 *
 * @author Sergey Basalaev
 */
public final class JvmReferenceArray implements JvmArray {

    private final JvmClassOrArray componentClass;

    /** Constructs {@code JvmReferenceArray} with components of given class. */
    public JvmReferenceArray(JvmClassOrArray componentClass) {
        this.componentClass = componentClass;
    }

    /** Class of the components of this array. */
    public JvmClassOrArray componentClass() {
        return componentClass;
    }

    @Override
    public String binaryName() {
        return switch (componentClass) {
            case JvmClass clazz -> "[L" + clazz.binaryName() + ';';
            case JvmArray array -> "["  + array.binaryName();
        };
    }

    @Override
    public String toString() {
        return componentClass.toString() + "[]";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmReferenceArray array
            && this.componentClass.equals(array.componentClass);
    }

    @Override
    public int hashCode() {
        return '[' + componentClass.hashCode();
    }
}
