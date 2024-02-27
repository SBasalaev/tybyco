
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import me.sbasalaev.tybyco.CompiledClass;
import me.sbasalaev.tybyco.Tybyco;
import static me.sbasalaev.tybyco.descriptors.Jvm.CLASS;
import static me.sbasalaev.tybyco.descriptors.Jvm.METHOD;
import static me.sbasalaev.tybyco.descriptors.Jvm.TYPE;
import static me.sbasalaev.tybyco.descriptors.Jvm.VOID;
import static me.sbasalaev.tybyco.descriptors.Mod.PUBLIC;
import static me.sbasalaev.tybyco.descriptors.Mod.STATIC;

/**
 * Generate "Hello, world!" program.
 * When this program is run it generates "Hello.class" class file in the current
 * directory. The contents of the class generated is roughly the same as
 * {@snippet :
 * public class Hello {
 *     public static void main(String[] args) {
 *         System.out.println("Hello, world!");
 *     }
 * }
 * }
 * No constructor or debugging information is written into the class.
 */
public class HelloWorld {

    private HelloWorld() { }

    public static void main(String[] args) throws IOException {
        Tybyco tybyco = Tybyco.getDefault();
        CompiledClass compiled = tybyco
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
    }
}
