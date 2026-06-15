package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Oracle for Duplicate Assert smell.
 * Detection: A test method that contains more than one assertion statement with the same parameters.
 */
public class DuplicateAssertOracle {

    @Test
    public void test1_LiteralDuplicate() {
        // Simple case: same literal values
        assertEquals(10, 10);
        assertEquals(10, 10);
    }

    @Test
    public void test2_VariableDuplicate() {
        // Duplicate using variables
        int expected = 5;
        int actual = 2 + 3;
        assertEquals(expected, actual);
        assertEquals(expected, actual);
    }

    @Test
    public void test3_CopyPasteSmell() {
        // Accidental copy-paste of an entire assertion block
        String message = "Hello";
        assertNotNull(message);
        assertTrue(message.length() > 0);
        
        // Accidental duplicate
        assertNotNull(message);
    }

    @Test
    public void test4_DebuggingActivity() {
        // Developer might have left multiple identical assertions while debugging
        boolean result = true;
        assertTrue(result);
        System.out.println("Debug: result is true");
        assertTrue(result);
    }

    @Test
    public void test5_ComplexExpressionDuplicate() {
        // Same complex expression
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("item");
        assertEquals("item", list.get(0));
        assertEquals("item", list.get(0));
    }

    @Test
    public void test6_GroupedConditionsWithDuplicate() {
        // Grouping multiple conditions but repeating one
        int x = 10, y = 20;
        assertTrue(x < y);
        assertTrue(y > 0);
        assertTrue(x < y); // Duplicate of first one
    }

    @Test
    public void test7_MultipleDuplicates() {
        // More than two identical assertions
        assertNull(null);
        assertNull(null);
        assertNull(null);
    }

    @Test
    public void test8_ObjectReferenceDuplicate() {
        Object obj = new Object();
        assertSame(obj, obj);
        assertSame(obj, obj);
    }

    @Test
    public void test9_BooleanExpressions() {
        int a = 1, b = 2;
        assertTrue(a != b);
        assertTrue(a != b);
    }

    @Test
    public void test10_FailDuplicate() {
        // Rare but possible
        if (false) {
            fail("Should not happen");
            fail("Should not happen");
        }
    }

    /**
     * NEGATIVE TEST: These should NOT be detected as Duplicate Assert
     */
    @Test
    public void testValid_DifferentValues() {
        // Testing the same method with different values is NOT a duplicate assert
        // according to the "same parameters" rule.
        assertEquals(10, 5 + 5);
        assertEquals(20, 10 + 10);
    }

    @Test
    public void testValid_DifferentVariables() {
        int a = 1, b = 1;
        assertEquals(1, a);
        assertEquals(1, b); // Different parameters (a vs b)
    }
}
