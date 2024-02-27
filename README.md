## Typesafe Java bytecode generator

**tybyco** is a library to programmaticaly generate Java class files.
It is not as fast and flexible as
[Objectweb ASM](https://asm.ow2.io/) or
[Apache BCEL](https://commons.apache.org/proper/commons-bcel/).
Instead it aims to simplify code generation and to ease catching bugs related to
code generation. I created this library mainly for a toy programming language
I'm developing.

As an example the following code builds a class file for the "Hello, world!"
program.
```java
CompiledClass compiled = Tybyco.getDefault()
.buildClass(CLASS("Hello"), PUBLIC)
    .method("main", METHOD(VOID, TYPE(String[].class)), PUBLIC, STATIC)
        .code("args")
            .getStaticField(CLASS(System.class), "out", TYPE(PrintStream.class))
            .pushConst("Hello, world!")
            .invokeVirtual(CLASS(PrintStream.class), "println", METHOD(VOID, TYPE(String.class)))
            .voidReturn()
        .end()
    .end()
.end();
Path filePath = Paths.get(compiled.className().binaryName() + ".class");
Files.write(filePath, compiled.code());
```
