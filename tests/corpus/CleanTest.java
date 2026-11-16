package corpus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CleanTest {

    private static final int EXPECTED_SUM = 4;

    @Test
    void sumsTwoNumbers() {
        int result = 2 + 2;

        assertEquals(EXPECTED_SUM, result, "2 + 2 should be 4");
    }
}
