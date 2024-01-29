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
package me.sbasalaev.tybyco.builders;

import static me.sbasalaev.API.concat;
import static me.sbasalaev.API.list;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Map;
import me.sbasalaev.tybyco.descriptors.*;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Builder of a code block of a method or constructor.
 * <h3>Stack checks</h3>
 * The builder checks that the values on stack are at least of correct
 * {@link TypeKind}. In doing so it even distinguishes between {@code boolean},
 * {@code byte}, {@code char}, {@code short} and {@code int}. To convert between
 * numeric types you may use
 * {@link #numericCast(me.sbasalaev.tybyco.descriptors.TypeKind) numericCast()}.
 * There is no conversion between an {@code int} and a {@code boolean}.
 * <h3>invokespecial and invokeinterface</h3>
 * There are no matching methods for {@code invokespecial} and {@code invokeinterface}
 * instructions. Instead, the correct instruction is selected based on a class
 * reference and the intention:
 * <ul>
 * <li>
 *   Use
 *   {@link #invokeConstructor(me.sbasalaev.tybyco.descriptors.JvmClass, me.sbasalaev.tybyco.descriptors.JvmMethodDescriptor) invokeConstructor()}
 *   to call a constructor.
 * <li>
 *   Use
 *   {@link #invokeStatic(me.sbasalaev.tybyco.descriptors.JvmClass, java.lang.String, me.sbasalaev.tybyco.descriptors.JvmMethodDescriptor) invokeStatic()}
 *   to call a static method of a class or interface.
 * <li>
 *   use
 *   {@link #invokeVirtual(me.sbasalaev.tybyco.descriptors.JvmClass, java.lang.String, me.sbasalaev.tybyco.descriptors.JvmMethodDescriptor) invokeVirtual()}
 *   to call an instance method of a class or interface. This includes {@code private}
 *   methods.
 * <li>
 *   use
 *   {@link #invokeSuper(me.sbasalaev.tybyco.descriptors.JvmClass, java.lang.String, me.sbasalaev.tybyco.descriptors.JvmMethodDescriptor) invokeSuper()}
 *   to call an instance method of a superclass or superinterface.
 * </ul>
 *
 * @author Sergey Basalaev
 */
public interface CodeBlockBuilder<Result> extends CodeBuilder<Result> {

    /* ARITHMETIC */

    /**
     * Writes appropriate instruction to add two numbers on the stack.
     * The top of the stack must contain two values of the same numeric type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same numeric type.
     */
    CodeBlockBuilder<Result> add();

    /**
     * Writes appropriate instruction to subtract two numbers on the stack.
     * The top of the stack must contain two values of the same numeric type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same numeric type.
     */
    CodeBlockBuilder<Result> subtract();

    /**
     * Writes appropriate instruction to multiply two numbers on the stack.
     * The top of the stack must contain two values of the same numeric type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same numeric type.
     */
    CodeBlockBuilder<Result> multiply();

    /**
     * Writes appropriate instruction to divide two numbers on the stack.
     * The top of the stack must contain two values of the same numeric type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same numeric type.
     */
    CodeBlockBuilder<Result> divide();

    /**
     * Writes appropriate instruction to find the remainder of two numbers on the stack.
     * The top of the stack must contain two values of the same numeric type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same numeric type.
     */
    CodeBlockBuilder<Result> remainder();

    /**
     * Writes appropriate instruction to negate the number on the stack.
     * The top of the stack must contain the value of the numeric type.
     * If the type is {@code byte}, {@code char} or {@code short} it is
     * promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack is empty or the top is not
     *     a number.
     */
    CodeBlockBuilder<Result> negate();

    /**
     * Writes appropriate instruction for bitwise AND of two values on the stack.
     * The top of the stack must contain two values of the same integral type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same type.
     */
    CodeBlockBuilder<Result> and();

    /**
     * Writes appropriate instruction for bitwise inclusive OR of two values on the stack.
     * The top of the stack must contain two values of the same integral type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same type.
     */
    CodeBlockBuilder<Result> or();

    /**
     * Writes appropriate instruction for bitwise exclusive OR of two values on the stack.
     * The top of the stack must contain two values of the same integral type.
     * If either of values is of type {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are not the same type.
     */
    CodeBlockBuilder<Result> xor();

    /**
     * Writes appropriate instruction for the left shift.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are of invalid types.
     */
    CodeBlockBuilder<Result> shiftLeft();

    /**
     * Writes appropriate instruction for the signed right shift.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are of invalid types.
     */
    CodeBlockBuilder<Result> signedShiftRight();

    /**
     * Writes appropriate instruction for the unsigned right shift.
     *
     * @throws IllegalStateException if the stack has less than two values or
     *    the values are of invalid types.
     */
    CodeBlockBuilder<Result> unsignedShiftRight();

    /* STACK */

    /**
     * Pushes reference to this class on the stack.
     * The method must be an instance method for this operation.
     */
    CodeBlockBuilder<Result> pushThis();

    /**
     * Writes an instruction that pushes literal value on the stack.
     *
     * @param value  the value to be loaded on the stack. Must be one of
     *     {@code null}, {@link Byte}, {@link Character}, {@link Short},
     *     {@link Integer}, {@link Long}, {@link Float}, {@link Double},
     *     {@link String} or {@link JvmClassOrArray}.
     */
    CodeBlockBuilder<Result> push(@Nullable Object value);

    /** Writes an instruction that pops the topmost value from the stack. */
    CodeBlockBuilder<Result> pop();

    /** Writes an instruction that duplicates the topmost value from the stack. */
    CodeBlockBuilder<Result> dup();

    /* LOCALS */

    /**
     * Adds new local variable to the scope of the method but does not assign it.
     * This method does not write any instructions but reserves a slot for
     * the local and allows it to be assigned later by
     * {@link #store(me.sbasalaev.tybyco.builders.LocalVar) }.
     * This is most useful for variables with discontinuous assigned range.
     */
    CodeBlockBuilder<Result> open(LocalVar local);

    /**
     * Adds new local variable to the scope and stores top of the stack in it.
     */
    CodeBlockBuilder<Result> openAndStore(LocalVar local);

    /**
     * Writes instruction that stores top of the stack in given local variable.
     * The variable must be in scope of this method.
     */
    CodeBlockBuilder<Result> store(LocalVar local);

    /**
     * Writes instruction that loads value of the local variable on the stack.
     * The variable must be in the scope and assigned in this method.
     */
    CodeBlockBuilder<Result> load(LocalVar local);

    /**
     * Writes instruction that changes value of given local variable by given increment.
     * The variable must be {@code int}, in the scope and assigned in this method.
     */
    CodeBlockBuilder<Result> iinc(LocalVar local, short increment);

    /**
     * Marks the local variable as unassigned.
     * This method ends the continuous range of instructions where the variable
     * is defined but leaves it in the scope of the method.
     */
    CodeBlockBuilder<Result> setUnassigned(LocalVar local);

    /**
     * Marks the local variable as assigned.
     * This method starts new continuous range of instructions where the
     * variable is defined.
     */
    CodeBlockBuilder<Result> setAssigned(LocalVar local);

    /**
     * Removes given variable from the scope of this method.
     * This method frees the slot occupied by the variable.
     * If the variable is not anonymous it also adds entries for
     * {@code LocalVariableTable}, {@code LocalVariableTypeTable} and type
     * annotations on the type of the variable.
     */
    CodeBlockBuilder<Result> close(LocalVar local);

    /* CLASSES AND ARRAYS */

    /**
     * Writes instructions to allocate new object and immediately duplicate it.
     * Also writes type annotations on the type of the {@code new} expression.
     *
     * @param classType type of the allocated object.
     */
    CodeBlockBuilder<Result> newInstanceAndDup(JvmClassType classType);

    /**
     * Writes an instruction to allocate new array of given type.
     * Also writes type annotations on the type of {@code new} expression.
     *
     * @param arrayType the (possibly annotated) type of the allocated array.
     */
    CodeBlockBuilder<Result> newArray(JvmArrayType arrayType);

    /**
     * Writes an instruction to allocate new array of given type and dimensions.
     * Also writes type annotations on the type of {@code new} expression.
     *
     * @param arrayType the (possibly annotated) type of the allocated array.
     * @param dimensions the number of initialized dimensions of the array
     *     being allocated. Must be positive.
     */
    CodeBlockBuilder<Result> newArray(JvmArrayType arrayType, @Positive int dimensions);

    /** Writes appropriate {@code *aload} instruction. */
    CodeBlockBuilder<Result> arrayLoad(JvmType componentType);

    /** Writes appropriate {@code *astore} instruction. */
    CodeBlockBuilder<Result> arrayStore(JvmType componentType);

    /** Writes {@code arraylength} instruction. */
    CodeBlockBuilder<Result> arrayLength();

    /* FIELDS */

    /** Writes {@code getstatic} instruction. */
    CodeBlockBuilder<Result> getStaticField(JvmClass owner, String name, JvmType type);

    /** Writes {@code putstatic} instruction. */
    CodeBlockBuilder<Result> putStaticField(JvmClass owner, String name, JvmType type);

    /** Writes {@code getfield} instruction. */
    CodeBlockBuilder<Result> getInstanceField(JvmClass owner, String name, JvmType type);

    /** Writes {@code putfield} instruction. */
    CodeBlockBuilder<Result> putInstanceField(JvmClass owner, String name, JvmType type);

    /* METHODS */

    /** Writes call to a constructor of a class. */
    CodeBlockBuilder<Result> invokeConstructor(JvmClass owner, JvmMethodDescriptor descriptor);
    
    /** Writes call to a static method of a class or interface. */
    CodeBlockBuilder<Result> invokeStatic(JvmClass owner, String name, JvmMethodDescriptor descriptor);

    /** Writes call to an instance method of a class or interface. */
    CodeBlockBuilder<Result> invokeVirtual(JvmClass owner, String name, JvmMethodDescriptor descriptor);

    /** Writes call to an instance method of a superclass or superinterface. */
    CodeBlockBuilder<Result> invokeSuper(JvmClass owner, String name, JvmMethodDescriptor descriptor);

    /* TYPE ANNOTATIONS WITH NO CLEAR INSTRUCTION TO APPLY */

    /**
     * Writes annotations for type arguments of a constructor invocation expression.
     * This method call should follow the first instruction that denotes the expression.
     *
     * @param typeArguments type arguments to the expression. Must not be wildcards.
     */
    CodeBlockBuilder<Result> typeArgumentsForConstructor(List<? extends JvmTypeArgument> typeArguments);

    /**
     * Writes annotations for type arguments of a method invocation expression.
     * This method call should follow the first instruction that denotes the expression.
     *
     * @param typeArguments type arguments to the expression. Must not be wildcards.
     */
    CodeBlockBuilder<Result> typeArgumentsForMethod(List<? extends JvmTypeArgument> typeArguments);

    /**
     * Writes annotations for type arguments of a constructor reference expression.
     * This method call should follow the first instruction that denotes the expression.
     *
     * @param typeArguments type arguments to the expression. Must not be wildcards.
     */
    CodeBlockBuilder<Result> typeArgumentsForMethodReference(List<? extends JvmTypeArgument> typeArguments);


    /**
     * Writes annotations for type arguments of a method reference expression.
     * This method call should follow the first instruction that denotes the expression.
     *
     * @param typeArguments type arguments to the expression. Must not be wildcards.
     */
    CodeBlockBuilder<Result> typeArgumentsForConstructorReference(List<? extends JvmTypeArgument> typeArguments);

    /* TYPE CASTS */

    /**
     * Writes instructions to convert the number on the stack to given type.
     * Also writes type annotations on the type of the cast expression.
     * The effect is exactly the same as the corresponding numeric type cast
     * in Java, e.g. if the top of the stack is {@code n} then
     * <pre>numericCast(JvmPrimitiveType.BYTE)</pre>
     * is equivalent to Java code
     * <pre>(byte) n</pre>
     * The method may end up writing no instructions (e.g. when {@code n} is
     * already a {@code byte}) or writing more than one (e.g. {@code D2I, I2B}
     * when {@code n} is {@code double}).
     *
     * @throws IllegalArgumentException if {@code toType} is not a numeric type.
     * @throws IllegalStateException if the type on the stack is not a numeric one.
     */
    CodeBlockBuilder<Result> numericCast(JvmPrimitiveType toType);

    /**
     * Writes {@code instanceof} instruction.
     * Also writes type annotations on the type of {@code instanceof} expression.
     */
    CodeBlockBuilder<Result> instanceOf(JvmClassOrArrayType classType);

    /**
     * Writes {@code checkcast} instruction.
     * Also writes type annotations on the type of the cast expression.
     */
    default CodeBlockBuilder<Result> checkCast(JvmClassOrArrayType classType) {
        return checkCast(list(classType));
    }

    /**
     * Writes one or more {@code checkcast} instructions.
     * Also writes type annotations on the types of the cast expression.
     */
    default CodeBlockBuilder<Result> checkCast(JvmClassOrArrayType classType, JvmClassOrArrayType... interfaceTypes) {
        return checkCast(concat(list(classType), list(interfaceTypes)));
    }

    /**
     * Writes one or more {@code checkcast} instructions.
     * Also writes type annotations on the types of the cast expression.
     */
    CodeBlockBuilder<Result> checkCast(List<JvmClassOrArrayType> types);

    /* RETURNING */

    /**
     * Writes {@code return} instruction and ends the current block of code.
     * The type of return instruction is determined by the type of the value
     * on the stack.
     */
    CodeBuilder<Result> valueReturn();

    /**
     * Writes {@code return} instruction and ends the current block of code.
     */
    CodeBuilder<Result> voidReturn();

    /* JUMPS */

    /**
     * Writes unconditional jump and ends the current block of code.
     *
     * @param target target instruction to jump to.
     */
    CodeBuilder<Result> jump(Target target);

    /**
     * Writes conditional jump on {@code boolean} value.
     * The jump occures if given {@code boolean} value is on the stack.
     *
     * @param value the expected value.
     * @param target target instruction to jump to.
     */
    CodeBlockBuilder<Result> jumpIf(boolean value, Target target);

    /**
     * Writes conditional jump on comparison.
     * The jump occures if the comparison of two values on the stack yields
     * given comparison result. The two values must be of the same
     * {@link TypeKind}. If either of values is {@code byte}, {@code char} or
     * {@code short} it is promoted to {@code int}. If values are {@code boolean}
     * or of reference types then only {@link Cmp#EQ} and {@link Cmp#NE} are allowed.
     *
     * @param cmp the expected result of comparison.
     * @param target target instruction to jump to.
     */
    CodeBlockBuilder<Result> jumpIfCmp(Cmp cmp, Target target);

    /**
     * Writes conditional jump on comparison with integer zero.
     * The jump occures if the comparison of the value on the stack with zero
     * yields given result. The value on the stack must be one of {@code byte},
     * {@code char}, {@code short} or {@code int}.
     *
     * @param cmp the expected result of comparison with zero.
     * @param target target instruction to jump to.
     */
    CodeBlockBuilder<Result> jumpIfCmpZero(Cmp cmp, Target target);

    /**
     * Writes conditional jump on comparison with {@code null}.
     * The jump occures if the comparison of the value on the stack with
     * {@code null} yields given comparison result. The value on the stack
     * must be of reference type. Only {@link Cmp#EQ} and {@link Cmp#NE}
     * are allowed as comparison results.
     *
     * @param cmp the expected result of comparison with {@code null}, either
     *     {@link Cmp#EQ} or {@link Cmp#NE}.
     * @param target target instruction to jump to.
     */
    CodeBlockBuilder<Result> jumpIfCmpNull(Cmp cmp, Target target);

    /**
     * Writes switch instruction and ends the current block of code.
     * The switch instruction takes {@code int} key from the stack and jumps to
     * the target that matches the key or to the {@code dfltTarget} if none match.
     * The method automatically chooses between {@code TABLESWITCH} and
     * {@code LOOKUPSWITCH} instructions choosing the one that is shorter.
     *
     * @param keyTargets map from integer keys to target instructions.
     * @param dfltTarget the target if no keys match.
     */
    CodeBuilder<Result> doSwitch(Map<Integer, Target> keyTargets, Target dfltTarget);

    /* EXCEPTION HANDLING */

    /**
     * Writes {@code athrow} instruction and ends the current block of code.
     * {@code athrow} throws exception pushed on the top of the stack.
     */
    CodeBuilder<Result> athrow();

    /**
     * Marks current position as the start of try/catch block for given exception types.
     * This is a convenient variadic form of
     * {@link #atTryCatch(me.sbasalaev.tybyco.builders.Target, me.sbasalaev.tybyco.builders.Target, me.sbasalaev.collection.List) }
     * and has the same semantics.
     *
     * @param end            target position at which this try block ends.
     * @param catchHandler   target position at which catch block starts.
     * @param exceptionTypes exceptions types that are caught by this try block.
     */
    default CodeBlockBuilder<Result> atTryCatch(Target end, Target catchHandler,
            JvmClassType... exceptionTypes) {
        return atTryCatch(end, catchHandler, list(exceptionTypes));
    }

    /**
     * Marks current position as the start of try/catch block for given exception types.
     * The {@code end} and {@code catchHandler} must mark target instructions
     * further in the code, i.e. not already visited by
     * {@link #at(me.sbasalaev.tybyco.builders.Target) }. If no exceptions are
     * given this block catches all exceptions. Such blocks are used to implement
     * finally. This method also writes type annotations for the exception types.
     *
     * @param end            target position at which this try block ends.
     * @param catchHandler   target position at which catch block starts.
     * @param exceptionTypes exceptions types that are caught by this try block.
     */
    CodeBlockBuilder<Result> atTryCatch(Target end, Target catchHandler,
            List<JvmClassType> exceptionTypes);

    /* VARIOUS INSTRUCTIONS */

    /** Writes {@code nop} instruction.
     * {@code nop} does nothing.
     */
    CodeBlockBuilder<Result> nop();

    /** Writes {@code monitorenter} instruction. */
    CodeBlockBuilder<Result> monitorEnter();

    /** Writes {@code monitorexit} instruction. */
    CodeBlockBuilder<Result> monitorExit();

    /**
     * Marks source line number corresponding to the subsequent instructions.
     * Does nothing if the current line number coincides with given number.
     */
    CodeBlockBuilder<Result> lineNumber(@Positive int number);
}
