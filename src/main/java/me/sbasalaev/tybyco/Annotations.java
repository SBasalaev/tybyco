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

import java.util.function.Supplier;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.descriptors.*;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

/**
 * Annotation writers.
 *
 * @author Sergey Basalaev
 */
final class Annotations {

    private Annotations() { }

    @FunctionalInterface
    public interface VisitorGetter {
        AnnotationVisitor get(String descriptor, boolean visible);
    }

    @FunctionalInterface
    public interface TypeVisitorGetter {
        AnnotationVisitor get(int typeRef, TypePath typePath, String descriptor, boolean visible);
    }

    /** Visits annotations on a visitor supplied by given getter. */
    public static void visitAnnotations(VisitorGetter getter, Traversable<JvmAnnotation> annotations) {
        for (var anno : annotations) {
            visitAnnotation(getter, anno);
        }
    }

    /** Visits annotation on a visitor supplied by given getter. */
    public static void visitAnnotation(VisitorGetter getter, JvmAnnotation anno) {
        var av = getter.get('L' + anno.annotationClass().binaryName() + ';', anno.isRuntimeVisible());
        for (var entry : anno.entries().entries()) {
            visitAnnotationValue(av, entry.key(), entry.value());
        }
        av.visitEnd();
    }

    /** Visits type annotations on a visitor supplied by given getter. */
    public static void visitTypeAnnotation(TypeVisitorGetter getter, TypeReference typeRef, JvmAnnotation anno) {
        visitAnnotation((descriptor, visible) -> getter.get(typeRef.getValue(), null, descriptor, visible), anno);
    }

    /** Visits type annotations on a visitor supplied by given getter. */
    public static void visitTypeAnnotations(TypeVisitorGetter getter, TypeReference typeRef, JvmAnnotated type) {
        visitTypeAnnotationsForPath(getter, typeRef, "", type);
    }

    private static void visitTypeAnnotationsForPath(TypeVisitorGetter getter, TypeReference typeRef, String typePath, JvmAnnotated type) {
        // For nested class types the type annotation logic is reversed.
        // The annotation without a path is the annotation on an outer class,
        // and to annotate the inner class (the type itself!) we need an
        // additional type path element.
        String actualTypePath = typePath;
        if (type instanceof JvmClassType classType) {
            var ctype = classType;
            while (ctype.enclosing().nonEmpty()) {
                actualTypePath += ".";
                ctype = ctype.enclosing().first();
            }
        }
        for (var anno : type.annotations()) {
            String tp = actualTypePath;
            visitAnnotation((descriptor, visible) -> getter.get(typeRef.getValue(),
                TypePath.fromString(tp), descriptor, visible), anno);
        }
        switch (type) {
            case JvmArrayType arrayType -> {
                String tp = actualTypePath + '[';
                visitTypeAnnotationsForPath(getter, typeRef, tp, arrayType.componentType());
            }
            case JvmClassType classType -> {
                for (var argEntry : classType.typeArguments().indexed()) {
                    String tp = actualTypePath + argEntry.index() + ';';
                    visitTypeAnnotationsForPath(getter, typeRef, tp, (JvmAnnotated) argEntry.element());
                }
                for (var enclosing : classType.enclosing()) {
                    visitTypeAnnotationsForPath(getter, typeRef, typePath, enclosing);
                }
            }
            case JvmWildcard wildcard -> {
                visitTypeAnnotationsForPath(getter, typeRef, typePath + '*', wildcard.toType());
            }
            default -> { }
        }
    }

    /** Visits annotation value on an annotation visitor supplied by given getter. */
    public static void visitAnnotationDefault(Supplier<AnnotationVisitor> getter, Object value) {
        var av = getter.get();
        visitAnnotationValue(av, "", value);
        av.visitEnd();
    }

    /** Writes key-value pair into the annotation visitor. */
    private static void visitAnnotationValue(AnnotationVisitor av, String name, Object value) {
        switch (value) {
            case JvmTypeOrVoid type -> {
                av.visit(name, Type.getType(type.nonGenericString()));
            }
            case EnumValue enumValue -> {
                av.visitEnum(name, 'L' + enumValue.enumClass().binaryName() + ';', enumValue.enumConst());
            }
            case JvmAnnotation anno -> {
                visitAnnotation((descriptor, visible) -> av.visitAnnotation(name, descriptor), anno);
            }
            case List<?> list -> {
                var arrayAv = av.visitArray(name);
                for (var item : list) {
                    visitAnnotationValue(arrayAv, "", item);
                }
                arrayAv.visitEnd();
            }
            default -> av.visit(name, value);
        }
    }
}
