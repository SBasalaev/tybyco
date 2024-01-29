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

/**
 * Builder of the code section of a method or a constructor.
 * To enter a new code block the {@link #at(me.sbasalaev.tybyco.builders.Target) }
 * method must be called with a {@link Target} that is already visited by jump.
 *
 * @author Sergey Basalaev
 */
public interface CodeBuilder<Result> {

    /**
     * Marks current position in the code as the target.
     * If the code block has ended, i.e. one of {@code jump}, {@code return} or
     * {@code athrow} instructions were called, this method starts a new code
     * block. In this case the stack is restored to the state it had when any
     * jump instruction was visited for given target. In particular, if this is
     * a target for {@code catch} block the stack has a single {@link Throwable}
     * entry.
     */
    CodeBlockBuilder<Result> at(Target target);

    /**
     * Finishes writing the code and returns the enclosing builder.
     * This method also closes any unclosed local variables.
     */
    Result end();
}
