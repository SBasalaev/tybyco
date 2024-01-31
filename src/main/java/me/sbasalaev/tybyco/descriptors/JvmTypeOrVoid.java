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
 * Descriptor and signature of a type or of a {@code void} pseudotype.
 *
 * @author Sergey Basalaev
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.3.2">JVMS 4.3.2 Field Descriptors </a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.7.9.1">JVMS 4.7.9.1 Signatures</a>
 */
public sealed abstract class JvmTypeOrVoid
    extends JvmDescriptor
    implements JvmAnnotated
    permits JvmType, JvmVoid {

    /**
     * Returns a type that corresponds to given Java class.
     * Returns
     * <ul>
     * <li>{@link JvmArrayType} for an array class,
     * <li>{@link JvmPrimitiveType} for a primitive class,
     * <li>{@link JvmVoid} for {@code void.class},
     * <li>{@link JvmClassType} otherwise.
     * </ul>
     * @see JvmClass#of(java.lang.Class) 
     */
    public static JvmTypeOrVoid ofClass(Class<?> runtimeClass) {
        if (runtimeClass == void.class) {
            return JvmVoid.INSTANCE;
        } else if (runtimeClass == boolean.class) {
            return JvmPrimitiveType.BOOLEAN;
        } else if (runtimeClass == byte.class) {
            return JvmPrimitiveType.BYTE;
        } else if (runtimeClass == char.class) {
            return JvmPrimitiveType.CHAR;
        } else if (runtimeClass == float.class) {
            return JvmPrimitiveType.FLOAT;
        } else if (runtimeClass == double.class) {
            return JvmPrimitiveType.DOUBLE;
        } else if (runtimeClass == int.class) {
            return JvmPrimitiveType.INT;
        } else if (runtimeClass == long.class) {
            return JvmPrimitiveType.LONG;
        } else if (runtimeClass == short.class) {
            return JvmPrimitiveType.SHORT;
        } else if (runtimeClass.isArray()) {
            return new JvmArrayType((JvmType) ofClass(runtimeClass.getComponentType()));
        } else {
            return new JvmClassType(JvmClass.of(runtimeClass));
        }
    }

    private final List<JvmAnnotation> annotations;

    JvmTypeOrVoid(List<JvmAnnotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public List<JvmAnnotation> annotations() {
        return annotations;
    }

    @Override
    public abstract JvmTypeOrVoid unannotated();

    /**
     * Non-generic erasure of this type.
     * The returned type is also stripped of all annotations.
     */
    public abstract JvmTypeOrVoid erasure();

    /** Non-generic binary descriptor of this type. */
    @Override
    public abstract String nonGenericString();

    /** Generic signature of this type. */
    public abstract String genericString();

    /**
     * Whether given object is equal to this type.
     * Two types are equal if their representation in the bytecode is identical,
     * i.e. both {@link #nonGenericString() } and {@link #genericString() }
     * return the same value. Note, that equal types may have different sets of
     * type annotations applied.
     */
    @Override
    public abstract boolean equals(@Nullable Object obj);

    /** Hash code for this type. */
    @Override
    public abstract int hashCode();
}
