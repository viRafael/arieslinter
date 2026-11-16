package corpus;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Junit4Test {

    private String usedByOne;
    private String usedByNone;

    @Before
    public void setUp() {
        usedByOne = "a";
        usedByNone = "b";
    }

    @Ignore
    @Test
    public void ignored() {
        assertEquals("a", usedByOne);
    }

    @Test
    public void other() {
        assertEquals("c", "c");
    }

    @Test
    public void verbose() {
        int a = 1;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        a++;
        assertEquals(33, a);
    }
}
