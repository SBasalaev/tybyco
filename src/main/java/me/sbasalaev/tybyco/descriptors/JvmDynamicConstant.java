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

/**
 * A constant the value of which is calculated at runtime.
 *
 * @author Sergey Basalaev
 */
public class JvmDynamicConstant {

    private final String name;
    private final JvmType type;
    private final JvmBootstrapMethod bootstrap;

    /**
     * Creates new dynamic constant.
     * @param name  the name of the constant.
     * @param type  the type of the constant.
     * @param bootstrap the bootstrap method used to initialize the constant.
     */
    public JvmDynamicConstant(String name, JvmType type, JvmBootstrapMethod bootstrap) {
        this.name = name;
        this.type = type;
        this.bootstrap = bootstrap;
    }

    /** Builds arguments to a bootstrap method of a dynamic constant. */
    public static JvmBootstrapMethod.Builder<JvmDynamicConstant> build(String name, JvmType type, JvmMethodHandle handle) {
        return new JvmBootstrapMethod.Builder<>(list -> new JvmDynamicConstant(name, type, new JvmBootstrapMethod(handle, list)));
    }

    /** The name of the constant. */
    public String name() {
        return this.name;
    }

    /** The type of the constant. */
    public JvmType type() {
        return this.type;
    }

    /** The bootstrap method used to initialize the constant. */
    public JvmBootstrapMethod bootstrap() {
        return this.bootstrap;
    }

    /** Whether given object equals this dynamic constant. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmDynamicConstant c
            && this.name.equals(c.name)
            && this.type.equals(c.type)
            && this.bootstrap.equals(c.bootstrap);
    }

    /** The hash code for the constant. */
    @Override
    public int hashCode() {
        return Objects.hash(name, type, bootstrap);
    }

    /** String representation of this dynamic constant. */
    @Override
    public String toString() {
        return "JvmDynamicConstant{" + "name=" + name + ", type=" + type + ", bootstrap=" + bootstrap + '}';
    }
}
