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
package me.sbasalaev.tybyco;

import java.util.EnumSet;
import static me.sbasalaev.API.chain;
import static me.sbasalaev.API.list;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.JvmNestedClass;
import me.sbasalaev.tybyco.descriptors.Mod;

/**
 * In addition to Flags this class also checks correctness of modifiers.
 *
 * @author Sergey Basalaev
 */
final class CheckedFlags extends Flags {

    private final Options options;

    CheckedFlags(Options options) {
        this.options = options;
    }

    private static final EnumSet<Mod> ALLOWED_SYNTH = EnumSet.of(
            Mod.MANDATED,
            Mod.SYNTHETIC
    );
    private static final EnumSet<Mod> ALLOWED_REQUIRES = EnumSet.of(
            Mod.MANDATED,
            Mod.STATIC,
            Mod.SYNTHETIC,
            Mod.TRANSITIVE
    );
    private static final EnumSet<Mod> ALLOWED_MODULE = EnumSet.of(
            Mod.DEPRECATED,
            Mod.MANDATED,
            Mod.OPEN,
            Mod.SYNTHETIC
    );
    private static final EnumSet<Mod> ALLOWED_INNER_CLASS = EnumSet.of(
            Mod.ABSTRACT,
            Mod.FINAL, 
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.SYNTHETIC
    );
    private static final EnumSet<Mod> ALLOWED_CLASS = EnumSet.of(
            Mod.ABSTRACT,
            Mod.DEPRECATED,
            Mod.FINAL, 
            Mod.PUBLIC, 
            Mod.SYNTHETIC
    );
    private static final EnumSet<Mod> ALLOWED_CLASS_FIELD = EnumSet.of(
            Mod.DEPRECATED,
            Mod.FINAL,
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.SYNTHETIC,
            Mod.TRANSIENT,
            Mod.VOLATILE
    );
    private static final EnumSet<Mod> ALLOWED_INTERFACE_FIELD = EnumSet.of(
            Mod.DEPRECATED,
            Mod.FINAL,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.SYNTHETIC
    );
    private static final EnumSet<Mod> ALLOWED_CLASS_METHOD = EnumSet.of(
            Mod.ABSTRACT,
            Mod.BRIDGE,
            Mod.FINAL,
            Mod.NATIVE,
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.STRICT,
            Mod.SYNCHRONIZED,
            Mod.SYNTHETIC,
            Mod.VARARGS
    );
    private static final EnumSet<Mod> ALLOWED_INTERFACE_METHOD = EnumSet.of(
            Mod.ABSTRACT,
            Mod.BRIDGE,
            Mod.PRIVATE,
            Mod.PUBLIC,
            Mod.STATIC,
            Mod.STRICT,
            Mod.SYNTHETIC,
            Mod.VARARGS
    );
    private static final EnumSet<Mod> ALLOWED_CONSTRUCTOR = EnumSet.of(
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC,
            Mod.STRICT,
            Mod.SYNTHETIC,
            Mod.VARARGS
    );
    private static final EnumSet<Mod> ALLOWED_PARAMETER = EnumSet.of(
            Mod.FINAL,
            Mod.MANDATED,
            Mod.SYNTHETIC
    );
    private static final EnumSet<Mod> CONFLICTING_ACCESS = EnumSet.of(
            Mod.PRIVATE,
            Mod.PROTECTED,
            Mod.PUBLIC
    );
    private static final EnumSet<Mod> CONFLICTING_ABSTRACT = EnumSet.of(
            Mod.FINAL,
            Mod.NATIVE,
            Mod.PRIVATE,
            Mod.STATIC,
            Mod.STRICT,
            Mod.SYNCHRONIZED
    );

    @Override
    public int forNestedClass(JvmNestedClass className) {
        checkForClassElement(className.modifiers(), "nested classes", ALLOWED_INNER_CLASS);
        return super.forNestedClass(className);
    }

    @Override
    public int forClass(JvmClass className, Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "classes", ALLOWED_CLASS);
        if (className.classKind().isInterface()) {
            checkRequired(modifiers, Mod.ABSTRACT, "interfaces");
            checkInvalid(modifiers, Mod.FINAL, "interfaces");
        }
        return super.forClass(className, modifiers);
    }

    @Override
    public int forClassField(Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "class fields", ALLOWED_CLASS_FIELD);
        if (modifiers.exists(Mod.VOLATILE::equals) && modifiers.exists(Mod.FINAL::equals)) {
            throwConflictingMods(list(Mod.VOLATILE, Mod.FINAL), "field");
        }
        return super.forClassField(modifiers);
    }

    @Override
    public int forInterfaceField(Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "interface fields", ALLOWED_INTERFACE_FIELD);
        checkRequired(modifiers, Mod.PUBLIC, "interface fields");
        checkRequired(modifiers, Mod.STATIC, "interface fields");
        checkRequired(modifiers, Mod.FINAL, "interface fields");
        return super.forClassField(modifiers);
    }

    @Override
    public int forClassMethod(Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "class methods", ALLOWED_CLASS_METHOD);
        return super.forClassMethod(modifiers);
    }

    @Override
    public int forInterfaceMethod(Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "interface methods", ALLOWED_INTERFACE_METHOD);
        if (!options.version().atLeast(JavaVersion.V8)) {
            checkRequired(modifiers, Mod.PUBLIC, "interface methods unless Java version is ≥ 1.8");
            checkRequired(modifiers, Mod.ABSTRACT, "interface methods unless Java version is ≥ 1.8");
        }
        return super.forInterfaceMethod(modifiers);
    }

    @Override
    public int forConstructor(Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "constructors", ALLOWED_CONSTRUCTOR);
        return super.forConstructor(modifiers);
    }

    @Override
    public int forParameter(Traversable<Mod> modifiers) {
        checkForClassElement(modifiers, "formal parameters", ALLOWED_PARAMETER);
        return super.forParameter(modifiers);
    }

    private void checkForClassElement(Traversable<Mod> modifiers, String name, EnumSet<Mod> allowed) {
        for (Mod modifier : modifiers) {
            if (!allowed.contains(modifier)) {
                throwInvalidMod(modifier, name);
            }
            if (modifier == Mod.ABSTRACT) {
                var conflicting = modifiers.filter(CONFLICTING_ABSTRACT::contains);
                if (conflicting.nonEmpty()) {
                    throwConflictingMods(chain(list(Mod.ABSTRACT), conflicting), name);
                }
            }
            if (modifier == Mod.STRICT && options.version().atLeast(JavaVersion.V17)) {
                throw new IllegalArgumentException("Modifier STRICT is not supported in Java ≥ 17");
            }
        }
        var access = modifiers.filter(CONFLICTING_ACCESS::contains);
        if (access.count() > 1) {
            throwConflictingMods(access, name);
        }
    }

    @Override
    public int forModule(Traversable<Mod> modifiers) {
        checkForModuleElement(modifiers, "modules", ALLOWED_MODULE);
        return super.forModule(modifiers);
    }

    @Override
    public int forModuleRequires(Traversable<Mod> modifiers) {
        checkForModuleElement(modifiers, "module requires", ALLOWED_REQUIRES);
        return super.forModuleRequires(modifiers);
    }

    @Override
    public int forModuleExports(Traversable<Mod> modifiers) {
        checkForModuleElement(modifiers, "module exports", ALLOWED_SYNTH);
        return super.forModuleExports(modifiers);
    }

    @Override
    public int forModuleOpens(Traversable<Mod> modifiers) {
        checkForModuleElement(modifiers, "module opens", ALLOWED_SYNTH);
        return super.forModuleOpens(modifiers);
    }

    private static void checkForModuleElement(Traversable<Mod> modifiers, String name, EnumSet<Mod> allowed) {
        for (var modifier : modifiers) {
            if (!allowed.contains(modifier)) {
                throwInvalidMod(modifier, name);
            }
        }
    }

    private static void checkRequired(Traversable<Mod> modifiers, Mod required, String elementType) {
        if (!modifiers.exists(required::equals)) {
            throw new IllegalArgumentException("Modifier " + required + " is required for " + elementType);
        }
    }

    private static void checkInvalid(Traversable<Mod> modifiers, Mod invalid, String elementType) {
        if (modifiers.exists(invalid::equals)) {
            throwInvalidMod(invalid, elementType);
        }
    }

    private static void throwInvalidMod(Mod modifier, String elementType) {
        throw new IllegalArgumentException("Modifier " + modifier + " is not allowed for " + elementType);
    }

    private static void throwConflictingMods(Traversable<Mod> modifiers, String elementType) {
        throw new IllegalArgumentException("Conflicting modifiers " + modifiers.join(", ") + " for " + elementType);
    }
}
