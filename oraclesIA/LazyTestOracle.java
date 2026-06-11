package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Oracle for Lazy Test smell.
 * Detection: Multiple test methods calling the same production method.
 */

// Example 1: Basic Lazy Test (method 'calculate' called by 2 tests)
class LazyTestExample1 {
    Production p = new Production();
    @Test public void test1() { p.calculate(); }
    @Test public void test2() { p.calculate(); }
}

// Example 2: Method 'save' called by 3 tests
class LazyTestExample2 {
    Production p = new Production();
    @Test public void testA() { p.save(); }
    @Test public void testB() { p.save(); }
    @Test public void testC() { p.save(); }
}

// Example 3: Static method called by multiple tests
class LazyTestExample3 {
    @Test public void test1() { Production.staticMethod(); }
    @Test public void test2() { Production.staticMethod(); }
}

// Example 4: Method called with different parameters (still the same method)
class LazyTestExample4 {
    Production p = new Production();
    @Test public void testSmall() { p.process(1); }
    @Test public void testLarge() { p.process(100); }
}

// Example 5: Multiple lazy tests in one class
class LazyTestExample5 {
    Production p = new Production();
    @Test public void testInit1() { p.init(); }
    @Test public void testInit2() { p.init(); }
    @Test public void testRun1() { p.run(); }
    @Test public void testRun2() { p.run(); }
}

// Example 6: Method called in setup-like way within tests
class LazyTestExample6 {
    Production p = new Production();
    @Test public void testValue() { p.load(); assertEquals(10, p.getValue()); }
    @Test public void testStatus() { p.load(); assertTrue(p.isOk()); }
}

// Example 7
class LazyTestExample7 {
    Production p = new Production();
    @Test public void test7a() { p.doWork(); }
    @Test public void test7b() { p.doWork(); }
}

// Example 8
class LazyTestExample8 {
    Production p = new Production();
    @Test public void test8a() { p.cleanup(); }
    @Test public void test8b() { p.cleanup(); }
}

// Example 9
class LazyTestExample9 {
    Production p = new Production();
    @Test public void test9a() { p.refresh(); }
    @Test public void test9b() { p.refresh(); }
}

// Example 10
class LazyTestExample10 {
    Production p = new Production();
    @Test public void test10a() { p.validate(); }
    @Test public void test10b() { p.validate(); }
}

public class LazyTestOracle {
    @Test
    public void testNone() {
        // No lazy test here
    }
}

// Helper class representing production code
class Production {
    void calculate() {}
    void save() {}
    static void staticMethod() {}
    void process(int i) {}
    void init() {}
    void run() {}
    void load() {}
    int getValue() { return 10; }
    boolean isOk() { return true; }
    void doWork() {}
    void cleanup() {}
    void refresh() {}
    void validate() {}
}
