package br.ufba.arieslinter.oracles;

import org.junit.Test;
import java.io.*;
import java.sql.*;
import java.net.*;
import static org.junit.Assert.*;

/**
 * Oracle for Mystery Guest smell.
 * Detection: A test method containing object instances of files and databases classes.
 */
public class MysteryGuestOracle {

    @Test
    public void test1_FileInstantiation() {
        File file = new File("test.txt");
        assertTrue(file.exists());
    }

    @Test
    public void test2_FileInputStream() throws IOException {
        FileInputStream fis = new FileInputStream("data.bin");
        fis.read();
        fis.close();
    }

    @Test
    public void test3_FileOutputStream() throws IOException {
        FileOutputStream fos = new FileOutputStream("output.txt");
        fos.write(65);
        fos.close();
    }

    @Test
    public void test4_FileReader() throws IOException {
        FileReader fr = new FileReader("config.prop");
        fr.read();
        fr.close();
    }

    @Test
    public void test5_FileWriter() throws IOException {
        FileWriter fw = new FileWriter("log.txt");
        fw.write("test");
        fw.close();
    }

    @Test
    public void test6_RandomAccessFile() throws IOException {
        RandomAccessFile raf = new RandomAccessFile("db.dat", "rw");
        raf.seek(10);
        raf.close();
    }

    @Test
    public void test7_SocketInstantiation() throws IOException {
        // Network resources are often also considered mystery guests
        Socket socket = new Socket("localhost", 8080);
        socket.close();
    }

    @Test
    public void test8_URLInstantiation() throws Exception {
        URL url = new URL("http://google.com");
        url.openStream().close();
    }

    @Test
    public void test9_DatabaseClasses() throws Exception {
        // Using DriverManager to get a connection is a common Mystery Guest smell
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db");
        assertNotNull(conn);
        conn.close();
    }

    @Test
    public void test10_MultipleResources() throws IOException {
        File f1 = new File("a.txt");
        File f2 = new File("b.txt");
        assertTrue(f1.exists() || f2.exists());
    }

    /**
     * NEGATIVE TEST: These should NOT be detected as Mystery Guest.
     * They use in-memory alternatives or mocks.
     */
    @Test
    public void testValid_InMemory() {
        StringReader reader = new StringReader("data");
        // StringReader is not in the forbidden list as it's in-memory
        assertNotNull(reader);
    }

    @Test
    public void testValid_ByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        assertTrue(baos.size() > 0);
    }
}
