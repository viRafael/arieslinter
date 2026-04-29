package br.ufba.arieslinter.oracles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AssertionRouletteOracle {

    @Test
    public void test1() {
        assertEquals(1, 1);
        assertEquals(2, 2);
    }

    @Test
    public void test2() {
        assertTrue(true);
        assertTrue(false);
    }

    @Test
    public void test3() {
        assertEquals("a", "a");
        assertEquals("b", "b");
        assertEquals("c", "c");
    }

    @Test
    public void test4() {
        assertTrue(1 > 0);
        assertEquals(10, 5 + 5);
    }

    @Test
    public void test5() {
        assertEquals(0.1, 0.1, 0.001);
        assertEquals(0.2, 0.2, 0.001);
    }

    @Test
    public void test6() {
        assertTrue("message", true); // This one has a message
        assertEquals(1, 1);
        assertEquals(2, 2); // Still roulette because of these two
    }

    @Test
    public void test7() {
        Object obj = new Object();
        assertEquals(obj, obj);
        assertTrue(obj != null);
    }

    @Test
    public void test8() {
        int x = 10;
        assertEquals(10, x);
        assertTrue(x > 0);
    }

    @Test
    public void test9() {
        String s = "test";
        assertEquals("test", s);
        assertEquals(4, s.length());
    }

    @Test
    public void test10() {
        boolean b = true;
        assertTrue(b);
        assertTrue(!(!b));
    }
}
