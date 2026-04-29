package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class SleepyTestOracle {

    @Test
    public void testSleep1() throws InterruptedException {
        Thread.sleep(100);
        assertTrue(true);
    }

    @Test
    public void testSleep2() throws InterruptedException {
        Thread.sleep(1000);
        assertTrue(true);
    }

    @Test
    public void testSleep3() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        assertTrue(true);
    }

    @Test
    public void testSleep4() throws InterruptedException {
        Thread.sleep(10);
        assertTrue(true);
    }

    @Test
    public void testSleep5() throws InterruptedException {
        Thread.sleep(2000);
        assertTrue(true);
    }

    @Test
    public void testSleep6() throws InterruptedException {
        Thread.sleep(1500);
        assertTrue(true);
    }

    @Test
    public void testSleep7() throws InterruptedException {
        Thread.sleep(3000);
        assertTrue(true);
    }

    @Test
    public void testSleep8() throws InterruptedException {
        Thread.sleep(5000);
        assertTrue(true);
    }

    @Test
    public void testSleep9() throws InterruptedException {
        Thread.sleep(250);
        assertTrue(true);
    }

    @Test
    public void testSleep10() throws InterruptedException {
        Thread.sleep(50);
        assertTrue(true);
    }
}
