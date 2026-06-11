package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VerboseTestOracle {

    @Test
    public void testVerbose1() {
        int a = 1;
        int b = 2;
        int c = a + b;
        assertEquals(3, c);
        c = c * 2;
        assertEquals(6, c);
        c = c - 1;
        assertEquals(5, c);
        c = c / 5;
        assertEquals(1, c);
        assertTrue(c > 0);
        assertNotNull(c);
        System.out.println("Step 1");
        System.out.println("Step 2");
        System.out.println("Step 3");
        System.out.println("Step 4");
        System.out.println("Step 5");
        System.out.println("Step 6");
        System.out.println("Step 7");
        System.out.println("Step 8");
        System.out.println("Step 9");
        System.out.println("Step 10");
        assertTrue(true);
    }

    @Test
    public void testVerbose2() {
        String s = "verbose test";
        assertNotNull(s);
        s = s.toUpperCase();
        assertTrue(s.contains("VERBOSE"));
        s = s.replace(" ", "_");
        assertEquals("VERBOSE_TEST", s);
        s = s.substring(0, 7);
        assertEquals("VERBOSE", s);
        s = s.toLowerCase();
        assertEquals("verbose", s);
        System.out.println("Log 1");
        System.out.println("Log 2");
        System.out.println("Log 3");
        System.out.println("Log 4");
        System.out.println("Log 5");
        System.out.println("Log 6");
        System.out.println("Log 7");
        System.out.println("Log 8");
        System.out.println("Log 9");
        System.out.println("Log 10");
        assertTrue(s.length() > 0);
    }

    @Test
    public void testVerbose3() {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        assertEquals(5, list.size());
        list.remove(0);
        assertEquals(4, list.size());
        list.clear();
        assertTrue(list.isEmpty());
        System.out.println("Processing list...");
        System.out.println("Still processing...");
        System.out.println("Almost done...");
        System.out.println("Line 1");
        System.out.println("Line 2");
        System.out.println("Line 3");
        System.out.println("Line 4");
        System.out.println("Line 5");
        System.out.println("Line 6");
        System.out.println("Line 7");
        System.out.println("Line 8");
        System.out.println("Line 9");
        System.out.println("Line 10");
        assertTrue(true);
    }

    @Test
    public void testVerbose4() {
        double d = 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        d += 1.0;
        assertEquals(11.0, d, 0.0);
        System.out.println("A");
        System.out.println("B");
        System.out.println("C");
        System.out.println("D");
        System.out.println("E");
        System.out.println("F");
        System.out.println("G");
        System.out.println("H");
        System.out.println("I");
        System.out.println("J");
        assertTrue(d > 0);
    }

    @Test
    public void testVerbose5() {
        Object o = new Object();
        assertNotNull(o);
        int hash = o.hashCode();
        assertTrue(hash != 0);
        String str = o.toString();
        assertNotNull(str);
        System.out.println("Msg 1");
        System.out.println("Msg 2");
        System.out.println("Msg 3");
        System.out.println("Msg 4");
        System.out.println("Msg 5");
        System.out.println("Msg 6");
        System.out.println("Msg 7");
        System.out.println("Msg 8");
        System.out.println("Msg 9");
        System.out.println("Msg 10");
        System.out.println("Msg 11");
        System.out.println("Msg 12");
        System.out.println("Msg 13");
        System.out.println("Msg 14");
        System.out.println("Msg 15");
        assertTrue(true);
    }

    @Test
    public void testVerbose6() {
        boolean b = true;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        b = !b;
        assertTrue(b);
        System.out.println("1");
        System.out.println("2");
        System.out.println("3");
        System.out.println("4");
        System.out.println("5");
        System.out.println("6");
        System.out.println("7");
        System.out.println("8");
        System.out.println("9");
        System.out.println("10");
        System.out.println("11");
        System.out.println("12");
    }

    @Test
    public void testVerbose7() {
        long l = 0;
        l++; l++; l++; l++; l++; l++; l++; l++; l++; l++;
        l++; l++; l++; l++; l++; l++; l++; l++; l++; l++;
        assertEquals(20, l);
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
        System.out.println("X");
    }

    @Test
    public void testVerbose8() {
        String x = "x";
        x = x + x;
        x = x + x;
        x = x + x;
        x = x + x;
        assertEquals(16, x.length());
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
        System.out.println("Y");
    }

    @Test
    public void testVerbose9() {
        int i = 0;
        i = i + 1;
        i = i + 2;
        i = i + 3;
        i = i + 4;
        i = i + 5;
        assertEquals(15, i);
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
        System.out.println("Z");
    }

    @Test
    public void testVerbose10() {
        char c = 'a';
        c++; c++; c++; c++; c++;
        assertEquals('f', c);
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
        System.out.println("W");
    }
}
