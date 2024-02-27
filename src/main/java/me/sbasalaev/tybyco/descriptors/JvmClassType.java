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

import java.util.Objects;
import static me.sbasalaev.API.append;
import static me.sbasalaev.API.none;
import static me.sbasalaev.API.some;
import me.sbasalaev.Opt;
import me.sbasalaev.collection.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Parameterized class type.
 *
 * @author Sergey Basalaev
 */
public final class JvmClassType extends JvmClassOrArrayType {

    private final Opt<JvmClassType> enclosing;
    private final JvmClass className;
    private final List<JvmTypeArgument> typeArguments;

    private JvmClassType(Opt<JvmClassType> enclosing,
                         JvmClass className,
                         List<JvmTypeArgument> typeArguments,
                         List<JvmAnnotation> annotations) {
        super(annotations);
        if (className instanceof JvmNestedClass nested && nested.isInstanceMember() && enclosing.isEmpty()) {
            throw new IllegalArgumentException("instance member class requires enclosing class type");
        }
        this.enclosing = enclosing;
        this.className = className;
        this.typeArguments = typeArguments;
    }


    /** Constructs new class type for the non-member class with given type arguments and annotations. */
    public JvmClassType(JvmClass className,
                        List<JvmTypeArgument> typeArguments,
                        List<JvmAnnotation> annotations) {
        this(none(), className, typeArguments, annotations);
    }

    /** Constructs new class type for the non-member class with given type arguments. */
    public JvmClassType(JvmClass className, List<JvmTypeArgument> typeArguments) {
        this(none(), className, typeArguments, List.empty());
    }

    /** Constructs new class type for the non-member class with given type arguments. */
    public JvmClassType(JvmClass className, JvmTypeArgument... typeArguments) {
        this(none(), className, List.of(typeArguments), List.empty());
    }

    /** Constructs new class type for the non-member class without type arguments. */
    public JvmClassType(JvmClass className) {
        this(none(), className, List.empty(), List.empty());
    }

    /** Constructs new class type for the member of this class with given type arguments and annotations. */
    public JvmClassType newMemberType(JvmNestedClass member,
                                      List<JvmTypeArgument> typeArguments,
                                      List<JvmAnnotation> annotations) {
        if (!member.isInstanceMember()) {
            throw new IllegalArgumentException("argument is not an instance member class");
        }
        return new JvmClassType(some(this), member, typeArguments, annotations);
    }

    /** Constructs new class type for the member of this class with given type arguments. */
    public JvmClassType newMemberType(JvmNestedClass member, List<JvmTypeArgument> typeArguments) {
        return newMemberType(member, typeArguments, List.empty());
    }

    /** Constructs new class type for the member of this class with given type arguments. */
    public JvmClassType newMemberType(JvmNestedClass member, JvmTypeArgument... typeArguments) {
        return newMemberType(member, List.of(typeArguments), List.empty());
    }

    /** Constructs new class type for the member of this class without type arguments. */
    public JvmClassType newMemberType(JvmNestedClass member) {
        return newMemberType(member, List.empty(), List.empty());
    }

    @Override
    public JvmClass className() {
        return className;
    }

    /** Type arguments applied to this class. */
    public List<JvmTypeArgument> typeArguments() {
        return typeArguments;
    }

    /** Enclosing class type of an instance member type. */
    public Opt<JvmClassType> enclosing() {
        return enclosing;
    }

    @Override
    public boolean isGeneric() {
        return enclosing.exists(t -> t.typeArguments.nonEmpty()) || typeArguments.nonEmpty();
    }

    @Override
    public String nonGenericString() {
        return "L" + className.binaryName() + ";";
    }

    @Override
    public String genericString() {
        var sb = new StringBuilder();
        return buildSignature(sb).append(';').toString();
    }

    private StringBuilder buildSignature(StringBuilder sb) {
        enclosing.matchDo(
            (JvmClassType ct) -> ct.buildSignature(sb)
                    .append('.').append(((JvmNestedClass) className).simpleName()),
            () -> sb.append('L').append(className.binaryName())
        );
        if (typeArguments.nonEmpty()) {
            sb.append('<');
            for (var argument : typeArguments) {
                sb.append(argument.genericString());
            }
            sb.append('>');
        }
        return sb;
    }

    @Override
    public JvmClassType erasure() {
        if (notGeneric() && notDeeplyAnnotated()) return this;
        return new JvmClassType(enclosing.mapped(JvmClassType::erasure), className, List.empty(), List.empty());
    }

    @Override
    public boolean isDeeplyAnnotated() {
        return annotations().nonEmpty()
            || typeArguments.exists(JvmAnnotated::isDeeplyAnnotated)
            || enclosing.exists(JvmAnnotated::isDeeplyAnnotated);
    }

    @Override
    public JvmClassType annotated(JvmAnnotation anno) {
        return new JvmClassType(enclosing, className, typeArguments,
            append(annotations(), anno));
    }

    @Override
    public JvmClassType unannotated() {
        if (notDeeplyAnnotated()) return this;
        return new JvmClassType(enclosing.mapped(JvmClassType::unannotated), className,
            typeArguments.mapped(JvmTypeArgument::unannotated), List.empty());
    }

    @Override
    public String toString() {
        if (typeArguments.isEmpty()) {
            return className.toString();
        }
        return className + "<" + typeArguments.join(",") + ">";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmClassType ct
            && this.enclosing.equals(ct.enclosing)
            && this.className.equals(ct.className)
            && this.typeArguments.equals(ct.typeArguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enclosing, className, typeArguments);
    }
}
