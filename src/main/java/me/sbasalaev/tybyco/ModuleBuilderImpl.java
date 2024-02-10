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

import static me.sbasalaev.API.list;
import me.sbasalaev.Opt;
import me.sbasalaev.collection.List;
import me.sbasalaev.collection.Traversable;
import me.sbasalaev.tybyco.builders.ModuleBuilder;
import me.sbasalaev.tybyco.descriptors.JvmClass;
import me.sbasalaev.tybyco.descriptors.Mod;
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

    ModuleBuilderImpl(Options options, Traversable<Mod> modifiers, String name, Opt<String> version) {
        super(options, JvmClass.MODULE_INFO);
        cw.visit(
            /* version = */ options.version().major(),
            /* access  = */ Opcodes.ACC_MODULE | (modifiers.exists(Mod.DEPRECATED::equals) ? Opcodes.ACC_DEPRECATED : 0),
            /* name    = */ className.binaryName(),
            null, null, null);
        mv = cw.visitModule(name, options.flags().forModule(modifiers), version.orElseNull());
    }

    @Override
    public ModuleBuilder requires(String moduleName, Opt<String> version, Traversable<Mod> modifiers) {
        mv.visitRequire(moduleName, options.flags().forModuleRequires(modifiers), version.orElseNull());
        return this;
    }

    @Override
    public ModuleBuilder exports(String packageName, List<String> toModules, Mod... modifiers) {
        mv.visitExport(packageName, options.flags().forModuleExports(list(modifiers)),
                toModules.isEmpty() ? null : toModules.toArray(String[]::new));
        return this;
    }

    @Override
    public ModuleBuilder opens(String packageName, List<String> toModules, Mod... modifiers) {
        mv.visitOpen(packageName, options.flags().forModuleOpens(list(modifiers)),
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
