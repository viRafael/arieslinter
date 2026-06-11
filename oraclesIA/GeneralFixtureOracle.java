package br.ufba.arieslinter.oracles;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Oracle for General Fixture smell.
 * Detection: Not all fields instantiated within the setUp method are utilized by all test methods.
 */

// Example 1: Basic General Fixture
class GeneralFixtureExample1Test {
    private String fieldA;
    private String fieldB;

    @Before
    public void setUp() {
        fieldA = "A";
        fieldB = "B";
    }

    @Test
    public void testA() {
        assertNotNull(fieldA);
    }

    @Test
    public void testB() {
        assertNotNull(fieldB);
        // fieldA is not used here -> General Fixture
    }
}

// Example 2: Multiple unused fields
class GeneralFixtureExample2Test {
    private Object obj1, obj2, obj3;

    @Before
    public void setUp() {
        obj1 = new Object();
        obj2 = new Object();
        obj3 = new Object();
    }

    @Test
    public void test1() { assertNotNull(obj1); }
    @Test
    public void test2() { assertNotNull(obj2); }
    @Test
    public void test3() { assertNotNull(obj3); }
    // Each test only uses 1/3 of the fixture
}

// Example 3: One field never used
class GeneralFixtureExample3Test {
    private String data;
    private int count;

    @Before
    public void setUp() {
        data = "test";
        count = 10;
    }

    @Test
    public void test1() { assertNotNull(data); }
    @Test
    public void test2() { assertNotNull(data); }
    // count is never used by any test
}

// Example 4: Large fixture, specific tests
class GeneralFixtureExample4Test {
    private String s1, s2, s3, s4, s5;

    @Before
    public void setUp() {
        s1="1"; s2="2"; s3="3"; s4="4"; s5="5";
    }

    @Test
    public void testSmall() { assertNotNull(s1); }
    @Test
    public void testLarge() { 
        assertNotNull(s1); assertNotNull(s2); assertNotNull(s3); assertNotNull(s4); assertNotNull(s5); 
    }
}

// Example 5: Initializing complex objects not always needed
class GeneralFixtureExample5Test {
    private java.util.List<String> list;
    private java.util.Map<String, String> map;

    @Before
    public void setUp() {
        list = new java.util.ArrayList<>();
        map = new java.util.HashMap<>();
    }

    @Test
    public void testList() { list.add("x"); }
    @Test
    public void testMap() { map.put("k", "v"); }
}

// Example 6: Sibling classes (to count as 10 examples in total for the linter)
class GeneralFixtureExample6Test {
    private int x, y;
    @Before public void setUp() { x=1; y=2; }
    @Test public void testX() { assert x > 0; }
    @Test public void testY() { assert y > 0; }
}

class GeneralFixtureExample7Test {
    private String a, b;
    @Before public void setUp() { a="a"; b="b"; }
    @Test public void testA() { assertNotNull(a); }
    @Test public void testB() { assertNotNull(b); }
}

class GeneralFixtureExample8Test {
    private Object o1, o2;
    @Before public void setUp() { o1=new Object(); o2=new Object(); }
    @Test public void test1() { assertNotNull(o1); }
    @Test public void test2() { assertNotNull(o2); }
}

class GeneralFixtureExample9Test {
    private double d1, d2;
    @Before public void setUp() { d1=1.0; d2=2.0; }
    @Test public void test1() { assert d1 > 0; }
    @Test public void test2() { assert d2 > 0; }
}

class GeneralFixtureExample10Test {
    private boolean b1, b2;
    @Before public void setUp() { b1=true; b2=false; }
    @Test public void test1() { assertTrue(b1); }
    @Test public void test2() { assertFalse(b2); }
}

// Main oracle class (Negative test case - No Smell)
public class GeneralFixtureOracle {
    private String commonField;

    @Before
    public void setUp() {
        commonField = "shared";
    }

    @Test
    public void test1() {
        assertNotNull(commonField);
    }

    @Test
    public void test2() {
        assertEquals("shared", commonField);
    }
    // Every test uses commonField -> No General Fixture
}
