// commons-collections/src/test/java/org/apache/commons/collections4/list/LazyListTest.java
@Test
@Override
public void testCanonicalEmptyCollectionExists() {
    // Factory and Transformer are not serializable
}

// commons-collections/src/test/java/org/apache/commons/collections4/list/LazyListTest.java
@Test
@Override
public void testCanonicalFullCollectionExists() {
    // Factory and Transformer are not serializable
}

// commons-collections/src/test/java/org/apache/commons/collections4/list/LazyListTest.java
@Test
@Override
public void testSerializeDeserializeThenCompare() {
    // Factory and Transformer are not serializable
}

// commons-collections/src/test/java/org/apache/commons/collections4/list/LazyListTest.java
@Test
@Override
public void testSimpleSerialization() {
    // Factory and Transformer are not serializable
}

// commons-collections/src/test/java/org/apache/commons/collections4/keyvalue/UnmodifiableMapEntryTest.java
@Test
@Override
public void testSelfReferenceHandling() {
    // block
}

// commons-collections/src/test/java/org/apache/commons/collections4/keyvalue/TiedMapEntryTest.java
/**
 * Tests the constructors.
 */
@Test
@Override
public void testConstructors() {
    // ignore
}

// commons-collections/src/test/java/org/apache/commons/collections4/map/LazyMapTest.java
@Test
@Override
public void testMapGet() {
    // TODO eliminate need for this via superclass - see svn history.
}

// commons-io/src/test/java/org/apache/commons/io/comparator/DirectoryFileComparatorTest.java
/**
 * Test the comparator array sort.
 */
@Override
@Test
void testSortArray() {
    // skip sort test
}

// commons-io/src/test/java/org/apache/commons/io/comparator/DirectoryFileComparatorTest.java
/**
 * Test the comparator array sort.
 */
@Override
@Test
void testSortList() {
    // skip sort test
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/stat/correlation/SpearmansRankCorrelationTest.java
// Not relevant here
@Override
@Test
public void testStdErrorConsistency() {
}

// ==================== SEM O EMPTY TEST ====================

// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetExtensionPathCornerCases() {
    assertNull(FileNameUtils.getExtension((String) null));
    assertEquals("", FileNameUtils.getExtension(Paths.get("foo.")));
    assertEquals("foo", FileNameUtils.getExtension(Paths.get("bar/.foo")));
}

// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetBaseNamePathCornerCases() {
    assertNull(FileNameUtils.getBaseName((Path) null));
    assertEquals("foo", FileNameUtils.getBaseName(Paths.get("foo.")));
    assertEquals("", FileNameUtils.getBaseName(Paths.get("bar/.foo")));
}

// commons-lang/src/test/java/org/apache/commons/lang3/tuple/MutablePairTest.java
@Test
void testEquals() {
    assertEquals(MutablePair.of(null, "foo"), MutablePair.of(null, "foo"));
    assertNotEquals(MutablePair.of("foo", 0), MutablePair.of("foo", null));
    assertNotEquals(MutablePair.of("foo", "bar"), MutablePair.of("xyz", "bar"));

    final MutablePair<String, String> p = MutablePair.of("foo", "bar");
    assertEquals(p, p);
    assertNotEquals(p, new Object());
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanPropertyValueEqualsPredicateTest.java
@Test
void testEvaluateWithMappedProperty() {
    // try a key that is in the map
    BeanPropertyValueEqualsPredicate<TestBean, String> predicate = new BeanPropertyValueEqualsPredicate<>(
            "mappedProperty(test-key)", "match");
    final TestBean testBean = new TestBean();
    testBean.setMappedProperty("test-key", "match");
    assertTrue(predicate.test(testBean));
    testBean.setMappedProperty("test-key", "no-match");
    assertFalse(predicate.test(testBean));

    // try a key that isn't in the map
    predicate = new BeanPropertyValueEqualsPredicate<>("mappedProperty(invalid-key)", "match");
    assertFalse(predicate.test(testBean));
}

// commons-collections/src/test/java/org/apache/commons/collections4/bloomfilter/SimpleBloomFilterTest.java
@Test
void testMergeShortBitMapExtractor() {
    final SimpleBloomFilter filter = createEmptyFilter(getTestShape());
    // create a bitMapExtractor that returns too few values
    // shape expects 2 longs we are sending 1.
    final BitMapExtractor bitMapExtractor = p -> p.test(2L);
    assertTrue(filter.merge(bitMapExtractor));
    assertEquals(1, filter.cardinality());
}

@Ignore
@Test
public void testConstrainedRosenWithMoreInterpolationPoints() {
    final int dim = 12;
    final double[] startPoint = OptimTestUtils.point(dim, 0.1);
    final double[][] boundaries = boundaries(dim, -1, 2);
    final PointValuePair expected = new PointValuePair(OptimTestUtils.point(dim, 1.0), 0.0);

    // This should have been 78 because in the code the hard limit is
    // said to be
    // ((DIM + 1) * (DIM + 2)) / 2 - (2 * DIM + 1)
    // i.e. 78 in this case, but the test fails for 48, 59, 62, 63, 64,
    // 65, 66, ...
    final int maxAdditionalPoints = 47;

    for (int num = 1; num <= maxAdditionalPoints; num++) {
        doTest(TestFunction.ROSENBROCK.withDimension(dim), startPoint, boundaries,
                GoalType.MINIMIZE,
                1e-12, 1e-6, 2000,
                num,
                expected,
                "num=" + num);
    }
}

@Test
@SuppressWarnings("resource") // not necessary to close these resources
public void testConstructor2() {
    final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
    final byte[] one = new byte[1];
    final byte[] some = new byte[25];

    UnsynchronizedByteArrayInputStream is = newStream(empty, 0);
    assertEquals(empty.length, is.available());
    is = newStream(empty, 1);
    assertEquals(0, is.available());

    is = newStream(one, 0);
    assertEquals(one.length, is.available());
    is = newStream(one, 1);
    assertEquals(0, is.available());
    is = newStream(one, 2);
    assertEquals(0, is.available());

    is = newStream(some, 0);
    assertEquals(some.length, is.available());
    is = newStream(some, 1);
    assertEquals(some.length - 1, is.available());
    is = newStream(some, 10);
    assertEquals(some.length - 10, is.available());
    is = newStream(some, some.length);
    assertEquals(0, is.available());
}

// commons-lang/src/test/java/org/apache/commons/lang3/SystemUtilsTest.java
/**
 * Assumes no security manager exists.
 */
@Test
void testGetUserDir() {
    final File dir = SystemUtils.getUserDir();
    assertNotNull(dir);
    assertTrue(dir.exists());
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/analysis/solvers/UnivariateSolverUtilsTest.java
@Test(expected = MathIllegalArgumentException.class)
public void testSolveBadEndpoints() {
    double root = UnivariateSolverUtils.solve(sin, 4.0, -0.1, 1e-6);
    System.out.println("root=" + root);
}

// commons-text/src/test/java/org/apache/commons/text/lookup/external/CustomStringSubstitutorTest.java
@Test
void testFencedFiles() throws IOException {
    FileStringLookupTest.testFence(createStringSubstitutor());
}