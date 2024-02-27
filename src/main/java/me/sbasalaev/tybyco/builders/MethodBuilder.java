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

import static me.sbasalaev.API.list;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.descriptors.JvmAnnotation;
import me.sbasalaev.tybyco.descriptors.JvmClassType;
import me.sbasalaev.tybyco.descriptors.JvmTypeVariable;
import me.sbasalaev.tybyco.descriptors.Mod;

/**
 * Builder of a method or constuctor.
 *
 * @param <Result> type of the class builder.
 * @author Sergey Basalaev
 */
public interface MethodBuilder<Result>
        extends GenericElementBuilder<MethodBuilder<Result>, Result> {

    /** Puts annotation on the receiver type (this) of the method. */
    MethodBuilder<Result> receiverAnnotation(JvmAnnotation anno);

    /** Adds thrown exception to the signature of this method. */
    MethodBuilder<Result> exception(JvmClassType exceptionType);

    /** Adds thrown exception to the signature of this method. */
    MethodBuilder<Result> exception(JvmTypeVariable exceptionType);

    /**
     * Adds formal parameter name to this method and returns a builder to visit its annotations.
     */
    ParameterBuilder<MethodBuilder<Result>> parameter(String name, Traversable<Mod> modifiers);

    /**
     * Adds formal parameter name to this method and returns a builder to visit its annotations.
     *
     * @param name the name of the parameter.
     * @param modifiers the modifiers among {@link Mod#FINAL}, {@link Mod#MANDATED}
     *     and {@link Mod#SYNTHETIC}.
     */
    default ParameterBuilder<MethodBuilder<Result>> parameter(String name, Mod... modifiers) {
        return parameter(name, list(modifiers));
    }

    /**
     * Builds code for this method or constructor.
     * 
     * @param parameterNames
     *     names of method parameters that will be assigned to local variables.
     *     Their number must match number of types in the method descriptor.
     *     The names may differ from the ones given in
     *     {@link #parameter(java.lang.String, me.sbasalaev.tybyco.descriptors.Mod...) parameter()}
     *     method and may be empty strings if the corresponding local is to
     *     remain anonymous.
     */
    CodeBlockBuilder<MethodBuilder<Result>> code(List<String> parameterNames);

    /**
     * Builds code for this method or constructor.
     *
     * @param parameterNames
     *     names of method parameters that will be assigned to local variables.
     *     Their number must match number of types in the method descriptor.
     *     The names may differ from the ones given in
     *     {@link #parameter(java.lang.String, me.sbasalaev.tybyco.descriptors.Mod...) parameter()}
     *     method and may be empty strings if the corresponding local is to
     *     remain anonymous.
     */
    default CodeBlockBuilder<MethodBuilder<Result>> code(String... parameterNames) {
        return code(list(parameterNames));
    }
}
