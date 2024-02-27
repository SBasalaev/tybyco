/*
 * The MIT License
 *
 * Copyright 2023-2024 Sergey Basalaev
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

import static me.sbasalaev.API.append;
import me.sbasalaev.collection.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Java super wildcard bound.
 *
 * @author Sergey Basalaev
 */
public final class JvmSuperWildcard extends JvmWildcard {

    private final JvmReferenceType bound;

    /** Constructs super wildcard without annotations. */
    public JvmSuperWildcard(JvmReferenceType bound) {
        super(List.empty());
        this.bound = bound;
    }

    /** Constructs super wildcard with given annotations. */
    public JvmSuperWildcard(JvmReferenceType bound, List<JvmAnnotation> annotations) {
        super(annotations);
        this.bound = bound;
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty() || bound.isDeeplyAnnotated();
    }

    @Override
    public JvmSuperWildcard annotated(JvmAnnotation anno) {
        return new JvmSuperWildcard(bound, append(annotations(), anno));
    }

    @Override
    public JvmSuperWildcard unannotated() {
        if (notDeeplyAnnotated()) return this;
        return new JvmSuperWildcard(bound.unannotated());
    }

    @Override
    public JvmReferenceType toType() {
        return bound;
    }

    @Override
    public String genericString() {
        return "-" + bound.genericString();
    }

    @Override
    public String toString() {
        return "? super " + bound;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmSuperWildcard wild
            && this.bound.equals(wild.bound);
    }

    @Override
    public int hashCode() {
        return 17 ^ bound.hashCode();
    }
}
