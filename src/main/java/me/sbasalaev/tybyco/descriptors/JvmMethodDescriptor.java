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

import java.util.NoSuchElementException;
import java.util.Objects;
import static me.sbasalaev.API.concat;
import static me.sbasalaev.API.list;
import me.sbasalaev.collection.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Descriptor of a Java method or constructor.
 *
 * @author Sergey Basalaev
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.3.3">JVMS 4.3.3 Method Descriptors</a>
 */
public final class JvmMethodDescriptor extends JvmDescriptor {

    private final List<JvmType> argumentTypes;
    private final JvmTypeOrVoid returnType;

    /** Creates new method descriptor with given argument and return types. */
    public JvmMethodDescriptor(List<JvmType> argumentTypes, JvmTypeOrVoid returnType) {
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
    }

    /** Types of the arguments of this method. */
    public List<JvmType> argumentTypes() {
        return argumentTypes;
    }

    /** Return type of this method or {@code JvmVoid} if it is {@code void}. */
    public JvmTypeOrVoid returnType() {
        return returnType;
    }

    /** Returns method descriptor with given argument types added at the beginning. */
    public JvmMethodDescriptor prepend(JvmType... types) {
        if (types.length == 0) return this;
        return new JvmMethodDescriptor(concat(list(types), argumentTypes), returnType);
    }

    /** Returns method descriptor with given argument types added at the end. */
    public JvmMethodDescriptor append(JvmType... types) {
        if (types.length == 0) return this;
        return new JvmMethodDescriptor(concat(argumentTypes, list(types)), returnType);
    }

    /** Returns method descriptor with the first argument removed. */
    public JvmMethodDescriptor dropFirst() {
        if (argumentTypes.isEmpty()) throw new NoSuchElementException("The descriptor has no argument types");
        return new JvmMethodDescriptor(argumentTypes.from(1), returnType);
    }

    /** Returns method descriptor with the return type replaced by a given type. */
    public JvmMethodDescriptor withReturnType(JvmTypeOrVoid type) {
        if (type.equals(this.returnType)) return this;
        return new JvmMethodDescriptor(argumentTypes, type);
    }

    @Override
    public boolean isGeneric() {
        return argumentTypes.exists(JvmType::isGeneric) || returnType.isGeneric();
    }

    /** Binary method descriptor. */
    public String nonGenericString() {
        return "(" + argumentTypes.map(JvmType::nonGenericString).join("") + ")"
            + returnType.nonGenericString();
    }

    /** The descriptor with erased types. */
    public JvmMethodDescriptor erasure() {
        if (!isGeneric()) return this;
        return new JvmMethodDescriptor(argumentTypes.mapped(JvmType::erasure), returnType.erasure());
    }

    /**
     * String representation of this method descriptor.
     */
    @Override
    public String toString() {
        return returnType + " (" + argumentTypes.join(", ") + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(argumentTypes, returnType);
    }

    /**
     * Whether given object is equal to this method descriptor.
     * Two method descriptors are equal if they have the same argument
     * and return types.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmMethodDescriptor other
            && other.argumentTypes.equals(this.argumentTypes)
            && other.returnType.equals(this.returnType);
    }
}
