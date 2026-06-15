package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MagicNumberOracle {

    @Test
    public void testMagic1() {
        assertEquals(42, 40 + 2);
    }

    @Test
    public void testMagic2() {
        assertTrue(100 > 50);
    }

    @Test
    public void testMagic3() {
        assertEquals(3.14159, 22.0/7.0, 0.01);
    }

    @Test
    public void testMagic4() {
        assertEquals(1024, Math.pow(2, 10), 0);
    }

    @Test
    public void testMagic5() {
        assertTrue(500 < 1000);
    }

    @Test
    public void testMagic6() {
        assertEquals(12345, Integer.parseInt("12345"));
    }

    @Test
    public void testMagic7() {
        assertEquals(99, 100 - 1);
    }

    @Test
    public void testMagic8() {
        assertTrue(7 == 7);
    }

    @Test
    public void testMagic9() {
        assertEquals(88, 8 * 11);
    }

    @Test
    public void testMagic10() {
        assertEquals(1337, 1000 + 337);
    }
}
