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
package me.sbasalaev.tybyco;

import me.sbasalaev.collection.List;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.JvmClassType;
import me.sbasalaev.tybyco.descriptors.JvmReferenceType;

/**
 * Common utilities for generic signatures of classes and methods.
 *
 * @author Sergey Basalaev
 */
final class Signatures {

    /** Appends bounds of a type parameter to the signature. */
    private static void appendBounds(StringBuilder sb, List<JvmReferenceType> bounds) {
        if (bounds.size() > 1 && bounds.first().equals(new JvmClassType(JvmClass.JVM_OBJECT))) {
            sb.append(':');
            bounds = bounds.from(1);
        }
        for (var bound : bounds) {
            sb.append(':').append(bound.genericString());
        }
    }

    /** Appends type parameters to the signature being built. */
    public static void appendTypeParameters(StringBuilder sb,
                                             List<TypeParameterInfo> typeParameters) {
        if (typeParameters.nonEmpty()) {
            sb.append('<');
            for (var typeParam : typeParameters) {
                sb.append(typeParam.name());
                appendBounds(sb, typeParam.bounds());
            }
            sb.append('>');
        }
    }
}
