import java.io.File;

public class ConfigMigrationTest {

    public static void main (String[] args) {
        ZipTesterDirectoryWalker directoryWalker = new ZipTesterDirectoryWalker();
        directoryWalker.walk(new File("."));
    }
}
