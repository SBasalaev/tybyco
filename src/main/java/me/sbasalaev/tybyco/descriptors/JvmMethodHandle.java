/*
 * The MIT License
 *
 * Copyright 2024 Sergey Basalaev
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
import me.sbasalaev.tybyco.builders.CodeBlockBuilder;

/**
 * Reference to a method, constructor, field getter or setter.
 * The kinds of this class are not in one-to-one correspondence with the tags
 * of {@code CONSTANT_MethodHandle_info} structure. Instead, the correct tag is
 * chosen by a {@link CodeBlockBuilder} based on a Java version, class kind and
 * implied semantics.
 *
 * @author Sergey Basalaev
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.4.8">JVMS 4.4.8 The CONSTANT_MethodHandle_info Structure</a>
 */
public final class JvmMethodHandle {

    /** Kind of a {@code JvmHandle}. */
    public enum Kind {
        /** Instance field getter reference. */
        INSTANCE_FIELD_GETTER,
        /** Static field getter reference. */
        STATIC_FIELD_GETTER,
        /** Instance field setter reference. */
        INSTANCE_FIELD_SETTER,
        /** Static field setter reference. */
        STATIC_FIELD_SETTER,
        /** Reference to a static method of a class or interface. */
        STATIC_METHOD,
        /** Reference to a non-private instance method of a class or interface. */
        VIRTUAL_METHOD,
        /** Reference to a private instance method of a class or interface. */
        PRIVATE_METHOD,
        /** Reference to a method of a superclass or direct superinterface. */
        SUPER_METHOD,
        /** Reference to a constructor. */
        CONSTRUCTOR
    }

    /** Returns new handle for the getter of given instance field. */
    public static JvmMethodHandle ofInstanceFieldGetter(JvmClass owner, String name, JvmType type) {
        return new JvmMethodHandle(Kind.INSTANCE_FIELD_GETTER, owner, name, type);
    }

    /** Returns new handle for the setter of given instance field. */
    public static JvmMethodHandle ofInstanceFieldSetter(JvmClass owner, String name, JvmType type) {
        return new JvmMethodHandle(Kind.INSTANCE_FIELD_SETTER, owner, name, type);
    }

    /** Returns new handle for the getter of given static field. */
    public static JvmMethodHandle ofStaticFieldGetter(JvmClass owner, String name, JvmType type) {
        return new JvmMethodHandle(Kind.STATIC_FIELD_GETTER, owner, name, type);
    }

    /** Returns new handle for the setter of given static field. */
    public static JvmMethodHandle ofStaticFieldSetter(JvmClass owner, String name, JvmType type) {
        return new JvmMethodHandle(Kind.STATIC_FIELD_SETTER, owner, name, type);
    }

    /** Returns new handle for given constructor. */
    public static JvmMethodHandle ofConstructor(JvmClass owner, JvmMethodDescriptor descriptor) {
        return new JvmMethodHandle(Kind.CONSTRUCTOR, owner, "<init>", descriptor);
    }

    /** Returns new handle for given non-private instance method. */
    public static JvmMethodHandle ofVirtualMethod(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        return new JvmMethodHandle(Kind.VIRTUAL_METHOD, owner, name, descriptor);
    }

    /** Returns new handle for given private instance method. */
    public static JvmMethodHandle ofPrivateMethod(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        return new JvmMethodHandle(Kind.PRIVATE_METHOD, owner, name, descriptor);
    }

    /** Returns new handle for given superclass instance method. */
    public static JvmMethodHandle ofSuperMethod(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        return new JvmMethodHandle(Kind.SUPER_METHOD, owner, name, descriptor);
    }

    /** Returns new handle for given static method. */
    public static JvmMethodHandle ofStaticMethod(JvmClass owner, String name, JvmMethodDescriptor descriptor) {
        return new JvmMethodHandle(Kind.STATIC_METHOD, owner, name, descriptor);
    }

    private final Kind kind;
    private final JvmClass owner;
    private final String name;
    private final JvmDescriptor descriptor;

    private JvmMethodHandle(Kind kind, JvmClass owner, String name, JvmDescriptor descriptor) {
        this.kind = kind;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    public Kind kind() {
        return this.kind;
    }

    /** Owner class of the referenced member. */
    public JvmClass owner() {
        return this.owner;
    }

    /**
     * Name of the referenced member.
     * Returns {@code <init>} if this handle's kind is {@link Kind#CONSTRUCTOR}.
     */
    public String name() {
        return this.name;
    }

    /**
     * Descriptor of the referenced member.
     * Returns {@link JvmType} if this is a handle to a field,
     * {@link JvmMethodType} if this is a handle to a method or constructor.
     */
    public JvmDescriptor descriptor() {
        return this.descriptor;
    }

    /** Whether given object equals this handle. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmMethodHandle handle
            && this.kind == handle.kind
            && this.owner.equals(handle.owner)
            && this.name.equals(handle.name)
            && this.descriptor.equals(handle.descriptor);
    }

    /** The hash code for this handle. */
    @Override
    public int hashCode() {
        return Objects.hash(kind, owner, name, descriptor);
    }

    @Override
    public String toString() {
        if (kind == Kind.CONSTRUCTOR) {
            return "JvmHandle{" + "kind=" + kind + ", owner=" + owner + ", descriptor=" + descriptor + '}';
        }
        return "JvmHandle{" + "kind=" + kind + ", owner=" + owner + ", name=" + name + ", descriptor=" + descriptor + '}';
    }
}
