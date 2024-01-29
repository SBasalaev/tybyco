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

import me.sbasalaev.tybyco.builders.FieldBuilder;
import me.sbasalaev.tybyco.descriptors.JvmAnnotation;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypeReference;

/**
 *
 * @author Sergey Basalaev
 */
final class FieldBuilderImpl<Result> implements FieldBuilder<Result> {

    private final ClassBuilderImpl<?> classBuilder;
    private final FieldVisitor fv;

    public FieldBuilderImpl(ClassBuilderImpl<?> outerBuilder, FieldVisitor fv) {
        this.classBuilder = outerBuilder;
        this.fv = fv;
    }

    @Override
    public FieldBuilder<Result> annotation(JvmAnnotation anno) {
        classBuilder.learnClasses(anno);
        Annotations.visitAnnotation(fv::visitAnnotation, anno);
        return this;
    }

    @Override
    public FieldBuilder<Result> typeAnnotation(JvmAnnotation anno) {
        classBuilder.learnClasses(anno);
        TypeReference typeRef = TypeReference.newTypeReference(TypeReference.FIELD);
        Annotations.visitTypeAnnotation(fv::visitTypeAnnotation, typeRef, anno);
        return this;
    }

    @Override
    public Result end() {
        fv.visitEnd();
        return (Result) classBuilder;
    }
}
