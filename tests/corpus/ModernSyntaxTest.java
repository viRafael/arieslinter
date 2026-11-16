package corpus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Uses Java 14-17 syntax to expose parser differences between Checkstyle versions. */
class ModernSyntaxTest {

    record Point(int x, int y) { }

    sealed interface Shape permits Circle, Square { }
    record Circle(double r) implements Shape { }
    record Square(double s) implements Shape { }

    @Test
    void switchExpressionAndTextBlock() {
        var text = """
                hello
                world""";
        int len = switch (text.length()) {
            case 11 -> 11;
            default -> 0;
        };
        assertEquals(11, len, "length");
    }

    @Test
    void patternMatching() {
        Object o = new Point(1, 2);
        if (o instanceof Point p) {
            assertEquals(1, p.x(), "x");
        }
    }
}
