package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class RedundantPrintOracle {

    @Test
    public void testPrint1() {
        System.out.println("Starting test...");
        assertTrue(true);
    }

    @Test
    public void testPrint2() {
        System.err.println("Error potential here");
        assertTrue(true);
    }

    @Test
    public void testPrint3() {
        System.out.println(1 + 2);
        assertTrue(true);
    }

    @Test
    public void testPrint4() {
        System.out.printf("Format %s", "test");
        assertTrue(true);
    }

    @Test
    public void testPrint5() {
        System.out.print("partial ");
        System.out.println("line");
        assertTrue(true);
    }

    @Test
    public void testPrint6() {
        try {
            // some code
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        assertTrue(true);
    }

    @Test
    public void testPrint7() {
        System.out.println("Debugging value: " + System.currentTimeMillis());
        assertTrue(true);
    }

    @Test
    public void testPrint8() {
        System.err.println("Should not reach here");
        assertTrue(true);
    }

    @Test
    public void testPrint9() {
        System.out.println("End of test");
        assertTrue(true);
    }

    @Test
    public void testPrint10() {
        System.out.println("Running test 10");
        assertTrue(true);
    }
}
