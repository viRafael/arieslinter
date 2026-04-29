package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Oracle for Default Test smell.
 * Detection: A test class name starts with 'ExampleUnitTest' or 'ExampleInstrumentedTest'.
 */

// Example 1
class ExampleUnitTest {
    @Test
    public void test() { assertEquals(4, 2 + 2); }
}

// Example 2
class ExampleInstrumentedTest {
    @Test
    public void test() { assertEquals(4, 2 + 2); }
}

// Example 3
class ExampleUnitTest_Variation1 {} 

// Example 4
class ExampleUnitTest_Variation2 {} 

// Example 5
class ExampleInstrumentedTest_Variation1 {} 

// Example 6
class ExampleInstrumentedTest_Variation2 {} 

// Example 7
class ExampleUnitTest_Old {}

// Example 8
class ExampleInstrumentedTest_Backup {}

// Example 9
class ExampleUnitTestTest {} 

// Example 10
class ExampleInstrumentedTestTest {} 

public class DefaultTestOracle {
    @Test
    public void test() {}
}

/**
 * Example of a correctly named test class (Negative test case).
 * This class DOES NOT start with the forbidden prefixes.
 */
class ProperNameTest {
    @Test
    public void testMethod() {
        assertEquals(1, 1);
    }
}
/**
 * Another negative test case to verify startsWith precision.
 */
class MyExampleUnitTest {} // This contains the string but DOES NOT start with it.
