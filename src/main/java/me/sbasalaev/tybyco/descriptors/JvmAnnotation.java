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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Map;
import me.sbasalaev.collection.MutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents Java annotation that may be applied to a type or declaration.
 *
 * @author Sergey Basalaev
 */
public final class JvmAnnotation {

    private final JvmClass annoClass;
    private final boolean isVisible;
    private final Map<String, Object> entries;

    private JvmAnnotation(JvmClass annoClass, boolean isVisible, Map<String, Object> entries) {
        this.annoClass = annoClass;
        this.isVisible = isVisible;
        this.entries = entries;
    }

    /**
     * Constructs new annotation with no arguments.
     * 
     * @param annoClass annotation class, must be of {@link ClassKind#ANNOTATION }.
     * @param isVisible whether this annotation will be visible by runtime reflection.
     */
    public static JvmAnnotation of(JvmClass annoClass, boolean isVisible) {
        if (annoClass.classKind() != ClassKind.ANNOTATION) {
            throw new IllegalArgumentException(annoClass + " is not an annotation class");
        }
        return new JvmAnnotation(annoClass, isVisible, Map.empty());
    }

    /**
     * Constructs new annotation with no arguments.
     * The annotation will be runtime visible iff the class is annotated as
     * <pre>@Retention(RetentionPolicy.RUNTIME)</pre>
     */
    public static JvmAnnotation of(Class<?> runtimeClass) {
        return build(runtimeClass).end();
    }

    /** Returns builder to construct composite annotation. */
    public static Builder<JvmAnnotation> build(JvmClass annoClass, boolean isVisible) {
        if (annoClass.classKind() != ClassKind.ANNOTATION) {
            throw new IllegalArgumentException(annoClass + " is not an annotation class");
        }
        return new Builder<>(annoClass, isVisible, Function.identity());
    }

    /**
     * Returns builder to construct composite annotation.
     * The annotation will be runtime visible iff the class is annotated as
     * <pre>@Retention(RetentionPolicy.RUNTIME)</pre>
     */
    public static Builder<JvmAnnotation> build(Class<?> runtimeClass) {
        if (!runtimeClass.isAnnotation()) {
            throw new IllegalArgumentException(runtimeClass + " is not an annotation class");
        }
        var clazz = JvmClass.of(runtimeClass);
        var retentionAnno = runtimeClass.getAnnotation(Retention.class);
        boolean isVisible = retentionAnno != null && retentionAnno.value() == RetentionPolicy.RUNTIME;
        return new Builder<>(clazz, isVisible, Function.identity());
    }

    /** Class of this annotation. */
    public JvmClass annotationClass() {
        return annoClass;
    }

    /** Whether this annotation will be visible by runtime reflection. */
    public boolean isRuntimeVisible() {
        return isVisible;
    }

    /**
     * Key-value pairs of this annotation.
     * Each value is one of the types {@link Byte}, {@link Boolean},
     * {@link Character}, {@link Short}, {@link Integer}, {@link Long},
     * {@link Float}, {@link Double}, {@link String}, {@link JvmClassOrArrayType},
     * {@link JvmPrimitiveType}, {@link JvmVoid}, {@link EnumValue},
     * {@link JvmAnnotation} or a {@link List} of those types.
     */
    public Map<String, Object> entries() {
        return entries;
    }

    /**
     * Whether given object is equal to this annotation.
     * Two annotations are equal if they have the same {@link #annotationClass() }
     * and the same {@link #entries() }.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmAnnotation anno
            && this.annoClass.equals(anno.annoClass)
            && this.entries.equals(anno.entries);
    }

    /** Hash code of this annnotation. */
    @Override
    public int hashCode() {
        return Objects.hash(annoClass, entries);
    }

    /** String representation of this annotation. */
    @Override
    public String toString() {
        if (entries.isEmpty()) return '@' + annoClass.qualifiedName();
        var sb = new StringBuilder()
            .append('@').append(annoClass.qualifiedName())
            .append('(');
        boolean first = true;
        for (var entry : entries.entries()) {
            if (first) first = false;
            else sb.append(", ");
            sb.append(entry.key()).append(" = ").append(entry.value());
        }
        return sb.append(')').toString();
    }

    /** Builder of annotation values. */
    public static final class Builder<T> {

        private final JvmClass className;
        private final boolean isVisible;
        private final Map.Builder<String, Object> values = Map.build();
        private final Function<JvmAnnotation, T> returnFunction;

        private Builder(JvmClass className,
                boolean isVisible, Function<JvmAnnotation, T> returnFunction) {
            this.className = className;
            this.isVisible = isVisible;
            this.returnFunction = returnFunction;
        }

        /** Adds {@code boolean} value to the annotation being built. */
        public Builder<T> addBoolean(String name, boolean value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code byte} value to the annotation being built. */
        public Builder<T> addByte(String name, byte value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code char} value to the annotation being built. */
        public Builder<T> addChar(String name, char value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code short} value to the annotation being built. */
        public Builder<T> addShort(String name, short value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code int} value to the annotation being built. */
        public Builder<T> addInt(String name, int value) {
            values.add(name, value);
            return this;
        }


        /** Adds {@code long} value to the annotation being built. */
        public Builder<T> addLong(String name, long value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code float} value to the annotation being built. */
        public Builder<T> addFloat(String name, float value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code double} value to the annotation being built. */
        public Builder<T> addDouble(String name, double value) {
            values.add(name, value);
            return this;
        }

        /** Adds {@code String} value to the annotation being built. */
        public Builder<T> addString(String name, String value) {
            values.add(name, value);
            return this;
        }

        /** Adds enum constant value to the annotation being built. */
        public Builder<T> addEnum(String name, EnumValue value) {
            values.add(name, value);
            return this;
        }

        /** Adds enum constant value to the annotation being built. */
        public Builder<T> addEnum(String name, JvmClass enumClass, String enumValue) {
            values.add(name, new EnumValue(enumClass, enumValue));
            return this;
        }

        /** Adds enum constant value to the annotation being built. */
        public Builder<T> addEnum(String name, Enum<?> runtimeValue) {
            values.add(name, EnumValue.of(runtimeValue));
            return this;
        }


        /** Adds annotation value to the annotation being built. */
        public Builder<T> addAnnotation(String name, JvmAnnotation annotation) {
            values.add(name, annotation);
            return this;
        }

        /**
         * Adds class value to the annotation being built.
         * The value must be
         * <ul>
         * <li>{@link JvmClassType} if it represents ordinary class,
         * <li>{@link JvmArrayType} if it represents array class,
         * <li>{@link JvmPrimitiveType} if it represents primitive class,
         * <li>{@link JvmVoid} if it represents {@code void.class}.
         * </ul>
         */
        public Builder<T> addClass(String name, JvmTypeOrVoid value) {
            if (!isValidClassValue(value)) {
                throw new IllegalArgumentException("Invalid value for this builder: " + value);
            }
            values.add(name, value);
            return this;
        }

        /** Adds class value to the annotation being built. */
        public Builder<T> addClass(String name, JvmClassOrArray value) {
            values.add(name, classType(value));
            return this;
        }

        private static JvmClassOrArrayType classType(JvmClassOrArray value) {
            return switch (value) {
                case JvmClass clazz -> new JvmClassType(clazz);
                case JvmReferenceArray array -> new JvmArrayType(classType(array.componentClass()));
                case JvmPrimitiveArray array -> new JvmArrayType(array.componentType());
            };
        }

        /** Adds class value to the annotation being built. */
        public Builder<T> addClass(String name, Class<?> value) {
            values.add(name, JvmType.ofClass(value));
            return this;
        }

        /** Adds {@code boolean[]} value to the annotation being built. */
        public Builder<T> addBooleans(String name, boolean... values) {
            var arrayBuilder = startBooleans(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code byte[]} value to the annotation being built. */
        public Builder<T> addBytes(String name, byte... values) {
            var arrayBuilder = startBytes(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code char[]} value to the annotation being built. */
        public Builder<T> addChars(String name, char... values) {
            var arrayBuilder = startChars(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code short[]} value to the annotation being built. */
        public Builder<T> addShorts(String name, short... values) {
            var arrayBuilder = startShorts(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code int[]} value to the annotation being built. */
        public Builder<T> addInts(String name, int... values) {
            var arrayBuilder = startInts(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code long[]} value to the annotation being built. */
        public Builder<T> addLongs(String name, long... values) {
            var arrayBuilder = startLongs(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code float[]} value to the annotation being built. */
        public Builder<T> addFloats(String name, float... values) {
            var arrayBuilder = startFloats(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code double[]} value to the annotation being built. */
        public Builder<T> addDoubles(String name, double... values) {
            var arrayBuilder = startDoubles(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /** Adds {@code String[]} value to the annotation being built. */
        public Builder<T> addStrings(String name, String... values) {
            var arrayBuilder = startStrings(name);
            for (var value : values) arrayBuilder.add(value);
            return arrayBuilder.end();
        }

        /**
         * Starts building an array of {@code boolean} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Boolean> startBooleans(String name) {
            return new ArrayBuilder<>(this, name, Boolean.class::isInstance);
        }

        /**
         * Starts building an array of {@code byte} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Byte> startBytes(String name) {
            return new ArrayBuilder<>(this, name, Byte.class::isInstance);
        }

        /**
         * Starts building an array of {@code char} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Character> startChars(String name) {
            return new ArrayBuilder<>(this, name, Character.class::isInstance);
        }

        /**
         * Starts building an array of {@code short} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Short> startShorts(String name) {
            return new ArrayBuilder<>(this, name, Short.class::isInstance);
        }

        /**
         * Starts building an array of {@code int} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Integer> startInts(String name) {
            return new ArrayBuilder<>(this, name, Integer.class::isInstance);
        }
        /**
         * Starts building an array of {@code long} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Long> startLongs(String name) {
            return new ArrayBuilder<>(this, name, String.class::isInstance);
        }

        /**
         * Starts building an array of {@code float} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Float> startFloats(String name) {
            return new ArrayBuilder<>(this, name, Float.class::isInstance);
        }

        /**
         * Starts building an array of {@code double} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, Double> startDoubles(String name) {
            return new ArrayBuilder<>(this, name, Double.class::isInstance);
        }

        /**
         * Starts building an array of {@code String} values.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, String> startStrings(String name) {
            return new ArrayBuilder<>(this, name, String.class::isInstance);
        }

        /**
         * Starts building an array of enum constants.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, EnumValue> startEnums(String name) {
            return new ArrayBuilder<>(this, name, EnumValue.class::isInstance);
        }

        /**
         * Starts building an array of class values.
         * The values added to the array must be {@link JvmClassOrArrayType},
         * {@link JvmPrimitiveType} or {@link JvmVoid}.
         * The array is not added to this builder until
         * {@link ArrayBuilder#end() } is called on the returned builder.
         */
        public ArrayBuilder<Builder<T>, JvmTypeOrVoid> startClasses(String name) {
            return new ArrayBuilder<>(this, name, Builder::isValidClassValue);
        }

        /**
         * Returns builder to build and add annotation value to this builder.
         * The annotation is not added to this builder until
         * {@link Builder#end() } is called on the returned builder.
         */
        public Builder<Builder<T>> startAnnotation(String name, JvmClass annoClass) {
            return new Builder<>(annoClass, false, (anno) -> {
                values.add(name, anno);
                return this;
            });
        }

        /**
         * Returns builder to build and add annotation value to this builder.
         * The annotation is not added to this builder until
         * {@link Builder#end() } is called on the returned builder.
         */
        public Builder<Builder<T>> startAnnotation(String name, Class<?> runtimeClass) {
            if (!runtimeClass.isAnnotation()) {
                throw new IllegalArgumentException(runtimeClass + " is not an annotation class");
            }
            var clazz = JvmClass.of(runtimeClass);
            return startAnnotation(name, clazz);
        }

        /**
         * Starts building an array of {@code JvmAnnotation} values.
         * The array is not added to this builder until
         * {@link AnnotationArrayBuilder#end() } is called on the returned builder.
         */
        public AnnotationArrayBuilder<Builder<T>> startAnnotations(String name) {
            return new AnnotationArrayBuilder<>(this, name);
        }

        /** For the callback of array builders. */
        void addArray(String name, List<?> list) {
            values.add(name, list);
        }

        /** Builds annotation and returns the original caller. */
        public T end() {
            var anno = new JvmAnnotation(className, isVisible, values.toMap());
            return returnFunction.apply(anno);
        }

        private static boolean isValidClassValue(JvmTypeOrVoid type) {
            return type instanceof JvmPrimitiveType
                || type instanceof JvmVoid
                || type instanceof JvmClassOrArrayType;
        }
    }

    /**
     * Builder of arrays of primitive values.
     * @param <Outer> type of the outer builder returned by {@link #end()}.
     * @param <Type>  type of the array elements.
     */
    public static final class ArrayBuilder<Outer extends Builder<?>, Type extends Object> {

        private final Outer outer;
        private final String name;
        private final MutableList<Type> list = MutableList.empty();
        private final Predicate<? super Type> typeChecker;

        ArrayBuilder(Outer outer, String name, Predicate<? super Type> typeChecker) {
            this.outer = outer;
            this.name = name;
            this.typeChecker = typeChecker;
        }

        /**
         * Adds given value to the value array.
         * @return this biulder.
         */
        public ArrayBuilder<Outer, Type> add(Type value) {
            if (!typeChecker.test(value)) {
                throw new IllegalArgumentException("Invalid value for this builder: " + value);
            }
            list.add(value);
            return this;
        }

        /**
         * Adds all given values to the value array.
         * @return this biulder.
         */
        public ArrayBuilder<Outer, Type> add(Type value, Type... moreValues) {
            list.add(value);
            list.addAll(List.of(moreValues));
            return this;
        }

        /** Adds array of values to the outer builder and returns it. */
        public Outer end() {
            outer.addArray(name, list.clone());
            return outer;
        }
    }

    /**
     * Builder of arrays of annotation values.
     * @param <Outer> type of the outer builder returned by {@link #end()}.
     */
    public static final class AnnotationArrayBuilder<Outer extends Builder<?>> {

        private final Outer outer;
        private final String name;
        private final MutableList<JvmAnnotation> list = MutableList.empty();

        AnnotationArrayBuilder(Outer outer, String name) {
            this.outer = outer;
            this.name = name;
        }

        /**
         * Adds annotation to the array being built.
         * @return this builder.
         */
        public AnnotationArrayBuilder<Outer> add(JvmAnnotation anno) {
            list.add(anno);
            return this;
        }

        /**
         * Starts building annotation value.
         * The annotation will not be added to the array being built
         * until {@link Builder#end() } is called.
         */
        public Builder<AnnotationArrayBuilder<Outer>> start(JvmClass annoClass) {
            return new Builder<>(annoClass, false, (anno) -> {
                list.add(anno);
                return this;
            });
        }

        /**
         * Starts building annotation value.
         * The annotation will not be added to the array being built
         * until {@link Builder#end() } is called.
         */
        public Builder<AnnotationArrayBuilder<Outer>> start(Class<?> runtimeClass) {
            if (!runtimeClass.isAnnotation()) {
                throw new IllegalArgumentException(runtimeClass + " is not an annotation class");
            }
            var clazz = JvmClass.of(runtimeClass);
            return start(clazz);
        }

        /** Adds array of values to the outer builder and returns it. */
        public Outer end() {
            outer.addArray(name, list.clone());
            return outer;
        }
    }
}
