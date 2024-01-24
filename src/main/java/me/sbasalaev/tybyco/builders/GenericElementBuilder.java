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

import me.sbasalaev.collection.List;
import me.sbasalaev.tybyco.descriptors.JvmClassType;
import me.sbasalaev.tybyco.descriptors.JvmTypeVariable;

/**
 * Common interface for builders of generic elements of Java language.
 *
 * @param <Self>   type of this builder.
 * @param <Result> type of the result produced by this builder.
 * @author Sergey Basalaev
 */
public interface GenericElementBuilder<Self extends GenericElementBuilder<Self, Result>, Result>
    extends ElementBuilder<Self, Result> {

    /** Adds type parameter with given bound. */
    TypeParameterBuilder<Self> typeParameter(String name, JvmTypeVariable bound);

    /** Adds type parameter with given bounds. */
    TypeParameterBuilder<Self> typeParameter(String name,
                                             JvmClassType classBound,
                                             List<JvmClassType> interfaceBounds);
}
