package net.atos.tfc.test;

import java.io.File;

public class ConfigMigrationTester {

    public static void main (String[] args) {
        ZipTesterDirectoryWalker directoryWalker = new ZipTesterDirectoryWalker();
        directoryWalker.walk(new File("."));
    }
}
