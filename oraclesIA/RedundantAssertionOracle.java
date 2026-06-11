package br.ufba.arieslinter.oracles;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Oracle for Redundant Assertion smell.
 * Detection: A test method that contains an assertion statement in which 
 * the expected and actual parameters are the same, or are always true/false.
 */
public class RedundantAssertionOracle {

    @Test
    public void test1_LiteralEquality() {
        // Expected and actual are both literal 1
        assertEquals(1, 1);
    }

    @Test
    public void test2_VariableEquality() {
        int x = 10;
        // Comparing a variable with itself
        assertEquals(x, x);
    }

    @Test
    public void test3_AlwaysTrue() {
        // Boolean literal true is always true
        assertTrue(true);
    }

    @Test
    public void test4_AlwaysFalse() {
        // Boolean literal false is always false
        assertFalse(false);
    }

    @Test
    public void test5_NullRedundancy() {
        // assertNull on literal null
        assertNull(null);
    }

    @Test
    public void test6_NotNullNewObject() {
        // assertNotNull on a freshly instantiated object is always true
        assertNotNull(new Object());
    }

    @Test
    public void test7_StringEquality() {
        // Comparing identical string literals
        assertEquals("test", "test");
    }

    @Test
    public void test8_SameObjectIdentity() {
        Object obj = new Object();
        // Comparing same object reference
        assertSame(obj, obj);
    }

    @Test
    public void test9_ComplexExpressionRedundancy() {
        int a = 5;
        // Structural identity: (a + 1) vs (a + 1)
        assertEquals(a + 1, a + 1);
    }

    @Test
    public void test10_MethodCallRedundancy() {
        List<String> list = new ArrayList<>();
        // Structural identity: list.size() vs list.size()
        assertEquals(list.size(), list.size());
    }

    /**
     * NEGATIVE TEST: These should NOT be detected as Redundant Assertion.
     */
    @Test
    public void testValid_Equality() {
        int expected = 10;
        int actual = 5 + 5;
        assertEquals(expected, actual); // Different parameters, even if values match
    }

    @Test
    public void testValid_Boolean() {
        boolean result = (10 > 5);
        assertTrue(result); // result is a variable, not a literal
    }

    @Test
    public void testValid_NullCheck() {
        Object obj = getSomeObject();
        assertNull(obj); // obj could be null or not
    }

    private Object getSomeObject() { return null; }
}
