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
import me.sbasalaev.collection.MutableSet;
import me.sbasalaev.tybyco.builders.ClassfileBuilder;
import me.sbasalaev.tybyco.descriptors.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ClassWriter;

/**
 * @author Sergey Basalaev
 */
abstract class ClassfileBuilderImpl<Self extends ClassfileBuilder<Self>>
    implements ClassfileBuilder<Self> {

    final Options options;

    @Nullable String source = null;
    @Nullable String debug = null;
    final JvmClass className;

    private final MutableSet<JvmNestedClass> knownNestedClasses = MutableSet.empty();

    final ClassWriter cw;

    ClassfileBuilderImpl(Options options, JvmClass className) {
        this.options = options;
        this.className = className;
        this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    }

    Self self() {
        return (Self) this;
    }

    @Override
    public Self source(String source) {
        this.source = source;
        return self();
    }

    @Override
    public Self debugExtension(String debugString) {
        this.debug = debugString;
        return self();
    }

    @Override
    public Self annotation(JvmAnnotation anno) {
        learnClasses(anno);
        Annotations.visitAnnotation(cw::visitAnnotation, anno);
        return self();
    }

    /** Registers nested class to be written into {@code InnerClasses} attribute. */
    public void learnClass(JvmClassOrArray className) {
        switch (className) {
            case JvmNestedClass nested -> {
                learnClass(nested.enclosingClass());
                knownNestedClasses.add(nested);
            }
            case JvmReferenceArray array -> learnClass(array.componentClass());
            default -> { }
        }
    }

    /** Registers nested classes appearing in the descriptor to be written into {@code InnerClasses} attribute. */
    public void learnClasses(JvmMethodDescriptor descriptor) {
        for (var type : descriptor.argumentTypes()) {
            learnClasses(type);
        }
        learnClasses(descriptor.returnType());
    }

    /** Registers nested classes appearing in the descriptor to be written into {@code InnerClasses} attribute. */
    public void learnClasses(JvmAnnotated annotated) {
        for (var anno : annotated.annotations()) {
            learnClasses(anno);
        }
        switch (annotated) {
            case JvmTypeOrVoid type -> learnClassesForType(type);
            case JvmWildcard wild -> learnClassesForType(wild.toType());
        }
    }

    /** Registers nested classes appearing in the annotation to be written into {@code InnerClasses} attribute. */
    public void learnClasses(JvmAnnotation anno) {
        learnClass(anno.annotationClass());
        anno.entries().values().forEach(this::learnClassesFromAnnotationValue);
    }

    private void learnClassesForType(JvmTypeOrVoid type) {
        switch (type) {
            case JvmTypeVariable typeVar -> {
                learnClass(typeVar.className());
            }
            case JvmArrayType array -> learnClasses(array.componentType());
            case JvmClassType clazz -> {
                learnClass(clazz.className());
                for (var typeArg : clazz.typeArguments()) {
                    learnClasses(typeArg);
                }
            }
            case JvmPrimitiveType prim -> { }
            case JvmVoid vd -> { }
        }
    }

    private void learnClassesFromAnnotationValue(Object value) {
        switch (value) {
            case JvmAnnotation anno -> learnClasses(anno);
            case JvmMethodDescriptor desc -> learnClasses(desc);
            case EnumValue enumValue -> learnClass(enumValue.enumClass());
            case List<?> list -> list.forEach(this::learnClassesFromAnnotationValue);
            default -> { }
        }
    }

    /** Visits source and inner classes. */
    public void visitCommon() {
        cw.visitSource(source, debug);
        for (var nestedClass : knownNestedClasses) {
            cw.visitInnerClass(
                nestedClass.binaryName(),
                nestedClass.isMember() ? nestedClass.enclosingClass().binaryName() : null,
                nestedClass.isAnonymous() ? null : nestedClass.simpleName(),
                options.flags().forNestedClass(nestedClass)
            );
        }
    }
}
