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
package me.sbasalaev.tybyco.descriptors;

import static me.sbasalaev.API.set;
import me.sbasalaev.Require;
import me.sbasalaev.collection.MutableSet;
import me.sbasalaev.collection.Traversable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Reference to a class or interface.
 * {@code JvmClass} is initialized with the qualified name with dot delimiters.
 * The {@link #binaryName() } method returns the class name for JVM with
 * dots replaced by slashes.
 *
 * @author Sergey Basalaev
 */
public sealed abstract class JvmClass
    implements JvmClassOrArray
    permits JvmTopLevelClass, JvmNestedClass {

    /* CONSTRUCTORS AND BUILDERS */

    private static final String STR_PACKAGE_INFO = "package-info";
    private static final String STR_MODULE_INFO = "module-info";

    private static ClassKind kindOf(Class<?> clazz) {
        if (clazz.isHidden()) throw new IllegalArgumentException("Hidden class: " + clazz.getName());
        if (clazz.isArray()) throw new IllegalArgumentException("Array class: " + clazz.getName());
        if (clazz.isPrimitive()) throw new IllegalArgumentException("Primitive class: " + clazz.getName());
        if (clazz.isRecord()) return ClassKind.RECORD;
        if (clazz.isEnum()) return ClassKind.ENUM;
        if (clazz.isAnnotation()) return ClassKind.ANNOTATION;
        if (clazz.getSimpleName().equals(STR_PACKAGE_INFO)) return ClassKind.PACKAGE;
        if (clazz.getSimpleName().equals(STR_MODULE_INFO)) return ClassKind.MODULE;
        if (clazz.isInterface()) return ClassKind.INTERFACE;
        return ClassKind.CLASS;
    }

    /**
     * Returns class name corresponding to given Java class.
     * 
     * @throws IllegalArgumentException if given class is an array, hidden or primitive class.
     * @see JvmTypeOrVoid#ofClass(java.lang.Class) 
     */
    public static JvmClass of(Class<?> clazz) {
        ClassKind kind = kindOf(clazz);
        Class<?> enclosingJvmClass = clazz.getEnclosingClass();
        if (enclosingJvmClass == null) {
            return new JvmTopLevelClass(kind, clazz.getName());
        }
        JvmClass enclosing = of(enclosingJvmClass);
        MutableSet<Mod> mods = MutableSet.empty();
        for (var access : clazz.accessFlags()) {
            switch (access) {
                case ABSTRACT   -> mods.add(Mod.ABSTRACT);
                case FINAL      -> mods.add(Mod.FINAL);
                case PRIVATE    -> mods.add(Mod.PRIVATE);
                case PROTECTED  -> mods.add(Mod.PROTECTED);
                case PUBLIC     -> mods.add(Mod.PUBLIC);
                case STATIC     -> mods.add(Mod.STATIC);
                case SYNTHETIC  -> mods.add(Mod.SYNTHETIC);
            }
        }
        if (clazz.isMemberClass()) {
            return enclosing.newMember(kind, clazz.getSimpleName(), mods);
        } else {
            return enclosing.newNonMember(kind, mods, clazz.getName(), clazz.getSimpleName());
        }
    }

    /**
     * Returns the member class of this class.
     * The qualified name of the member is formed as
     * <pre>this.qualifiedName + '$' + simpleName</pre>
     */
    public JvmNestedClass newMember(ClassKind kind, String simpleName, Traversable<Mod> modifiers) {
        switch (kind) {
            case MODULE, PACKAGE -> throw new IllegalArgumentException("Member class can not be " + kind.keyword());
        }
        verifyModifiers(modifiers);
        String qname = qualifiedName + '$' + simpleName;
        return new JvmNestedClass(this, kind, modifiers, qname, simpleName, true);
    }

    /**
     * Returns the member class of this class.
     * The qualified name of the member is formed as
     * <pre>this.qualifiedName + '$' + simpleName</pre>
     */
    public JvmNestedClass newMember(ClassKind kind, String simpleName, Mod... modifiers) {
        return newMember(kind, simpleName, set(modifiers));
    }

    /**
     * Returns the local class nested in this class.
     * Integer {@code index} is used to distinguish between different nested
     * classes with the same simple name. The qualified name of the nested class
     * is formed as
     * <pre>qualifiedName + '$' + index + simpleName</pre>
     */
    public JvmNestedClass newLocal(ClassKind kind, int index, String simpleName, Traversable<Mod> modifiers) {
        Require.nonNegative(index, "index");
        String qname = qualifiedName + '$' + index + simpleName;
        return newNonMember(kind, modifiers, qname, simpleName);
    }

    /**
     * Returns the local class nested in this class.
     * Integer {@code index} is used to distinguish between different nested
     * classes with the same simple name. The qualified name of the nested class
     * is formed as
     * <pre>qualifiedName + '$' + index + simpleName</pre>
     */
    public JvmNestedClass newLocal(ClassKind kind, int index, String simpleName, Mod... modifiers) {
        return newLocal(kind, index, simpleName, set(modifiers));
    }

    /**
     * Returns the anonymous class nested in this class.
     * Integer {@code index} is used to distinguish between different anonymous
     * nested classes. The qualified name of the anonymous nested class is
     * formed as
     * <pre>qualifiedName + '$' + index</pre>
     */
    public JvmNestedClass newAnonymous(ClassKind kind, int index, Traversable<Mod> modifiers) {
        Require.nonNegative(index, "index");
        String qname = qualifiedName + '$' + index;
        return newNonMember(kind, modifiers, qname, "");
    }

    /**
     * Returns the anonymous class nested in this class.
     * Integer {@code index} is used to distinguish between different anonymous
     * nested classes. The qualified name of the anonymous nested class is
     * formed as
     * <pre>qualifiedName + '$' + index</pre>
     */
    public JvmNestedClass newAnonymous(ClassKind kind, int index, Mod... modifiers) {
        return newAnonymous(kind, index, set(modifiers));
    }

    private JvmNestedClass newNonMember(ClassKind kind,
            Traversable<Mod> modifiers, String qualifiedName, String simpleName) {
        switch (kind) {
            case MODULE, PACKAGE, ANNOTATION -> throw new IllegalArgumentException("Local class can not be " + kind.keyword());
        }
        verifyModifiers(modifiers);
        return new JvmNestedClass(this, kind, modifiers, qualifiedName, simpleName, false);
    }

    private static void verifyModifiers(Traversable<Mod> modifiers) {
        for (var modifier : modifiers) {
            switch (modifier) {
                case PUBLIC, PROTECTED, PRIVATE, STATIC, FINAL, ABSTRACT, SYNTHETIC -> { /* ok */ }
                default -> throw new IllegalArgumentException(modifier + " is not valid for a nested class");
            }
        }
    }

    /** Class name for {@code java.lang.Class}. */
    public static final JvmTopLevelClass JVM_CLASS = new JvmTopLevelClass(ClassKind.CLASS, "java.lang.Class");

    /** Class name for {@code java.lang.Object}. */
    public static final JvmTopLevelClass JVM_OBJECT = new JvmTopLevelClass(ClassKind.CLASS, "java.lang.Object");

    /** Class name for {@code java.lang.String}. */
    public static final JvmTopLevelClass JVM_STRING = new JvmTopLevelClass(ClassKind.CLASS, "java.lang.String");

    /** Class name for module descriptor class. */
    public static final JvmTopLevelClass MODULE_INFO = new JvmTopLevelClass(ClassKind.MODULE, STR_MODULE_INFO);

    /** Class name for the package-info descriptor with given package name. */
    public static JvmTopLevelClass packageInfo(String packageName) {
        return new JvmTopLevelClass(ClassKind.PACKAGE, packageName + '.' + STR_PACKAGE_INFO);
    }

    /* INSTANCE */

    private final ClassKind kind;
    private final String qualifiedName;

    /** Non-public constructor for subclasses. */
    JvmClass(ClassKind kind, String qualifiedName) {
        if (qualifiedName.indexOf('/') >= 0) {
            throw new IllegalArgumentException("Unexpected character /");
        }
        this.kind = kind;
        this.qualifiedName = qualifiedName;
    }

    /** Kind of this class. */
    public ClassKind classKind() {
        return kind;
    }

    /** Qualified name of this class. */
    public String qualifiedName() {
        return qualifiedName;
    }

    @Override
    public String binaryName() {
        return qualifiedName.replace('.', '/');
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        return obj instanceof JvmClass clz
            && this.qualifiedName.equals(clz.qualifiedName);
    }

    @Override
    public int hashCode() {
        return qualifiedName.hashCode();
    }
}
