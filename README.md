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

Class sections that are automatically handled by this library
include:
* `Signature`. Type and method descriptors in Tybyco are handled not by strings
  but by the dedicated descriptor classes. For instance, the descriptor for
  `List<? extends Number>` type may be created using
  ```java
  TYPE(List.class, EXTENDS(TYPE(Number.class)))
  ```
  and the appropriate descriptors and signatures will be generated everywhere it
  is used.
* `*TypeAnnotations`. Annotations may be attached to the type descriptor and the
  corresponding type annotation sections are filled everywhere the type is used.
  ```
  var nonnullString = JvmType.ofClass(String.class).annotated(JvmAnnotation.of(Nullable.class));
  ```
* `InnerClasses`. By specification for every used class that is not a member of
  a package there must be an entry in the `InnerClasses` table. Tybyco fills
  this table automatically for each encountered class reference both for runtime
  class references (instances of `java.lang.Class`) and symbolic references
  (instances of `JvmNestedClass`).
* `StackMapTable`. By virtue of using Objectweb ASM under the hood which also
   handles stack maps automatically.
