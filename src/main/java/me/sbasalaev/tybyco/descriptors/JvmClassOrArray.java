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
 * Reference to a class, interface or array.
 *
 * @author Sergey Basalaev
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.2.1">JVMS 4.2.1 Binary Class and Interface Names</a>
 */
public sealed interface JvmClassOrArray
    permits JvmArray, JvmClass {

    /** Binary name of the class, interface or array represented by this reference. */
    String binaryName();

    /**
     * Whether this class or array name is equal to given object.
     * Two class names are equal if their
     * {@link #binaryName() binary representations}
     * are equal.
     */
    @Override
    public boolean equals(Object obj);

    /** Hash code of this class name. */
    @Override
    public int hashCode();

    /**
     * String representation of this reference as would appear in the source.
     * To obtain a binary name suitable to be written in a class file use
     * {@link #binaryName() }.
     */
    @Override String toString();
}
