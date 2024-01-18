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
 * Unbounded JVM wildcard.
 *
 * @author Sergey Basalaev
 */
public final class JvmUnboundedWildcard extends JvmWildcard {

    /** Instance of the class without annotations. */
    public static final JvmUnboundedWildcard INSTANCE = new JvmUnboundedWildcard(List.empty());

    /** Constructs new unbounded wildcard with given annotations. */
    public JvmUnboundedWildcard(List<JvmAnnotation> annotations) {
        super(annotations);
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty();
    }

    @Override
    public JvmUnboundedWildcard unannotated() {
        return INSTANCE;
    }

    @Override
    public JvmReferenceType toType() {
        return new JvmClassType(JvmClass.JVM_OBJECT);
    }

    @Override
    public String genericString() {
        return "*";
    }

    @Override
    public String toString() {
        return "?";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof JvmUnboundedWildcard;
    }

    @Override
    public int hashCode() {
        return '?';
    }
}
