// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanComparatorTest.java
class BeanComparatorTest {

    /**
     * The test beans for each test.
     */
    protected TestBean bean;
    protected AlphaBean alphaBean1;

    protected AlphaBean alphaBean2;

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() {
        bean = new TestBean();
        alphaBean1 = new AlphaBean("alphaBean1");
        alphaBean2 = new AlphaBean("alphaBean2");
    }

    /**
     * Tears down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {
        bean = null;
        alphaBean1 = null;
        alphaBean2 = null;
    }

    /**
     * Tests comparing one bean against itself.
     */
    @Test
    void testCompareBeanAgainstSelf() {
        final BeanComparator<AlphaBean, String> beanComparator = new BeanComparator<>("name");
        final int result = beanComparator.compare(alphaBean1, alphaBean1);
        assertEquals(0, result, () -> "Comparator did not sort properly.  Result:" + result);
    }

    /**
     * Tests comparing two beans via their name using the default Comparator where
     * they have the same value.
     */
    @Test
    void testCompareIdentical() {
        alphaBean1 = new AlphaBean("alphabean");
        alphaBean2 = new AlphaBean("alphabean");
        final BeanComparator<AlphaBean, String> beanComparator = new BeanComparator<>("name");
        final int result = beanComparator.compare(alphaBean1, alphaBean2);
        assertEquals(0, result, () -> "Comparator did not sort properly.  Result:" + result);
    }

    /**
     * Tests comparing two beans on a boolean property, which is not possible.
     */
    @Test
    void testCompareOnBooleanProperty() {
        try {
            final TestBean testBeanA = new TestBean();
            final TestBean testBeanB = new TestBean();

            testBeanA.setBooleanProperty(true);
            testBeanB.setBooleanProperty(false);

            final BeanComparator<TestBean, String> beanComparator = new BeanComparator<>("booleanProperty");
            beanComparator.compare(testBeanA, testBeanB);

            // **** java.lang.Boolean implements Comparable from JDK 1.5 onwards
            // so this test no longer fails
            // fail("BeanComparator should throw an exception when comparing two
            // booleans.");

        } catch (final ClassCastException cce) {
            // Expected result
        }
    }

    // .
    // .
    // .

}

// commons-lang/src/test/java/org/apache/commons/lang3/time/DateUtilsFragmentTest.java
class DateUtilsFragmentTest extends AbstractLangTest {

    private static final Date MAX_DATE = new Date(Long.MAX_VALUE);
    private static final int months = 7; // second final prime before 12
    private static final int days = 23; // second final prime before 31 (and valid)
    private static final int hours = 19; // second final prime before 24
    private static final int minutes = 53; // second final prime before 60
    private static final int seconds = 47; // third final prime before 60
    private static final int millis = 991; // second final prime before 1000

    private Date aDate;
    private Calendar aCalendar;

    @BeforeEach
    public void setUp() {
        aCalendar = Calendar.getInstance();
        aCalendar.set(2005, months, days, hours, minutes, seconds);
        aCalendar.set(Calendar.MILLISECOND, millis);
        aDate = aCalendar.getTime();
    }

    @Test
    void testDateFragmentInLargerUnitWithCalendar() {
        assertEquals(0, DateUtils.getFragmentInDays(aCalendar, Calendar.DATE));
    }

    @Test
    void testDateFragmentInLargerUnitWithDate() {
        assertEquals(0, DateUtils.getFragmentInDays(aDate, Calendar.DATE));
        assertEquals(0, DateUtils.getFragmentInDays(MAX_DATE, Calendar.DATE));
    }

    // .
    // .
    // .

}

// commons-lang/src/test/java/org/apache/commons/lang3/DoubleRangeTest.java
class DoubleRangeTest extends AbstractLangTest {

    private static DoubleRange of(final double min, final double max) {
        return DoubleRange.of(min, max);
    }

    private static DoubleRange of(final Double min, final Double max) {
        return DoubleRange.of(min, max);
    }

    private DoubleRange range1;

    private DoubleRange range2;

    private DoubleRange range3;

    private DoubleRange rangeFull;

    @BeforeEach
    public void setUp() {
        range1 = of(10, 20);
        range2 = of(10, 20);
        range3 = of(-2, -1);
        rangeFull = of(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    @Test
    void testContainsInt() {
        assertFalse(range1.contains(null));

        assertTrue(rangeFull.contains(Double.MIN_VALUE));
        assertTrue(rangeFull.contains(Double.MAX_VALUE));

        assertFalse(range1.contains(5d));
        assertTrue(range1.contains(10d));
        assertTrue(range1.contains(15d));
        assertTrue(range1.contains(20d));
        assertFalse(range1.contains(25d));
    }

    // .
    // .
    // .

}

// commons-math/commons-math-legacy-core/src/test/java/org/apache/commons/math4/legacy/core/dfp/DfpTest.java
public class DfpTest extends ExtendedFieldElementAbstractTest<Dfp> {

    private DfpField field;
    private Dfp pinf;
    private Dfp ninf;
    private Dfp nan;
    private Dfp snan;
    private Dfp qnan;

    @Override
    protected Dfp build(final double x) {
        return field.newDfp(x);
    }

    @Before
    public void setUp() {
        // Some basic setup. Define some constants and clear the status flags
        field = new DfpField(20);
        pinf = field.newDfp("1").divide(field.newDfp("0"));
        ninf = field.newDfp("-1").divide(field.newDfp("0"));
        nan = field.newDfp("0").divide(field.newDfp("0"));
        snan = field.newDfp((byte) 1, Dfp.SNAN);
        qnan = field.newDfp((byte) 1, Dfp.QNAN);
        ninf.getField().clearIEEEFlags();
    }

    @After
    public void tearDown() {
        field = null;
        pinf = null;
        ninf = null;
        nan = null;
        snan = null;
        qnan = null;
    }

    // Generic test function. Takes params x and y and tests them for
    // equality. Then checks the status flags against the flags argument.
    // If the test fail, it prints the desc string
    private void test(Dfp x, Dfp y, int flags, String desc) {
        boolean b = x.equals(y);

        if (!x.equals(y) && !x.unequal(y)) { // NaNs involved
            b = x.toString().equals(y.toString());
        }

        if (x.equals(field.newDfp("0"))) { // distinguish +/- zero
            b = b && (x.toString().equals(y.toString()));
        }

        b = b && x.getField().getIEEEFlags() == flags;

        if (!b) {
            Assert.assertTrue(
                    "assertion failed " + desc + " x = " + x.toString() + " flags = " + x.getField().getIEEEFlags(), b);
        }

        x.getField().clearIEEEFlags();
    }

    @Test
    public void testByteConstructor() {
        Assert.assertEquals("0.", new Dfp(field, (byte) 0).toString());
        Assert.assertEquals("1.", new Dfp(field, (byte) 1).toString());
        Assert.assertEquals("-1.", new Dfp(field, (byte) -1).toString());
        Assert.assertEquals("-128.", new Dfp(field, Byte.MIN_VALUE).toString());
        Assert.assertEquals("127.", new Dfp(field, Byte.MAX_VALUE).toString());
    }

    @Test
    public void testIntConstructor() {
        Assert.assertEquals("0.", new Dfp(field, 0).toString());
        Assert.assertEquals("1.", new Dfp(field, 1).toString());
        Assert.assertEquals("-1.", new Dfp(field, -1).toString());
        Assert.assertEquals("1234567890.", new Dfp(field, 1234567890).toString());
        Assert.assertEquals("-1234567890.", new Dfp(field, -1234567890).toString());
        Assert.assertEquals("-2147483648.", new Dfp(field, Integer.MIN_VALUE).toString());
        Assert.assertEquals("2147483647.", new Dfp(field, Integer.MAX_VALUE).toString());
    }

    @Test
    public void testLongConstructor() {
        Assert.assertEquals("0.", new Dfp(field, 0L).toString());
        Assert.assertEquals("1.", new Dfp(field, 1L).toString());
        Assert.assertEquals("-1.", new Dfp(field, -1L).toString());
        Assert.assertEquals("1234567890.", new Dfp(field, 1234567890L).toString());
        Assert.assertEquals("-1234567890.", new Dfp(field, -1234567890L).toString());
        Assert.assertEquals("-9223372036854775808.", new Dfp(field, Long.MIN_VALUE).toString());
        Assert.assertEquals("9223372036854775807.", new Dfp(field, Long.MAX_VALUE).toString());
    }

    // .
    // .
    // .

}

// commons-math/commons-math-legacy-core/src/test/java/org/apache/commons/math4/legacy/core/jdkmath/AccurateMathTest.java
public class AccurateMathTest {
    // CHECKSTYLE: stop Regexp
    // The above comment allows System.out.print

    private static final double MAX_ERROR_ULP = 0.51;
    private static final int NUMBER_OF_TRIALS = 1000;

    private DfpField field;
    private UniformRandomProvider generator;

    @Before
    public void setUp() {
        field = new DfpField(40);
        generator = RandomSource.MT.create(6176597458463500194L);
    }

    @Test
    public void testMinMaxDouble() {
        double[][] pairs = {
                { -50.0, 50.0 },
                { Double.POSITIVE_INFINITY, 1.0 },
                { Double.NEGATIVE_INFINITY, 1.0 },
                { Double.NaN, 1.0 },
                { Double.POSITIVE_INFINITY, 0.0 },
                { Double.NEGATIVE_INFINITY, 0.0 },
                { Double.NaN, 0.0 },
                { Double.NaN, Double.NEGATIVE_INFINITY },
                { Double.NaN, Double.POSITIVE_INFINITY },
                { Precision.SAFE_MIN, Precision.EPSILON }
        };
        for (double[] pair : pairs) {
            assertEquals("min(" + pair[0] + ", " + pair[1] + ")",
                    Math.min(pair[0], pair[1]),
                    AccurateMath.min(pair[0], pair[1]),
                    Precision.EPSILON);
            assertEquals("min(" + pair[1] + ", " + pair[0] + ")",
                    Math.min(pair[1], pair[0]),
                    AccurateMath.min(pair[1], pair[0]),
                    Precision.EPSILON);
            assertEquals("max(" + pair[0] + ", " + pair[1] + ")",
                    Math.max(pair[0], pair[1]),
                    AccurateMath.max(pair[0], pair[1]),
                    Precision.EPSILON);
            assertEquals("max(" + pair[1] + ", " + pair[0] + ")",
                    Math.max(pair[1], pair[0]),
                    AccurateMath.max(pair[1], pair[0]),
                    Precision.EPSILON);
        }
    }

    // .
    // .
    // .

}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/ml/clustering/KMeansPlusPlusClustererTest.java
public class KMeansPlusPlusClustererTest {

    private UniformRandomProvider random;

    @Before
    public void setUp() {
        random = RandomSource.MT_64.create(1746432956321L);
    }

    /**
     * JIRA: MATH-305
     *
     * Two points, one cluster, one iteration
     */
    @Test
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<DoublePoint> transformer = new KMeansPlusPlusClusterer<>(1, 1);

        DoublePoint[] points = new DoublePoint[] {
                new DoublePoint(new int[] { 1959, 325100 }),
                new DoublePoint(new int[] { 1960, 373200 }), };
        List<? extends Cluster<DoublePoint>> clusters = transformer.cluster(Arrays.asList(points));
        Assert.assertEquals(1, clusters.size());
        Assert.assertEquals(2, clusters.get(0).getPoints().size());
        DoublePoint pt1 = new DoublePoint(new int[] { 1959, 325100 });
        DoublePoint pt2 = new DoublePoint(new int[] { 1960, 373200 });
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt1));
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt2));
    }

    // .
    // .
    // .

}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/random/SobolSequenceGeneratorTest.java
public class SobolSequenceGeneratorTest {
    private static final String RESOURCE_NAME = "/assets/org/apache/commons/math4/legacy/random/new-joe-kuo-6.21201";

    private double[][] referenceValues = {
            { 0.0, 0.0, 0.0 },
            { 0.5, 0.5, 0.5 },
            { 0.75, 0.25, 0.25 },
            { 0.25, 0.75, 0.75 },
            { 0.375, 0.375, 0.625 },
            { 0.875, 0.875, 0.125 },
            { 0.625, 0.125, 0.875 },
            { 0.125, 0.625, 0.375 },
            { 0.1875, 0.3125, 0.9375 },
            { 0.6875, 0.8125, 0.4375 }
    };

    private SobolSequenceGenerator generator;

    @Before
    public void setUp() {
        generator = new SobolSequenceGenerator(3);
    }

    @Test
    public void test3DReference() {
        for (int i = 0; i < referenceValues.length; i++) {
            double[] result = generator.get();
            Assert.assertArrayEquals(referenceValues[i], result, 1e-6);
            Assert.assertEquals(i + 1, generator.getNextIndex());
        }
    }

    @Test
    public void testConstructor() {
        try {
            new SobolSequenceGenerator(0);
            Assert.fail("an exception should have been thrown");
        } catch (OutOfRangeException e) {
            // expected
        }

        try {
            new SobolSequenceGenerator(21202);
            Assert.fail("an exception should have been thrown");
        } catch (OutOfRangeException e) {
            // expected
        }
    }

    // .
    // .
    // .

}

// commons-lang/src/test/java/org/apache/commons/lang3/LongRangeTest.java
class LongRangeTest extends AbstractLangTest {

    private static LongRange of(final int min, final int max) {
        return LongRange.of(min, max);
    }

    private static LongRange of(final Long min, final Long max) {
        return LongRange.of(min, max);
    }

    private LongRange range1;

    private LongRange range2;

    private LongRange range3;

    private LongRange rangeFull;

    @BeforeEach
    public void setUp() {
        range1 = of(10, 20);
        range2 = of(10, 20);
        range3 = of(-2, -1);
        rangeFull = of(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Test
    void testContainsInt() {
        assertFalse(range1.contains(null));

        assertFalse(range1.contains(5L));
        assertTrue(range1.contains(10L));
        assertTrue(range1.contains(15L));
        assertTrue(range1.contains(20L));
        assertFalse(range1.contains(25L));
    }

    @Test
    void testContainsLong() {
        assertFalse(range1.contains(null));

        assertTrue(rangeFull.contains(Long.MAX_VALUE));
        assertTrue(rangeFull.contains(Long.MIN_VALUE));
        assertTrue(rangeFull.contains((long) Integer.MAX_VALUE + 1));
        assertTrue(rangeFull.contains((long) Integer.MIN_VALUE - 1));
        assertTrue(rangeFull.contains((long) Integer.MAX_VALUE));
        assertTrue(rangeFull.contains((long) Integer.MIN_VALUE));

        assertFalse(range1.contains(5L));
        assertTrue(range1.contains(10L));
        assertTrue(range1.contains(15L));
        assertTrue(range1.contains(20L));
    }

    // .
    // .
    // .

}

// commons-io/src/test/java/org/apache/commons/io/input/TeeReaderTest.java
class TeeReaderTest {

    private StringBuilderWriter output;

    private Reader tee;

    @BeforeEach
    public void setUp() {
        final Reader input = new CharSequenceReader("abc");
        output = new StringBuilderWriter();
        tee = new TeeReader(input, output);
    }

    /**
     * Tests that the main {@code Reader} is closed when closing the branch
     * {@code Writer} throws an
     * exception on {@link TeeReader#close()}, if specified to do so.
     */
    @Test
    void testCloseBranchIOException() throws Exception {
        final StringReader goodR = mock(StringReader.class);
        final Writer badW = new ThrowOnCloseWriter();

        final TeeReader nonClosingTr = new TeeReader(goodR, badW, false);
        nonClosingTr.close();
        verify(goodR).close();

        final TeeReader closingTr = new TeeReader(goodR, badW, true);
        assertThrows(IOException.class, closingTr::close);
        verify(goodR, times(2)).close();
    }

    /**
     * Tests that the branch {@code Writer} is closed when closing the main
     * {@code Reader} throws an
     * exception on {@link TeeReader#close()}, if specified to do so.
     */
    @Test
    void testCloseMainIOException() throws IOException {
        final Reader badR = new ThrowOnCloseReader();
        final StringWriter goodW = mock(StringWriter.class);

        final TeeReader nonClosingTr = new TeeReader(badR, goodW, false);
        assertThrows(IOException.class, nonClosingTr::close);
        verify(goodW, never()).close();

        final TeeReader closingTr = new TeeReader(badR, goodW, true);
        assertThrows(IOException.class, closingTr::close);
        verify(goodW).close();
    }

    // .
    // .
    // .

}

// commons-collections/src/test/java/org/apache/commons/collections4/SetUtilsTest.java
class SetUtilsTest {

    private Set<Integer> setA;
    private Set<Integer> setB;

    @BeforeEach
    public void setUp() {
        setA = new HashSet<>();
        setA.add(1);
        setA.add(2);
        setA.add(3);
        setA.add(4);
        setA.add(5);

        setB = new HashSet<>();
        setB.add(3);
        setB.add(4);
        setB.add(5);
        setB.add(6);
        setB.add(7);
    }

    @Test
    void testDifference() {
        final SetView<Integer> set = SetUtils.difference(setA, setB);
        assertEquals(2, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        for (final Integer i : setB) {
            assertFalse(set.contains(i));
        }

        final Set<Integer> set2 = SetUtils.difference(setA, SetUtils.<Integer>emptySet());
        assertEquals(setA, set2);

        assertThrows(NullPointerException.class, () -> SetUtils.difference(setA, null));
        assertThrows(NullPointerException.class, () -> SetUtils.difference(null, setA));
    }

    @Test
    void testDisjunction() {
        final SetView<Integer> set = SetUtils.disjunction(setA, setB);
        assertEquals(4, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(6));
        assertTrue(set.contains(7));
        assertFalse(set.contains(3));
        assertFalse(set.contains(4));
        assertFalse(set.contains(5));

        final Set<Integer> set2 = SetUtils.disjunction(setA, SetUtils.<Integer>emptySet());
        assertEquals(setA, set2);

        assertThrows(NullPointerException.class, () -> SetUtils.disjunction(setA, null));
        assertThrows(NullPointerException.class, () -> SetUtils.disjunction(null, setA));
    }

    @Test
    void testEmptyIfNull() {
        assertTrue(SetUtils.emptyIfNull(null).isEmpty());

        final Set<Long> set = new HashSet<>();
        assertSame(set, SetUtils.emptyIfNull(set));
    }

    @Test
    void testEquals() {
        final Collection<String> data = Arrays.asList("a", "b", "c");

        final Set<String> a = new HashSet<>(data);
        final Set<String> b = new HashSet<>(data);

        assertEquals(a, b);
        assertTrue(SetUtils.isEqualSet(a, b));
        a.clear();
        assertFalse(SetUtils.isEqualSet(a, b));
        assertFalse(SetUtils.isEqualSet(a, null));
        assertFalse(SetUtils.isEqualSet(null, b));
        assertTrue(SetUtils.isEqualSet(null, null));
    }

    // .
    // .
    // .

}

// ================== SEM O GENERAL FIXTURE ====================

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanPropertyValueEqualsPredicateTest.java
@Test
void testEvaluateWithBooleanProperty() {
    final BeanPropertyValueEqualsPredicate<TestBean, Boolean> predicate = new BeanPropertyValueEqualsPredicate<>(
            "booleanProperty", expectedBooleanValue);
    assertTrue(predicate.test(new TestBean(expectedBooleanValue.booleanValue())));
    assertFalse(predicate.test(new TestBean(!expectedBooleanValue.booleanValue())));
}

// commons-collections/src/test/java/org/apache/commons/collections4/properties/SortedPropertiesFactoryTest.java
class SortedPropertiesFactoryTest extends AbstractPropertiesFactoryTest<SortedProperties> {

    SortedPropertiesFactoryTest() {
        super(SortedPropertiesFactory.INSTANCE);
    }

    @Test
    @Override
    public void testInstance() {
        assertNotNull(SortedPropertiesFactory.INSTANCE);
    }

}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/SparseFieldMatrixTest.java
public class SparseFieldMatrixTest {

    // .
    // .
    // .

    public SparseFieldMatrixTest() {
        testDataLU = new Dfp[][] { { Dfp25.of(2), Dfp25.of(5), Dfp25.of(3) },
                { Dfp25.of(.5d), Dfp25.of(-2.5d), Dfp25.of(6.5d) },
                { Dfp25.of(0.5d), Dfp25.of(0.2d), Dfp25.of(.2d) } };
        luDataLUDecomposition = new Dfp[][] { { Dfp25.of(6), Dfp25.of(9), Dfp25.of(8) },
                { Dfp25.of(0), Dfp25.of(5), Dfp25.of(7) },
                { Dfp25.of(0.33333333333333), Dfp25.of(0), Dfp25.of(0.33333333333333) } };
        subTestData = new Dfp[][] { { Dfp25.of(1), Dfp25.of(2), Dfp25.of(3), Dfp25.of(4) },
                { Dfp25.of(1.5), Dfp25.of(2.5), Dfp25.of(3.5), Dfp25.of(4.5) },
                { Dfp25.of(2), Dfp25.of(4), Dfp25.of(6), Dfp25.of(8) },
                { Dfp25.of(4), Dfp25.of(5), Dfp25.of(6), Dfp25.of(7) } };
        subRows31Cols31 = new Dfp[][] { { Dfp25.of(7), Dfp25.of(5) }, { Dfp25.of(4.5), Dfp25.of(2.5) } };
        subRows01Cols23 = new Dfp[][] { { Dfp25.of(3), Dfp25.of(4) }, { Dfp25.of(3.5), Dfp25.of(4.5) } };
        subColumn1 = new Dfp[][] { { Dfp25.of(2) }, { Dfp25.of(2.5) }, { Dfp25.of(4) }, { Dfp25.of(5) } };
        subColumn3 = new Dfp[][] { { Dfp25.of(4) }, { Dfp25.of(4.5) }, { Dfp25.of(8) }, { Dfp25.of(7) } };
    }

    /** test dimensions */
    @Test
    public void testDimensions() {
        SparseFieldMatrix<Dfp> m = createSparseMatrix(testData);
        SparseFieldMatrix<Dfp> m2 = createSparseMatrix(testData2);
        Assert.assertEquals("testData row dimension", 3, m.getRowDimension());
        Assert.assertEquals("testData column dimension", 3, m.getColumnDimension());
        Assert.assertTrue("testData is square", m.isSquare());
        Assert.assertEquals("testData2 row dimension", m2.getRowDimension(), 2);
        Assert.assertEquals("testData2 column dimension", m2.getColumnDimension(), 3);
        Assert.assertFalse("testData2 is not square", m2.isSquare());
    }

    // .
    // .
    // .

}

// commons-csv/src/test/java/org/apache/commons/csv/CSVRecordTest.java
@Test
void testCSVRecordNULLValues() throws IOException {
    try (CSVParser parser = CSVParser.parse("A,B\r\nONE,TWO", CSVFormat.DEFAULT.withHeader())) {
        final CSVRecord csvRecord = new CSVRecord(parser, null, null, 0L, 0L, 0L);
        assertEquals(0, csvRecord.size());
        assertThrows(IllegalArgumentException.class, () -> csvRecord.get("B"));
    }
}

// commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/InputStreamDataSourceTest.java
@Test
void testGetInputStream() throws IOException {
    final byte[] testData = "Test data for InputStream".getBytes();
    final InputStream testInputStream = new ByteArrayInputStream(testData);
    final InputStreamDataSource dataSource = new InputStreamDataSource(testInputStream, "application/octet-stream");
    try (InputStream inputStream = dataSource.getInputStream()) {
        final byte[] readData = new byte[testData.length];
        final int bytesRead = inputStream.read(readData);
        assertEquals(testData.length, bytesRead);
        assertArrayEquals(testData, readData);
    }
}

// commons-collections/src/test/java/org/apache/commons/collections4/iterators/ExtendedIteratorTest.java
class ExtendedIteratorTest {

    /**
     * Collection of {@link Integer}s
     */
    private List<Integer> collectionA;

    @BeforeEach
    public void setUp() {
        collectionA = new ArrayList<>();
        collectionA.add(1);
        collectionA.add(2);
        collectionA.add(3);
        collectionA.add(4);
        collectionA.add(5);
        collectionA.add(6);
    }

    @Test
    void testAndThen() {
        final Iterator<Integer> iter1 = Arrays.asList(1, 2, 3).iterator();
        final Iterator<Integer> iter2 = Arrays.asList(4, 5, 6).iterator();

        final ExtendedIterator<Integer> underTest = ExtendedIterator.create(iter1).andThen(iter2);
        final List<Integer> actual = new ArrayList<>();
        underTest.forEachRemaining(actual::add);
        assertEquals(collectionA, actual);
    }

    // .
    // .
    // .

}

class EnumerationUtilsTest {

    public static final String TO_LIST_FIXTURE = "this is a test";

    @Test
    void testAsIterableFor() {
        final Vector<String> vector = new Vector<>();
        vector.addElement("zero");
        vector.addElement("one");
        final Enumeration<String> en = vector.elements();
        final Iterator<String> iterator = EnumerationUtils.asIterable(en).iterator();
        assertTrue(iterator.hasNext());
        assertEquals("zero", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("one", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testAsIterableForNull() {
        assertThrows(NullPointerException.class,
                () -> EnumerationUtils.asIterable((Enumeration) null).iterator().next());
    }

    // .
    // .
    // .
}

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestEvictionConfig.java
@Test
void testConstructor1s() {
    final EvictionConfig config = new EvictionConfig(Duration.ofMillis(1), Duration.ofMillis(1), 1);
    assertEquals(1, config.getIdleEvictDuration().toMillis());
    assertEquals(1, config.getIdleSoftEvictDuration().toMillis());
    assertEquals(1, config.getMinIdle());
    // toString() should never throw
    assertFalse(config.toString().isEmpty());
}

// commons-lang/src/test/java/org/apache/commons/lang3/concurrent/MultiBackgroundInitializerTest.java
@Test
void testIsInitialized()
        throws ConcurrentException, InterruptedException {
    final AbstractChildBackgroundInitializer childOne = createChildBackgroundInitializer();
    final AbstractChildBackgroundInitializer childTwo = createChildBackgroundInitializer();

    childOne.enableLatch();
    childTwo.enableLatch();

    assertFalse(initializer.isInitialized(), "Initialized without having anything to initialize");

    initializer.addInitializer("child one", childOne);
    initializer.addInitializer("child two", childTwo);
    initializer.start();

    final long startTime = System.currentTimeMillis();
    final long waitTime = 3000;
    final long endTime = startTime + waitTime;
    // wait for the children to start
    while (!childOne.isStarted() || !childTwo.isStarted()) {
        if (System.currentTimeMillis() > endTime) {
            fail("children never started");
            Thread.sleep(PERIOD_MILLIS);
        }
    }

    assertFalse(initializer.isInitialized(), "Initialized with two children running");

    childOne.releaseLatch();
    childOne.get(); // ensure this child finishes initializing
    assertFalse(initializer.isInitialized(), "Initialized with one child running");

    childTwo.releaseLatch();
    childTwo.get(); // ensure this child finishes initializing
    assertTrue(initializer.isInitialized(), "Not initialized with no children running");
}

// commons-lang/src/test/java/org/apache/commons/lang3/concurrent/EventCountCircuitBreakerTest.java
@Test
void testChangeEvents() {
    final EventCountCircuitBreaker breaker = new EventCountCircuitBreaker(OPENING_THRESHOLD, 1,
            TimeUnit.SECONDS);
    final ChangeListener listener = new ChangeListener(breaker);
    breaker.addChangeListener(listener);
    breaker.open();
    breaker.close();
    listener.verify(Boolean.TRUE, Boolean.FALSE);
}