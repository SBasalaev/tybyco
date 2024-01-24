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
import me.sbasalaev.collection.Set;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.Mod;

/**
 * Builder of module-info class file.
 *
 * @author Sergey Basalaev
 */
public interface ModuleBuilder extends ClassfileBuilder<ModuleBuilder> {

    /**
     * Sets main class of this module.
     * @return this builder.
     */
    ModuleBuilder mainClass(JvmClass mainClass);

    /**
     * Adds requires directive without a version.
     * @return this builder.
     */
    ModuleBuilder requires(Set<Mod> modifiers, String moduleName);

    /**
     * Adds requires directive with given compiled version.
     * @return this builder.
     */
    ModuleBuilder requires(Set<Mod> modifiers, String moduleName, String compiledVersion);

    /**
     * Adds exports directive.
     * @return this builder.
     */
    ModuleBuilder exports(Set<Mod> modifiers, String packageName, List<String> toModules);

    /**
     * Adds exports directive.
     * @return this builder.
     */
    default ModuleBuilder exports(Set<Mod> modifiers, String packageName, String... toModules) {
        return exports(modifiers, packageName, list(toModules));
    }

    /**
     * Adds opens directive.
     * @return this builder.
     */
    ModuleBuilder opens(Set<Mod> modifiers, String packageName, List<String> toModules);

    /**
     * Adds opens directive.
     * @return this builder.
     */
    default ModuleBuilder opens(Set<Mod> modifiers, String packageName, String... toModules) {
        return opens(modifiers, packageName, list(toModules));
    }

    /**
     * Adds uses directive.
     * @return this builder.
     */
    public ModuleBuilder uses(JvmClass serviceClass);

    /**
     * Adds provides directive.
     * @return this builder.
     */
    ModuleBuilder provides(JvmClass serviceClass, List<? extends JvmClass> implementations);

    /**
     * Adds provides directive.
     * @return this builder.
     */
    default ModuleBuilder provides(JvmClass serviceClass, JvmClass firstImpl, JvmClass... otherImpls) {
        return provides(serviceClass, concat(list(firstImpl), list(otherImpls)));
    }
}
