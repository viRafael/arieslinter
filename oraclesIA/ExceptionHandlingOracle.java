package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Oracle for Exception Handling smell.
 * Detection: A test method that contains either a throw statement or a catch clause.
 */
public class ExceptionHandlingOracle {

    @Test
    public void test1_SimpleCatch() {
        try {
            int x = 1 / 0;
        } catch (ArithmeticException e) {
            assertTrue(true);
        }
    }

    @Test
    public void test2_ExplicitThrow() {
        if (System.currentTimeMillis() < 0) {
            throw new RuntimeException("Should not happen");
        }
    }

    @Test
    public void test3_CatchWithFail() {
        try {
            doSomething();
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void test4_NestedCatch() {
        try {
            try {
                throw new java.io.IOException();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            // caught
        }
    }

    @Test
    public void test5_CatchAndIgnore() {
        try {
            Object o = null;
            o.toString();
        } catch (NullPointerException e) {
            // Ignoring exception is a smell
        }
    }

    @Test
    public void test6_ThrowInLoop() {
        for (int i = 0; i < 5; i++) {
            if (i == 3) throw new IllegalStateException();
        }
    }

    @Test
    public void test7_MultiCatch() {
        try {
            Class.forName("InvalidClass").newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test8_TryCatchFinallyWithCatch() {
        try {
            System.out.println("Try");
        } catch (Exception e) {
            System.err.println("Catch");
        } finally {
            System.out.println("Finally");
        }
    }

    @Test
    public void test9_AnonymousThrow() {
        new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException();
            }
        }.run();
    }

    @Test
    public void test10_ReThrow() {
        try {
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    private void doSomething() throws Exception {}

    /**
     * NEGATIVE TEST: These should NOT be detected as Exception Handling smell.
     * They use JUnit's built-in exception handling or standard declarations.
     */
    @Test(expected = ArithmeticException.class)
    public void testValid_JUnitExpected() {
        int x = 1 / 0;
    }

    @Test
    public void testValid_ThrowsSignature() throws Exception {
        // Declaring 'throws' in signature is fine, only 'throw' inside body is a smell.
        doSomething();
    }
}
