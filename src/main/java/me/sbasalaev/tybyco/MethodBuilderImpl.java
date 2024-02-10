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

import static me.sbasalaev.API.concat;
import static me.sbasalaev.API.list;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.MutableList;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.builders.CodeBlockBuilder;
import me.sbasalaev.tybyco.builders.MethodBuilder;
import me.sbasalaev.tybyco.builders.ParameterBuilder;
import me.sbasalaev.tybyco.builders.TypeParameterBuilder;
import me.sbasalaev.tybyco.descriptors.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypeReference;

/**
 *
 * @author Sergey Basalaev
 */
final class MethodBuilderImpl<Result> implements MethodBuilder<Result> {

    private final ClassBuilderImpl<?> classBuilder;
    private final JvmMethodDescriptor descriptor;
    private final MutableList<TypeParameterInfo> typeParameters = MutableList.empty();
    private final MutableList<JvmReferenceType> exceptions = MutableList.empty();
    private final boolean isMemberConstructor;
    private final boolean isStatic;
    int parameterCount = 0;

    private final MethodVisitor mv;

    MethodBuilderImpl(ClassBuilderImpl<?> classBuilder, JvmMethodDescriptor descriptor,
            boolean isMemberConstructor, boolean isStatic, MethodVisitor mv) {
        this.classBuilder = classBuilder;
        this.descriptor = descriptor;
        this.isMemberConstructor = isMemberConstructor;
        this.isStatic = isStatic;
        this.mv = mv;
        var typeRef = TypeReference.newTypeReference(TypeReference.METHOD_RETURN);
        Annotations.visitTypeAnnotations(mv::visitTypeAnnotation, typeRef, descriptor.returnType());
        List<JvmType> args = descriptor.argumentTypes().from(isMemberConstructor ? 1 : 0);
        args.forEachIndexed((type, index) -> {
            var paramRef = TypeReference.newFormalParameterReference(index);
            Annotations.visitTypeAnnotations(mv::visitTypeAnnotation, paramRef, type);
        });
    }

    @Override
    public MethodBuilder<Result> annotation(JvmAnnotation anno) {
        classBuilder.learnClasses(anno);
        Annotations.visitAnnotation(mv::visitAnnotation, anno);
        return this;
    }

    @Override
    public MethodBuilder<Result> receiverAnnotation(JvmAnnotation anno) {
        classBuilder.learnClasses(anno);
        TypeReference typeRef = TypeReference.newTypeReference(TypeReference.METHOD_RECEIVER);
        Annotations.visitTypeAnnotation(mv::visitTypeAnnotation, typeRef, anno);
        return this;
    }

    @Override
    public TypeParameterBuilder<MethodBuilder<Result>>
            typeParameter(String name, JvmTypeVariable bound) {
        return addTypeParameter(name, list(bound));
    }

    @Override
    public TypeParameterBuilder<MethodBuilder<Result>>
            typeParameter(String name, JvmClassType classBound, List<JvmClassType> interfaceBounds) {
        return addTypeParameter(name, concat(list(classBound), interfaceBounds));
    }

    private TypeParameterBuilder<MethodBuilder<Result>>
            addTypeParameter(String name, List<JvmReferenceType> bounds) {
        bounds.forEach(classBuilder::learnClasses);
        int index = typeParameters.size();
        typeParameters.add(new TypeParameterInfo(name, bounds));
        bounds.forEachIndexed((bound, boundIndex) -> {
            var boundRef = TypeReference.newTypeParameterBoundReference(
                    TypeReference.METHOD_TYPE_PARAMETER_BOUND, index, boundIndex);
            Annotations.visitTypeAnnotations(mv::visitTypeAnnotation, boundRef, bound);
        });
        return new TypeParameterBuilder<>() {
            @Override
            public TypeParameterBuilder<MethodBuilder<Result>> annotation(JvmAnnotation anno) {
                classBuilder.learnClasses(anno);
                var typeRef = TypeReference.newTypeParameterReference(TypeReference.METHOD_TYPE_PARAMETER, index);
                Annotations.visitTypeAnnotation(mv::visitTypeAnnotation, typeRef, anno);
                return this;
            }

            @Override
            public MethodBuilder<Result> end() {
                return MethodBuilderImpl.this;
            }
        };
    }

    @Override
    public ParameterBuilder<MethodBuilder<Result>> parameter(String name, Traversable<Mod> modifiers) {
        int index = parameterCount;
        parameterCount++;
        mv.visitParameter(name, classBuilder.options.flags().forParameter(modifiers));
        return new ParameterBuilder<>() {
            @Override
            public ParameterBuilder<MethodBuilder<Result>> annotation(JvmAnnotation anno) {
                classBuilder.learnClasses(anno);
                Annotations.visitAnnotation((descriptor, visible) -> mv.visitParameterAnnotation(index, descriptor, visible), anno);
                return this;
            }

            @Override
            public MethodBuilder<Result> end() {
                return MethodBuilderImpl.this;
            }
        };
    }

    @Override
    public MethodBuilder<Result> exception(JvmClassType exceptionType) {
        return addException(exceptionType);
    }

    @Override
    public MethodBuilder<Result> exception(JvmTypeVariable exceptionType) {
        return addException(exceptionType);
    }

    private MethodBuilder<Result> addException(JvmReferenceType exceptionType) {
        classBuilder.learnClasses(exceptionType);
        var typeRef = TypeReference.newExceptionReference(exceptions.size());
        exceptions.add(exceptionType);
        Annotations.visitTypeAnnotations(mv::visitTypeAnnotation, typeRef, exceptionType);
        return this;
    }

    /** Returns method signature or {@code null} if the method is not generic. */
    @Nullable String methodSignature() {
        JvmMethodDescriptor desc;
        if (isMemberConstructor) {
            desc = descriptor.dropFirst();
        } else {
            desc = descriptor;
        }
        if (typeParameters.isEmpty() && !desc.isGeneric() && !exceptions.exists(JvmReferenceType::isGeneric)) {
            return null;
        }
        var sb = new StringBuilder();
        Signatures.appendTypeParameters(sb, typeParameters);
        sb.append('(');
        for (var arg : desc.argumentTypes()) {
            sb.append(arg.genericString());
        }
        sb.append(')').append(desc.returnType().genericString());
        for (var exc : exceptions) {
            sb.append('^').append(exc.genericString());
        }
        return sb.toString();
    }

    @Override
    public CodeBlockBuilder<MethodBuilder<Result>> code(List<String> parameterNames) {
        return new CheckedCodeBuilderImpl<>(classBuilder, this, mv, isStatic, descriptor, parameterNames);
    }

    @Override
    public Result end() {
        // Visit signature and exceptions here since we can not visit them
        // when the MethodVisitor is created.
        @Nullable String signature = methodSignature();
        if (signature != null) {
            mv.visitAttribute(new SignatureAttribute(signature));
        }
        if (exceptions.nonEmpty()) {
            mv.visitAttribute(new ExceptionsAttribute(
                exceptions.map(t -> t.className().binaryName()).toArray(String[]::new)
            ));
        }
        mv.visitEnd();
        return (Result) classBuilder;
    }
}
