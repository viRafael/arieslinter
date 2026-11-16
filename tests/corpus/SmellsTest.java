package corpus;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SmellsTest {

    private int unusedByMost;

    public SmellsTest() {
        unusedByMost = 5;
    }

    @Test
    void emptyTest() {
    }

    @Test
    void noAssertions() {
        int x = 1 + 1;
    }

    @Test
    void assertionRoulette() {
        assertEquals(1, 1);
        assertTrue(true);
        assertNotNull("a");
    }

    @Test
    void duplicateAssert() {
        String s = "abc";
        assertEquals("abc", s, "same");
        assertEquals("abc", s, "same");
    }

    @Test
    void conditionalLogic() {
        for (int i = 0; i < 3; i++) {
            if (i == 1) {
                assertEquals(1, i, "i");
            }
        }
    }

    @Test
    void sleepy() throws Exception {
        Thread.sleep(500);
        assertTrue(System.currentTimeMillis() > 0, "time");
    }

    @Test
    void printing() {
        System.out.println("debug");
        assertTrue(true, "redundant");
    }

    @Test
    void exceptions() {
        try {
            Integer.parseInt("x");
        } catch (NumberFormatException e) {
            fail("bad");
        }
    }

    @Test
    void mysteryGuest() throws Exception {
        File f = new File("data.txt");
        FileReader r = new FileReader(f);
        assertNotNull(r, "reader");
    }

    @Test
    void sensitiveEquality() {
        Object o = new Object();
        assertEquals("x", o.toString(), "str");
    }

    @Test
    void magicNumber() {
        assertEquals(42, compute(), "value");
    }

    @Disabled
    @Test
    void ignored() {
        assertTrue(false, "never runs");
    }

    private int compute() {
        return 42;
    }
}
