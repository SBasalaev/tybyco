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
 * The pseudotype for {@code void}.
 *
 * @author Sergey Basalaev
 */
public final class JvmVoid extends JvmTypeOrVoid {

    /** Instance of the class without annotations. */
    public static final JvmVoid INSTANCE = new JvmVoid(List.empty());

    /** Constructs JvmVoid with given annotations. */
    public JvmVoid(List<JvmAnnotation> annotations) {
        super(annotations);
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty();
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public String genericString() {
        return "V";
    }

    @Override
    public String nonGenericString() {
        return "V";
    }

    @Override
    public JvmVoid unannotated() {
        return INSTANCE;
    }

    @Override
    public JvmVoid erasure() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof JvmVoid;
    }

    @Override
    public int hashCode() {
        return 'V';
    }
}
