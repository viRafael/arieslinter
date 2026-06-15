// commons-beanutils/src/test/java/org/apache/commons/beanutils2/MethodUtilsTest.java
class MethodUtilsTest {

    private static void assertMethod(final Method method, final String methodName) {
        assertNotNull(method);
        assertEquals(methodName, method.getName(), "Method is not named correctly");
        assertTrue(Modifier.isPublic(method.getModifiers()), "Method is not public");
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEachc
    public void setUp() {
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test {@link MethodUtils#clearCache()}.
     */
    @Test
    void testClearCache() throws Exception {
        MethodUtils.clearCache(); // make sure it starts empty
        final PublicSubBean bean = new PublicSubBean();
        assertNotNull(MethodUtils.getAccessibleMethod(bean.getClass(), "setFoo", new Class[] { String.class }));
        assertEquals(1, MethodUtils.clearCache());
        assertEquals(0, MethodUtils.clearCache());
    }

    // .
    // .
    // .

    @Test
    void testGetAccessibleMethodFromInterface() {
        Method method;
        // trickier this one - find a method in a direct interface
        method = MethodUtils.getAccessibleMethod(PrivateBeanFactory.create().getClass(), "methodBar", String.class);

        assertMethod(method, "methodBar");
    }

    @Test
    void testGetAccessibleMethodIndirectInterface() {
        Method method;
        // trickier this one - find a method in a indirect interface
        method = MethodUtils.getAccessibleMethod(PrivateBeanFactory.createSubclass().getClass(), "methodBaz",
                String.class);

        assertMethod(method, "methodBaz");
    }

    @Test
    void testNoCaching() throws Exception {
        // no caching
        MethodUtils.setCacheMethods(false);
        final PublicSubBean bean = new PublicSubBean();
        MethodUtils.getAccessibleMethod(bean.getClass(), "setFoo", new Class[] { String.class });
        assertEquals(0, MethodUtils.clearCache());
        // reset default
        MethodUtils.setCacheMethods(true);
    }

    // .
    // .
    // .
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/ConvertUtilsTest.java
class ConvertUtilsTest {

    private void checkIntegerArray(final Object value, final int[] intArray) {

        assertNotNull(value, "Returned value is not null");
        assertEquals(intArray.getClass(), value.getClass(), "Returned value is int[]");
        final int[] results = (int[]) value;
        assertEquals(intArray.length, results.length, "Returned array length");
        for (int i = 0; i < intArray.length; i++) {
            assertEquals(intArray[i], results[i], "Returned array value " + i);
        }

    }

    private void checkStringArray(final Object value, final String[] stringArray) {

        assertNotNull(value, "Returned value is not null");
        assertEquals(stringArray.getClass(), value.getClass(), "Returned value is String[]");
        final String[] results = (String[]) value;
        assertEquals(stringArray.length, results.length, "Returned array length");
        for (int i = 0; i < stringArray.length; i++) {
            assertEquals(stringArray[i], results[i], "Returned array value " + i);
        }

    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() {
        BeanUtilsBean.setInstance(new BeanUtilsBean());
        ConvertUtils.deregister();

    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {
        // No action required
    }

    @Test
    @SuppressWarnings({ "rawtypes" })
    // We need to use raw types in order to test legacy converters
    void testConvertToString() throws Exception {
        final Converter dummyConverter = (type, value) -> value;

        final Converter fooConverter = (type, value) -> "Foo-Converter";

        final DateConverter dateConverter = new DateConverter();
        dateConverter.setLocale(Locale.US);

        final ConvertUtilsBean utils = new ConvertUtilsBean();
        utils.register(dateConverter, java.util.Date.class);
        utils.register(fooConverter, String.class);

        // Convert using registered DateConverter
        final java.util.Date today = new java.util.Date();
        final DateFormat fmt = new SimpleDateFormat("M/d/yy"); /* US Short Format */
        final String expected = fmt.format(today);
        assertEquals(expected, utils.convert(today, String.class), "DateConverter M/d/yy");

        // Date converter doesn't do String conversion - use String Converter
        utils.register(dummyConverter, java.util.Date.class);
        assertEquals("Foo-Converter", utils.convert(today, String.class),
                "Date Converter doesn't do String conversion");

        // No registered Date converter - use String Converter
        utils.deregister(java.util.Date.class);
        assertEquals("Foo-Converter", utils.convert(today, String.class), "No registered Date converter");

        // String Converter doesn't do Strings!!!
        utils.register(dummyConverter, String.class);
        assertEquals(today.toString(), utils.convert(today, String.class), "String Converter doesn't do Strings!!!");

        // No registered Date or String converter - use Object's toString()
        utils.deregister(String.class);
        assertEquals(today.toString(), utils.convert(today, String.class), "Object's toString()");

    }

    // .
    // .
    // .

}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanIntrospectionDataTest.java
class BeanIntrospectionDataTest {

    /** Constant for the test bean class. */
    private static final Class<?> BEAN_CLASS = FluentIntrospectionTestBean.class;

    /** Constant for the name of the test property. */
    private static final String TEST_PROP = "fluentGetProperty";

    /**
     * Creates an array with property descriptors for the test bean class.
     *
     * @return the array with property descriptors
     */
    private static PropertyDescriptor[] fetchDescriptors() {
        final PropertyUtilsBean pub = new PropertyUtilsBean();
        pub.removeBeanIntrospector(SuppressPropertiesBeanIntrospector.SUPPRESS_CLASS);
        pub.addBeanIntrospector(new FluentPropertyBeanIntrospector());
        return pub.getPropertyDescriptors(BEAN_CLASS);
    }

    /**
     * Returns the property descriptor for the test property.
     *
     * @param bid the data object
     * @return the test property descriptor
     */
    private static PropertyDescriptor fetchTestDescriptor(final BeanIntrospectionData bid) {
        return bid.getDescriptor(TEST_PROP);
    }

    /**
     * Creates a test instance which is initialized with default property
     * descriptors.
     *
     * @return the test instance
     */
    private static BeanIntrospectionData setUpData() {
        return new BeanIntrospectionData(fetchDescriptors());
    }

    /**
     * Tests getWriteMethod() if the method cannot be resolved. (This is a corner
     * case which should normally not happen in practice.)
     */
    @Test
    void testGetWriteMethodNonExisting() throws Exception {
        final PropertyDescriptor pd = new PropertyDescriptor(TEST_PROP, BEAN_CLASS.getMethod("getFluentGetProperty"),
                BEAN_CLASS.getMethod("setFluentGetProperty", String.class));
        final Map<String, String> methods = new HashMap<>();
        methods.put(TEST_PROP, "hashCode");
        final BeanIntrospectionData data = new BeanIntrospectionData(new PropertyDescriptor[] { pd }, methods);
        pd.setWriteMethod(null);
        assertNull(data.getWriteMethod(BEAN_CLASS, pd), "Got a write method");
    }

    // .
    // .
    // .

}

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

    // .
    // .
    // .

    /**
     * Tests comparing two beans on a boolean property, then changing the property
     * and testing/
     */
    @Test
    void testSetProperty() {
        final TestBean testBeanA = new TestBean();
        final TestBean testBeanB = new TestBean();

        testBeanA.setDoubleProperty(5.5);
        testBeanB.setDoubleProperty(1.0);

        final BeanComparator<TestBean, String> beanComparator = new BeanComparator<>("doubleProperty");
        final int result1 = beanComparator.compare(testBeanA, testBeanB);

        assertEquals(1, result1, () -> "Comparator did not sort properly.  Result:" + result1);

        testBeanA.setStringProperty("string 1");
        testBeanB.setStringProperty("string 2");

        beanComparator.setProperty("stringProperty");

        final int result2 = beanComparator.compare(testBeanA, testBeanB);

        assertEquals(-1, result2, () -> "Comparator did not sort properly.  Result:" + result2);
    }

    // .
    // .
    // .

}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BasicDynaBeanTest.java
class BasicDynaBeanTest {

    /**
     * The set of property names we expect to have returned when calling
     * {@code getDynaProperties()}. You should update this list when new properties
     * are added
     * to TestBean.
     */
    protected static final String[] properties = { "booleanProperty", "booleanSecond", "doubleProperty",
            "floatProperty", "intArray", "intIndexed",
            "intProperty", "listIndexed", "longProperty", "mappedProperty", "mappedIntProperty", "nullProperty",
            "shortProperty", "stringArray",
            "stringIndexed", "stringProperty", };

    /**
     * The basic test bean for each test.
     */
    protected DynaBean bean;

    /**
     * Create and return a {@code DynaClass} instance for our test {@code DynaBean}.
     */
    protected DynaClass createDynaClass() {
        final int[] intArray = {};
        final String[] stringArray = {};
        final DynaClass dynaClass = new BasicDynaClass("TestDynaClass", null, new DynaProperty[] {
                new DynaProperty("booleanProperty", Boolean.TYPE),
                new DynaProperty("booleanSecond", Boolean.TYPE), new DynaProperty("doubleProperty", Double.TYPE),
                new DynaProperty("floatProperty", Float.TYPE),
                new DynaProperty("intArray", intArray.getClass()), new DynaProperty("intIndexed", intArray.getClass()),
                new DynaProperty("intProperty", Integer.TYPE), new DynaProperty("listIndexed", List.class),
                new DynaProperty("longProperty", Long.TYPE),
                new DynaProperty("mappedProperty", Map.class), new DynaProperty("mappedIntProperty", Map.class),
                new DynaProperty("nullProperty", String.class),
                new DynaProperty("shortProperty", Short.TYPE), new DynaProperty("stringArray", stringArray.getClass()),
                new DynaProperty("stringIndexed", stringArray.getClass()),
                new DynaProperty("stringProperty", String.class), });
        return dynaClass;
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Instantiate a new DynaBean instance
        final DynaClass dynaClass = createDynaClass();
        bean = dynaClass.newInstance();
        // Initialize the DynaBean's property values (like TestBean)
        bean.set("booleanProperty", Boolean.valueOf(true));
        bean.set("booleanSecond", Boolean.valueOf(true));
        bean.set("doubleProperty", Double.valueOf(321.0));
        bean.set("floatProperty", Float.valueOf((float) 123.0));
        final int[] intArray = { 0, 10, 20, 30, 40 };
        bean.set("intArray", intArray);
        final int[] intIndexed = { 0, 10, 20, 30, 40 };
        bean.set("intIndexed", intIndexed);
        bean.set("intProperty", Integer.valueOf(123));
        final List<String> listIndexed = new ArrayList<>();
        listIndexed.add("String 0");
        listIndexed.add("String 1");
        listIndexed.add("String 2");
        listIndexed.add("String 3");
        listIndexed.add("String 4");
        bean.set("listIndexed", listIndexed);
        bean.set("longProperty", Long.valueOf(321));
        final HashMap<String, String> mappedProperty = new HashMap<>();
        mappedProperty.put("First Key", "First Value");
        mappedProperty.put("Second Key", "Second Value");
        bean.set("mappedProperty", mappedProperty);
        final HashMap<String, Integer> mappedIntProperty = new HashMap<>();
        mappedIntProperty.put("One", Integer.valueOf(1));
        mappedIntProperty.put("Two", Integer.valueOf(2));
        bean.set("mappedIntProperty", mappedIntProperty);
        // Property "nullProperty" is not initialized, so it should return null
        bean.set("shortProperty", Short.valueOf((short) 987));
        final String[] stringArray = { "String 0", "String 1", "String 2", "String 3", "String 4" };
        bean.set("stringArray", stringArray);
        final String[] stringIndexed = { "String 0", "String 1", "String 2", "String 3", "String 4" };
        bean.set("stringIndexed", stringIndexed);
        bean.set("stringProperty", "This is a string");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {

        bean = null;

    }

    /**
     * Corner cases on getDynaProperty invalid arguments.
     */
    @Test
    void testGetDescriptorArguments() {
        assertNull(bean.getDynaClass().getDynaProperty("unknown"));
        assertThrows(NullPointerException.class, () -> bean.getDynaClass().getDynaProperty(null));
    }

    // .
    // .
    // .

}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/PropertyUtilsBeanTest.java
/**
 * Tests {@link PropertyUtilsBean}.
 */
class PropertyUtilsBeanTest {

    @Test
    void testGetMappedPropertyDescriptors() throws Exception {
        assertNull(new PropertyUtilsBean().getMappedPropertyDescriptors((Object) null));
        assertNull(new PropertyUtilsBean().getMappedPropertyDescriptors((Class<?>) null));
    }

    @Test
    void testGetPropertyDescriptor() throws Exception {
        assertThrows(NullPointerException.class,
                () -> new PropertyUtilsBean().getPropertyDescriptor((Object) null, null));
        assertThrows(NullPointerException.class, () -> new PropertyUtilsBean().getPropertyDescriptor("", null));
    }

    // .
    // .
    // .

}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/WrapDynaBeanTest.java
class WrapDynaBeanTest extends BasicDynaBeanTest {

    /**
     * Helper method for testing whether basic access to properties works as
     * expected.
     */
    private void checkSimplePropertyAccess() {
        // Invalid getter
        assertThrows(IllegalArgumentException.class, () -> bean.get("invalidProperty"));

        // Invalid setter
        assertThrows(IllegalArgumentException.class, () -> bean.set("invalidProperty", "XYZ"));

        // Set up initial Value
        String testValue = "Original Value";
        final String testProperty = "stringProperty";
        final TestBean instance = (TestBean) ((WrapDynaBean) bean).getInstance();
        instance.setStringProperty(testValue);
        assertEquals(testValue, instance.getStringProperty(), "Check String property");

        // Test Valid Get & Set
        testValue = "Some new value";
        bean.set(testProperty, testValue);
        assertEquals(testValue, instance.getStringProperty(), "Test Set");
        assertEquals(testValue, bean.get(testProperty), "Test Get");
    }

    /**
     * Do serialization and deserialization.
     */
    private Object serializeDeserialize(final Object target, final String text)
            throws IOException, ClassNotFoundException {
        // Serialize the test object
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(target);
            oos.flush();
        }
        // Deserialize the test object
        Object result = null;
        try (final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                final ObjectInputStream ois = new ObjectInputStream(bais)) {
            result = ois.readObject();
        }
        return result;
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        bean = new WrapDynaBean(new TestBean());
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    @AfterEach
    public void tearDown() {
        bean = null;
    }

    /** Tests getInstance method */
    @Test
    void testGetInstance() {
        final AlphaBean alphaBean = new AlphaBean("Now On Air... John Peel");
        final WrapDynaBean dynaBean = new WrapDynaBean(alphaBean);
        final Object wrappedInstance = dynaBean.getInstance();
        assertInstanceOf(AlphaBean.class, wrappedInstance, "Object type is AlphaBean");
        final AlphaBean wrappedAlphaBean = (AlphaBean) wrappedInstance;
        assertSame(wrappedAlphaBean, alphaBean, "Same Object");
    }

    // .
    // .
    // .

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
                assertEquals(newSize, consumer.size(), () -> String.format("Bad filter. Seed=%d, bit=%d", seed, bit));
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

// =================== SEM O EAGER TEST ======================

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/DynaPropertyTest.java
@Test
void testHashCode() {

    final int initialHashCode = testPropertyWithNameAndTypeAndContentType.hashCode();
    assertEquals(testPropertyWithName.hashCode(), testProperty1Duplicate.hashCode());
    assertEquals(testPropertyWithNameAndType.hashCode(), testProperty2Duplicate.hashCode());
    assertEquals(testPropertyWithNameAndTypeAndContentType.hashCode(), testProperty3Duplicate.hashCode());
    assertEquals(initialHashCode, testPropertyWithNameAndTypeAndContentType.hashCode());
}

// commons-csv/src/test/java/org/apache/commons/csv/TokenTest.java
@ParameterizedTest
@EnumSource(Token.Type.class)
void testToString(final Token.Type type) {
    // Should never blow up
    final Token token = new Token();
    final String resetName = Token.Type.INVALID.name();
    assertTrue(token.toString().contains(resetName));
    token.reset();
    assertTrue(token.toString().contains(resetName));
    token.type = null;
    assertFalse(token.toString().isEmpty());
    token.reset();
    token.type = type;
    assertTrue(token.toString().contains(type.name()));
    token.content.setLength(1000);
    assertTrue(token.toString().contains(type.name()));
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/RealVectorTest.java
@Test
@Ignore("Abstract class RealVector does not implement append(RealVector).")
@Override
public void testAppendVector() {
    // Do nothing
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/RealMatrixFormatAbstractTest.java
@Test
@Ignore
public void testParseSimpleWithClosingRowSeparator() {
    String source = "{{1, 1, 1},{1, 1, 1}, }}";
    RealMatrix expected = MatrixUtils.createRealMatrix(new double[][] { { 1, 1, 1 }, { 1, 1, 1 } });
    RealMatrix actual = realMatrixFormat.parse(source);
    Assert.assertEquals(expected, actual);
}

// commons-compress/src/test/java/org/apache/commons/compress/harmony/pack200/ArchiveTest.java
// Test with an archive containing Annotations
@Test
void testWithAnnotations2() throws Exception {
    final File file = createTempFile("annotations", ".jar");
    try (InputStream input = Archive.class.getResourceAsStream("/pack200/annotationsRI.pack.gz");
            JarOutputStream jout = new JarOutputStream(new FileOutputStream(file))) {
        final org.apache.commons.compress.harmony.unpack200.Archive archive = new org.apache.commons.compress.harmony.unpack200.Archive(
                input, jout);
        archive.unpack();
    }
    try (JarFile jarFile = new JarFile(file);
            JarFile jarFile2 = new JarFile(new File(Archive.class.getResource("/pack200/annotationsRI.jar").toURI()))) {
        compareFiles(jarFile, jarFile2);
    }
}

// commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/EmailTest.java
@Test
void testAddBcc() throws Exception {
    // Test Success
    final List<InternetAddress> arrExpected = new ArrayList<>();
    arrExpected.add(new InternetAddress("me@home.com"));
    arrExpected.add(new InternetAddress("joe.doe@apache.org"));
    arrExpected.add(new InternetAddress("someone_here@work-address.com.au"));

    for (final String address : VALID_EMAILS) {
        email.addBcc(address);
    }

    // retrieve and verify
    assertEquals(arrExpected.size(), email.getBccAddresses().size());
    assertEquals(arrExpected.toString(), email.getBccAddresses().toString());
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

// commons-collections/src/test/java/org/apache/commons/collections4/set/UnmodifiableNavigableSetTest.java
@Test
@SuppressWarnings("unchecked")
void testUnmodifiable() {
    setupSet();
    verifyUnmodifiable(set);
    verifyUnmodifiable(set.descendingSet());
    verifyUnmodifiable(set.headSet((E) Integer.valueOf(1)));
    verifyUnmodifiable(set.headSet((E) Integer.valueOf(1), true));
    verifyUnmodifiable(set.tailSet((E) Integer.valueOf(1)));
    verifyUnmodifiable(set.tailSet((E) Integer.valueOf(1), false));
    verifyUnmodifiable(set.subSet((E) Integer.valueOf(1), (E) Integer.valueOf(3)));
    verifyUnmodifiable(set.subSet((E) Integer.valueOf(1), false, (E) Integer.valueOf(3), false));
    verifyUnmodifiable(set.subSet((E) Integer.valueOf(1), true, (E) Integer.valueOf(3), true));
}

// commons-compress/src/test/java/org/apache/commons/compress/archivers/zip/CircularBufferTest.java
@Test
void testCopy() {
    final CircularBuffer buffer = new CircularBuffer(16);

    buffer.put(1);
    buffer.put(2);
    buffer.get();
    buffer.get();

    // copy uninitialized data
    buffer.copy(6, 8);

    for (int i = 2; i < 6; i++) {
        assertEquals(0, buffer.get(), "buffer[" + i + "]");
    }
    assertEquals(1, buffer.get(), "buffer[" + 6 + "]");
    assertEquals(2, buffer.get(), "buffer[" + 7 + "]");
    assertEquals(0, buffer.get(), "buffer[" + 8 + "]");
    assertEquals(0, buffer.get(), "buffer[" + 9 + "]");

    for (int i = 10; i < 14; i++) {
        buffer.put(i);
        buffer.get();
    }

    assertFalse(buffer.available(), "available");

    // copy data and wrap
    buffer.copy(2, 8);

    for (int i = 14; i < 18; i++) {
        assertEquals(i % 2 == 0 ? 12 : 13, buffer.get(), "buffer[" + i + "]");
    }
}