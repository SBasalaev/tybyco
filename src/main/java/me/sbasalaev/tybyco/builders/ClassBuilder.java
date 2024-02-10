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

import static me.sbasalaev.API.list;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.descriptors.JvmClassType;
import me.sbasalaev.tybyco.descriptors.JvmMethodDescriptor;
import me.sbasalaev.tybyco.descriptors.Mod;

/**
 * Builder of a class.
 *
 * @author Sergey Basalaev
 */
public interface ClassBuilder<Self extends ClassBuilder<Self>>
    extends ClassOrInterfaceBuilder<Self> {

    /** Sets extended superclass of this class. */
    Self superClass(JvmClassType classType);

    /** Returns builder for the constructor of this class. */
    MethodBuilder<Self> constructor(JvmMethodDescriptor descriptor, Traversable<Mod> modifiers);

    /** Returns builder for the constructor of this class. */
    default MethodBuilder<Self> constructor(JvmMethodDescriptor descriptor, Mod... modifiers) {
        return constructor(descriptor, list(modifiers));
    }
}
