package br.ufba.arieslinter.oracles;

import org.junit.Test;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Oracle for Sensitive Equality smell.
 * Detection: A test method invokes the toString() method of an object.
 */
public class SensitiveEqualityOracle {

    @Test
    public void test1_BasicToString() {
        Object obj = new Object();
        // Smell: direct call to toString() for comparison
        assertEquals("java.lang.Object", obj.toString().substring(0, 16));
    }

    @Test
    public void test2_DateToString() {
        Date date = new Date(0);
        // Smell: date.toString() is locale and timezone sensitive
        assertTrue(date.toString().contains("1970"));
    }

    @Test
    public void test3_ListToString() {
        List<String> list = new ArrayList<>();
        list.add("item");
        // Smell: list.toString() depends on internal implementation
        assertEquals("[item]", list.get(0).toString()); // Error here too on String
        assertEquals("[item]", list.toString());
    }

    @Test
    public void test4_MapToString() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        // Smell: map.toString()
        assertTrue(map.toString().contains("key=value"));
    }

    @Test
    public void test5_ExceptionToString() {
        Exception e = new Exception("error");
        // Smell: exception.toString()
        assertEquals("java.lang.Exception: error", e.toString());
    }

    @Test
    public void test6_IntegerToString() {
        Integer val = 100;
        // Smell: val.toString()
        assertEquals("100", val.toString());
    }

    @Test
    public void test7_CustomObjectToString() {
        Person p = new Person("John");
        // Smell: p.toString()
        assertEquals("Person{name='John'}", p.toString());
    }

    @Test
    public void test8_StringBuilderToString() {
        StringBuilder sb = new StringBuilder("test");
        // Smell: sb.toString() is often used, but strictly follows the definition
        assertEquals("test", sb.toString());
    }

    @Test
    public void test9_ClassToString() {
        Class<?> clazz = String.class;
        // Smell: clazz.toString()
        assertEquals("class java.lang.String", clazz.toString());
    }

    @Test
    public void test10_ImplicitVsExplicit() {
        Object obj = new Object();
        // Explicit call is the detected smell
        String s = "Obj: " + obj.toString();
        assertNotNull(s);
    }

    /**
     * NEGATIVE TEST: These should NOT be detected as Sensitive Equality 
     * if the linter only looks for explicit .toString() calls.
     * Note: Implicit calls (concatenation) might be missed by simple AST checks.
     */
    @Test
    public void testValid_ImplicitToString() {
        Object obj = new Object();
        // Implicit call via concatenation (usually not detected by simple IDENT/DOT check)
        String s = "Value: " + obj; 
        assertNotNull(s);
    }

    private static class Person {
        String name;
        Person(String name) { this.name = name; }
        @Override public String toString() { return "Person{name='" + name + "'}"; }
    }
}
