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
package me.sbasalaev.tybyco;

import static me.sbasalaev.API.concat;
import static me.sbasalaev.API.list;
import static me.sbasalaev.API.set;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.MutableList;
import me.sbasalaev.collection.Set;
import me.sbasalaev.tybyco.builders.*;
import me.sbasalaev.tybyco.descriptors.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeReference;

/**
 *
 * @author Sergey Basalaev
 */
final class ClassBuilderImpl<Self extends ClassBuilder<Self>>
    extends ClassfileBuilderImpl<Self>
    implements ClassBuilder<Self> {

    private static final String INIT = "<init>";
    private static final String CLINIT = "<clinit>";

    private final int access;
    private final MutableList<TypeParameterInfo> typeParameters = MutableList.empty();
    private JvmClassType superClass = new JvmClassType(JvmClass.JVM_OBJECT);
    private final MutableList<JvmClassType> interfaces = MutableList.empty();

    ClassBuilderImpl(Options options, Set<Mod> modifiers, JvmClass className) {
        super(options, className);
        this.access = Flags.forClass(className, modifiers);
        // visit here to tell ASM the version, will be visited again at the end
        cw.visit(options.version().major(), access, className.binaryName(), null, null, null);
    }

    /** Returns type of this class. */
    JvmClassType classType() {
        return new JvmClassType(className,
            typeParameters.mapped(tp -> new JvmTypeVariable(tp.name(), (JvmClass) tp.bounds().first().className())));
    }

    @Override
    public TypeParameterBuilder<Self> typeParameter(String name, JvmTypeVariable bound) {
        return addTypeParameter(name, list(bound));
    }

    @Override
    public TypeParameterBuilder<Self> typeParameter(String name, JvmClassType classBound, List<JvmClassType> interfaceBounds) {
        return addTypeParameter(name, concat(list(classBound), interfaceBounds));
    }

    private TypeParameterBuilder<Self> addTypeParameter(String name, List<JvmReferenceType> bounds) {
        bounds.forEach(this::learnClasses);
        int index = typeParameters.size();
        typeParameters.add(new TypeParameterInfo(name, bounds));
        bounds.forEachIndexed((bound, boundIndex) -> {
            var boundRef = TypeReference.newTypeParameterBoundReference(
                    TypeReference.CLASS_TYPE_PARAMETER_BOUND, index, boundIndex);
            Annotations.visitTypeAnnotations(cw::visitTypeAnnotation, boundRef, bound);
        });
        return new TypeParameterBuilder<Self>() {
            @Override
            public TypeParameterBuilder<Self> annotation(JvmAnnotation anno) {
                learnClasses(anno);
                var typeRef = TypeReference.newTypeParameterReference(TypeReference.CLASS_TYPE_PARAMETER, index);
                Annotations.visitTypeAnnotation(cw::visitTypeAnnotation, typeRef, anno);
                return this;
            }

            @Override
            public Self end() {
                return self();
            }
        };
    }

    @Override
    public Self superClass(JvmClassType classType) {
        learnClasses(classType);
        this.superClass = classType;
        return self();
    }

    @Override
    public Self superInterface(JvmClassType interfaceType) {
        learnClasses(interfaceType);
        int index = interfaces.size();
        Annotations.visitTypeAnnotations(cw::visitTypeAnnotation,
            TypeReference.newSuperTypeReference(index), interfaceType);
        interfaces.add(interfaceType);
        return self();
    }

    @Override
    public Self permittedSubclass(JvmClass className) {
        learnClass(className);
        cw.visitPermittedSubclass(className.binaryName());
        return self();
    }

    @Override
    public Self nestHost(JvmClass nestHost) {
        learnClass(nestHost);
        cw.visitNestHost(nestHost.binaryName());
        return self();
    }

    @Override
    public Self nestMember(JvmClass nestMember) {
        learnClass(nestMember);
        cw.visitNestMember(nestMember.binaryName());
        return self();
    }

    @Override
    public Self enclosingClass(JvmClass enclosingClass) {
        learnClass(enclosingClass);
        cw.visitOuterClass(enclosingClass.binaryName(), null, null);
        return self();
    }

    @Override
    public Self enclosingMethod(JvmClass enclosingClass, String name, JvmMethodDescriptor descriptor) {
        learnClass(enclosingClass);
        learnClasses(descriptor);
        cw.visitOuterClass(enclosingClass.binaryName(), name, descriptor.nonGenericString());
        return self();
    }

    @Override
    public Self enclosingConstructor(JvmClass enclosingClass, JvmMethodDescriptor descriptor) {
        learnClass(enclosingClass);
        learnClasses(descriptor);
        cw.visitOuterClass(enclosingClass.binaryName(), INIT, descriptor.nonGenericString());
        return self();
    }

    @Override
    public FieldBuilder<Self> field(Set<Mod> modifiers, String name, JvmType type) {
        learnClasses(type);
        var fv = cw.visitField(Flags.forField(modifiers), name,
            type.nonGenericString(),
            type.isGeneric() ? type.genericString() : null,
            null);
        return new FieldBuilderImpl<>(this, fv);
    }

    @Override
    public FieldBuilder<Self> constant(Set<Mod> modifiers, String name, Object value) {
        Object constValue = value;
        JvmType type = switch (value) {
            case Boolean z -> {
                constValue = z ? 1 : 0;
                yield JvmPrimitiveType.BOOLEAN;
            }
            case Byte b -> {
                constValue = (int) b;
                yield JvmPrimitiveType.BYTE;
            }
            case Character c -> {
                constValue = (int) c;
                yield JvmPrimitiveType.CHAR;
            }
            case Short s -> {
                constValue = (int) s;
                yield JvmPrimitiveType.SHORT;
            }
            case Double    __ -> JvmPrimitiveType.DOUBLE;
            case Float     __ -> JvmPrimitiveType.FLOAT;
            case Integer   __ -> JvmPrimitiveType.INT;
            case Long      __ -> JvmPrimitiveType.LONG;
            case String    __ -> new JvmClassType(JvmClass.JVM_STRING);
            default -> throw new IllegalArgumentException("invalid constant value: " + value);
        };
        var fv = cw.visitField(
            Flags.forField(Set.union(modifiers, set(Mod.FINAL, Mod.STATIC))),
            name, type.nonGenericString(), null, constValue);
        return new FieldBuilderImpl<>(this, fv);
    }

    @Override
    public MethodBuilder<Self> method(Set<Mod> modifiers, String name, JvmMethodDescriptor descriptor) {
        switch (name) {
            case INIT -> throw new IllegalArgumentException("use constructor() to visit constructors");
            case CLINIT -> throw new IllegalArgumentException("use staticInitializer() to visit static initilializers");
        }
        int flags = Flags.forMethod(modifiers);
        var mv = cw.visitMethod(flags, name, descriptor.nonGenericString(), null, null);
        boolean isStatic = modifiers.contains(Mod.STATIC);
        return new MethodBuilderImpl<>(this, descriptor, false, isStatic, mv);
    }

    @Override
    public MethodBuilder<Self> constructor(Set<Mod> modifiers, JvmMethodDescriptor descriptor) {
        int flags = Flags.forConstructor(modifiers);
        var mv = cw.visitMethod(flags, INIT, descriptor.nonGenericString(), null, null);
        boolean isMemberConstructor = className instanceof JvmNestedClass nested
                && nested.isInstanceMember();
        return new MethodBuilderImpl<>(this, descriptor, isMemberConstructor, false, mv);
    }

    @Override
    public CodeBlockBuilder<Self> staticInitializer() {
        var staticDesc = new JvmMethodDescriptor(list(), JvmVoid.INSTANCE);
        var mv = cw.visitMethod(Opcodes.ACC_STATIC, CLINIT, staticDesc.nonGenericString(), null, null);
        return new CheckedCodeBuilderImpl<>(this, self(), mv, true, staticDesc, list());
    }

    /** Returns class signature or {@code null} if the class is not generic. */
    private @Nullable String classSignature() {
        if (typeParameters.isEmpty() && !superClass.isGeneric()
                && !interfaces.exists(JvmType::isGeneric)) {
            return null;
        }
        var sb = new StringBuilder();
        Signatures.appendTypeParameters(sb, typeParameters);
        sb.append(superClass.genericString());
        for (var interfaceType : interfaces) {
            sb.append(interfaceType.genericString());
        }
        return sb.toString();
    }

    @Override
    public CompiledClass end() {
        cw.visit(options.version().major(), access, className.binaryName(),
            classSignature(), superClass.className().binaryName(),
            interfaces.isEmpty() ? null : interfaces.map(t -> t.className().binaryName()).toArray(String[]::new));
        Annotations.visitTypeAnnotations(cw::visitTypeAnnotation, TypeReference.newSuperTypeReference(-1), superClass);
        visitCommon();
        cw.visitEnd();
        return new CompiledClass(className, cw.toByteArray());
    }
}
