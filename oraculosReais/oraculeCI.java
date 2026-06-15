// commons-collections/src/test/java/org/apache/commons/collections4/properties/PropertiesFactoryTest.java
class PropertiesFactoryTest extends AbstractPropertiesFactoryTest<Properties> {

    PropertiesFactoryTest() {
        super(PropertiesFactory.INSTANCE);
    }

    @Test
    @Override
    public void testInstance() {
        assertNotNull(PropertiesFactory.INSTANCE);
    }

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

// commons-collections/src/test/java/org/apache/commons/collections4/properties/OrderedPropertiesFactoryTest.java
class OrderedPropertiesFactoryTest extends AbstractPropertiesFactoryTest<OrderedProperties> {

    OrderedPropertiesFactoryTest() {
        super(OrderedPropertiesFactory.INSTANCE);
    }

    @Test
    @Override
    public void testInstance() {
        assertNotNull(OrderedPropertiesFactory.INSTANCE);
    }

}

// commons-collections/src/test/java/org/apache/commons/collections4/properties/AbstractPropertiesFactoryTest.java
public abstract class AbstractPropertiesFactoryTest<T extends Properties> {

    public static Stream<Arguments> getParameters() {
        return Stream.of(
                arguments(".properties"),
                arguments(".xml"));
    }

    private final AbstractPropertiesFactory<T> factory;

    protected AbstractPropertiesFactoryTest(final AbstractPropertiesFactory<T> factory) {
        this.factory = factory;
    }

    // .
    // .
    // .

    @Test
    void testInstance() {
        assertNotNull(PropertiesFactory.INSTANCE);
    }

    @ParameterizedTest
    @MethodSource(value = "getParameters")
    void testLoadClassLoaderMissingResource(final String fileExtension) throws Exception {
        assertNull(factory.load(ClassLoader.getSystemClassLoader(), "missing/test" + fileExtension));
    }

    // .
    // .
    // .

}

// commons-io/src/test/java/org/apache/commons/io/monitor/FileAlterationMonitorTest.java
/**
 * {@link FileAlterationMonitor} Test Case.
 */
class FileAlterationMonitorTest extends AbstractMonitorTest {

    /**
     * Constructs a new test case.
     */
    FileAlterationMonitorTest() {
        listener = new CollectionFileListener(false);
    }

    /**
     * Check all the File Collections have the expected sizes.
     */
    private void checkFile(final String label, final File file, final Collection<File> files) {
        for (int i = 0; i < 20; i++) {
            if (files.contains(file)) {
                return; // found, test passes
            }
            TestUtils.sleepQuietly(pauseTime);
        }
        fail(label + " " + file + " not found");
    }

    /**
     * Test add/remove observers.
     */
    @Test
    void testAddRemoveObservers() {
        FileAlterationObserver[] observers = null;

        // Null Observers
        FileAlterationMonitor monitor = new FileAlterationMonitor(123, observers);
        assertEquals(123, monitor.getInterval(), "Interval");
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[1]");

        // Null Observer
        observers = new FileAlterationObserver[1]; // observer is null
        monitor = new FileAlterationMonitor(456, observers);
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[2]");

        // Null Observer
        monitor.addObserver(null);
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[3]");
        monitor.removeObserver(null);

        // Add Observer
        final FileAlterationObserver observer = new FileAlterationObserver("foo");
        monitor.addObserver(observer);
        final Iterator<FileAlterationObserver> it = monitor.getObservers().iterator();
        assertTrue(it.hasNext(), "Observers[4]");
        assertEquals(observer, it.next(), "Added");
        assertFalse(it.hasNext(), "Observers[5]");

        // Remove Observer
        monitor.removeObserver(observer);
        assertFalse(monitor.getObservers().iterator().hasNext(), "Observers[6]");
    }

    @Test
    void testCollectionConstructor() {
        observer = new FileAlterationObserver("foo");
        final Collection<FileAlterationObserver> observers = Arrays.asList(observer);
        final FileAlterationMonitor monitor = new FileAlterationMonitor(0, observers);
        final Iterator<FileAlterationObserver> iterator = monitor.getObservers().iterator();
        assertEquals(observer, iterator.next());
    }

    // .
    // .
    // .
}

// commons-io/src/test/java/org/apache/commons/io/monitor/FileAlterationObserverTest.java
class FileAlterationObserverTest extends AbstractMonitorTest {

    private static final String PATH_STRING_FIXTURE = "/foo";

    /**
     * Constructs a new instance.
     */
    FileAlterationObserverTest() {
        listener = new CollectionFileListener(true);
    }

    /**
     * Call {@link FileAlterationObserver#checkAndNotify()}.
     */
    protected void checkAndNotify() {
        observer.checkAndNotify();
    }

    private String directoryToUnixString(final FileAlterationObserver observer) {
        return FilenameUtils.separatorsToUnix(observer.getDirectory().toString());
    }

    /**
     * Test add/remove listeners.
     */
    @Test
    void testAddRemoveListeners() {
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(PATH_STRING_FIXTURE)
                .getUnchecked();
        // Null Listener
        observer.addListener(null);
        assertFalse(observer.getListeners().iterator().hasNext(), "Listeners[1]");
        observer.removeListener(null);
        assertFalse(observer.getListeners().iterator().hasNext(), "Listeners[2]");

        // Add Listener
        final FileAlterationListenerAdaptor listener = new FileAlterationListenerAdaptor();
        observer.addListener(listener);
        final Iterator<FileAlterationListener> it = observer.getListeners().iterator();
        assertTrue(it.hasNext(), "Listeners[3]");
        assertEquals(listener, it.next(), "Added");
        assertFalse(it.hasNext(), "Listeners[4]");

        // Remove Listener
        observer.removeListener(listener);
        assertFalse(observer.getListeners().iterator().hasNext(), "Listeners[5]");
    }

    @Test
    void testBuilder_File() {
        final File file = new File(PATH_STRING_FIXTURE);
        final FileAlterationObserver observer = FileAlterationObserver.builder().setFile(file).getUnchecked();
        assertEquals(file, observer.getDirectory());
    }

    @Test
    void testBuilder_File_FileFilter() {
        final File file = new File(PATH_STRING_FIXTURE);
        // @formatter:off
        final FileAlterationObserver observer = FileAlterationObserver.builder()
                .setFile(file)
                .setFileFilter(CanReadFileFilter.CAN_READ)
                .getUnchecked();
        // @formatter:on
        assertEquals(file, observer.getDirectory());
        assertEquals(CanReadFileFilter.CAN_READ, observer.getFileFilter());
    }

    // .
    // .
    // .

}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/stat/descriptive/moment/VectorialCovarianceTest.java
public class VectorialCovarianceTest {
    private double[][] points;

    public VectorialCovarianceTest() {
        points = new double[][] {
                { 1.2, 2.3, 4.5 },
                { -0.7, 2.3, 5.0 },
                { 3.1, 0.0, -3.1 },
                { 6.0, 1.2, 4.2 },
                { -0.7, 2.3, 5.0 }
        };
    }

    @Test
    public void testMismatch() {
        try {
            new VectorialCovariance(8, true).increment(new double[5]);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            Assert.assertEquals(5, dme.getArgument());
            Assert.assertEquals(8, dme.getDimension());
        }
    }

    @Test
    public void testSimplistic() {
        VectorialCovariance stat = new VectorialCovariance(2, true);
        stat.increment(new double[] { -1.0, 1.0 });
        stat.increment(new double[] { 1.0, -1.0 });
        RealMatrix c = stat.getResult();
        Assert.assertEquals(2.0, c.getEntry(0, 0), 1.0e-12);
        Assert.assertEquals(-2.0, c.getEntry(1, 0), 1.0e-12);
        Assert.assertEquals(2.0, c.getEntry(1, 1), 1.0e-12);
    }

    // .
    // .
    // .
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/stat/descriptive/moment/VectorialMeanTest.java
public class VectorialMeanTest {
    private double[][] points;

    public VectorialMeanTest() {
        points = new double[][] {
                { 1.2, 2.3, 4.5 },
                { -0.7, 2.3, 5.0 },
                { 3.1, 0.0, -3.1 },
                { 6.0, 1.2, 4.2 },
                { -0.7, 2.3, 5.0 }
        };
    }

    @Test
    public void testMismatch() {
        try {
            new VectorialMean(8).increment(new double[5]);
            Assert.fail("an exception should have been thrown");
        } catch (DimensionMismatchException dme) {
            Assert.assertEquals(5, dme.getArgument());
            Assert.assertEquals(8, dme.getDimension());
        }
    }

    @Test
    public void testSimplistic() {
        VectorialMean stat = new VectorialMean(2);
        stat.increment(new double[] { -1.0, 1.0 });
        stat.increment(new double[] { 1.0, -1.0 });
        double[] mean = stat.getResult();
        Assert.assertEquals(0.0, mean[0], 1.0e-12);
        Assert.assertEquals(0.0, mean[1], 1.0e-12);
    }

    // .
    // .
    // .

}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/ode/sampling/StepNormalizerTest.java
public class StepNormalizerTest {

    public StepNormalizerTest() {
        pb = null;
        integ = null;
    }

    @Test
    public void testBoundaries()
            throws DimensionMismatchException, NumberIsTooSmallException,
            MaxCountExceededException, NoBracketingException {
        double range = pb.getFinalTime() - pb.getInitialTime();
        setLastSeen(false);
        integ.addStepHandler(new StepNormalizer(range / 10.0,
                new FixedStepHandler() {
                    private boolean firstCall = true;

                    @Override
                    public void init(double t0, double[] y0, double t) {
                    }

                    @Override
                    public void handleStep(double t,
                            double[] y,
                            double[] yDot,
                            boolean isLast) {
                        if (firstCall) {
                            checkValue(t, pb.getInitialTime());
                            firstCall = false;
                        }
                        if (isLast) {
                            setLastSeen(true);
                            checkValue(t, pb.getFinalTime());
                        }
                    }
                }));
        integ.integrate(pb,
                pb.getInitialTime(), pb.getInitialState(),
                pb.getFinalTime(), new double[pb.getDimension()]);
        Assert.assertTrue(lastSeen);
    }

    // .
    // .
    // .

}

// ================= SEM O CONSTRUCTOR INITILIZATION =================

// commons-compress/src/test/java/org/apache/commons/compress/archivers/zip/ZipIoUtilTest.java
@Test
void testWriteFully_whenFullAtOnce_thenSucceed() throws IOException {
    try (SeekableByteChannel channel = mockSeekableByteChannel()) {
        when(channel.write((ByteBuffer) any())).thenAnswer(answer -> {
            ((ByteBuffer) answer.getArgument(0)).position(5);
            return 5;
        }).thenAnswer(answer -> {
            ((ByteBuffer) answer.getArgument(0)).position(6);
            return 6;
        });
        ZipIoUtil.writeAll(channel, ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8)));
        ZipIoUtil.writeAll(channel, ByteBuffer.wrap("world\n".getBytes(StandardCharsets.UTF_8)));
        verify(channel, times(2)).write((ByteBuffer) any());
    }
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

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestAbandonedObjectPool.java
@Test
void testConcurrentInvalidation() throws Exception {
    final int POOL_SIZE = 30;
    pool.setMaxTotal(POOL_SIZE);
    pool.setMaxIdle(POOL_SIZE);
    pool.setBlockWhenExhausted(false);

    // Exhaust the connection pool
    final ArrayList<PooledTestObject> vec = new ArrayList<>();
    for (int i = 0; i < POOL_SIZE; i++) {
        vec.add(pool.borrowObject());
    }

    // Abandon all borrowed objects
    for (final PooledTestObject element : vec) {
        element.setAbandoned(true);
    }

    // Try launching a bunch of borrows concurrently. Abandoned sweep will be
    // triggered for each.
    final int CONCURRENT_BORROWS = 5;
    final Thread[] threads = new Thread[CONCURRENT_BORROWS];
    for (int i = 0; i < CONCURRENT_BORROWS; i++) {
        threads[i] = new ConcurrentBorrower(vec);
        threads[i].start();
    }

    // Wait for all the threads to finish
    for (int i = 0; i < CONCURRENT_BORROWS; i++) {
        threads[i].join();
    }

    // Return all objects that have not been destroyed
    for (final PooledTestObject pto : vec) {
        if (pto.isActive()) {
            pool.returnObject(pto);
        }
    }

    // Now, the number of active instances should be 0
    assertEquals(0, pool.getNumActive(), "numActive should have been 0, was " + pool.getNumActive());
}

// commons-text/src/test/java/org/apache/commons/text/matcher/StringMatcherOnCharSequenceStringTest.java
@Test
void testNoneMatcher() {
    final StringMatcher matcher = StringMatcherFactory.INSTANCE.noneMatcher();
    assertEquals(0, matcher.size());
    assertSame(StringMatcherFactory.INSTANCE.noneMatcher(), matcher);
    //
    assertEquals(0, matcher.isMatch(INPUT1, 0, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 1, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 2, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 3, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 4, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 5, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 6, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 7, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 8, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 9, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 10, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 11, 0, INPUT1.length()));
    assertEquals(0, matcher.isMatch(INPUT1, 12, 0, INPUT1.length()));
    //
    assertEquals(0, matcher.isMatch(INPUT1, 0));
    assertEquals(0, matcher.isMatch(INPUT1, 1));
    assertEquals(0, matcher.isMatch(INPUT1, 2));
    assertEquals(0, matcher.isMatch(INPUT1, 3));
    assertEquals(0, matcher.isMatch(INPUT1, 4));
    assertEquals(0, matcher.isMatch(INPUT1, 5));
    assertEquals(0, matcher.isMatch(INPUT1, 6));
    assertEquals(0, matcher.isMatch(INPUT1, 7));
    assertEquals(0, matcher.isMatch(INPUT1, 8));
    assertEquals(0, matcher.isMatch(INPUT1, 9));
    assertEquals(0, matcher.isMatch(INPUT1, 10));
    assertEquals(0, matcher.isMatch(INPUT1, 11));
    assertEquals(0, matcher.isMatch(INPUT1, 12));
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

// commons-collections/src/test/java/org/apache/commons/collections4/bloomfilter/IndexFilterTest.java
class IndexFilterTest {

    @ParameterizedTest
    @CsvSource({
            "1, 64",
            "2, 64",
            "3, 64",
            "7, 357",
            "7, 17",
    })
    void testFilter(final int k, final int m) {
        final Shape shape = Shape.fromKM(k, m);
        final BitSet used = new BitSet(m);
        for (int n = 0; n < 10; n++) {
            used.clear();
            final List<Integer> consumer = new ArrayList<>();
            final IntPredicate filter = IndexFilter.create(shape, consumer::add);

            // Make random indices; these may be duplicates
            final long seed = ThreadLocalRandom.current().nextLong();
            final SplittableRandom rng = new SplittableRandom(seed);
            for (int i = Math.min(k, m / 2); i-- > 0;) {
                final int bit = rng.nextInt(m);
                // duplicates should not alter the list size
                final int newSize = consumer.size() + (used.get(bit) ? 0 : 1);
                assertTrue(filter.test(bit));
                assertEquals(newSize, consumer.size(),
                        () -> String.format("Bad filter. Seed=%d, bit=%d", seed, bit));
                used.set(bit);
            }

            // The list should have unique entries
            assertArrayEquals(used.stream().toArray(), consumer.stream().mapToInt(i -> (int) i).sorted().toArray());
            final int size = consumer.size();

            // Second observations do not change the list size
            used.stream().forEach(bit -> {
                assertTrue(filter.test(bit));
                assertEquals(size, consumer.size(), () -> String.format("Bad filter. Seed=%d, bit=%d", seed, bit));
            });

            assertThrows(IndexOutOfBoundsException.class, () -> filter.test(m));
            assertThrows(IndexOutOfBoundsException.class, () -> filter.test(-1));
        }
    }

    // .
    // .
    // .

}

// commons-collections/src/test/java/org/apache/commons/collections4/bloomfilter/SimpleBloomFilterTest.java
class SimpleBloomFilterTest extends AbstractBloomFilterTest<SimpleBloomFilter> {

    @Override
    protected SimpleBloomFilter createEmptyFilter(final Shape shape) {
        return new SimpleBloomFilter(shape);
    }

    @Test
    void testMergeShortBitMapExtractor() {
        final SimpleBloomFilter filter = createEmptyFilter(getTestShape());
        // create a bitMapExtractor that returns too few values
        // shape expects 2 longs we are sending 1.
        final BitMapExtractor bitMapExtractor = p -> p.test(2L);
        assertTrue(filter.merge(bitMapExtractor));
        assertEquals(1, filter.cardinality());
    }
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