// commons-beanutils/src/test/java/org/apache/commons/beanutils2/converters/ArrayConverterTest.java
@Test
void testStringArrayToNumber() {

    // Configure Converter
    final IntegerConverter intConverter = new IntegerConverter();
    final ArrayConverter arrayConverter = new ArrayConverter(int[].class, intConverter);

    // Test Data
    final String[] array = { "10", "  11", "12  ", "  13  " };
    final ArrayList<String> list = new ArrayList<>();
    Collections.addAll(list, array);

    // Expected results
    String msg = null;
    final int[] expectedInt = { 10, 11, 12, 13 };
    final Integer[] expectedInteger = { Integer.valueOf(expectedInt[0]), Integer.valueOf(expectedInt[1]),
            Integer.valueOf(expectedInt[2]),
            Integer.valueOf(expectedInt[3]) };

    // Test String[] --> int[]
    msg = "String[] --> int[]";
    checkArray(msg, expectedInt, arrayConverter.convert(int[].class, array));

    // Test String[] --> Integer[]
    msg = "String[] --> Integer[]";
    checkArray(msg, expectedInteger, arrayConverter.convert(Integer[].class, array));

    // Test List --> int[]
    msg = "List --> int[]";
    checkArray(msg, expectedInt, arrayConverter.convert(int[].class, list));

    // Test List --> Integer[]
    msg = "List --> Integer[]";
    checkArray(msg, expectedInteger, arrayConverter.convert(Integer[].class, list));
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

// commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/SendWithAttachmentsTest.java
@Test
void testSendNoAttachments() throws EmailException, IOException {
    getMailServer();

    final String strSubject = "Test HTML Send #1 Subject (w charset)";

    email = new MockHtmlEmailConcrete();
    email.setHostName(strTestMailServer);
    email.setSmtpPort(getMailServerPort());
    email.setFrom(strTestMailFrom);
    email.addTo(strTestMailTo);

    email.setAuthentication(strTestUser, strTestPasswd);

    email.setCharset(EmailConstants.ISO_8859_1);
    email.setSubject(strSubject);

    final URL url = new URL(EmailConfiguration.TEST_URL);
    final String cid = email.embed(url, "Apache Logo");

    final String strHtmlMsg = "<html>The Apache logo - <img src=\"cid:" + cid + "\"><html>";

    email.setHtmlMsg(strHtmlMsg);
    email.setTextMsg("Your email client does not support HTML emails");

    email.send();
    fakeMailServer.stop();

    // validate txt message
    validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(),
            email.getCcAddresses(),
            email.getBccAddresses(), true);

    // validate html message
    validateSend(fakeMailServer, strSubject, email.getHtml(), email.getFromAddress(), email.getToAddresses(),
            email.getCcAddresses(),
            email.getBccAddresses(), false);
}

// commons-io/src/test/java/org/apache/commons/io/input/ReaderInputStreamTest.java
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

// ================ SEM UNKNOW TEST =================

@Test
public void testReadSingle() {
    UnsynchronizedByteArrayInputStream is = newStream(IOUtils.EMPTY_BYTE_ARRAY);
    assertEquals(END_OF_STREAM, is.read());

    is = newStream(new byte[] { (byte) 0xa, (byte) 0xb, (byte) 0xc });
    assertEquals(0xa, is.read());
    assertEquals(0xb, is.read());
    assertEquals(0xc, is.read());
    assertEquals(END_OF_STREAM, is.read());
}

@Test
public void testReadArray() {
    byte[] buf = new byte[10];
    UnsynchronizedByteArrayInputStream is = newStream(IOUtils.EMPTY_BYTE_ARRAY);
    int read = is.read(buf);
    assertEquals(END_OF_STREAM, read);
    assertArrayEquals(new byte[10], buf);

    buf = IOUtils.EMPTY_BYTE_ARRAY;
    is = newStream(new byte[] { (byte) 0xa, (byte) 0xb, (byte) 0xc });
    read = is.read(buf);
    assertEquals(0, read);

    buf = new byte[10];
    is = newStream(new byte[] { (byte) 0xa, (byte) 0xb, (byte) 0xc });
    read = is.read(buf);
    assertEquals(3, read);
    assertEquals(0xa, buf[0]);
    assertEquals(0xb, buf[1]);
    assertEquals(0xc, buf[2]);
    assertEquals(0, buf[3]);

    buf = new byte[2];
    is = newStream(new byte[] { (byte) 0xa, (byte) 0xb, (byte) 0xc });
    read = is.read(buf);
    assertEquals(2, read);
    assertEquals(0xa, buf[0]);
    assertEquals(0xb, buf[1]);
    read = is.read(buf);
    assertEquals(1, read);
    assertEquals(0xc, buf[0]);
}

@Test
public void testInvalidSkipNUnder() {
    @SuppressWarnings("resource") // not necessary to close these resources
    final UnsynchronizedByteArrayInputStream is = newStream(new byte[] { (byte) 0xa, (byte) 0xb, (byte) 0xc });
    assertThrows(IllegalArgumentException.class, () -> {
        is.skip(-1);
    });
}

@Test
public void testEquals_fullControl() {
    assertFalse(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.SENSITIVE));
    assertTrue(FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.INSENSITIVE));
    assertEquals(WINDOWS, FilenameUtils.equals("file.txt", "FILE.TXT", true, IOCase.SYSTEM));
    assertFalse(FilenameUtils.equals("file.txt", "FILE.TXT", true, null));
}

@Test
public void testEqualsNormalized() {
    assertTrue(FilenameUtils.equalsNormalized(null, null));
    assertFalse(FilenameUtils.equalsNormalized(null, ""));
    assertFalse(FilenameUtils.equalsNormalized("", null));
    assertTrue(FilenameUtils.equalsNormalized("", ""));
    assertTrue(FilenameUtils.equalsNormalized("file.txt", "file.txt"));
    assertFalse(FilenameUtils.equalsNormalized("file.txt", "FILE.TXT"));
    assertTrue(FilenameUtils.equalsNormalized("a\\b\\file.txt", "a/b/file.txt"));
    assertFalse(FilenameUtils.equalsNormalized("a/b/", "a/b"));
}

/**
 * Test for https://issues.apache.org/jira/browse/IO-128
 */
@Test
public void testEqualsNormalizedError_IO_128() {
    assertFalse(FilenameUtils.equalsNormalizedOnSystem("//file.txt", "file.txt"));
    assertFalse(FilenameUtils.equalsNormalizedOnSystem("file.txt", "//file.txt"));
    assertFalse(FilenameUtils.equalsNormalizedOnSystem("//file.txt", "//file.txt"));
}

@Test
public void testEqualsNormalizedOnSystem() {
    assertTrue(FilenameUtils.equalsNormalizedOnSystem(null, null));
    assertFalse(FilenameUtils.equalsNormalizedOnSystem(null, ""));
    assertFalse(FilenameUtils.equalsNormalizedOnSystem("", null));
    assertTrue(FilenameUtils.equalsNormalizedOnSystem("", ""));
    assertTrue(FilenameUtils.equalsNormalizedOnSystem("file.txt", "file.txt"));
    assertEquals(WINDOWS, FilenameUtils.equalsNormalizedOnSystem("file.txt", "FILE.TXT"));
    assertTrue(FilenameUtils.equalsNormalizedOnSystem("a\\b\\file.txt", "a/b/file.txt"));
    assertFalse(FilenameUtils.equalsNormalizedOnSystem("a/b/", "a/b"));
    assertFalse(FilenameUtils.equalsNormalizedOnSystem("//a.html", "//ab.html"));
}

@Test
public void testEqualsOnSystem() {
    assertTrue(FilenameUtils.equalsOnSystem(null, null));
    assertFalse(FilenameUtils.equalsOnSystem(null, ""));
    assertFalse(FilenameUtils.equalsOnSystem("", null));
    assertTrue(FilenameUtils.equalsOnSystem("", ""));
    assertTrue(FilenameUtils.equalsOnSystem("file.txt", "file.txt"));
    assertEquals(WINDOWS, FilenameUtils.equalsOnSystem("file.txt", "FILE.TXT"));
    assertFalse(FilenameUtils.equalsOnSystem("a\\b\\file.txt", "a/b/file.txt"));
}

@Test
public void testGetBaseName() {
    assertNull(FilenameUtils.getBaseName(null));
    assertEquals("noseparator", FilenameUtils.getBaseName("noseparator.inthispath"));
    assertEquals("c", FilenameUtils.getBaseName("a/b/c.txt"));
    assertEquals("c", FilenameUtils.getBaseName("a/b/c"));
    assertEquals("", FilenameUtils.getBaseName("a/b/c/"));
    assertEquals("c", FilenameUtils.getBaseName("a\\b\\c"));
    assertEquals("file.txt", FilenameUtils.getBaseName("file.txt.bak"));
}

@Test
public void testGetBaseName_with_null_character() {
    assertThrows(IllegalArgumentException.class, () -> FilenameUtils.getBaseName("fil\u0000e.txt.bak"));
}
