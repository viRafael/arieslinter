package br.ufba.arieslinter.checks.constants;

import java.util.Set;

public final class TestAnnotations {
    public static final String TEST = "Test";
    public static final String PARAMETERIZED_TEST = "ParameterizedTest";
    public static final String REPEATED_TEST = "RepeatedTest";
    public static final String TEST_FACTORY = "TestFactory";
    public static final String TEST_TEMPLATE = "TestTemplate";
    public static final String BEFORE = "Before";
    public static final String AFTER = "After";
    public static final String IGNORE = "Ignore";

    public static final Set<String> ALL_TEST_ANNOTATIONS = Set.of(
            TEST, PARAMETERIZED_TEST, REPEATED_TEST, TEST_FACTORY, TEST_TEMPLATE);

    private TestAnnotations() {
    }
}