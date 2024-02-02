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

import java.util.Objects;
import java.util.function.Function;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.MutableList;
import me.sbasalaev.tybyco.builders.CodeBlockBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a bootstrap method.
 * Bootstrap methods are used to initialize dynamic call sites and dynamically
 * computed constants.
 *
 * @author Sergey Basalaev
 */
public final class JvmBootstrapMethod {

    private final JvmMethodHandle handle;
    private final List<Object> arguments;

    /**
     * Creates new bootstrap method with given handle and arguments.
     * 
     * @param handle handle to a method called for the bootstrap.
     * @param arguments static arguments passed to the method. Each argument
     *     must be non-null and one of the types accepted by
     *     {@link CodeBlockBuilder#pushConst(java.lang.Object) }.
     */
    public JvmBootstrapMethod(JvmMethodHandle handle, List<Object> arguments) {
        this.handle = handle;
        this.arguments = arguments.clone();
    }

    /** Builds arguments for a bootstrap method. */
    public static Builder<JvmBootstrapMethod> build(JvmMethodHandle handle) {
        return new Builder<>(list -> new JvmBootstrapMethod(handle, list));
    }

    /** Handle to a method called for the bootstrap. */
    public JvmMethodHandle handle() {
        return this.handle;
    }

    /** Static arguments passed to the bootstrap method. */
    public List<Object> arguments() {
        return this.arguments;
    }

    /** Whether given object equals this bootstrap method. */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmBootstrapMethod m
            && this.handle.equals(m.handle)
            && this.arguments.equals(m.arguments);
    }

    /** The hash code for this bootstrap method. */
    @Override
    public int hashCode() {
        return Objects.hash(handle, arguments);
    }

    /** String representation of this bootstrap method. */
    @Override
    public String toString() {
        return "JvmBootstrapMethod{" + "handle=" + handle + ", arguments=" + arguments + '}';
    }

    /**
     * Typesafe builder of arguments of a bootstrap method.
     * 
     * @param <Result> the type being built.
     */
    public static class Builder<Result> {

        private final Function<List<Object>, Result> build;
        private final MutableList<Object> args = MutableList.empty();

        Builder(Function<List<Object>, Result> resultFunction) {
            this.build = resultFunction;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addBoolean(boolean value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addByte(byte value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addChar(char value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addShort(short value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addInt(int value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addLong(long value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addFloat(float value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addDouble(double value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addString(String value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addClass(JvmClassOrArray value) {
            args.add(value);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addMethodType(JvmMethodDescriptor desc) {
            args.add(desc);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addMethodHandle(JvmMethodHandle handle) {
            args.add(handle);
            return this;
        }

        /** Adds given value to the arguments of a bootstrap method. */
        public Builder<Result> addDynamicConstant(JvmDynamicConstant cnst) {
            args.add(cnst);
            return this;
        }

        /** Starts building arguments for a dynamic constant. */
        public Builder<Builder<Result>> startDynamicConstant(String name, JvmType type, JvmMethodHandle handle) {
            return new Builder<>(list -> {
                args.add(new JvmDynamicConstant(name, type, new JvmBootstrapMethod(handle, list)));
                return this;
            });
        }

        public Result end() {
            return build.apply(args);
        }
    }
}
