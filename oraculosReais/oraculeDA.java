// commons-pool/src/test/java/org/apache/commons/pool3/TestBaseObjectPool.java
@Test
void testBaseAddObject() {
    try {
        pool = makeEmptyPool(3);
    } catch (final UnsupportedOperationException e) {
        return; // skip this test if unsupported
    }
    try {
        assertEquals(0, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());
        pool.addObject();
        assertEquals(1, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());
        final String obj = pool.borrowObject();
        assertEquals(getNthObject(0), obj);
        assertEquals(0, pool.getNumIdle());
        assertEquals(1, pool.getNumActive());
        pool.returnObject(obj);
        assertEquals(1, pool.getNumIdle());
        assertEquals(0, pool.getNumActive());
    } catch (final UnsupportedOperationException e) {
        return; // skip this test if one of those calls is unsupported
    } finally {
        pool.close();
    }
}

// commons-pool/src/test/java/org/apache/commons/pool3/TestBaseObjectPool.java
@Test
void testBaseBorrowReturn() {
    try {
        pool = makeEmptyPool(3);
    } catch (final UnsupportedOperationException e) {
        return; // skip this test if unsupported
    }
    String obj0 = pool.borrowObject();
    assertEquals(getNthObject(0), obj0);
    String obj1 = pool.borrowObject();
    assertEquals(getNthObject(1), obj1);
    String obj2 = pool.borrowObject();
    assertEquals(getNthObject(2), obj2);
    pool.returnObject(obj2);
    obj2 = pool.borrowObject();
    assertEquals(getNthObject(2), obj2);
    pool.returnObject(obj1);
    obj1 = pool.borrowObject();
    assertEquals(getNthObject(1), obj1);
    pool.returnObject(obj0);
    pool.returnObject(obj2);
    obj2 = pool.borrowObject();
    if (isLifo()) {
        assertEquals(getNthObject(2), obj2);
    }
    if (isFifo()) {
        assertEquals(getNthObject(0), obj2);
    }

    obj0 = pool.borrowObject();
    if (isLifo()) {
        assertEquals(getNthObject(0), obj0);
    }
    if (isFifo()) {
        assertEquals(getNthObject(2), obj0);
    }
    pool.close();
}

// commons-pool/src/test/java/org/apache/commons/pool3/TestBaseObjectPool.java
@Test
void testBaseInvalidateObject() {
    try {
        pool = makeEmptyPool(3);
    } catch (final UnsupportedOperationException e) {
        return; // skip this test if unsupported
    }
    assertEquals(0, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    final String obj0 = pool.borrowObject();
    final String obj1 = pool.borrowObject();
    assertEquals(2, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    pool.invalidateObject(obj0);
    assertEquals(1, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    pool.invalidateObject(obj1);
    assertEquals(0, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    pool.close();
}

// commons-pool/src/test/java/org/apache/commons/pool3/TestBaseObjectPool.java
@Test
void testBaseNumActiveNumIdle() {
    try {
        pool = makeEmptyPool(3);
    } catch (final UnsupportedOperationException e) {
        return; // skip this test if unsupported
    }
    assertEquals(0, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    final String obj0 = pool.borrowObject();
    assertEquals(1, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    final String obj1 = pool.borrowObject();
    assertEquals(2, pool.getNumActive());
    assertEquals(0, pool.getNumIdle());
    pool.returnObject(obj1);
    assertEquals(1, pool.getNumActive());
    assertEquals(1, pool.getNumIdle());
    pool.returnObject(obj0);
    assertEquals(0, pool.getNumActive());
    assertEquals(2, pool.getNumIdle());
    pool.close();
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanPropertyValueEqualsPredicateTest.java
@Test
void testEvaluateWithIndexedProperty() {
    // try a valid index
    BeanPropertyValueEqualsPredicate<TestBean, Object> predicate = new BeanPropertyValueEqualsPredicate<>(
            "intIndexed[0]", expectedIntegerValue);
    final TestBean testBean = new TestBean();
    testBean.setIntIndexed(0, expectedIntegerValue.intValue());
    assertTrue(predicate.test(testBean));
    testBean.setIntIndexed(0, expectedIntegerValue.intValue() - 1);
    assertFalse(predicate.test(testBean));

    // try an invalid index
    predicate = new BeanPropertyValueEqualsPredicate<>("intIndexed[999]", "exception-ahead");

    try {
        assertFalse(predicate.test(testBean));
    } catch (final ArrayIndexOutOfBoundsException e) {
        /* this is what should happen */
    }
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

// commons-collections/src/test/java/org/apache/commons/collections4/queue/CircularFifoQueueTest.java
@Test
@SuppressWarnings("unchecked")
void testCircularFifoQueueCircular() {
    final List<E> list = new ArrayList<>();
    list.add((E) "A");
    list.add((E) "B");
    list.add((E) "C");
    final Queue<E> queue = new CircularFifoQueue<>(list);

    assertTrue(queue.contains("A"));
    assertTrue(queue.contains("B"));
    assertTrue(queue.contains("C"));

    queue.add((E) "D");

    assertFalse(queue.contains("A"));
    assertTrue(queue.contains("B"));
    assertTrue(queue.contains("C"));
    assertTrue(queue.contains("D"));

    assertEquals("B", queue.peek());
    assertEquals("B", queue.remove());
    assertEquals("C", queue.remove());
    assertEquals("D", queue.remove());
}

// commons-collections/src/test/java/org/apache/commons/collections4/queue/CircularFifoQueueTest.java
@Test
@SuppressWarnings("unchecked")
void testRepeatedSerialization() throws Exception {
    // bug 31433
    final CircularFifoQueue<E> b = new CircularFifoQueue<>(2);
    b.add((E) "a");
    assertEquals(1, b.size());
    assertTrue(b.contains("a"));

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    new ObjectOutputStream(bos).writeObject(b);

    final CircularFifoQueue<E> b2 = (CircularFifoQueue<E>) new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray())).readObject();

    assertEquals(1, b2.size());
    assertTrue(b2.contains("a"));
    b2.add((E) "b");
    assertEquals(2, b2.size());
    assertTrue(b2.contains("a"));
    assertTrue(b2.contains("b"));

    bos = new ByteArrayOutputStream();
    new ObjectOutputStream(bos).writeObject(b2);

    final CircularFifoQueue<E> b3 = (CircularFifoQueue<E>) new ObjectInputStream(
            new ByteArrayInputStream(bos.toByteArray())).readObject();

    assertEquals(2, b3.size());
    assertTrue(b3.contains("a"));
    assertTrue(b3.contains("b"));
    b3.add((E) "c");
    assertEquals(2, b3.size());
    assertTrue(b3.contains("b"));
    assertTrue(b3.contains("c"));
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/ArrayRealVectorTest.java
@Test
public void testConstructors() {
    final double[] vec1 = { 1d, 2d, 3d };
    final double[] vec3 = { 7d, 8d, 9d };
    final double[] vec4 = { 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d };
    final Double[] dvec1 = { 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d };

    ArrayRealVector v0 = new ArrayRealVector();
    Assert.assertEquals("testData len", 0, v0.getDimension());

    ArrayRealVector v1 = new ArrayRealVector(7);
    Assert.assertEquals("testData len", 7, v1.getDimension());
    Assert.assertEquals("testData is 0.0 ", 0.0, v1.getEntry(6), 0);

    ArrayRealVector v2 = new ArrayRealVector(5, 1.23);
    Assert.assertEquals("testData len", 5, v2.getDimension());
    Assert.assertEquals("testData is 1.23 ", 1.23, v2.getEntry(4), 0);

    ArrayRealVector v3 = new ArrayRealVector(vec1);
    Assert.assertEquals("testData len", 3, v3.getDimension());
    Assert.assertEquals("testData is 2.0 ", 2.0, v3.getEntry(1), 0);

    ArrayRealVector v3_bis = new ArrayRealVector(vec1, true);
    Assert.assertEquals("testData len", 3, v3_bis.getDimension());
    Assert.assertEquals("testData is 2.0 ", 2.0, v3_bis.getEntry(1), 0);
    Assert.assertNotSame(v3_bis.getDataRef(), vec1);
    Assert.assertNotSame(v3_bis.toArray(), vec1);

    ArrayRealVector v3_ter = new ArrayRealVector(vec1, false);
    Assert.assertEquals("testData len", 3, v3_ter.getDimension());
    Assert.assertEquals("testData is 2.0 ", 2.0, v3_ter.getEntry(1), 0);
    Assert.assertSame(v3_ter.getDataRef(), vec1);
    Assert.assertNotSame(v3_ter.toArray(), vec1);

    ArrayRealVector v4 = new ArrayRealVector(vec4, 3, 2);
    Assert.assertEquals("testData len", 2, v4.getDimension());
    Assert.assertEquals("testData is 4.0 ", 4.0, v4.getEntry(0), 0);
    try {
        new ArrayRealVector(vec4, 8, 3);
        Assert.fail("MathIllegalArgumentException expected");
    } catch (MathIllegalArgumentException ex) {
        // expected behavior
    }

    RealVector v5_i = new ArrayRealVector(dvec1);
    Assert.assertEquals("testData len", 9, v5_i.getDimension());
    Assert.assertEquals("testData is 9.0 ", 9.0, v5_i.getEntry(8), 0);

    ArrayRealVector v5 = new ArrayRealVector(dvec1);
    Assert.assertEquals("testData len", 9, v5.getDimension());
    Assert.assertEquals("testData is 9.0 ", 9.0, v5.getEntry(8), 0);

    ArrayRealVector v6 = new ArrayRealVector(dvec1, 3, 2);
    Assert.assertEquals("testData len", 2, v6.getDimension());
    Assert.assertEquals("testData is 4.0 ", 4.0, v6.getEntry(0), 0);
    try {
        new ArrayRealVector(dvec1, 8, 3);
        Assert.fail("MathIllegalArgumentException expected");
    } catch (MathIllegalArgumentException ex) {
        // expected behavior
    }

    ArrayRealVector v7 = new ArrayRealVector(v1);
    Assert.assertEquals("testData len", 7, v7.getDimension());
    Assert.assertEquals("testData is 0.0 ", 0.0, v7.getEntry(6), 0);

    RealVectorTestImpl v7_i = new RealVectorTestImpl(vec1);

    ArrayRealVector v7_2 = new ArrayRealVector(v7_i);
    Assert.assertEquals("testData len", 3, v7_2.getDimension());
    Assert.assertEquals("testData is 0.0 ", 2.0d, v7_2.getEntry(1), 0);

    ArrayRealVector v8 = new ArrayRealVector(v1, true);
    Assert.assertEquals("testData len", 7, v8.getDimension());
    Assert.assertEquals("testData is 0.0 ", 0.0, v8.getEntry(6), 0);
    Assert.assertNotSame("testData not same object ", v1.getDataRef(), v8.getDataRef());

    ArrayRealVector v8_2 = new ArrayRealVector(v1, false);
    Assert.assertEquals("testData len", 7, v8_2.getDimension());
    Assert.assertEquals("testData is 0.0 ", 0.0, v8_2.getEntry(6), 0);
    Assert.assertEquals("testData same object ", v1.getDataRef(), v8_2.getDataRef());

    ArrayRealVector v9 = new ArrayRealVector(v1, v3);
    Assert.assertEquals("testData len", 10, v9.getDimension());
    Assert.assertEquals("testData is 1.0 ", 1.0, v9.getEntry(7), 0);

    ArrayRealVector v10 = new ArrayRealVector(v2, new RealVectorTestImpl(vec3));
    Assert.assertEquals("testData len", 8, v10.getDimension());
    Assert.assertEquals("testData is 1.23 ", 1.23, v10.getEntry(4), 0);
    Assert.assertEquals("testData is 7.0 ", 7.0, v10.getEntry(5), 0);

    ArrayRealVector v11 = new ArrayRealVector(new RealVectorTestImpl(vec3), v2);
    Assert.assertEquals("testData len", 8, v11.getDimension());
    Assert.assertEquals("testData is 9.0 ", 9.0, v11.getEntry(2), 0);
    Assert.assertEquals("testData is 1.23 ", 1.23, v11.getEntry(3), 0);

    ArrayRealVector v12 = new ArrayRealVector(v2, vec3);
    Assert.assertEquals("testData len", 8, v12.getDimension());
    Assert.assertEquals("testData is 1.23 ", 1.23, v12.getEntry(4), 0);
    Assert.assertEquals("testData is 7.0 ", 7.0, v12.getEntry(5), 0);

    ArrayRealVector v13 = new ArrayRealVector(vec3, v2);
    Assert.assertEquals("testData len", 8, v13.getDimension());
    Assert.assertEquals("testData is 9.0 ", 9.0, v13.getEntry(2), 0);
    Assert.assertEquals("testData is 1.23 ", 1.23, v13.getEntry(3), 0);

    ArrayRealVector v14 = new ArrayRealVector(vec3, vec4);
    Assert.assertEquals("testData len", 12, v14.getDimension());
    Assert.assertEquals("testData is 9.0 ", 9.0, v14.getEntry(2), 0);
    Assert.assertEquals("testData is 1.0 ", 1.0, v14.getEntry(3), 0);
}

// ===================== SEM DUPLICATE ASSERT ====================

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestGenericObjectPool.java
@SuppressWarnings("deprecation")
@Test
void testAbandonedPool() throws TestException, InterruptedException {
    final GenericObjectPoolConfig<String> config = new GenericObjectPoolConfig<>();
    config.setJmxEnabled(false);
    GenericObjectPool<String, TestException> abandoned = new GenericObjectPool<>(simpleFactory, config);
    abandoned.setDurationBetweenEvictionRuns(Duration.ofMillis(100)); // Starts evictor
    assertEquals(abandoned.getRemoveAbandonedTimeoutDuration(), abandoned.getRemoveAbandonedTimeoutDuration());

    // This is ugly, but forces GC to hit the pool
    final WeakReference<GenericObjectPool<String, TestException>> ref = new WeakReference<>(abandoned);
    abandoned = null;
    while (ref.get() != null) {
        System.gc();
        Thread.sleep(100);
    }
}

// commons-csv/src/test/java/org/apache/commons/csv/CSVRecordTest.java
@Test
void testIterator() {
    int i = 0;
    for (final String value : record) {
        assertEquals(values[i], value);
        i++;
    }
}

// commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/util/IDNEmailAddressConverterTest.java
@Test
void testRoundTripConversionOfIDNEmailAddress() {
    for (final String email : IDN_EMAIL_ADDRESSES) {
        assertEquals(email, idnEmailConverter.toUnicode(idnEmailConverter.toASCII(email)));
    }
}

// commons-csv/src/test/java/org/apache/commons/csv/issues/JiraCsv264Test.java
@Test
void testJiraCsv264WithGapAllowEmpty() throws IOException {
    final CSVFormat csvFormat = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_EMPTY)
            .setAllowMissingColumnNames(true)
            .get();
    try (StringReader reader = new StringReader(CSV_STRING_GAP); CSVParser parser = csvFormat.parse(reader)) {
        // empty
    }
}

// commons-io/src/test/java/org/apache/commons/io/comparator/DirectoryFileComparatorTest.java
/**
 * Test the comparator array sort.
 */
@Override
void testSortList() {
    // skip sort test
}

// commons-csv/src/test/java/org/apache/commons/csv/CSVParserTest.java
@Test
void testParseNullFileFormat() {
    assertThrows(NullPointerException.class,
            () -> CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
}

// commons-compress/src/test/java/org/apache/commons/compress/harmony/pack200/BHSDCodecTest.java
@ParameterizedTest
@MethodSource("encodeDecodeRange")
void testEncodeDecode(final int i) throws IOException, Pack200Exception {
    final BHSDCodec codec = (BHSDCodec) CodecEncoding.getCodec(i, null, null);

    if (!codec.isDelta()) {
        // Test encode-decode with a selection of numbers within the
        // range of the codec
        final long largest = codec.largest();
        long smallest = codec.isSigned() ? codec.smallest() : 0;
        if (smallest < Integer.MIN_VALUE) {
            smallest = Integer.MIN_VALUE;
        }
        final long difference = (largest - smallest) / 4;
        for (long j = smallest; j <= largest; j += difference) {
            if (j > Integer.MAX_VALUE) {
                break;
            }
            final byte[] encoded = codec.encode((int) j, 0);
            long decoded = 0;
            try {
                decoded = codec.decode(new ByteArrayInputStream(encoded), 0);
            } catch (final EOFException e) {
                System.out.println(e);
            }
            if (j != decoded) {
                fail("Failed with codec: " + i + ", " + codec + " expected: " + j + ", got: " + decoded);
            }
        }
    }

    // Test encode-decode with 0
    assertEquals(0, codec.decode(new ByteArrayInputStream(codec.encode(0, 0)), 0));
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

// commons-compress/src/test/java/org/apache/commons/compress/archivers/zip/StreamCompressorTest.java
@Test
void testDeflatedEntries() throws Exception {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (StreamCompressor sc = StreamCompressor.create(baos)) {
        sc.deflate(new ByteArrayInputStream("AAAAAABBBBBB".getBytes()), ZipEntry.DEFLATED);
        assertEquals(12, sc.getBytesRead());
        assertEquals(8, sc.getBytesWrittenForLastEntry());
        assertEquals(3299542, sc.getCrc32());

        final byte[] actuals = baos.toByteArray();
        final byte[] expected = { 115, 116, 4, 1, 39, 48, 0, 0 };
        // Note that this test really asserts stuff about the java Deflater, which might
        // be a little bit brittle
        assertArrayEquals(expected, actuals);
    }
}