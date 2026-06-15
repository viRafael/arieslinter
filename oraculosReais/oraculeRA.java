// commons-collections/src/test/java/org/apache/commons/collections4/IteratorUtilsTest.java
/**
 * Test empty iterator
 */
@Test
void testEmptyIterator() {
    assertSame(EmptyIterator.INSTANCE, IteratorUtils.EMPTY_ITERATOR);
    assertSame(EmptyIterator.RESETTABLE_INSTANCE, IteratorUtils.EMPTY_ITERATOR);
    assertTrue(IteratorUtils.EMPTY_ITERATOR instanceof Iterator);
    assertTrue(IteratorUtils.EMPTY_ITERATOR instanceof ResettableIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR instanceof OrderedIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR instanceof ListIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR instanceof MapIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR.hasNext());
    IteratorUtils.EMPTY_ITERATOR.reset();
    assertSame(IteratorUtils.EMPTY_ITERATOR, IteratorUtils.EMPTY_ITERATOR);
    assertSame(IteratorUtils.EMPTY_ITERATOR, IteratorUtils.emptyIterator());

    assertThrows(NoSuchElementException.class, () -> IteratorUtils.EMPTY_ITERATOR.next());
    assertThrows(IllegalStateException.class, () -> IteratorUtils.EMPTY_ITERATOR.remove());
}

// commons-collections/src/test/java/org/apache/commons/collections4/functors/NullPredicateTest.java
@Test
void testNullPredicate() {
    assertSame(NullPredicate.nullPredicate(), NullPredicate.nullPredicate());
    assertPredicateTrue(NullPredicate.nullPredicate(), null);
}

// commons-collections/src/test/java/org/apache/commons/collections4/bloomfilter/ShapeTest.java
/**
 * Test equality of shape.
 */
@ParameterizedTest
@CsvSource({
        "3, 24",
        "1, 24",
        "1, 1",
        "13, 124",
        "13, 224",
})
void testEqualsAndHashCode(final int k, final int m) {
    final Shape shape1 = Shape.fromKM(k, m);
    assertEquals(shape1, shape1);
    assertEquals(Arrays.hashCode(new int[] { m, k }), shape1.hashCode(),
            "Doesn't match Arrays.hashCode(new int[] {m, k})");
    assertNotEquals(shape1, null);
    assertNotEquals(shape1, "text");
    assertNotEquals(shape1, Integer.valueOf(3));
    assertNotEquals(shape1, Shape.fromKM(k, m + 1));
    assertNotEquals(shape1, Shape.fromKM(k + 1, m));

    // Test this is reproducible
    final Shape shape2 = Shape.fromKM(k, m);
    assertEquals(shape1, shape2);
    assertEquals(shape1.hashCode(), shape2.hashCode());
}

// commons-collections/src/test/java/org/apache/commons/collections4/comparators/BooleanComparatorTest.java
@Test
void testEqualsCompatibleInstance() {
    assertEquals(new BooleanComparator(), new BooleanComparator(false));
    assertEquals(new BooleanComparator(false), new BooleanComparator(false));
    assertEquals(new BooleanComparator(false), BooleanComparator.getFalseFirstComparator());
    assertSame(BooleanComparator.getFalseFirstComparator(), BooleanComparator.booleanComparator(false));

    assertEquals(new BooleanComparator(true), new BooleanComparator(true));
    assertEquals(new BooleanComparator(true), BooleanComparator.getTrueFirstComparator());
    assertSame(BooleanComparator.getTrueFirstComparator(), BooleanComparator.booleanComparator(true));

    assertNotEquals(new BooleanComparator(), new BooleanComparator(true));
    assertNotEquals(new BooleanComparator(true), new BooleanComparator(false));
}

// commons-compress/src/test/java/org/apache/commons/compress/harmony/pack200/CPFloatTest.java
@Test
void testEquals() {
    final CPFloat a = new CPFloat(42);
    final CPFloat b = new CPFloat(42);
    final CPFloat c = new CPFloat(99);
    // Reflexivity
    assertEquals(a, a);
    // Symmetry
    assertEquals(a, b);
    assertEquals(b, a);
    // Inequality
    assertNotEquals(a, c);
    assertNotEquals(c, a);
    // Null and different type
    assertNotEquals(null, a);
    assertNotEquals(a, "42");
}

// commons-io/src/test/java/org/apache/commons/io/ByteOrderMarkTest.java
/** Tests {@link ByteOrderMark#equals(Object)} */
@SuppressWarnings("EqualsWithItself")
@Test
void testEquals_second_method() {
    assertEquals(ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16BE);
    assertEquals(ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16LE);
    assertEquals(ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32BE);
    assertEquals(ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32LE);
    assertEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_8);

    assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE);
    assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE);
    assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_32BE);
    assertNotEquals(ByteOrderMark.UTF_8, ByteOrderMark.UTF_32LE);

    assertEquals(TEST_BOM_1, TEST_BOM_1, "test1 equals");
    assertEquals(TEST_BOM_2, TEST_BOM_2, "test2 equals");
    assertEquals(TEST_BOM_3, TEST_BOM_3, "test3 equals");

    assertNotEquals(TEST_BOM_1, new Object(), "Object not equal");
    assertNotEquals(TEST_BOM_1, new ByteOrderMark("1a", 2), "test1-1 not equal");
    assertNotEquals(TEST_BOM_1, new ByteOrderMark("1b", 1, 2), "test1-2 not test2");
    assertNotEquals(TEST_BOM_2, new ByteOrderMark("2", 1, 1), "test2 not equal");
    assertNotEquals(TEST_BOM_3, new ByteOrderMark("3", 1, 2, 4), "test3 not equal");
}

// commons-io/src/test/java/org/apache/commons/io/file/CleaningPathVisitorTest.java
/**
 * Tests a directory with one file of size 1.
 */
@ParameterizedTest
@MethodSource("cleaningPathVisitors")
void testCleanFolders1FileSize1(final CleaningPathVisitor visitor) throws IOException {
    PathUtils.copyDirectory(Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1"), tempDir);
    final CleaningPathVisitor visitFileTree = PathUtils.visitFileTree(visitor, tempDir);
    assertCounts(1, 1, 1, visitFileTree);
    assertSame(visitor, visitFileTree);
    //
    assertNotEquals(visitFileTree, CleaningPathVisitor.withLongCounters());
    assertNotEquals(visitFileTree.hashCode(), CleaningPathVisitor.withLongCounters().hashCode());
    assertEquals(visitFileTree, visitFileTree);
    assertEquals(visitFileTree.hashCode(), visitFileTree.hashCode());
}

// commons-lang/src/test/java/org/apache/commons/lang3/tuple/MutablePairTest.java
@Test
void testEquals_third_method() {
    assertEquals(MutablePair.of(null, "foo"), MutablePair.of(null, "foo"));
    assertNotEquals(MutablePair.of("foo", 0), MutablePair.of("foo", null));
    assertNotEquals(MutablePair.of("foo", "bar"), MutablePair.of("xyz", "bar"));

    final MutablePair<String, String> p = MutablePair.of("foo", "bar");
    assertEquals(p, p);
    assertNotEquals(p, new Object());
}

// commons-lang/src/test/java/org/apache/commons/lang3/mutable/MutableBooleanTest.java
@Test
void testEquals_fourth_method() {
    final MutableBoolean mutBoolA = new MutableBoolean(false);
    final MutableBoolean mutBoolB = new MutableBoolean(false);
    final MutableBoolean mutBoolC = new MutableBoolean(true);

    assertEquals(mutBoolA, mutBoolA);
    assertEquals(mutBoolA, mutBoolB);
    assertEquals(mutBoolB, mutBoolA);
    assertEquals(mutBoolB, mutBoolB);
    assertNotEquals(mutBoolA, mutBoolC);
    assertNotEquals(mutBoolB, mutBoolC);
    assertEquals(mutBoolC, mutBoolC);
    assertNotEquals(null, mutBoolA);
    assertNotEquals(mutBoolA, Boolean.FALSE);
    assertNotEquals("false", mutBoolA);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/RealVectorAbstractTest.java
@Test
public void testEquals_fifth_method() {
    final RealVector v = create(new double[] { 0, 1, 2 });

    Assert.assertEquals(v, v);
    Assert.assertEquals(v, v.copy());
    Assert.assertNotEquals(v, null);
    Assert.assertNotEquals(v, v.getSubVector(0, v.getDimension() - 1));
    Assert.assertEquals(v, v.getSubVector(0, v.getDimension()));
}

// ======================== SEM O REDUNDANT ASSERTION =================

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/converters/DateLocaleConverterTest.java
@Test
void testInvalidDate() {

    converter = DateLocaleConverter.buildDer().setLocale(defaultLocale).get();

    try {
        converter.convert("01/10/2004", "dd-MM-yyyy");
    } catch (final ConversionException e) {
        assertEquals("Error parsing date '01/10/2004' at position = 2", e.getMessage(), "Parse Error");
    }

    try {
        converter.convert("01-10-2004X", "dd-MM-yyyy");
    } catch (final ConversionException e) {
        assertEquals("Date '01-10-2004X' contains unparsed characters from position = 10", e.getMessage(),
                "Parse Length");
    }

}

// commons-text/src/test/java/org/apache/commons/text/lookup/DefaultStringLookupTest.java
@Test
void testEnumValues() {
    final Map<String, StringLookup> stringLookupMap = new HashMap<>();
    StringLookupFactory.INSTANCE.addDefaultStringLookups(stringLookupMap);
    // Loop through all enums
    for (final DefaultStringLookup stringLookup : DefaultStringLookup.values()) {
        assertSame(stringLookupMap.get(stringLookup.getKey()), stringLookupMap.get(stringLookup.getKey()));
    }
}

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestLinkedBlockingDeque.java
@Test
@Timeout(value = 10000, unit = TimeUnit.MILLISECONDS)
void testPossibleBug() {

    deque = new LinkedBlockingDeque<>();
    for (int i = 0; i < 3; i++) {
        deque.add(Integer.valueOf(i));
    }

    // This particular sequence of method calls() (there may be others)
    // creates an internal state that triggers an infinite loop in the
    // iterator.
    final Iterator<Integer> iter = deque.iterator();
    iter.next();

    deque.remove(Integer.valueOf(1));
    deque.remove(Integer.valueOf(0));
    deque.remove(Integer.valueOf(2));

    iter.next();
}

// commons-text/src/test/java/org/apache/commons/text/lookup/external/CustomStringSubstitutorTest.java
@Test
void testFencedFiles() throws IOException {
    FileStringLookupTest.testFence(createStringSubstitutor());
}

@Test
public void testScaleInPlace() {
    final double[] test = new double[] { -2.5, -1, 0, 1, 2.5 };
    final double[] correctScaled = new double[] { 5.25, 2.1, 0, -2.1, -5.25 };
    MathArrays.scaleInPlace(-2.1, test);

    // Make sure test has changed
    for (int i = 0; i < test.length; i++) {
        Assert.assertEquals(correctScaled[i], test[i], 0);
    }
}

@Test(expected = DimensionMismatchException.class)
public void testEbeAddPrecondition() {
    MathArrays.ebeAdd(new double[3], new double[4]);
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/converters/AbstractNumberConverterTest.java
@Test
void testNumberToStringDefault() {
    final NumberConverter<T> converter = makeConverter();

    // Default Number --> String conversion
    assertEquals(numbers[0].toString(), converter.convert(String.class, numbers[0]),
            () -> "Default Convert " + numbers[0]);
    assertEquals(numbers[1].toString(), converter.convert(String.class, numbers[1]),
            () -> "Default Convert " + numbers[1]);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/analysis/solvers/MullerSolverTest.java
@Test
public void testParameters() {
    UnivariateFunction f = new Sin();
    UnivariateSolver solver = new MullerSolver();

    try {
        // bad interval
        double root = solver.solve(100, f, 1, -1);
        System.out.println("root=" + root);
        Assert.fail("Expecting NumberIsTooLargeException - bad interval");
    } catch (NumberIsTooLargeException ex) {
        // expected
    }
    try {
        // no bracketing
        solver.solve(100, f, 2, 3);
        Assert.fail("Expecting NoBracketingException - no bracketing");
    } catch (NoBracketingException ex) {
        // expected
    }
}

// commons-email/commons-email2-javax/src/test/java/org/apache/commons/mail2/javax/EmailTest.java
@Test
void testCorrectContentTypeForPNG() throws Exception {
    email.setHostName(strTestMailServer);
    email.setSmtpPort(getMailServerPort());
    email.setFrom("a@b.com");
    email.addTo("c@d.com");
    email.setSubject("test mail");

    email.setCharset(StandardCharsets.ISO_8859_1.name());
    final File png = new File("./target/test-classes/images/logos/maven-feather.png");
    email.setContent(png, "image/png");
    email.buildMimeMessage();
    final MimeMessage msg = email.getMimeMessage();
    msg.saveChanges();
    assertEquals("image/png", msg.getContentType());
}

// commons-io/src/test/java/org/apache/commons/io/input/ReaderInputStreamTest.java
@Test
public void testAvailableAfterClose() throws IOException {
    try (InputStream inputStream = createInputStream()) {
        inputStream.close();
        assertEquals(0, inputStream.available());
    }
}