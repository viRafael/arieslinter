// commons-beanutils/src/test/java/org/apache/commons/beanutils2/converters/ArrayConverterTest.java
@Test
void testComponentIntegerConverter() {

    final IntegerConverter intConverter = new IntegerConverter(Integer.valueOf(0));
    intConverter.setPattern("#,###");
    intConverter.setLocale(Locale.US);
    final ArrayConverter arrayConverter = new ArrayConverter(int[].class, intConverter, 0);
    arrayConverter.setAllowedChars(new char[] { ',', '-' });
    arrayConverter.setDelimiter(';');

    // Expected results
    final int[] intArray = { 1111, 2222, 3333, 4444 };
    final String stringA = "1,111; 2,222; 3,333; 4,444";
    final String stringB = intArray[0] + ";" + intArray[1] + ";" + intArray[2] + ";" + intArray[3];
    final String[] strArray = { "" + intArray[0], "" + intArray[1], "" + intArray[2], "" + intArray[3] };
    final long[] longArray = { intArray[0], intArray[1], intArray[2], intArray[3] };
    final Long[] LONGArray = { Long.valueOf(intArray[0]), Long.valueOf(intArray[1]), Long.valueOf(intArray[2]),
            Long.valueOf(intArray[3]) };
    final Integer[] IntegerArray = { Integer.valueOf(intArray[0]), Integer.valueOf(intArray[1]),
            Integer.valueOf(intArray[2]),
            Integer.valueOf(intArray[3]) };
    final ArrayList<String> strList = new ArrayList<>();
    final ArrayList<Long> longList = new ArrayList<>();
    for (int i = 0; i < strArray.length; i++) {
        strList.add(strArray[i]);
        longList.add(LONGArray[i]);
    }

    String msg = null;

    // String --> int[]
    msg = "String --> int[]";
    checkArray(msg, intArray, arrayConverter.convert(int[].class, stringA));

    // String --> int[] (with braces)
    msg = "String --> Integer[] (with braces)";
    checkArray(msg, IntegerArray, arrayConverter.convert(Integer[].class, "{" + stringA + "}"));

    // String[] --> int[]
    msg = "String[] --> int[]";
    checkArray(msg, intArray, arrayConverter.convert(int[].class, strArray));

    // String[] --> Integer[]
    msg = "String[] --> Integer[]";
    checkArray(msg, IntegerArray, arrayConverter.convert(Integer[].class, strArray));

    // long[] --> int[]
    msg = "long[] --> int[]";
    checkArray(msg, intArray, arrayConverter.convert(int[].class, longArray));

    // Long --> int[]
    msg = "Long --> int[]";
    checkArray(msg, new int[] { LONGArray[0].intValue() }, arrayConverter.convert(int[].class, LONGArray[0]));

    // LONG[] --> int[]
    msg = "LONG[] --> int[]";
    checkArray(msg, intArray, arrayConverter.convert(int[].class, LONGArray));

    // Long --> String
    msg = "Long --> String";
    assertEquals(LONGArray[0] + "", arrayConverter.convert(String.class, LONGArray[0]), msg);

    // LONG[] --> String (first)
    msg = "LONG[] --> String (first)";
    assertEquals(LONGArray[0] + "", arrayConverter.convert(String.class, LONGArray), msg);

    // LONG[] --> String (all)
    msg = "LONG[] --> String (all)";
    arrayConverter.setOnlyFirstToString(false);
    assertEquals(stringB, arrayConverter.convert(String.class, LONGArray), msg);

    // Collection of Long --> String
    msg = "Collection of Long --> String";
    assertEquals(stringB, arrayConverter.convert(String.class, longList), msg);

    // LONG[] --> String[]
    msg = "long[] --> String[]";
    checkArray(msg, strArray, arrayConverter.convert(String[].class, LONGArray));

    // Collection of String --> Integer[]
    msg = "Collection of String --> Integer[]";
    checkArray(msg, IntegerArray, arrayConverter.convert(Integer[].class, strList));

    // Collection of Long --> int[]
    msg = "Collection of Long --> int[]";
    checkArray(msg, intArray, arrayConverter.convert(int[].class, longList));
}

// commons-collections/src/test/java/org/apache/commons/collections4/set/ListOrderedSet2Test.java
@Test
@SuppressWarnings("unchecked")
void testListAddIndexed() {
    final ListOrderedSet<E> set = makeObject();
    set.add((E) ZERO);
    set.add((E) TWO);

    set.add(1, (E) ONE);
    assertEquals(3, set.size());
    assertSame(ZERO, set.get(0));
    assertSame(ONE, set.get(1));
    assertSame(TWO, set.get(2));

    set.add(0, (E) ONE);
    assertEquals(3, set.size());
    assertSame(ZERO, set.get(0));
    assertSame(ONE, set.get(1));
    assertSame(TWO, set.get(2));

    final List<E> list = new ArrayList<>();
    list.add((E) ZERO);
    list.add((E) TWO);

    set.addAll(0, list);
    assertEquals(3, set.size());
    assertSame(ZERO, set.get(0));
    assertSame(ONE, set.get(1));
    assertSame(TWO, set.get(2));

    list.add(0, (E) THREE); // list = [3,0,2]
    set.remove(TWO); // set = [0,1]
    set.addAll(1, list);
    assertEquals(4, set.size());
    assertSame(ZERO, set.get(0));
    assertSame(THREE, set.get(1));
    assertSame(TWO, set.get(2));
    assertSame(ONE, set.get(3));
}

// commons-compress/src/test/java/org/apache/commons/compress/archivers/zip/ZipArchiveEntryTest.java
@Test
void testAddAsFirstExtraField() {
    final AsiExtraField a = new AsiExtraField();
    a.setDirectory(true);
    a.setMode(0755);
    final UnrecognizedExtraField u = new UnrecognizedExtraField();
    u.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
    u.setLocalFileDataData(ByteUtils.EMPTY_BYTE_ARRAY);

    final ZipArchiveEntry ze = new ZipArchiveEntry("test/");
    ze.setExtraFields(new ZipExtraField[] { a, u });
    final byte[] data1 = ze.getExtra();

    final UnrecognizedExtraField u2 = new UnrecognizedExtraField();
    u2.setHeaderId(ExtraFieldUtilsTest.UNRECOGNIZED_HEADER);
    u2.setLocalFileDataData(new byte[] { 1 });

    ze.addAsFirstExtraField(u2);
    final byte[] data2 = ze.getExtra();
    ZipExtraField[] result = ze.getExtraFields();
    assertEquals(2, result.length, "second pass");
    assertSame(u2, result[0]);
    assertSame(a, result[1]);
    assertEquals(data1.length + 1, data2.length, "length second pass");

    final UnrecognizedExtraField u3 = new UnrecognizedExtraField();
    u3.setHeaderId(new ZipShort(2));
    u3.setLocalFileDataData(new byte[] { 1 });
    ze.addAsFirstExtraField(u3);
    result = ze.getExtraFields();
    assertEquals(3, result.length, "third pass");
    assertSame(u3, result[0]);
    assertSame(u2, result[1]);
    assertSame(a, result[2]);
}

// commons-csv/src/test/java/org/apache/commons/csv/issues/JiraCsv263Test.java
@Test
void testPrintFromReaderWithQuotes() throws IOException {
    // @formatter:off
        final CSVFormat format = CSVFormat.RFC4180.builder()
            .setDelimiter(',')
            .setQuote('"')
            .setEscape('?')
            .setQuoteMode(QuoteMode.NON_NUMERIC)
            .get();
        // @formatter:on
    final StringBuilder out = new StringBuilder();

    final Reader atStartOnly = new StringReader("\"a,b,c\r\nx,y,z");
    format.print(atStartOnly, out, true);
    assertEquals("\"\"\"a,b,c\r\nx,y,z\"", out.toString());

    final Reader atEndOnly = new StringReader("a,b,c\r\nx,y,z\"");
    out.setLength(0);
    format.print(atEndOnly, out, true);
    assertEquals("\"a,b,c\r\nx,y,z\"\"\"", out.toString());

    final Reader atBeginEnd = new StringReader("\"a,b,c\r\nx,y,z\"");
    out.setLength(0);
    format.print(atBeginEnd, out, true);
    assertEquals("\"\"\"a,b,c\r\nx,y,z\"\"\"", out.toString());

    final Reader embeddedBeginMiddle = new StringReader("\"a\",b,c\r\nx,\"y\",z");
    out.setLength(0);
    format.print(embeddedBeginMiddle, out, true);
    assertEquals("\"\"\"a\"\",b,c\r\nx,\"\"y\"\",z\"", out.toString());

    final Reader embeddedMiddleEnd = new StringReader("a,\"b\",c\r\nx,y,\"z\"");
    out.setLength(0);
    format.print(embeddedMiddleEnd, out, true);
    assertEquals("\"a,\"\"b\"\",c\r\nx,y,\"\"z\"\"\"", out.toString());

    final Reader nested = new StringReader("a,\"b \"and\" c\",d");
    out.setLength(0);
    format.print(nested, out, true);
    assertEquals("\"a,\"\"b \"\"and\"\" c\"\",d\"", out.toString());
}

// commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/InvalidInternetAddressTest.java
@Test
void testValidateMethodCharset() throws Exception {
    // Prove InternetAddress constructor isn't throwing exception and
    // the validate() method is

    for (int i = 0; i < ARR_INVALID_EMAILS.length; i++) {

        final InternetAddress address = new InternetAddress(ARR_INVALID_EMAILS[i], "Joe",
                StandardCharsets.UTF_8.name());

        // validate() doesn't check addresses containing quotes or '['
        final boolean quoted = ARR_INVALID_EMAILS[i].contains("\"");
        final int atIndex = ARR_INVALID_EMAILS[i].indexOf("@");
        final boolean domainBracket = atIndex >= 0 && ARR_INVALID_EMAILS[i].indexOf("[", atIndex) >= 0;

        try {
            address.validate();
            if (!(quoted || domainBracket)) {
                fail("Validate " + i + " passed: " + ARR_INVALID_EMAILS[i]);
            }

        } catch (final Exception ex) {
            if (quoted || domainBracket) {
                fail("Validate " + i + " failed: " + ARR_INVALID_EMAILS[i] + " - " + ex.getMessage());
            }
        }

    }

    // test valid 'quoted' Email addresses
    assertDoesNotThrow(() -> new InternetAddress(VALID_QUOTED_EMAIL, "Joe", StandardCharsets.UTF_8.name()).validate(),
            () -> "Valid Quoted Email failed: " + VALID_QUOTED_EMAIL);

}

// commons-io/src/test/java/org/apache/commons/io/input/UnsynchronizedByteArrayInputStreamTest.java
@Test
@SuppressWarnings("resource") // not necessary to close these resources
public void testConstructor3() {
    final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
    final byte[] one = new byte[1];
    final byte[] some = new byte[25];

    UnsynchronizedByteArrayInputStream is = newStream(empty, 0);
    assertEquals(empty.length, is.available());
    is = newStream(empty, 1);
    assertEquals(0, is.available());
    is = newStream(empty, 0, 1);
    assertEquals(0, is.available());
    is = newStream(empty, 1, 1);
    assertEquals(0, is.available());

    is = newStream(one, 0);
    assertEquals(one.length, is.available());
    is = newStream(one, 1);
    assertEquals(one.length - 1, is.available());
    is = newStream(one, 2);
    assertEquals(0, is.available());
    is = newStream(one, 0, 1);
    assertEquals(1, is.available());
    is = newStream(one, 1, 1);
    assertEquals(0, is.available());
    is = newStream(one, 0, 2);
    assertEquals(1, is.available());
    is = newStream(one, 2, 1);
    assertEquals(0, is.available());
    is = newStream(one, 2, 2);
    assertEquals(0, is.available());

    is = newStream(some, 0);
    assertEquals(some.length, is.available());
    is = newStream(some, 1);
    assertEquals(some.length - 1, is.available());
    is = newStream(some, 10);
    assertEquals(some.length - 10, is.available());
    is = newStream(some, some.length);
    assertEquals(0, is.available());
    is = newStream(some, some.length, some.length);
    assertEquals(0, is.available());
    is = newStream(some, some.length - 1, some.length);
    assertEquals(1, is.available());
    is = newStream(some, 0, 7);
    assertEquals(7, is.available());
    is = newStream(some, 7, 7);
    assertEquals(7, is.available());
    is = newStream(some, 0, some.length * 2);
    assertEquals(some.length, is.available());
    is = newStream(some, some.length - 1, 7);
    assertEquals(1, is.available());
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/ode/nonstiff/EulerStepInterpolatorTest.java
@Test
public void interpolationAtBounds() throws MaxCountExceededException {

    double t0 = 0;
    double[] y0 = { 0.0, 1.0, -2.0 };

    double[] y = y0.clone();
    double[][] yDot = { new double[y0.length] };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true,
            new EquationsMapper(0, y.length),
            new EquationsMapper[0]);
    interpolator.storeTime(t0);

    double dt = 1.0;
    interpolator.shift();
    y[0] = 1.0;
    y[1] = 3.0;
    y[2] = -4.0;
    yDot[0][0] = (y[0] - y0[0]) / dt;
    yDot[0][1] = (y[1] - y0[1]) / dt;
    yDot[0][2] = (y[2] - y0[2]) / dt;
    interpolator.storeTime(t0 + dt);

    interpolator.setInterpolatedTime(interpolator.getPreviousTime());
    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(JdkMath.abs(result[i] - y0[i]) < 1.0e-10);
    }

    interpolator.setInterpolatedTime(interpolator.getCurrentTime());
    result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(JdkMath.abs(result[i] - y[i]) < 1.0e-10);
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

// ========================================= SEM O VERBOSE TEST ============================================

@Test
@Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
public void testBufferSmallest() throws IOException {
    final Charset charset = StandardCharsets.UTF_8;
    // @formatter:off
        try (InputStream in = new ReaderInputStream(
                new StringReader("\uD800"),
                charset, (int)
                ReaderInputStream.minBufferSize(charset.newEncoder()))) {
            in.read();
        }
        try (InputStream in = ReaderInputStream.builder()
                .setReader(new StringReader("\uD800"))
                .setCharset(charset)
                .setBufferSize((int) ReaderInputStream.minBufferSize(charset.newEncoder()))
                .get()) {
            in.read();
        }
        // @formatter:on
}

@Test
public void testBufferTooSmall() {
    assertThrows(IllegalArgumentException.class,
            () -> new ReaderInputStream(new StringReader("\uD800"), StandardCharsets.UTF_8, -1));
    assertThrows(IllegalArgumentException.class,
            () -> new ReaderInputStream(new StringReader("\uD800"), StandardCharsets.UTF_8, 0));
    assertThrows(IllegalArgumentException.class,
            () -> new ReaderInputStream(new StringReader("\uD800"), StandardCharsets.UTF_8, 1));
}

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

@SuppressWarnings("resource")
@Test
public void testAvailableAfterClose() throws Exception {
    final InputStream shadow;
    try (InputStream in = createInputStream(new byte[] { 1, 2 })) {
        assertTrue(in.available() > 0);
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(1, in.read());
        shadow = in;
    }
    assertEquals(0, shadow.available());
}

@Test
public void testAvailableAfterOpen() throws Exception {
    try (InputStream in = createInputStream(new byte[] { 1, 2 })) {
        assertTrue(in.available() > 0);
        assertEquals(1, in.read());
        assertTrue(in.available() > 0);
        assertEquals(2, in.read());
        assertTrue(in.available() > 0);
        assertEquals(1, in.read());
        assertTrue(in.available() > 0);
    }
}

@Test
void testDelimiterSameAsEscapeThrowsException1() {
    assertThrows(IllegalArgumentException.class,
            () -> CSVFormat.DEFAULT.builder().setDelimiter('!').setEscape('!').get());
}

@Test
void testDelimiterSameAsRecordSeparatorThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> CSVFormat.newFormat(CR));
}

@Test
void testDelimiterStringLineBreakCrThrowsException1() {
    assertThrows(IllegalArgumentException.class,
            () -> CSVFormat.DEFAULT.builder().setDelimiter(String.valueOf(Constants.CR)).get());
}

@Test
void testDelimiterStringLineBreakLfThrowsException1() {
    assertThrows(IllegalArgumentException.class,
            () -> CSVFormat.DEFAULT.builder().setDelimiter(String.valueOf(Constants.LF)).get());
}

@Test
void testDuplicateHeaderElements() {
    final String[] header = { "A", "A" };
    final CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(header).get();
    assertEquals(2, format.getHeader().length);
    assertArrayEquals(header, format.getHeader());
}
