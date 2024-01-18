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

/**
 * Kind of a class represented by {@code JvmClass}.
 * Corresponds to the keyword used to define the class in Java source.
 *
 * @author Sergey Basalaev
 * @see JvmClass#classKind() 
 */
public enum ClassKind {
    /** Annotation interface. */
    ANNOTATION("@interface"),
    /** Regular class. */
    CLASS("class"),
    /** Enum class. */
    ENUM("enum"),
    /** Interface but not an annotation. */
    INTERFACE("interface"),
    /** Module info class. */
    MODULE("module"),
    /** Package info class. */
    PACKAGE("package"),
    /** Record class. */
    RECORD("record"),
    ;

    private final String keyword;

    private ClassKind(String keyword) {
        this.keyword = keyword;
    }

    /** Whether this kind of classes is represented by an interface. */
    public boolean isInterface() {
        return switch (this) {
            case ANNOTATION, INTERFACE, PACKAGE -> true;
            default -> false;
        };
    }

    /** Keyword used to declare classes of this kind. */
    public String keyword() {
        return keyword;
    }
}
