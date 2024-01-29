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
package me.sbasalaev.tybyco.builders;

import me.sbasalaev.tybyco.descriptors.JvmType;

/**
 * Local variable in the code of a method or constructor.
 * A local variable may be anonymous. In this case it is recognised as
 * a synthetic construct and is not written in {@code LocalVariableTable} and
 * {@code LocalVariableTypeTable}.
 *
 * @author Sergey Basalaev
 */
public final class LocalVar {

    private final String name;
    private final JvmType type;

    /**
     * Creates new local variable with given name and type.
     * 
     * @param name a valid Java identifier.
     * @param type a (possibly annotated) type of the variable.
     */
    public LocalVar(String name, JvmType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Creates new anonymous local variable with given type.
     *
     * @param type a (possibly annotated) type of the variable.
     */
    public LocalVar(JvmType type) {
        this.name = "";
        this.type = type;
    }

    /**
     * The name of the variable.
     * Empty string for anonymous variable.
     */
    public String name() {
        return this.name;
    }

    /** The type of the variable. */
    public JvmType type() {
        return this.type;
    }

    /** Whether this variable has no name. */
    public boolean isAnonymous() {
        return name.isEmpty();
    }

    /**
     * String representation of this variable as would appear in the source code.
     */
    @Override
    public String toString() {
        return type + " " + (name.isEmpty() ? "<anonymous>" : name);
    }
}
