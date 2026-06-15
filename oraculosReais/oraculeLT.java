// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanPropertyValueEqualsPredicateTest.java
/**
 * Test evaluate with simple String property and null values.
 */
@Test
void testEvaluateWithSimpleStringPropertyWithNullValues() {
    final BeanPropertyValueEqualsPredicate<TestBean, String> predicate = new BeanPropertyValueEqualsPredicate<>(
            "stringProperty", null);
    assertTrue(predicate.test(new TestBean((String) null)));
    assertFalse(predicate.test(new TestBean("bar")));
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/MethodUtilsTest.java
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
     * Tests whether a write method can be queried if it is defined in the
     * descriptor.
     */
    @Test
    void testGetWriteMethodDefined() {
        final BeanIntrospectionData data = setUpData();
        final PropertyDescriptor pd = fetchTestDescriptor(data);
        assertNotNull(pd.getWriteMethod(), "No write method");
        assertEquals(pd.getWriteMethod(), data.getWriteMethod(BEAN_CLASS, pd), "Wrong write method");
    }

    // .
    // .
    // .

}

// commons-collections/src/test/java/org/apache/commons/collections4/ArrayStackTest.java
@SuppressWarnings("deprecation") // we test a deprecated class
public class ArrayStackTest<E> extends AbstractArrayListTest<E> {

    @Override
    public String getCompatibilityVersion() {
        return "4";
    }

    @Override
    public ArrayStack<E> makeObject() {
        return new ArrayStack<>();
    }

    @Test
    void testNewStack() {
        final ArrayStack<E> stack = makeObject();
        assertTrue(stack.empty(), "New stack is empty");
        assertEquals(0, stack.size(), "New stack has size zero");

        assertThrows(EmptyStackException.class, () -> stack.peek());

        assertThrows(EmptyStackException.class, () -> stack.pop());
    }

    // .
    // .
    // .

}

// commons-collections/src/test/java/org/apache/commons/collections4/bloomfilter/BloomFilterExtractorTest.java
class BloomFilterExtractorTest {

    @Test
    void testFlattenEmpty() {
        assertThrows(NullPointerException.class, () -> BloomFilterExtractor.fromBloomFilterArray().flatten());
    }

    // .
    // .
    // .

}

// commons-collections/src/test/java/org/apache/commons/collections4/map/StaticBucketMapTest.java
public class StaticBucketMapTest<K, V> extends AbstractIterableMapTest<K, V> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFailFastExpected() {
        return false;
    }

    @Override
    public StaticBucketMap<K, V> makeObject() {
        return new StaticBucketMap<>(30);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_containsKey_nullMatchesIncorrectly() {
        final StaticBucketMap<K, V> map = new StaticBucketMap<>(17);
        map.put(null, (V) "A");
        assertTrue(map.containsKey(null));
        // loop so we find a string that is in the same bucket as the null
        for (int i = 'A'; i <= 'Z'; i++) {
            final String str = String.valueOf((char) i);
            assertFalse(map.containsKey(str), "String: " + str);
        }
    }

    // .
    // .
    // .

}

// commons-collections/src/test/java/org/apache/commons/collections4/EnumerationUtilsTest.java
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

// commons-compress/src/test/java/org/apache/commons/compress/utils/ParsingUtilsTest.java
class ParsingUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = { Integer.MIN_VALUE + "1", "x.x", "9e999", "1.1", "one", Integer.MAX_VALUE + "1" })
    void testParseIntValueInvalidValues(final String value) {
        assertThrows(IOException.class, () -> ParsingUtils.parseIntValue(value, 10));
    }

    @ParameterizedTest
    @ValueSource(strings = { Integer.MIN_VALUE + "", "-1", "1", "123456", Integer.MAX_VALUE + "" })
    void testParseIntValueValidValues(final String value) throws Exception {
        assertEquals(Long.parseLong(value), ParsingUtils.parseIntValue(value, 10));
    }

    // .
    // .
    // .

}

// commons-compress/src/test/java/org/apache/commons/compress/utils/BitInputStreamTest.java
class BitInputStreamTest {

    private ByteArrayInputStream getStream() {
        return new ByteArrayInputStream(new byte[] { (byte) 0xF8, // 11111000
                0x40, // 01000000
                0x01, // 00000001
                0x2F }); // 00101111
    }

    @Test
    void testAlignWithByteBoundaryWhenAtBoundary() throws Exception {
        try (BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN)) {
            assertEquals(0xF8, bis.readBits(8));
            bis.alignWithByteBoundary();
            assertEquals(0, bis.readBits(4));
        }
    }

    @Test
    void testAlignWithByteBoundaryWhenNotAtBoundary() throws Exception {
        try (BitInputStream bis = new BitInputStream(getStream(), ByteOrder.LITTLE_ENDIAN)) {
            assertEquals(0x08, bis.readBits(4));
            assertEquals(4, bis.bitsCached());
            bis.alignWithByteBoundary();
            assertEquals(0, bis.bitsCached());
            assertEquals(0, bis.readBits(4));
        }
    }

    // .
    // .
    // .

}

// commons-compress/src/test/java/org/apache/commons/compress/archivers/cpio/CpioArchiveEntryTest.java
class CpioArchiveEntryTest {

    @Test
    void testCpioEntrySizeOldAsciiFormatOver4GiB() {
        final CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII);
        entry.setSize(0x1FFFFFFFFL);
    }

    @ParameterizedTest
    @ValueSource(shorts = { CpioConstants.FORMAT_NEW, CpioConstants.FORMAT_NEW_CRC, CpioConstants.FORMAT_OLD_BINARY })
    void testCpioEntrySizeUnder4GiBNotOldAsciiFormat(final short format) {
        final CpioArchiveEntry entry = new CpioArchiveEntry(format);
        assertThrows(IllegalArgumentException.class, () -> entry.setSize(0x1FFFFFFFFL));
    }

    // .
    // .
    // .

}

// ======================= SEM O LAZY TEST ===================

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

// commons-io/src/test/java/org/apache/commons/io/input/ReaderInputStreamTest.java
@ParameterizedTest
@MethodSource("charsetData")
public void testCharsetEncoderFlush(final String charsetName, final String data) throws IOException {
    final Charset charset = Charset.forName(charsetName);
    final byte[] expected = data.getBytes(charset);
    try (InputStream in = new ReaderInputStream(new StringReader(data), charset)) {
        assertEquals(Arrays.toString(expected), Arrays.toString(IOUtils.toByteArray(in)));
    }
    try (InputStream in = ReaderInputStream.builder().setReader(new StringReader(data)).setCharset(charset).get()) {
        assertEquals(Arrays.toString(expected), Arrays.toString(IOUtils.toByteArray(in)));
    }
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/ode/nonstiff/ThreeEighthesStepInterpolatorTest.java
@Test
public void derivativesConsistency()
        throws DimensionMismatchException, NumberIsTooSmallException,
        MaxCountExceededException, NoBracketingException {
    TestProblem3 pb = new TestProblem3();
    double step = (pb.getFinalTime() - pb.getInitialTime()) * 0.001;
    ThreeEighthesIntegrator integ = new ThreeEighthesIntegrator(step);
    StepInterpolatorTestUtils.checkDerivativesConsistency(integ, pb, 0.01, 6.6e-12);
}

// commons-text/src/test/java/org/apache/commons/text/lookup/external/CustomStringSubstitutorTest.java
@Test
void testFencedFiles() throws IOException {
    FileStringLookupTest.testFence(createStringSubstitutor());
}

@Test
void testIsEmptyMap() {
    assertTrue(EmailUtils.isEmpty((Map<?, ?>) null));
    final HashMap<String, String> map = new HashMap<>();
    assertTrue(EmailUtils.isEmpty(map));
    map.put("k", "v");
    assertFalse(EmailUtils.isEmpty(map));
}

@Test
void testIsEmptyString() {
    assertTrue(EmailUtils.isEmpty((String) null));
    assertTrue(EmailUtils.isEmpty(""));
    assertFalse(EmailUtils.isEmpty("a"));
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

// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetBaseNamePathCornerCases() {
    assertNull(FileNameUtils.getBaseName((Path) null));
    assertEquals("foo", FileNameUtils.getBaseName(Paths.get("foo.")));
    assertEquals("", FileNameUtils.getBaseName(Paths.get("bar/.foo")));
}