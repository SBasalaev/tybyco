/*
 * The MIT License
 *
 * Copyright 2024 Sergey Basalaev
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

import static me.sbasalaev.API.list;
import me.sbasalaev.Require;

/**
 * Static methods and constants to build descriptors.
 * When statically imported this class provides a cleaner way to make
 * types and method descriptors. For instance, the descriptor for the
 * {@code Object.equals} method is
 * {@snippet :
 * METHOD(BOOLEAN, TYPE(Object.class))
 * }
 * and the descriptor for {@code List<? extends Number>} type is
 * {@snippet :
 * TYPE(List.class, EXTENDS(TYPE(Number.class)))
 * }
 *
 * @author Sergey Basalaev
 */
public final class Jvm {

    private Jvm() { }

    /** Primitive {@code void} pseudotype. */
    public static final JvmVoid VOID = JvmVoid.INSTANCE;

    /** Primitive {@code boolean} type. */
    public static final JvmPrimitiveType BOOLEAN = JvmPrimitiveType.BOOLEAN;

    /** Primitive {@code byte} type. */
    public static final JvmPrimitiveType BYTE = JvmPrimitiveType.BYTE;

    /** Primitive {@code char} type. */
    public static final JvmPrimitiveType CHAR = JvmPrimitiveType.CHAR;

    /** Primitive {@code short} type. */
    public static final JvmPrimitiveType SHORT = JvmPrimitiveType.SHORT;

    /** Primitive {@code int} type. */
    public static final JvmPrimitiveType INT = JvmPrimitiveType.INT;

    /** Primitive {@code long} type. */
    public static final JvmPrimitiveType LONG = JvmPrimitiveType.LONG;

    /** Primitive {@code float} type. */
    public static final JvmPrimitiveType FLOAT = JvmPrimitiveType.FLOAT;

    /** Primitive {@code double} type. */
    public static final JvmPrimitiveType DOUBLE = JvmPrimitiveType.DOUBLE;

    /** Unbounded Java wildcard {@literal <?>}. */
    public static final JvmUnboundedWildcard WILD = JvmUnboundedWildcard.INSTANCE;

    /**
     * Type variable with given upper bound.
     * 
     * @param name  name of the type variable.
     * @param bound non-generic upper bound of the type variable.
     */
    public static JvmTypeVariable TYPEVAR(String name, JvmClass bound) {
        return new JvmTypeVariable(name, bound);
    }

    /**
     * Type variable with {@code java.lang.Object} as upper bound.
     * 
     * @param name the name of the type variable.
     */
    public static JvmTypeVariable TYPEVAR(String name) {
        return new JvmTypeVariable(name);
    }

    /**
     * Array type with given component.
     * 
     * @param componentType the type of the component of the array.
     */
    public static JvmArrayType ARRAY(JvmType componentType) {
        return new JvmArrayType(componentType);
    }

    /**
     * Array type with given component and dimensions.
     *
     * @param componentType the type of the component of the array.
     * @param dimensions array dimensions, must be positive.
     */
    public static JvmArrayType ARRAY(JvmType componentType, int dimensions) {
        Require.positive(dimensions, "dimensions");
        var arrayType = ARRAY(componentType);
        for (int i = 1; i < dimensions; i++) {
            arrayType = ARRAY(arrayType);
        }
        return arrayType;
    }

    /**
     * Class type with given class and type arguments.
     *
     * @param className the generic class being parameterized.
     * @param typeArguments type arguments applied to the generic class.
     */
    public static JvmClassType TYPE(JvmClass className, JvmTypeArgument... typeArguments) {
        return new JvmClassType(className, typeArguments);
    }

    /**
     * Class type with given class and no type arguments.
     *
     * @param className the generic class being parameterized.
     */
    public static JvmClassType TYPE(JvmClass className) {
        return new JvmClassType(className);
    }

    /**
     * Class type with given class and type arguments.
     *
     * @param runtimeClass the {@code Class} instance representing class,
     *     not array or primitive type.
     * @param typeArguments type arguments applied to the generic class.
     */
    public static JvmClassType TYPE(Class<?> runtimeClass, JvmTypeArgument... typeArguments) {
        JvmClass clazz = JvmClass.of(runtimeClass);
        return new JvmClassType(clazz, typeArguments);
    }

    /**
     * Type of given runtime class, array or primitive type.
     *
     * @param runtimeClass the {@code Class} instance representing class,
     *     array or primitive type.
     */
    public static JvmType TYPE(Class<?> runtimeClass) {
        JvmTypeOrVoid typeOrVoid = JvmType.ofClass(runtimeClass);
        return switch (typeOrVoid) {
            case JvmType type -> type;
            case JvmVoid v -> throw new IllegalArgumentException("void.class");
        };
    }

    /**
     * Extends wildcard with given upper bound.
     *
     * @param bound the upper bound of the wildcard.
     */
    public static JvmExtendsWildcard EXTENDS(JvmReferenceType bound) {
        return new JvmExtendsWildcard(bound);
    }

    /**
     * Super wildcard with given lower bound.
     *
     * @param bound the lower bound of the wildcard.
     */
    public static JvmSuperWildcard SUPER(JvmReferenceType bound) {
        return new JvmSuperWildcard(bound);
    }

    /**
     * Class with given qualified name.
     *
     * @param qualifiedName qualified name of the class, using dots as delimiters.
     */
    public static JvmTopLevelClass CLASS(String qualifiedName) {
        return new JvmTopLevelClass(ClassKind.CLASS, qualifiedName);
    }

    /**
     * Interface with given qualified name.
     *
     * @param qualifiedName qualified name of the class, using dots as delimiters.
     */
    public static JvmTopLevelClass INTERFACE(String qualifiedName) {
        return new JvmTopLevelClass(ClassKind.INTERFACE, qualifiedName);
    }

    /**
     * Annotation class with given qualified name.
     *
     * @param qualifiedName qualified name of the class, using dots as delimiters.
     */
    public static JvmTopLevelClass ANNOTATION(String qualifiedName) {
        return new JvmTopLevelClass(ClassKind.ANNOTATION, qualifiedName);
    }

    /**
     * Enum class with given qualified name.
     *
     * @param qualifiedName qualified name of the class, using dots as delimiters.
     */
    public static JvmTopLevelClass ENUM(String qualifiedName) {
        return new JvmTopLevelClass(ClassKind.ENUM, qualifiedName);
    }

    /**
     * Method descriptor with given return type and argument types.
     * 
     * @param returnType the return type of the method, or {@link #VOID}.
     * @param argumentTypes the types of the arguments of the method.
     */
    public static JvmMethodDescriptor METHOD(JvmTypeOrVoid returnType, JvmType... argumentTypes) {
        return new JvmMethodDescriptor(returnType, list(argumentTypes));
    }
}
