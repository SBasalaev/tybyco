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
 * Primitive JVM type.
 *
 * @author Sergey Basalaev
 */
public final class JvmPrimitiveType extends JvmType {

    /** Primitive {@code boolean} type with no type annotations. */
    public static final JvmPrimitiveType BOOLEAN = new JvmPrimitiveType(TypeKind.BOOLEAN);
    /** Primitive {@code byte} type with no type annotations. */
    public static final JvmPrimitiveType BYTE = new JvmPrimitiveType(TypeKind.BYTE);
    /** Primitive {@code char} type with no type annotations. */
    public static final JvmPrimitiveType CHAR = new JvmPrimitiveType(TypeKind.CHAR);
    /** Primitive {@code short} type with no type annotations. */
    public static final JvmPrimitiveType SHORT = new JvmPrimitiveType(TypeKind.SHORT);
    /** Primitive {@code int} type with no type annotations. */
    public static final JvmPrimitiveType INT = new JvmPrimitiveType(TypeKind.INT);
    /** Primitive {@code long} type with no type annotations. */
    public static final JvmPrimitiveType LONG = new JvmPrimitiveType(TypeKind.LONG);
    /** Primitive {@code float} type with no type annotations. */
    public static final JvmPrimitiveType FLOAT = new JvmPrimitiveType(TypeKind.FLOAT);
    /** Primitive {@code double} type with no type annotations. */
    public static final JvmPrimitiveType DOUBLE = new JvmPrimitiveType(TypeKind.DOUBLE);
    
    /** Returns primitive type of given kind without annotations. */
    public static JvmPrimitiveType of(TypeKind kind) {
        return switch (kind) {
            case BOOLEAN -> BOOLEAN;
            case BYTE    -> BYTE;
            case CHAR    -> CHAR;
            case DOUBLE  -> DOUBLE;
            case FLOAT   -> FLOAT;
            case INT     -> INT;
            case LONG    -> LONG;
            case SHORT   -> SHORT;
            case REFERENCE -> throw new IllegalArgumentException("Reference kind");
        };
    }

    private final TypeKind kind;

    /** Constructor for predefined types without annotations. */
    private JvmPrimitiveType(TypeKind kind) {
        super(List.empty());
        this.kind = kind;
    }

    /** Constructor for primitive type with annotations. */
    public JvmPrimitiveType(TypeKind kind, List<JvmAnnotation> annotations) {
        super(annotations);
        if (kind == TypeKind.REFERENCE) {
            throw new IllegalArgumentException("Reference kind");
        }
        this.kind = kind;
    }

    @Override
    public TypeKind kind() {
        return kind;
    }

    /**
     * @return returns false.
     */
    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty();
    }

    @Override
    public String genericString() {
        return nonGenericString();
    }

    @Override
    public String nonGenericString() {
        return switch (kind) {
            case BOOLEAN -> "Z";
            case BYTE    -> "B";
            case CHAR    -> "C";
            case DOUBLE  -> "D";
            case FLOAT   -> "F";
            case INT     -> "I";
            case LONG    -> "J";
            case SHORT   -> "S";
            default -> throw new IncompatibleClassChangeError();
        };
    }

    @Override
    public JvmPrimitiveType erasure() {
        return of(kind);
    }

    @Override
    public JvmPrimitiveType unannotated() {
        return of(kind);
    }

    @Override
    public String toString() {
        return switch (kind) {
            case BOOLEAN -> "boolean";
            case BYTE    -> "byte";
            case CHAR    -> "char";
            case DOUBLE  -> "double";
            case FLOAT   -> "float";
            case INT     -> "int";
            case LONG    -> "long";
            case SHORT   -> "short";
            default -> throw new IncompatibleClassChangeError();
        };
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmPrimitiveType type
            && this.kind.equals(type.kind);
    }

    @Override
    public int hashCode() {
        return kind.hashCode();
    }
}
