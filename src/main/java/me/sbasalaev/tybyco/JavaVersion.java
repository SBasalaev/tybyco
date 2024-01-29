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

import org.objectweb.asm.Opcodes;

/**
 * Supported Java versions.
 *
 * @author Sergey Basalaev
 */
public enum JavaVersion {

    V11(Opcodes.V11),
    V12(Opcodes.V12);

    private final int major;

    private JavaVersion(int major) {
        this.major = major;
    }

    int major() {
        return this.major;
    }

    /** Whether this version is the same or higher than given version. */
    public boolean atLeast(JavaVersion version) {
        return this.ordinal() >= version.ordinal();
    }

    /** Returns the latest supported target Java version. */
    public static JavaVersion latest() {
        return V12;
    }

    /**
     * Returns the latest target Java version not greater than the current runtime version.
     * Note, that tybyco is compiled with Java 21 features so the runtime feature
     * version is at least 21.
     */
    public static JavaVersion runtimeCompatible() {
        return switch (Runtime.version().feature()) {
            default -> V12;
        };
    }
}
