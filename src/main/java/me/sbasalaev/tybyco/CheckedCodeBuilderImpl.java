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

import java.util.Arrays;
import java.util.Comparator;
import static me.sbasalaev.API.list;
import me.sbasalaev.Require;
import me.sbasalaev.collection.*;
import me.sbasalaev.tybyco.builders.*;
import me.sbasalaev.tybyco.descriptors.*;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.*;

/**
 * Code builder that verifies some of the instructions.
 *
 * @author Sergey Basalaev
 */
final class CheckedCodeBuilderImpl<Result> implements CodeBlockBuilder<Result> {

    // TODO: How to conveniently put type annotations on instructions? The ASM's
    //       "after the instruction" is unusable when the instruction corresponding
    //       to the start of the expression is arbitrary.
    // TODO: invokedynamic
    // TODO: LDC with ConstantDynamic
    // TODO: promote to int automatically

    private final ClassBuilderImpl<?> classBuilder;
    private final Result result;
    private final MethodVisitor mv;
    private final boolean isStatic;

    CheckedCodeBuilderImpl(ClassBuilderImpl<?> classBuilder, Result result, MethodVisitor mv,
            boolean isStatic, JvmMethodDescriptor descriptor, List<String> argumentNames) {
        this.classBuilder = classBuilder;
        this.result = result;
        this.mv = mv;
        this.isStatic = isStatic;
        int nextIndex = 0;
        if (!isStatic) {
            locals.add(new Slot(new LocalVar("this", classBuilder.classType()), nextIndex, LocalRole.PARAMETER));
            nextIndex++;
        }
        int len = descriptor.argumentTypes().size();
        for (int i = 0; i < len; i++) {
            Slot slot = new Slot(
                new LocalVar(argumentNames.get(i), descriptor.argumentTypes().get(i)),
                nextIndex, LocalRole.PARAMETER
            );
            locals.add(slot);
            nextIndex += slot.width();
        }
    }

    /* Line numbers. */

    private int line = -1;

    @Override
    public CodeBlockBuilder<Result> lineNumber(int number) {
        if (number <= 0) throw new IllegalArgumentException("expected positive number");
        if (line != number) {
            line = number;
            Label label = new Label();
            mv.visitLabel(label);
            mv.visitLineNumber(number, label);
        }
        return this;
    }

    /* Locals. */

    private enum LocalRole {
        LOCAL_VAR,
        RESOURCE_VAR,
        PARAMETER;

        int typeRefSort() {
            return switch (this) {
                case LOCAL_VAR -> TypeReference.LOCAL_VARIABLE;
                case RESOURCE_VAR -> TypeReference.RESOURCE_VARIABLE;
                case PARAMETER -> 0;
            };
        }
    }

    private class Slot {

        final LocalVar local;
        final int index;
        final LocalRole role;

        // Starts and ends denote continuous instruction ranges where the
        // variable is assigned. If there are more starts than ends, the range
        // has not ended, otherwise the variable is unassigned.

        final MutableList<Label> starts = MutableList.empty();
        final MutableList<Label> ends = MutableList.empty();

        Slot(LocalVar var, int index, LocalRole role) {
            this.local = var;
            this.index = index;
            this.role = role;
        }

        public int width() {
            return switch (local.type().kind()) {
                case LONG, DOUBLE -> 2;
                default -> 1;
            };
        }

        public int nextIndex() {
            return index + width();
        }

        public boolean isAssigned() {
            return starts.size() > ends.size();
        }
    }

    private final MutableList<Slot> locals = MutableList.empty();

    /**
     * Returns slot that contains given local.
     * 
     * @throws IllegalArgumentException if the local is not in scope.
     */
    private Slot getSlot(LocalVar var) {
        for (var slot : locals) {
            if (slot.local == var) return slot;
        }
        throw new IllegalArgumentException("The local " + var.name() + " is not in the scope.");
    }

    /**
     * Creates and returns new slot for given local.
     * 
     * @throws IllegalArgumentException if the local is already in scope.
     */
    private Slot getNewSlot(LocalVar local, LocalRole role) {
        for (var slot : locals) {
            if (slot.local == local) {
                throw new IllegalArgumentException("The local " + local.name() + " is already in the scope.");
            }
        }
        int index = locals.isEmpty()
            ? (isStatic ? 0 : 1)
            : locals.last().nextIndex();
        Slot slot = new Slot(local, index, role);
        locals.add(slot);
        return slot;
    }

    @Override
    public CodeBlockBuilder<Result> open(LocalVar local) {
        checkReachable();
        getNewSlot(local, LocalRole.LOCAL_VAR);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> load(LocalVar local) {
        Slot slot = getSlot(local);
        if (!slot.isAssigned()) {
            throw new IllegalStateException("The local " + local.name() + " is not assigned.");
        }
        stackPush(local.type().kind());
        int insn = switch (local.type().kind()) {
            case BOOLEAN, BYTE, CHAR, SHORT, INT -> ILOAD;
            case LONG   -> LLOAD;
            case FLOAT   -> FLOAD;
            case DOUBLE   -> DLOAD;
            case REFERENCE -> ALOAD;
        };
        mv.visitVarInsn(insn, slot.index);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> store(LocalVar local) {
        return storeInLocal(getSlot(local));
    }

    @Override
    public CodeBlockBuilder<Result> openAndStore(LocalVar local) {
        return storeInLocal(getNewSlot(local, LocalRole.LOCAL_VAR));
    }

    private CodeBlockBuilder<Result> storeInLocal(Slot slot) {
        TypeKind kind = slot.local.type().kind();
        stackPop(kind);
        int insn = switch (kind) {
            case BOOLEAN, BYTE, CHAR, SHORT, INT -> ISTORE;
            case LONG -> LSTORE;
            case FLOAT -> FSTORE;
            case DOUBLE -> DSTORE;
            case REFERENCE -> ASTORE;
        };
        if (!slot.isAssigned()) {
            mv.visitVarInsn(insn, slot.index);
            var label = new Label();
            mv.visitLabel(label);
            slot.starts.add(label);
        }
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> iinc(LocalVar local, short increment) {
        checkReachable();
        Slot slot = getSlot(local);
        if (local.type().kind() != TypeKind.INT) {
            throw new IllegalArgumentException("The local " + local.name() + " is not of int type");
        }
        if (!slot.isAssigned()) {
            throw new IllegalStateException("The local " + local.name() + " is not assigned");
        }
        mv.visitIincInsn(slot.index, increment);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> setUnassigned(LocalVar local) {
        Slot slot = getSlot(local);
        if (!slot.isAssigned()) {
            throw new IllegalStateException("The local " + local.name() + " is not assigned");
        }
        var end = new Label();
        mv.visitLabel(end);
        slot.ends.add(end);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> setAssigned(LocalVar local) {
        Slot slot = getSlot(local);
        if (slot.isAssigned()) {
            throw new IllegalStateException("The local " + local.name() + " is already assigned");
        }
        if (slot.starts.isEmpty()) {
            throw new IllegalStateException("The local " + local.name() + " was never assigned");
        }
        var start = new Label();
        mv.visitLabel(start);
        slot.starts.add(start);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> close(LocalVar local) {
        Slot slot = getSlot(local);
        locals.remove(slot);
        closeSlot(slot);
        return this;
    }

    private void closeSlot(Slot slot) {
        if (slot.isAssigned()) {
            var end = new Label();
            mv.visitLabel(end);
            slot.ends.add(end);
        }
        if (slot.local.isAnonymous()) {
            return;
        }
        int count = slot.starts.size();
        for (int i = 0; i < count; i++) {
            var type = slot.local.type();
            mv.visitLocalVariable(
                slot.local.name(),
                type.nonGenericString(),
                type.isGeneric() ? type.genericString() : null,
                slot.starts.get(i), slot.ends.get(i), slot.index);
        }
        if (slot.starts.nonEmpty() && slot.role != LocalRole.PARAMETER) {
            Label[] starts = slot.starts.toArray(Label[]::new);
            Label[] ends = slot.ends.toArray(Label[]::new);
            int[] indices = new int[count];
            Arrays.fill(indices, slot.index);
            Annotations.visitTypeAnnotations((typeRef, typePath, descriptor, visible) ->
                    mv.visitLocalVariableAnnotation(typeRef, typePath, starts, ends, indices, descriptor, visible),
                TypeReference.newTypeReference(slot.role.typeRefSort()),
                slot.local.type()
            );
        }
    }

    /* Stack. */

    private final MutableList<TypeKind> stack = MutableList.empty();
    private final MutableMap<Target, Label> targetLabels = MutableMap.empty();
    private final MutableMap<Target, List<TypeKind>> targetStacks = MutableMap.empty();
    private boolean undefinedFrame = false;

    private void checkReachable() {
        if (undefinedFrame) {
            throw new IllegalStateException("Unreachable instruction");
        }
    }

    /** Marks that value of given kind is pushed on stack. */
    private void stackPush(TypeKind slotType) {
        checkReachable();
        stack.add(slotType);
    }

    private void compareStacks(List<TypeKind> expected, List<TypeKind> actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Trying to pop " + expected + " but the stack has " + actual);
        }
    }

    /** Pops last value from the stack. */
    private TypeKind stackPop() {
        checkReachable();
        if (stack.isEmpty()) {
            throw new IllegalStateException("Trying to pop a value but the stack is empty");
        }
        return stack.removeAt(stack.lastIndex());
    }

    /** Marks that values of given kinds are popped from stack. */
    private void stackPop(TypeKind... expected) {
        stackPop(list(expected));
    }

    /** Marks that values of given kinds are popped from stack. */
    private void stackPop(List<TypeKind> expected) {
        checkReachable();
        int len = expected.size();
        var stackLast = stack.size() < len ? stack : stack.from(stack.size() - len);
        compareStacks(expected, stackLast);
        stack.removeRange(stack.size() - len, stack.size());
    }

    private void stackAccept(boolean isStatic, JvmMethodDescriptor descriptor) {
        stackPop(descriptor.argumentTypes().map(JvmType::kind));
        if (!isStatic) {
            stackPop(TypeKind.REFERENCE);
        }
        if (descriptor.returnType() instanceof JvmType returnType) {
            stackPush(returnType.kind());
        }
    }

    /* Checking frame correctness. */

    /**
     * Restores stack state from given target.
     */
    private void restoreStateFrom(Target target) {
        for (var targetStack : targetStacks.get(target)) {
            undefinedFrame = false;
            stack.clear();
            stack.addAll(targetStack);
            return;
        }
        throw new IllegalArgumentException("The target " + target + " was not visited");
    }

    /**
     * Saves stack state for given target.
     * If the target already has an associated stack state the states are
     * compared for equality.
     */
    private void saveStateTo(Target target, List<TypeKind> expectedStack) {
        for (var targetStack : targetStacks.get(target)) {
            if (!expectedStack.equals(targetStack)) {
                throw new IllegalStateException("Target " + target + " is reached with differing stacks: "
                    + expectedStack + " and " + targetStack);
            }
            return;
        }
        targetStacks.set(target, expectedStack.clone());
    }

    /* ARITHMETIC */

    private static TypeKind promoteToInt(TypeKind kind) {
        return switch (kind) {
            case BYTE, CHAR, SHORT -> TypeKind.INT;
            default -> kind;
        };
    }

    private static void checkSameNumber(TypeKind lhs, TypeKind rhs) {
        if (lhs != rhs || lhs == TypeKind.BOOLEAN || lhs == TypeKind.REFERENCE) {
            throw new IllegalStateException("Trying to pop two numbers of the same type but the stack has {" + lhs + ", " + rhs + "}");
        }
    }

    private static void checkIsNumber(TypeKind type) {
        if (type == TypeKind.BOOLEAN || type == TypeKind.REFERENCE) {
            throw new IllegalStateException("Trying to pop a number but the stack has {" + type + "}");
        }
    }

    private static void checkIsInt(TypeKind type) {
        if (type != TypeKind.INT) {
            throw new IllegalStateException("Trying to pop an integer number but the stack has {" + type + "}");
        }
    }

    private static void checkIsIntOrLong(TypeKind type) {
        if (type != TypeKind.INT && type != TypeKind.LONG) {
            throw new IllegalStateException("Trying to pop an integer number but the stack has {" + type + "}");
        }
    }

    @Override
    public CodeBlockBuilder<Result> add() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IADD;
            case LONG   -> LADD;
            case FLOAT  -> FADD;
            case DOUBLE -> DADD;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> subtract() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> ISUB;
            case LONG   -> LSUB;
            case FLOAT  -> FSUB;
            case DOUBLE -> DSUB;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> multiply() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IMUL;
            case LONG   -> LMUL;
            case FLOAT  -> FMUL;
            case DOUBLE -> DMUL;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> divide() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IDIV;
            case LONG   -> LDIV;
            case FLOAT  -> FDIV;
            case DOUBLE -> DDIV;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> remainder() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IREM;
            case LONG   -> LREM;
            case FLOAT  -> FREM;
            case DOUBLE -> DREM;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> negate() {
        TypeKind rhs = promoteToInt(stackPop());
        checkIsNumber(rhs);
        stackPush(rhs);
        int insn = switch (rhs) {
            case INT    -> INEG;
            case LONG   -> LNEG;
            case FLOAT  -> FNEG;
            case DOUBLE -> DNEG;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> and() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        checkIsIntOrLong(lhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IAND;
            case LONG   -> LAND;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> or() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        checkIsIntOrLong(lhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IOR;
            case LONG   -> LOR;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> xor() {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        checkSameNumber(lhs, rhs);
        checkIsIntOrLong(lhs);
        stackPush(lhs);
        int insn = switch (lhs) {
            case INT    -> IXOR;
            case LONG   -> LXOR;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> shiftLeft() {
        stackPop(TypeKind.INT);
        TypeKind lhs = promoteToInt(stackPop());
        checkIsIntOrLong(lhs);
        int insn = switch (lhs) {
            case INT  -> ISHL;
            case LONG -> LSHL;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> signedShiftRight() {
        stackPop(TypeKind.INT);
        TypeKind lhs = promoteToInt(stackPop());
        checkIsIntOrLong(lhs);
        int insn = switch (lhs) {
            case INT  -> ISHR;
            case LONG -> LSHR;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> unsignedShiftRight() {
        stackPop(TypeKind.INT);
        TypeKind lhs = promoteToInt(stackPop());
        checkIsIntOrLong(lhs);
        int insn = switch (lhs) {
            case INT  -> IUSHR;
            case LONG -> LUSHR;
            default -> NOP;
        };
        mv.visitInsn(insn);
        return this;
    }

    /* VARIOUS INSTRUCTIONS */

    @Override
    public CodeBlockBuilder<Result> nop() {
        mv.visitInsn(NOP);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> monitorEnter() {
        stackPop(TypeKind.REFERENCE);
        mv.visitInsn(MONITORENTER);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> monitorExit() {
        stackPop(TypeKind.REFERENCE);
        mv.visitInsn(MONITOREXIT);
        return this;
    }

    /* CLASSES AND ARRAYS */

    @Override
    public CodeBlockBuilder<Result> newInstanceAndDup(JvmClass className) {
        classBuilder.learnClass(className);
        stackPush(TypeKind.REFERENCE);
        stackPush(TypeKind.REFERENCE);
        mv.visitTypeInsn(NEW, className.binaryName());
        mv.visitInsn(DUP);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> newArray(JvmArray arrayClass) {
        classBuilder.learnClass(arrayClass);
        stackPop(TypeKind.INT);
        stackPush(TypeKind.REFERENCE);
        switch (arrayClass) {
            case JvmPrimitiveArray primArray -> {
                int atype = switch (primArray.componentType().kind()) {
                    case BOOLEAN -> T_BOOLEAN;
                    case BYTE    -> T_BYTE;
                    case CHAR    -> T_CHAR;
                    case DOUBLE  -> T_DOUBLE;
                    case FLOAT   -> T_FLOAT;
                    case INT     -> T_INT;
                    case LONG    -> T_LONG;
                    case SHORT   -> T_SHORT;
                    case REFERENCE -> throw new IncompatibleClassChangeError();
                };
                mv.visitIntInsn(NEWARRAY, atype);
            }
            case JvmReferenceArray refArray -> {
                mv.visitTypeInsn(ANEWARRAY, refArray.componentClass().binaryName());
            }
        }
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> newArray(JvmArray arrayClass, int dimensions) {
        Require.argument(dimensions > 0, "dimensions must be positive");
        if (dimensions == 1) {
            return newArray(arrayClass);
        }
        classBuilder.learnClass(arrayClass);
        stackPop(List.repeat(TypeKind.INT, dimensions));
        stackPush(TypeKind.REFERENCE);
        mv.visitMultiANewArrayInsn(arrayClass.binaryName(), dimensions);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> arrayLoad(JvmType componentType) {
        stackPop(TypeKind.REFERENCE, TypeKind.INT);
        stackPush(componentType.kind());
        int insn = switch (componentType.kind()) {
            case BOOLEAN, BYTE -> BALOAD;
            case CHAR   -> CALOAD;
            case DOUBLE -> DALOAD;
            case FLOAT  -> FALOAD;
            case INT    -> IALOAD;
            case LONG   -> LALOAD;
            case SHORT  -> SALOAD;
            case REFERENCE -> AALOAD;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> arrayStore(JvmType componentType) {
        stackPop(TypeKind.REFERENCE, TypeKind.INT, componentType.kind());
        int insn = switch (componentType.kind()) {
            case BOOLEAN, BYTE -> BASTORE;
            case CHAR   -> CASTORE;
            case DOUBLE -> DASTORE;
            case FLOAT  -> FASTORE;
            case INT    -> IASTORE;
            case LONG   -> LASTORE;
            case SHORT  -> SASTORE;
            case REFERENCE -> AASTORE;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> arrayLength() {
        stackPop(TypeKind.REFERENCE);
        stackPush(TypeKind.INT);
        mv.visitInsn(ARRAYLENGTH);
        return this;
    }

    /* FIELDS */

    @Override
    public CodeBlockBuilder<Result> getStaticField(JvmClass owner, String name, JvmType type) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(type);
        stackPush(type.kind());
        mv.visitFieldInsn(GETSTATIC, owner.binaryName(), name, type.nonGenericString());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> putStaticField(JvmClass owner, String name, JvmType type) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(type);
        stackPop(type.kind());
        mv.visitFieldInsn(PUTSTATIC, owner.binaryName(), name, type.nonGenericString());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> getInstanceField(JvmClass owner, String name, JvmType type) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(type);
        stackPop(TypeKind.REFERENCE);
        stackPush(type.kind());
        mv.visitFieldInsn(GETFIELD, owner.binaryName(), name, type.nonGenericString());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> putInstanceField(JvmClass owner, String name, JvmType type) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(type);
        stackPop(TypeKind.REFERENCE, type.kind());
        mv.visitFieldInsn(PUTFIELD, owner.binaryName(), name, type.nonGenericString());
        return this;
    }

    /* METHODS */

    private int invokeVirtualInsn(JvmClass owner) {
        return owner.classKind().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;
    }

    private int invokePrivateInsn(JvmClass owner) {
        return classBuilder.options.version().atLeast(JavaVersion.V11)
                ? invokeVirtualInsn(owner) : INVOKESPECIAL;
    }

    @Override
    public CodeBlockBuilder<Result> invokeConstructor(JvmClass owner, JvmMethodDescriptor descriptor) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(descriptor);
        stackAccept(false, descriptor);
        mv.visitMethodInsn(INVOKESPECIAL,
                owner.binaryName(), "<init>", descriptor.nonGenericString(), false);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> invokeStatic(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(descriptor);
        stackAccept(true, descriptor);
        mv.visitMethodInsn(INVOKESTATIC,
                owner.binaryName(), name, descriptor.nonGenericString(), owner.classKind().isInterface());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> invokeVirtual(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(descriptor);
        stackAccept(false, descriptor);
        mv.visitMethodInsn(invokeVirtualInsn(owner),
                owner.binaryName(), name, descriptor.nonGenericString(), owner.classKind().isInterface());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> invokeSuper(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(descriptor);
        stackAccept(false, descriptor);
        mv.visitMethodInsn(INVOKESPECIAL,
                owner.binaryName(), name, descriptor.nonGenericString(), owner.classKind().isInterface());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> invokePrivate(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        classBuilder.learnClass(owner);
        classBuilder.learnClasses(descriptor);
        stackAccept(false, descriptor);
        mv.visitMethodInsn(invokePrivateInsn(owner),
                owner.binaryName(), name, descriptor.nonGenericString(), owner.classKind().isInterface());
        return this;
    }

    /* TYPE CASTS */

    @Override
    public CodeBlockBuilder<Result> numericCast(JvmPrimitiveType toType) {
        TypeKind toKind = toType.kind();
        if (toKind == TypeKind.BOOLEAN) {
            throw new IllegalArgumentException(toType + " is not a numeric type");
        }
        TypeKind fromKind = stackPop();
        checkIsNumber(fromKind);
        stackPush(toKind);
        switch (toKind) {
            case BYTE -> {
                switch (fromKind) {
                    case CHAR, SHORT, INT -> mv.visitInsn(I2B);
                    case LONG -> { mv.visitInsn(L2I); mv.visitInsn(I2B); }
                    case FLOAT -> { mv.visitInsn(F2I); mv.visitInsn(I2B); }
                    case DOUBLE -> { mv.visitInsn(D2I); mv.visitInsn(I2B); }
                }
            }
            case CHAR -> {
                switch (fromKind) {
                    case SHORT, INT -> mv.visitInsn(I2C);
                    case LONG -> { mv.visitInsn(L2I); mv.visitInsn(I2C); }
                    case FLOAT -> { mv.visitInsn(F2I); mv.visitInsn(I2C); }
                    case DOUBLE -> { mv.visitInsn(D2I); mv.visitInsn(I2C); }
                }
            }
            case SHORT -> {
                switch (fromKind) {
                    case CHAR, INT -> mv.visitInsn(I2S);
                    case LONG -> { mv.visitInsn(L2I); mv.visitInsn(I2S); }
                    case FLOAT -> { mv.visitInsn(F2I); mv.visitInsn(I2S); }
                    case DOUBLE -> { mv.visitInsn(D2I); mv.visitInsn(I2S); }
                }
            }
            case INT -> {
                switch (fromKind) {
                    case LONG -> mv.visitInsn(L2I);
                    case FLOAT -> mv.visitInsn(F2I);
                    case DOUBLE -> mv.visitInsn(D2I);
                }
            }
            case LONG -> {
                switch (fromKind) {
                    case BYTE, CHAR, SHORT, INT -> mv.visitInsn(I2L);
                    case FLOAT -> mv.visitInsn(F2L);
                    case DOUBLE -> mv.visitInsn(D2L);
                }
            }
            case FLOAT -> {
                switch (fromKind) {
                    case BYTE, CHAR, SHORT, INT -> mv.visitInsn(I2F);
                    case LONG -> mv.visitInsn(L2F);
                    case DOUBLE -> mv.visitInsn(D2F);
                }
            }
            case DOUBLE -> {
                switch (fromKind) {
                    case BYTE, CHAR, SHORT, INT -> mv.visitInsn(I2D);
                    case LONG -> mv.visitInsn(L2D);
                    case FLOAT -> mv.visitInsn(F2D);
                }
            }
        }
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> instanceOf(JvmClassOrArray className) {
        classBuilder.learnClass(className);
        stackPop(TypeKind.REFERENCE);
        stackPush(TypeKind.BOOLEAN);
        mv.visitTypeInsn(INSTANCEOF, className.binaryName());
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> checkCast(JvmClassOrArray className) {
        stackPop(TypeKind.REFERENCE);
        stackPush(TypeKind.REFERENCE);
        classBuilder.learnClass(className);
        mv.visitTypeInsn(CHECKCAST, className.binaryName());
        return this;
    }

    /* RETURNING */

    @Override
    public CodeBuilder<Result> valueReturn() {
        TypeKind value = stackPop();
        var insn = switch (value) {
            case BOOLEAN, BYTE, CHAR, SHORT, INT -> IRETURN;
            case LONG -> LRETURN;
            case FLOAT -> FRETURN;
            case DOUBLE -> DRETURN;
            case REFERENCE -> ARETURN;
        };
        undefinedFrame = true;
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBuilder<Result> voidReturn() {
        undefinedFrame = true;
        mv.visitInsn(RETURN);
        return this;
    }

    /* EXCEPTION HANDLING */

    @Override
    public CodeBuilder<Result> athrow() {
        stackPop(TypeKind.REFERENCE);
        undefinedFrame = true;
        mv.visitInsn(ATHROW);
        return this;
    }

    /* STACK */

    @Override
    public CodeBlockBuilder<Result> pushThis() {
        stackPush(TypeKind.REFERENCE);
        mv.visitVarInsn(ALOAD, 0);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> pushConst(Object value) {
        TypeKind kind = switch (value) {
            case null -> {
                mv.visitInsn(ACONST_NULL);
                yield TypeKind.REFERENCE;
            }
            case Boolean b -> {
                pushInt(b ? 1 : 0);
                yield TypeKind.BOOLEAN;
            }
            case Byte b -> {
                pushInt(b);
                yield TypeKind.BYTE;
            }
            case Character c -> {
                pushInt(c);
                yield TypeKind.CHAR;
            }
            case Short s -> {
                pushInt(s);
                yield TypeKind.SHORT;
            }
            case Integer i -> {
                pushInt(i);
                yield TypeKind.INT;
            }
            case Long l -> {
                if (l == 0L) mv.visitInsn(LCONST_0);
                else if (l == 1L) mv.visitInsn(LCONST_1);
                else mv.visitLdcInsn(l);
                yield TypeKind.LONG;
            }
            case Float f -> {
                if (Float.compare(f, 0.0f) == 0) mv.visitInsn(FCONST_0);
                else if (f == 1.0f) mv.visitInsn(FCONST_1);
                else if (f == 2.0f) mv.visitInsn(FCONST_2);
                else mv.visitLdcInsn(f);
                yield TypeKind.FLOAT;
            }
            case Double d -> {
                if (Double.compare(d, 0.0) == 0) mv.visitInsn(DCONST_0);
                else if (d == 1.0) mv.visitInsn(DCONST_1);
                else mv.visitLdcInsn(d);
                yield TypeKind.DOUBLE;
            }
            case String s -> {
                mv.visitLdcInsn(s);
                yield TypeKind.REFERENCE;
            }
            case JvmClassOrArray className -> {
                classBuilder.learnClass(className);
                mv.visitLdcInsn(Type.getObjectType(className.binaryName()));
                yield TypeKind.REFERENCE;
            }
            case JvmMethodDescriptor descriptor -> {
                classBuilder.learnClasses(descriptor);
                mv.visitLdcInsn(Type.getMethodType(descriptor.nonGenericString()));
                yield TypeKind.REFERENCE;
            }
            case JvmMethodHandle handle -> {
                learnHandleClasses(handle);
                mv.visitLdcInsn(asmHandle(handle));
                yield TypeKind.REFERENCE;
            }
            default -> throw new IllegalArgumentException(value.toString());
        };
        stackPush(kind);
        return this;
    }

    private Handle asmHandle(JvmMethodHandle handle) {
        boolean isInterface = handle.owner().classKind().isInterface();
        int tag = switch (handle.kind()) {
            case INSTANCE_FIELD_GETTER -> H_GETFIELD;
            case INSTANCE_FIELD_SETTER -> H_PUTFIELD;
            case STATIC_FIELD_GETTER   -> H_GETSTATIC;
            case STATIC_FIELD_SETTER   -> H_PUTSTATIC;
            case CONSTRUCTOR -> H_NEWINVOKESPECIAL;
            case SUPER_METHOD -> H_INVOKESPECIAL;
            case STATIC_METHOD -> H_INVOKESTATIC;
            case VIRTUAL_METHOD -> switch (invokeVirtualInsn(handle.owner())) {
                case INVOKEVIRTUAL -> H_INVOKEVIRTUAL;
                case INVOKEINTERFACE -> H_INVOKEINTERFACE;
                default -> 0;
            };
            case PRIVATE_METHOD -> switch (invokePrivateInsn(handle.owner())) {
                case INVOKEVIRTUAL -> H_INVOKEVIRTUAL;
                case INVOKEINTERFACE -> H_INVOKEINTERFACE;
                case INVOKESPECIAL -> H_INVOKESPECIAL;
                default -> 0;
            };
        };
        return new Handle(tag,
            handle.owner().binaryName(),
            handle.name(),
            handle.descriptor().nonGenericString(),
            isInterface);
    }

    private void learnHandleClasses(JvmMethodHandle handle) {
        classBuilder.learnClass(handle.owner());
        switch (handle.descriptor()) {
            case JvmMethodDescriptor md -> classBuilder.learnClasses(md);
            case JvmTypeOrVoid tv -> classBuilder.learnClasses(tv);
        }
    }

    private void pushInt(int value) {
        if (value >= -1 && value < 5) {
            mv.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    @Override
    public CodeBlockBuilder<Result> pop() {
        TypeKind value = stackPop();
        int insn = switch (value) {
            case LONG, DOUBLE -> POP2;
            default -> POP;
        };
        mv.visitInsn(insn);
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> dup() {
        TypeKind value = stackPop();
        stackPush(value);
        stackPush(value);
        int insn = switch (value) {
            case LONG, DOUBLE -> DUP2;
            default -> DUP;
        };
        mv.visitInsn(insn);
        return this;
    }

    /* JUMPS */

    @Override
    public CodeBlockBuilder<Result> at(Target target) {
        if (targetLabels.containsKey(target)) {
            throw new IllegalArgumentException("The target " + target.name() + " is already visited");
        }
        if (undefinedFrame) {
            restoreStateFrom(target);
        } else {
            saveStateTo(target, stack);
        }
        Label label = new Label();
        mv.visitLabel(label);
        targetLabels.set(target, label);
        return this;
    }

    private Label targetLabel(Target target) {
        return targetLabels.createIfMissing(target, Label::new);
    }

    @Override
    public CodeBuilder<Result> jump(Target target) {
        checkReachable();
        mv.visitJumpInsn(GOTO, targetLabel(target));
        saveStateTo(target, stack);
        undefinedFrame = true;
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> jumpIf(boolean value, Target target) {
        stackPop(TypeKind.BOOLEAN);
        saveStateTo(target, stack);
        mv.visitJumpInsn(value ? IFNE : IFEQ, targetLabel(target));
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> jumpIfCmpNull(Cmp cmp, Target target) {
        if (cmp != Cmp.EQ && cmp != Cmp.NE) {
            throw new IllegalArgumentException(cmp + " comparison is not valid for references.");
        }
        stackPop(TypeKind.REFERENCE);
        saveStateTo(target, stack);
        mv.visitJumpInsn(cmp == Cmp.EQ ? IFNULL : IFNONNULL, targetLabel(target));
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> jumpIfCmpZero(Cmp cmp, Target target) {
        checkIsInt(promoteToInt(stackPop()));
        saveStateTo(target, stack);
        int insn = switch (cmp) {
            case EQ -> IFEQ;
            case GE -> IFGE;
            case GT -> IFGT;
            case LE -> IFLE;
            case LT -> IFLT;
            case NE -> IFNE;
        };
        mv.visitJumpInsn(insn, targetLabel(target));
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> jumpIfCmp(Cmp cmp, Target target) {
        TypeKind rhs = promoteToInt(stackPop());
        TypeKind lhs = promoteToInt(stackPop());
        saveStateTo(target, stack);
        if (lhs != rhs) {
            throw new IllegalStateException("Trying to pop two values of the same kind from the stack, "
                    + "got {" + lhs + ", " + rhs + "}");
        }
        if ((lhs == TypeKind.REFERENCE || lhs == TypeKind.BOOLEAN) && cmp != Cmp.EQ && cmp != Cmp.NE) {
            throw new IllegalArgumentException(cmp + " comparison is not valid for " + lhs + " values");
        }
        int cmpinsn = switch (lhs) {
            case LONG -> LCMP;
            case FLOAT -> switch (cmp) {
                case EQ, NE, GT, GE -> FCMPL;
                case LE, LT -> FCMPG;
            };
            case DOUBLE -> switch (cmp) {
                case EQ, NE, GT, GE -> DCMPL;
                case LE, LT -> DCMPG;
            };
            default -> NOP;
        };
        if (cmpinsn != NOP) mv.visitInsn(cmpinsn);
        int insn = switch (lhs) {
            case REFERENCE -> cmp == Cmp.EQ ? IF_ACMPEQ : IF_ACMPNE;
            case LONG, FLOAT, DOUBLE -> switch (cmp) {
                case EQ -> IFEQ;
                case GE -> IFGE;
                case GT -> IFGT;
                case LE -> IFLE;
                case LT -> IFLT;
                case NE -> IFNE;
            };
            default -> switch (cmp) {
                case EQ -> IF_ICMPEQ;
                case GE -> IF_ICMPGE;
                case GT -> IF_ICMPGT;
                case LE -> IF_ICMPLE;
                case LT -> IF_ICMPLT;
                case NE -> IF_ICMPNE;
            };
        };
        mv.visitJumpInsn(insn, targetLabel(target));
        return this;
    }

    @Override
    public CodeBlockBuilder<Result> jumpSwitch(Map<Integer, Target> keyTargets, Target dfltTarget) {
        checkIsInt(promoteToInt(stackPop()));
        undefinedFrame = true;

        // calculate costs of TABLESWITCH vs LOOKUPSWITCH
        saveStateTo(dfltTarget, stack);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (var entry : keyTargets.entries()) {
            int key = entry.key();
            var target = entry.value();
            saveStateTo(target, stack);
            min = Math.min(min, key);
            max = Math.max(max, key);
        }
        // the sizes are computed in long since (max - min) may overflow
        long tablesize = (long) max - min + 4;
        long lookupsize = 2 + 2 * (long) keyTargets.size();

        if (keyTargets.size() < 3 || lookupsize < tablesize) {
            doLookupSwitch(keyTargets, dfltTarget);
        } else {
            doTableSwitch(keyTargets, dfltTarget, min, max);
        }
        return this;
    }

    private void doLookupSwitch(Map<Integer, Target> keyTargets, Target dfltTarget) {
        var list = MutableList.of(keyTargets.entries());
        list.sortBy(Comparator.comparing(Entry::key));
        int size = list.size();
        int keys[] = new int[size];
        Label labels[] = new Label[size];
        for (int i = 0; i < size; i++) {
            var entry = list.get(i);
            keys[i] = entry.key();
            labels[i] = targetLabel(entry.value());
        }
        mv.visitLookupSwitchInsn(targetLabel(dfltTarget), keys, labels);
    }

    private void doTableSwitch(Map<Integer, Target> keyTargets, Target dfltTarget, int min, int max) {
        Label dflt = targetLabel(dfltTarget);
        Label[] labels = new Label[max - min + 1];
        Arrays.fill(labels, dflt);
        for (var entry : keyTargets.entries()) {
            int key = entry.key();
            Target target = entry.value();
            labels[key - min] = targetLabel(target);
        }
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    /** Number of visited try/catch blocks. */
    private int tryCatchBlocks = 0;

    @Override
    public CodeBlockBuilder<Result> atTryCatch(Target tryEnd, Target catchHandler, List<JvmClassType> exceptionTypes) {
        if (targetLabels.containsKey(tryEnd)) {
            throw new IllegalArgumentException("Target " + tryEnd.name() + " is already visited by this builder.");
        }
        if (targetLabels.containsKey(catchHandler)) {
            throw new IllegalArgumentException("Target " + catchHandler.name() + " is already visited by this builder.");
        }
        Label start = new Label();
        Label end = targetLabel(tryEnd);
        Label handler = targetLabel(catchHandler);
        if (exceptionTypes.nonEmpty()) {
            for (var excType : exceptionTypes) {
                mv.visitTryCatchBlock(start, end, handler, excType.className().binaryName());
                int index = tryCatchBlocks;
                tryCatchBlocks = index + 1;
                if (excType.isDeeplyAnnotated()) {
                    var excRef = TypeReference.newTryCatchReference(index);
                    Annotations.visitTypeAnnotations(mv::visitTryCatchAnnotation, excRef, excType);
                }
            }
        } else {
            mv.visitTryCatchBlock(start, end, handler, null);
            tryCatchBlocks++;
        }
        mv.visitLabel(start);
        saveStateTo(catchHandler, list(TypeKind.REFERENCE));
        return this;
    }

    @Override
    public Result end() {
        if (!undefinedFrame) {
            throw new IllegalStateException("The current code block has not ended.");
        }
        for (var slot: locals) {
            closeSlot(slot);
        }
        locals.clear();
        mv.visitMaxs(-1, -1);
        return result;
    }
}
