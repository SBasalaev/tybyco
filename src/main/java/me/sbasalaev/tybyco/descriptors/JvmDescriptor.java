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

/**
 * Descriptor of a field or a method.
 *
 * @author Sergey Basalaev
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.3">JVMS 4.3 Descriptors</a>
 */
public abstract sealed class JvmDescriptor
    permits JvmTypeOrVoid, JvmMethodDescriptor {

    /** Non-public constructor for subclasses. */
    JvmDescriptor() { }

    /**
     * Whether the signature for the descriptor has generic elements.
     * This method returns {@code true} if the signature contains
     * type arguments or type variables.
     */
    public abstract boolean isGeneric();

    /** Whether this descripttor has no generic elements. */
    public final boolean notGeneric() {
        return !isGeneric();
    }

    /**
     * String representation of this descriptor as would appear in Java source.
     * Note, that the returned string is NOT in the binary format suitable to
     * be written in classfile. For that you need to use either of
     * {@code nonGenericString()} or {@code genericString()}.
     */
    @Override
    public abstract String toString();
}
