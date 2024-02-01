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

import java.lang.module.ModuleDescriptor;
import me.sbasalaev.collection.Set;
import me.sbasalaev.tybyco.builders.ClassBuilder;
import me.sbasalaev.tybyco.builders.ClassOrInterfaceBuilder;
import me.sbasalaev.tybyco.builders.ModuleBuilder;
import me.sbasalaev.tybyco.builders.PackageBuilder;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.Mod;

/**
 * Entry point for builders of classfiles.
 *
 * @author Sergey Basalaev
 */
public final class Tybyco {

    final Options options;

    private Tybyco(Options options) {
        this.options = options;
    }

    /** Builds options for a {@code Tybyco} instance. */
    public static Tybyco.Builder withOptions() {
        return new Tybyco.Builder();
    }

    /**
     * Returns a {@code Tybyco} instance with the default settings.
     * See the descriptions of {@link Builder} methods for the defaults
     * for each setting.
     */
    public static Tybyco getDefault() {
        return withOptions().build();
    }

    /**
     * Returns a builder for a module with given name and version.
     *
     * @param modifiers module modifiers among {@link Mod#OPEN},
     *     {@link Mod#DEPRECATED}, {@link Mod#SYNTHETIC} and {@link Mod#MANDATED}.
     * @param moduleName the name of the module.
     * @param version    the version of the module in the format described by {@link ModuleDescriptor.Version }.
     */
    public ModuleBuilder buildModule(Set<Mod> modifiers, String moduleName, String version) {
        if (!options.version().atLeast(JavaVersion.V9)) {
            throw new IllegalStateException("Modules require Java version ≥ 9");
        }
        return new ModuleBuilderImpl(options, modifiers, moduleName, version);
    }

    /**
     * Returns a builder for a module with given name.
     *
     * @param modifiers module modifiers among {@link Mod#OPEN},
     *     {@link Mod#DEPRECATED}, {@link Mod#SYNTHETIC} and {@link Mod#MANDATED}.
     * @param moduleName the name of the module.
     *
     * @throws IllegalArgumentException if the set of modifiers contains
     *     elements invalid for a module.
     */
    public ModuleBuilder buildModule(Set<Mod> modifiers, String moduleName) {
        if (!options.version().atLeast(JavaVersion.V9)) {
            throw new IllegalStateException("Modules require Java version ≥ 9");
        }
        return new ModuleBuilderImpl(options, modifiers, moduleName, null);
    }

    /** Returns a builder for a package-info file. */
    public PackageBuilder buildPackage(String packageName) {
        return new PackageBuilderImpl(options, packageName);
    }

    /**
     * Returns a builder to build an interface or an annotation with given name.
     * The {@link Mod#ABSTRACT} modifier may be skipped and will be added automatically.
     *
     * @param modifiers interface modifiers among {@link Mod#PUBLIC},
     *      {@link Mod#DEPRECATED} and {@link Mod#SYNTHETIC}.
     * @param interfaceName  the interface name.
     */
    public ClassOrInterfaceBuilder<?> buildInterface(Set<Mod> modifiers, JvmClass interfaceName) {
        if (modifiers.contains(Mod.FINAL)) {
            throw new IllegalArgumentException("Invalid modifier FINAL for an interface");
        }
        if (!interfaceName.classKind().isInterface()) {
            throw new IllegalArgumentException(interfaceName + " is not an interface");
        }
        return new ClassBuilderImpl<>(options, modifiers, interfaceName);
    }

    /**
     * Returns a builder to build a class with given name.
     *
     * @param modifiers class modifiers among {@link Mod#ABSTRACT},
     *     {@link Mod#DEPRECATED}, {@link Mod#FINAL}, {@link Mod#PUBLIC}
     *     and {@link Mod#SYNTHETIC}.
     * @param className  the class name.
     */
    public ClassBuilder<?> buildClass(Set<Mod> modifiers, JvmClass className) {
        if (modifiers.contains(Mod.ABSTRACT) && modifiers.contains(Mod.FINAL)) {
            throw new IllegalArgumentException("Conflicting modifiers ABSTRACT and FINAL");
        }
        if (className.classKind().isInterface()) {
            throw new IllegalArgumentException(className + " is an interface");
        }
        return new ClassBuilderImpl<>(options, modifiers, className);
    }

    /** Builder of Tybyco settings. */
    public static final class Builder {

        private JavaVersion version = JavaVersion.runtimeCompatible();
        private boolean verify = false;
        private boolean writeLocalTables = true;

        private Builder() { }

        /**
         * Set target Java version.
         * <p>
         * If not set explicitly the default value is the one returned by
         * {@link JavaVersion#runtimeCompatible() }.
         */
        public Builder version(JavaVersion version) {
            this.version = version;
            return this;
        }

        /**
         * Whether to (partially) verify classes being built.
         * <p>
         * If not set explicitly the default value is {@code false}.
         */
        public Builder verify(boolean value) {
            this.verify = value;
            return this;
        }

        /**
         * Whether to generate {@code LocalVariableTable} and {@code LocalVariableTypeTable}.
         * These tables provide information about local variables to debuggers.
         * <p>
         * If not set explicitly the default value is {@code true}.
         */
        public void writeLocalTables(boolean value) {
            this.writeLocalTables = value;
        }

        /**
         * Returns {@code Tybyco} instance with given options.
         * May be called multiple times.
         */
        public Tybyco build() {
            return new Tybyco(new Options(version, verify, writeLocalTables));
        }
    }
}
