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

// commons-collections/src/test/java/org/apache/commons/collections4/set/ListOrderedSet2Test.java
@Test
@SuppressWarnings("unchecked")
void testOrdering() {
    final ListOrderedSet<E> set = setupSet();
    Iterator<E> it = set.iterator();

    for (int i = 0; i < 10; i++) {
        assertEquals(Integer.toString(i), it.next(), "Sequence is wrong");
    }

    for (int i = 0; i < 10; i += 2) {
        assertTrue(set.remove(Integer.toString(i)), "Must be able to remove int");
    }

    it = set.iterator();
    for (int i = 1; i < 10; i += 2) {
        assertEquals(Integer.toString(i), it.next(), "Sequence is wrong after remove ");
    }

    for (int i = 0; i < 10; i++) {
        set.add((E) Integer.toString(i));
    }

    assertEquals(10, set.size(), "Size of set is wrong!");

    it = set.iterator();
    for (int i = 1; i < 10; i += 2) {
        assertEquals(Integer.toString(i), it.next(), "Sequence is wrong");
    }
    for (int i = 0; i < 10; i += 2) {
        assertEquals(Integer.toString(i), it.next(), "Sequence is wrong");
    }
}

// commons-compress/src/test/java/org/apache/commons/compress/archivers/zip/StreamCompressorTest.java
@Test
void testStoredEntries() throws Exception {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (StreamCompressor sc = StreamCompressor.create(baos)) {
        sc.deflate(new ByteArrayInputStream("A".getBytes()), ZipEntry.STORED);
        sc.deflate(new ByteArrayInputStream("BAD".getBytes()), ZipEntry.STORED);
        assertEquals(3, sc.getBytesRead());
        assertEquals(3, sc.getBytesWrittenForLastEntry());
        assertEquals(344750961, sc.getCrc32());
        sc.deflate(new ByteArrayInputStream("CAFE".getBytes()), ZipEntry.STORED);
        assertEquals("ABADCAFE", baos.toString());
    }
}

// commons-csv/src/test/java/org/apache/commons/csv/issues/JiraCsv288Test.java
@Test
// Before fix:
// expected: <a,b,c,d,,f> but was: <a,b,c,d,|f>
void testParseWithABADelimiter() throws Exception {
    final Reader in = new StringReader("a|~|b|~|c|~|d|~||~|f");
    final StringBuilder stringBuilder = new StringBuilder();
    try (CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.EXCEL);
            CSVParser parser = CSVParser.parse(in, CSVFormat.Builder.create().setDelimiter("|~|").get())) {
        for (final CSVRecord csvRecord : parser) {
            print(csvRecord, csvPrinter);
            assertEquals("a,b,c,d,,f", stringBuilder.toString());
        }
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

// commons-lang/src/test/java/org/apache/commons/lang3/NotImplementedExceptionTest.java
@Test
void testConstructors() {
    final Throwable nested = new RuntimeException();
    final String message = "Not Implemented";
    final String code = "CODE";

    NotImplementedException nie = new NotImplementedException(message);
    assertCorrect("Issue in (String)", nie, message, null, null);
    nie = new NotImplementedException(nested);
    assertCorrect("Issue in (Throwable)", nie, nested.toString(), nested, null);
    nie = new NotImplementedException(message, nested);
    assertCorrect("Issue in (String, Throwable)", nie, message, nested, null);
    nie = new NotImplementedException(message, code);
    assertCorrect("Issue in (String, String)", nie, message, null, code);
    nie = new NotImplementedException(nested, code);
    assertCorrect("Issue in (Throwable, String)", nie, nested.toString(), nested, code);
    nie = new NotImplementedException(message, nested, code);
    assertCorrect("Issue in (String, Throwable, String)", nie, message, nested, code);

    assertNull(new NotImplementedException().getCode());
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/stat/descriptive/StatisticalSummaryValuesTest.java
@Test
public void testToString() {
    StatisticalSummaryValues u = new StatisticalSummaryValues(4.5, 16, 10, 5, 4, 45);
    Locale d = Locale.getDefault();
    Locale.setDefault(Locale.US);
    Assert.assertEquals("StatisticalSummaryValues:\n" +
            "n: 10\n" +
            "min: 4.0\n" +
            "max: 5.0\n" +
            "mean: 4.5\n" +
            "std dev: 4.0\n" +
            "variance: 16.0\n" +
            "sum: 45.0\n", u.toString());
    Locale.setDefault(d);
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

// commons-text/src/test/java/org/apache/commons/text/lookup/UrlDecoderStringLookupTest.java
@Test
void testToString() {
    // does not blow up and gives some kind of string.
    Assertions.assertFalse(UrlDecoderStringLookup.INSTANCE.toString().isEmpty());
}

// =================== SEM O SENSITIVE EQUALITY ===========================
@Test
public void testReadZeroEmptyString() throws Exception {
    try (ReaderInputStream inputStream = new ReaderInputStream(new StringReader(""))) {
        final byte[] bytes = new byte[30];
        // Should always return 0 for length == 0
        assertEquals(0, inputStream.read(bytes, 0, 0));
        assertEquals(-1, inputStream.read(bytes, 0, 1));
        assertEquals(0, inputStream.read(bytes, 0, 0));
        assertEquals(-1, inputStream.read(bytes, 0, 1));
    }
}

@Test
public void testResetCharset() {
    assertNotNull(
            ReaderInputStream.builder().setReader(new StringReader("\uD800")).setCharset((Charset) null).getCharset());
}

@Test
public void testResetCharsetEncoder() {
    assertNotNull(ReaderInputStream.builder().setReader(new StringReader("\uD800")).setCharsetEncoder(null)
            .getCharsetEncoder());
}

@Test
public void testResetCharsetName() {
    assertNotNull(
            ReaderInputStream.builder().setReader(new StringReader("\uD800")).setCharset((String) null).getCharset());
}

@Test
public void testUTF16WithSingleByteRead() throws IOException {
    testWithSingleByteRead(TEST_STRING, UTF_16);
}

@Test
public void testUTF8WithBufferedRead() throws IOException {
    testWithBufferedRead(TEST_STRING, UTF_8);
}

@Test
public void testUTF8WithSingleByteRead() throws IOException {
    testWithSingleByteRead(TEST_STRING, UTF_8);
}

@Test
void testGetDescriptorBoolean() throws Exception {
    testGetDescriptorBase("booleanProperty", "getBooleanProperty", "setBooleanProperty");
}

/**
 * Positive getPropertyDescriptor on property {@code doubleProperty}.
 */
@Test
void testGetDescriptorDouble() throws Exception {
    testGetDescriptorBase("doubleProperty", "getDoubleProperty", "setDoubleProperty");
}

/**
 * Positive getPropertyDescriptor on property {@code floatProperty}.
 */
@Test
void testGetDescriptorFloat() throws Exception {
    testGetDescriptorBase("floatProperty", "getFloatProperty", "setFloatProperty");
}
