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
package me.sbasalaev.tybyco;

import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Set;
import me.sbasalaev.tybyco.builders.ModuleBuilder;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.Mod;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Sergey Basalaev
 */
final class ModuleBuilderImpl
    extends ClassfileBuilderImpl<ModuleBuilder>
    implements ModuleBuilder {

    private final ModuleVisitor mv;

    ModuleBuilderImpl(Options options, Set<Mod> modifiers, String name, @Nullable String version) {
        super(options, JvmClass.MODULE_INFO);
        cw.visit(
            /* version = */ options.version().major(),
            /* access  = */ Opcodes.ACC_MODULE | (modifiers.contains(Mod.DEPRECATED) ? Opcodes.ACC_DEPRECATED : 0),
            /* name    = */ className.binaryName(),
            null, null, null);
        mv = cw.visitModule(name, options.flags().forModule(modifiers), version);
    }

    @Override
    public ModuleBuilder requires(Set<Mod> modifiers, String moduleName, String version) {
        mv.visitRequire(moduleName, options.flags().forModuleRequires(modifiers), version);
        return this;
    }

    @Override
    public ModuleBuilder requires(Set<Mod> modifiers, String moduleName) {
        mv.visitRequire(moduleName, options.flags().forModuleRequires(modifiers), null);
        return this;
    }

    @Override
    public ModuleBuilder exports(Set<Mod> modifiers, String packageName, List<String> toModules) {
        mv.visitExport(packageName, options.flags().forModuleExports(modifiers),
                toModules.isEmpty() ? null : toModules.toArray(String[]::new));
        return this;
    }

    @Override
    public ModuleBuilder opens(Set<Mod> modifiers, String packageName, List<String> toModules) {
        mv.visitOpen(packageName, options.flags().forModuleOpens(modifiers),
                toModules.isEmpty() ? null : toModules.toArray(String[]::new));
        return this;
    }

    @Override
    public ModuleBuilder provides(JvmClass serviceClass, List<? extends JvmClass> implementations) {
        if (implementations.isEmpty()) {
            throw new IllegalArgumentException("At least one implementation required");
        }
        learnClass(serviceClass);
        implementations.forEach(this::learnClass);
        mv.visitProvide(serviceClass.binaryName(), implementations.map(JvmClass::binaryName).toArray(String[]::new));
        return this;
    }

    @Override
    public ModuleBuilder uses(JvmClass serviceClass) {
        learnClass(serviceClass);
        mv.visitUse(serviceClass.binaryName());
        return this;
    }

    @Override
    public ModuleBuilder mainClass(JvmClass mainClass) {
        learnClass(mainClass);
        mv.visitMainClass(mainClass.binaryName());
        return this;
    }

    @Override
    public CompiledClass end() {
        mv.visitEnd();
        visitCommon();
        cw.visitEnd();
        return new CompiledClass(className, cw.toByteArray());
    }
}
