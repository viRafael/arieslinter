package br.ufba.arieslinter.oracles;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle for Constructor Initialization smell.
 * Each class below should be detected as having the smell because they define a constructor.
 */

class Example1Test {
    private String name;
    public Example1Test() {
        this.name = "Test";
    }
    @Test
    public void test() {}
}

class Example2Test {
    private List<String> list;
    public Example2Test() {
        list = new ArrayList<>();
    }
    @Test
    public void test() {}
}

class Example3Test {
    public Example3Test() {
        System.out.println("Initializing test");
    }
    @Test
    public void test() {}
}

class Example4Test {
    private int value;
    Example4Test() {
        this.value = 42;
    }
    @Test
    public void test() {}
}

class Example5Test {
    protected Example5Test() {
        // Protected constructor
    }
    @Test
    public void test() {}
}

class Example6Test {
    private Object resource;
    public Example6Test() {
        this.resource = new Object();
    }
    @Test
    public void test() {}
}

class Example7Test {
    public Example7Test() {
        setupState();
    }
    private void setupState() {}
    @Test
    public void test() {}
}

class Example8Test {
    private double factor;
    public Example8Test() {
        this.factor = 1.5;
    }
    @Test
    public void test() {}
}

class Example9Test {
    public Example9Test() {
        super();
    }
    @Test
    public void test() {}
}

class Example10Test {
    private boolean initialized;
    public Example10Test() {
        this.initialized = true;
    }
    @Test
    public void test() {}
}

// Public class to match filename, though it also has the smell
public class ConstructorInitializationOracle {
    public ConstructorInitializationOracle() {
        // Constructor in the main oracle class
    }
    @Test
    public void test() {}
}

/**
 * Example of correct initialization without the smell.
 * This class should NOT be detected as having the Constructor Initialization smell.
 */
class ValidInitializationTest {
    private String name;
    private List<String> list;

    @org.junit.Before
    public void setUp() {
        this.name = "Test";
        this.list = new ArrayList<>();
    }

    @Test
    public void testMethod() {
        assert name != null;
    }
}
