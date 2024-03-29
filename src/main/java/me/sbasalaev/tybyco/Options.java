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

/**
 * Tybyco options.
 *
 * @author Sergey Basalaev
 */
final class Options {

    private final JavaVersion version;
    private final boolean verify;
    private final Flags flags;
    private final boolean writeLocalTables;

    Options(JavaVersion version,
            boolean verify,
            boolean writeLocalTables) {
        this.version = version;
        this.verify = verify;
        this.writeLocalTables = writeLocalTables;
        this.flags = verify ? new CheckedFlags(this) : new Flags();
    }

    public JavaVersion version() {
        return version;
    }

    public boolean verify() {
        return verify;
    }

    public boolean writeLocalTables() {
        return writeLocalTables;
    }

    public Flags flags() {
        return flags;
    }
}
