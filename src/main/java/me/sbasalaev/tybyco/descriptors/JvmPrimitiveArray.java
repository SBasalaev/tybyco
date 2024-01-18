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
 * Reference to an array of primitive values.
 *
 * @author Sergey Basalaev
 */
public enum JvmPrimitiveArray implements JvmArray {

    /** Reference to {@code boolean[]}. */
    BOOLEAN_ARRAY(JvmPrimitiveType.BOOLEAN),
    /** Reference to {@code byte[]}. */
    BYTE_ARRAY(JvmPrimitiveType.BYTE),
    /** Reference to {@code char[]}. */
    CHAR_ARRAY(JvmPrimitiveType.CHAR),
    /** Reference to {@code short[]}. */
    SHORT_ARRAY(JvmPrimitiveType.SHORT),
    /** Reference to {@code int[]}. */
    INT_ARRAY(JvmPrimitiveType.INT),
    /** Reference to {@code long[]}. */
    LONG_ARRAY(JvmPrimitiveType.LONG),
    /** Reference to {@code float[]}. */
    FLOAT_ARRAY(JvmPrimitiveType.FLOAT),
    /** Reference to {@code double[]}. */
    DOUBLE_ARRAY(JvmPrimitiveType.DOUBLE),

    ;

    /** Returns {@code JvmPrimitiveArray} instance for given primitive type. */
    public static JvmPrimitiveArray of(JvmPrimitiveType type) {
        return switch (type.kind()) {
            case BOOLEAN -> BOOLEAN_ARRAY;
            case BYTE -> BYTE_ARRAY;
            case CHAR -> CHAR_ARRAY;
            case DOUBLE -> DOUBLE_ARRAY;
            case FLOAT -> FLOAT_ARRAY;
            case INT -> INT_ARRAY;
            case LONG -> LONG_ARRAY;
            case SHORT -> SHORT_ARRAY;
            default -> throw new IncompatibleClassChangeError();
        };
    }

    private final JvmPrimitiveType componentType;

    private JvmPrimitiveArray(JvmPrimitiveType componentType) {
        this.componentType = componentType;
    }

    @Override
    public String binaryName() {
        return "[" + componentType.nonGenericString();
    }

    /** Type of the components of this array. */
    public JvmPrimitiveType componentType() {
        return componentType;
    }

    @Override
    public String toString() {
        return componentType.toString() + "[]";
    }
}
