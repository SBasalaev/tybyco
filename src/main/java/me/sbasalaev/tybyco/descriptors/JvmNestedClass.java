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

import me.sbasalaev.collection.Set;
import me.sbasalaev.collection.Traversable;

/**
 * Name and attributes for a nested, member, local or anonymous class.
 * Nested classes are treated specially in two contexts of the classfile.
 * First, the signature of a member class includes the signature of its
 * enclosing class. Second, the classfile stores simple names and attributes
 * of all the nested classes it references in the {@code InnerClasses} attribute.
 * To automate the generation of the corresponding attributes all the information
 * is gathered in this class.
 * <p>
 * To make a reference for a nested class first make a reference to its
 * enclosing class and then call the appropriate
 * {@link #newMember(me.sbasalaev.tybyco.descriptors.ClassKind, java.lang.String, me.sbasalaev.tybyco.descriptors.Mod...) newMember()},
 * {@link #newLocal(me.sbasalaev.tybyco.descriptors.ClassKind, int, java.lang.String, me.sbasalaev.tybyco.descriptors.Mod...) newLocal()}
 * or {@link #newAnonymous(me.sbasalaev.tybyco.descriptors.ClassKind, int, me.sbasalaev.tybyco.descriptors.Mod...) newAnonymous()}.
 *
 * @author Sergey Basalaev
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.7.6">JVMS 4.7.6 The {@code InnerClasses} Attribute</a>
 */
public final class JvmNestedClass extends JvmClass {

    private final JvmClass enclosing;
    private final String simpleName;
    private final boolean isMember;
    private final Set<Mod> modifiers;

    JvmNestedClass(JvmClass enclosing,
                   ClassKind kind,
                   Traversable<Mod> modifiers,
                   String qualifiedName,
                   String simpleName,
                   boolean isMember) {
        super(kind, qualifiedName);
        this.enclosing = enclosing;
        this.modifiers = modifiers.toSet();
        this.simpleName = simpleName;
        this.isMember = isMember;
    }

    /** Class that encloses this class. */
    public JvmClass enclosingClass() {
        return enclosing;
    }

    /**
     * Simple name of the class.
     * Empty string if this class is anonymous.
     */
    public String simpleName() {
        return simpleName;
    }

    /** Modifiers of this class. */
    public Set<Mod> modifiers() {
        return modifiers;
    }

    /**
     * Whether this is a member class.
     * @see Class#isMemberClass()
     */
    public boolean isMember() {
        return isMember;
    }

    /** Whether this is an instance member class. */
    public boolean isInstanceMember() {
        return isMember && !modifiers.contains(Mod.STATIC);
    }

    /**
     * Whether this class is anonymous.
     * @see Class#isAnonymousClass()
     */
    public boolean isAnonymous() {
        return !isMember && simpleName.isEmpty();
    }

    /**
     * Whether this class is local.
     * @see Class#isLocalClass()
     */
    public boolean isLocal() {
        return !isMember && !simpleName.isEmpty();
    }
}
