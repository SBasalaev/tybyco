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
package me.sbasalaev.tybyco.builders;

import static me.sbasalaev.API.concat;
import static me.sbasalaev.API.list;
import me.sbasalaev.Opt;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Traversable;
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
     *
     * @param mainClass main class of the module.
     *
     * @return this builder.
     */
    ModuleBuilder mainClass(JvmClass mainClass);

    /**
     * Adds requires directive.
     *
     * @param moduleName name of the required module.
     * @param compiledVersion compile-time version of the module or {@code none()}.
     * @param modifiers directive modifiers among {@link Mod#STATIC}, {@link Mod#TRANSITIVE},
     *     {@link Mod#MANDATED} and {@link Mod#SYNTHETIC}.
     *
     * @return this builder.
     */
    ModuleBuilder requires(String moduleName, Opt<String> compiledVersion, Traversable<Mod> modifiers);

    /**
     * Adds requires directive.
     *
     * @param moduleName name of the required module.
     * @param compiledVersion compile-time version of the module or {@code none()}.
     * @param modifiers directive modifiers among {@link Mod#STATIC}, {@link Mod#TRANSITIVE},
     *     {@link Mod#MANDATED} and {@link Mod#SYNTHETIC}.
     *
     * @return this builder.
     */
    default ModuleBuilder requires(String moduleName, Opt<String> compiledVersion, Mod... modifiers) {
        return requires(moduleName, compiledVersion, list(modifiers));
    }

    /**
     * Adds exports directive.
     *
     * @param packageName name of the package this module opens.
     * @param toModules names of the modules access to the package is open to.
     *     If empty, access is open to all modules.
     * @param modifiers either {@link Mod#MANDATED} or {@link Mod#SYNTHETIC}
     *   may be given.
     *
     * @return this builder.
     */
    ModuleBuilder exports(String packageName, List<String> toModules, Mod... modifiers);

    /**
     * Adds exports directive.
     *
     * @param packageName name of the package this module exports.
     * @param toModules names of the modules this package is exported to.
     *     If empty, access is open to all modules.
     *
     * @return this builder.
     */
    default ModuleBuilder exports(String packageName, String... toModules) {
        return exports(packageName, list(toModules));
    }

    /**
     * Adds opens directive.
     *
     * @param packageName name of the package this module opens.
     * @param toModules names of the modules access to the package is open to.
     *     If empty, access is open to all modules.
     * @param modifiers either {@link Mod#MANDATED} or {@link Mod#SYNTHETIC}
     *   may be given.
     *
     * @return this builder.
     */
    ModuleBuilder opens(String packageName, List<String> toModules, Mod... modifiers);

    /**
     * Adds opens directive.
     *
     * @param packageName name of the package this module opens.
     * @param toModules names of the modules access to the package is open to.
     *     If empty, access is open to all modules.
     *
     * @return this builder.
     */
    default ModuleBuilder opens(String packageName, String... toModules) {
        return opens(packageName, list(toModules));
    }

    /**
     * Adds uses directive.
     *
     * @param serviceClass service provider class.
     *
     * @return this builder.
     */
    ModuleBuilder uses(JvmClass serviceClass);

    /**
     * Adds provides directive.
     *
     * @param serviceClass service provider class.
     * @param firstImpl  implementation classes, the list must have at least one entry.
     *
     * @return this builder.
     */
    ModuleBuilder provides(JvmClass serviceClass, List<? extends JvmClass> implementations);

    /**
     * Adds provides directive.
     *
     * @param serviceClass service provider class.
     * @param firstImpl  implementation class.
     * @param otherImpls additional implementation classes.
     *
     * @return this builder.
     */
    default ModuleBuilder provides(JvmClass serviceClass, JvmClass firstImpl, JvmClass... otherImpls) {
        return provides(serviceClass, concat(list(firstImpl), list(otherImpls)));
    }
}
