package br.ufba.arieslinter.oracles;

import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

/**
 * Oracle for Resource Optimism smell.
 * Detection: A test method utilizes an instance of a File class without 
 * calling the exists(), isFile() or notExists() methods of the object.
 */
public class ResourceOptimismOracle {

    @Test
    public void test1_OptimisticDelete() {
        File file = new File("test.txt");
        // Smell: calling delete() without exists()
        file.delete();
    }

    @Test
    public void test2_OptimisticLength() {
        File file = new File("data.bin");
        // Smell: calling length() without exists()
        long size = file.length();
        assertTrue(size >= 0);
    }

    @Test
    public void test3_OptimisticListFiles() {
        File dir = new File("my_dir");
        // Smell: calling listFiles() without exists() or isDirectory()
        File[] files = dir.listFiles();
        assertNotNull(files);
    }

    @Test
    public void test4_OptimisticLastModified() {
        File file = new File("config.prop");
        // Smell: calling lastModified() without exists()
        long time = file.lastModified();
        assertTrue(time > 0);
    }

    @Test
    public void test5_OptimisticMkdir() {
        File dir = new File("new_folder");
        // Smell: calling mkdir() without exists()
        dir.mkdir();
    }

    @Test
    public void test6_OptimisticRead() {
        File file = new File("input.txt");
        // Smell: calling canRead() might be considered part of checks, 
        // but here we use it as a trigger for "usage" if it's not checked first.
        // Actually, let's use a clear usage:
        String path = file.getAbsolutePath();
        // getAbsolutePath is usually safe, but let's use list()
        file.list();
    }

    @Test
    public void test7_OptimisticRename() {
        File src = new File("old.txt");
        File dest = new File("new.txt");
        // Smell: source not checked for existence
        src.renameTo(dest);
    }

    @Test
    public void test8_OptimisticSetExecutable() {
        File script = new File("run.sh");
        // Smell: setExecutable without exists
        script.setExecutable(true);
    }

    @Test
    public void test9_OptimisticSetWritable() {
        File file = new File("readonly.txt");
        file.setWritable(true);
    }

    @Test
    public void test10_OptimisticHiddenCheck() {
        File file = new File(".secret");
        // Smell: isHidden() without exists()
        boolean hidden = file.isHidden();
        assertTrue(hidden);
    }

    /**
     * NEGATIVE TEST: These should NOT be detected as Resource Optimism.
     */
    @Test
    public void testValid_WithExistenceCheck() {
        File file = new File("valid.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testValid_WithIsFileCheck() {
        File file = new File("valid.txt");
        if (file.isFile()) {
            long len = file.length();
        }
    }
}
