package br.ufba.arieslinter.oracles;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Oracle for Ignored Test smell.
 * Detection: A test method or class that contains the @Ignore annotation.
 */

// Example 1: Ignored Class
@Ignore("Class level ignore")
class IgnoredClassExample {
    @Test
    public void test1() {}
}

// Example 2: Another Ignored Class
@Ignore
class AnotherIgnoredClass {}

public class IgnoredTestOracle {

    // Example 3
    @Ignore
    @Test
    public void testIgnored1() {
    }

    // Example 4
    @Ignore("Wait for fix")
    @Test
    public void testIgnored2() {
    }

    // Example 5
    @Ignore
    @Test
    public void testIgnored3() {
    }

    // Example 6
    @Ignore
    @Test
    public void testIgnored4() {
    }

    // Example 7
    @Ignore
    @Test
    public void testIgnored5() {
    }

    // Example 8
    @Ignore
    @Test
    public void testIgnored6() {
    }

    // Example 9
    @Ignore
    @Test
    public void testIgnored7() {
    }

    // Example 10
    @Ignore
    @Test
    public void testIgnored8() {
    }

    /**
     * NEGATIVE TEST: This should NOT be detected as Ignored Test.
     */
    @Test
    public void testActive() {
        // active test
    }
}
