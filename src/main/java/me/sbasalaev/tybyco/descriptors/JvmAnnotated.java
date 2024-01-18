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

/**
 * Annotated type or wildcard.
 *
 * @author Sergey Basalaev
 */
public sealed interface JvmAnnotated permits JvmTypeArgument, JvmTypeOrVoid, JvmWildcard {

    /** Type annotations applied directly to this type. */
    List<JvmAnnotation> annotations();

    /** This type with all the annotations removed. */
    JvmAnnotated unannotated();

    /**
     * Whether this type or its components have any annotations.
     * To check whether there are annotations directly on this type
     * use {@code annotations().nonEmpty()}.
     */
    boolean isDeeplyAnnotated();

    /**
     * Whether this type and all its components have no annotations.
     * To check whether there are annotations directly on this type
     * use {@code annotations().isEmpty()}.
     */
    default boolean notDeeplyAnnotated() {
        return !isDeeplyAnnotated();
    }
}
