package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DependentTestOracle {

    private static int counter = 0;
    private static String sharedState = "";

    @Test
    public void test1UpdateCounter() {
        counter = 10;
        assertTrue(true);
    }

    @Test
    public void test2ReadCounter() {
        assertEquals(10, counter);
    }

    @Test
    public void test3UpdateState() {
        sharedState = "ready";
        assertTrue(true);
    }

    @Test
    public void test4ReadState() {
        assertEquals("ready", sharedState);
    }

    @Test
    public void test5IncrementCounter() {
        counter++;
        assertTrue(true);
    }

    @Test
    public void test6CheckCounter() {
        assertTrue(counter > 10);
    }

    @Test
    public void test7ClearState() {
        sharedState = null;
        assertTrue(true);
    }

    @Test
    public void test8CheckNullState() {
        assertTrue(sharedState == null);
    }

    @Test
    public void test9SetCounterToMax() {
        counter = Integer.MAX_VALUE;
        assertTrue(true);
    }

    @Test
    public void test10CheckCounterMax() {
        assertEquals(Integer.MAX_VALUE, counter);
    }
}
