// commons-beanutils/src/test/java/org/apache/commons/beanutils2/converters/ArrayConverterTest.java
@Test
void testUnderscore_BEANUTILS_302() {
    final String value = "first_value,second_value";
    final ArrayConverter<String[]> converter = new ArrayConverter(String[].class, new StringConverter());

    // test underscore not allowed (the default)
    String[] result = converter.convert(String[].class, value);
    assertNotNull(result, "result.null");
    assertEquals(4, result.length, "result.length");
    assertEquals("first", result[0], "result[0]");
    assertEquals("value", result[1], "result[1]");
    assertEquals("second", result[2], "result[2]");
    assertEquals("value", result[3], "result[3]");

    // configure the converter to allow underscore
    converter.setAllowedChars(new char[] { '.', '-', '_' });

    // test underscore allowed
    result = converter.convert(String[].class, value);
    assertNotNull(result, "result.null");
    assertEquals(2, result.length, "result.length");
    assertEquals("first_value", result[0], "result[0]");
    assertEquals("second_value", result[1], "result[1]");
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

// commons-csv/src/test/java/org/apache/commons/csv/issues/JiraCsv211Test.java
@Test
void testJiraCsv211Format() throws IOException {
    // @formatter:off
        final CSVFormat printFormat = CSVFormat.DEFAULT.builder()
            .setDelimiter('\t')
            .setHeader("ID", "Name", "Country", "Age")
            .get();
        // @formatter:on
    final String formatted = printFormat.format("1", "Jane Doe", "USA", "");
    assertEquals("ID\tName\tCountry\tAge\r\n1\tJane Doe\tUSA\t", formatted);

    final CSVFormat parseFormat = CSVFormat.DEFAULT.builder().setDelimiter('\t').setHeader().setSkipHeaderRecord(true)
            .get();
    try (CSVParser parser = parseFormat.parse(new StringReader(formatted))) {
        parser.forEach(record -> {
            assertEquals("1", record.get(0));
            assertEquals("Jane Doe", record.get(1));
            assertEquals("USA", record.get(2));
            assertEquals("", record.get(3));
        });
    }
}

// commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/util/MimeMessageParserTest.java
@Test
void testAttachmentOnly() throws Exception {
    final Session session = Session.getDefaultInstance(new Properties());
    final MimeMessage message = MimeMessageUtils.createMimeMessage(session,
            Paths.get("./src/test/resources/eml/attachment-only.eml"));
    final MimeMessageParser mimeMessageParser = new MimeMessageParser(message);

    mimeMessageParser.parse();

    assertEquals("Kunde 100029   Auftrag   3600", mimeMessageParser.getSubject());
    assertNotNull(mimeMessageParser.getMimeMessage());
    assertFalse(mimeMessageParser.isMultipart());
    assertFalse(mimeMessageParser.hasHtmlContent());
    assertFalse(mimeMessageParser.hasPlainContent());
    assertNull(mimeMessageParser.getPlainContent());
    assertNull(mimeMessageParser.getHtmlContent());
    assertEquals(1, mimeMessageParser.getTo().size());
    assertTrue(mimeMessageParser.getCc().isEmpty());
    assertTrue(mimeMessageParser.getBcc().isEmpty());
    assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getFrom());
    assertEquals("siegfried.goeschl@it20one.at", mimeMessageParser.getReplyTo());
    assertTrue(mimeMessageParser.hasAttachments());
    final List<?> attachmentList = mimeMessageParser.getAttachmentList();
    assertEquals(1, attachmentList.size());

    final DataSource dataSource = mimeMessageParser.findAttachmentByName("Kunde 100029   Auftrag   3600.pdf");
    assertNotNull(dataSource);
    assertEquals("application/pdf", dataSource.getContentType());
}

// commons-io/src/test/java/org/apache/commons/io/input/ReaderInputStreamTest.java
@Test
public void testAvailableAfterClose() throws IOException {
    try (InputStream inputStream = createInputStream()) {
        inputStream.close();
        assertEquals(0, inputStream.available());
    }
}

// commons-lang/src/test/java/org/apache/commons/lang3/concurrent/CallableBackgroundInitializerTest.java
@Test
void testInitialize() throws Exception {
    final TestCallable call = new TestCallable();
    final CallableBackgroundInitializer<Integer> init = new CallableBackgroundInitializer<>(
            call);
    assertEquals(RESULT, init.initialize(), "Wrong result");
    assertEquals(1, call.callCount, "Wrong number of invocations");
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/ode/nonstiff/EulerStepInterpolatorTest.java
@Test
public void noReset() throws MaxCountExceededException {

    double[] y = { 0.0, 1.0, -2.0 };
    double[][] yDot = { { 1.0, 2.0, -2.0 } };
    EulerStepInterpolator interpolator = new EulerStepInterpolator();
    interpolator.reinitialize(new DummyIntegrator(interpolator), y, yDot, true,
            new EquationsMapper(0, y.length),
            new EquationsMapper[0]);
    interpolator.storeTime(0);
    interpolator.shift();
    interpolator.storeTime(1);

    double[] result = interpolator.getInterpolatedState();
    for (int i = 0; i < result.length; ++i) {
        Assert.assertTrue(JdkMath.abs(result[i] - y[i]) < 1.0e-10);
    }
}

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestAbandonedObjectPool.java
@Test
void testAbandonedInvalidate() throws Exception {
    abandonedConfig = new AbandonedConfig();
    abandonedConfig.setRemoveAbandonedOnMaintenance(true);
    abandonedConfig.setRemoveAbandonedTimeout(TestConstants.ONE_SECOND_DURATION);
    pool.close(); // Unregister pool created by setup
    pool = new GenericObjectPool<>(
            // destroys take 200 ms
            new SimpleFactory(200, 0),
            new GenericObjectPoolConfig<>(), abandonedConfig);
    final int n = 10;
    pool.setMaxTotal(n);
    pool.setBlockWhenExhausted(false);
    pool.setDurationBetweenEvictionRuns(Duration.ofMillis(500));
    PooledTestObject obj = null;
    for (int i = 0; i < 5; i++) {
        obj = pool.borrowObject();
    }
    Thread.sleep(1000); // abandon checked out instances and let evictor start
    pool.invalidateObject(obj); // Should not trigger another destroy / decrement
    Thread.sleep(2000); // give evictor time to finish destroys
    assertEquals(0, pool.getNumActive());
    assertEquals(5, pool.getDestroyedCount());
}

// commons-text/src/test/java/org/apache/commons/text/matcher/StringMatcherFactoryTest.java
@Test
void test_andMatcher() {
    assertNotNull(StringMatcherFactory.INSTANCE.andMatcher(StringMatcherFactory.INSTANCE.charMatcher('1'),
            StringMatcherFactory.INSTANCE.stringMatcher("2")));
    assertNotNull(StringMatcherFactory.INSTANCE.andMatcher(null, StringMatcherFactory.INSTANCE.stringMatcher("2")));
    assertNotNull(StringMatcherFactory.INSTANCE.andMatcher(null, null));
    StringMatcher andMatcher = StringMatcherFactory.INSTANCE.andMatcher();
    assertNotNull(andMatcher);
    assertEquals(0, andMatcher.size());
    andMatcher = StringMatcherFactory.INSTANCE.andMatcher(StringMatcherFactory.INSTANCE.charMatcher('1'));
    assertNotNull(andMatcher);
    assertEquals(1, andMatcher.size());
}

// ============== SEM MAGIC NUMBER SMELL ===============

@Test
public void testEqualsHashCode() {
    final DeletingPathVisitor visitor0 = DeletingPathVisitor.withLongCounters();
    final DeletingPathVisitor visitor1 = DeletingPathVisitor.withLongCounters();
    assertEquals(visitor0, visitor0);
    assertEquals(visitor0, visitor1);
    assertEquals(visitor1, visitor0);
    assertEquals(visitor0.hashCode(), visitor0.hashCode());
    assertEquals(visitor0.hashCode(), visitor1.hashCode());
    assertEquals(visitor1.hashCode(), visitor0.hashCode());
    visitor0.getPathCounters().getByteCounter().increment();
    assertEquals(visitor0, visitor0);
    assertNotEquals(visitor0, visitor1);
    assertNotEquals(visitor1, visitor0);
    assertEquals(visitor0.hashCode(), visitor0.hashCode());
    assertNotEquals(visitor0.hashCode(), visitor1.hashCode());
    assertNotEquals(visitor1.hashCode(), visitor0.hashCode());
}

@Test
public void testEqualsHashCode() {
    final AccumulatorPathVisitor visitor0 = AccumulatorPathVisitor.withLongCounters();
    final AccumulatorPathVisitor visitor1 = AccumulatorPathVisitor.withLongCounters();
    assertEquals(visitor0, visitor0);
    assertEquals(visitor0, visitor1);
    assertEquals(visitor1, visitor0);
    assertEquals(visitor0.hashCode(), visitor0.hashCode());
    assertEquals(visitor0.hashCode(), visitor1.hashCode());
    assertEquals(visitor1.hashCode(), visitor0.hashCode());
    visitor0.getPathCounters().getByteCounter().increment();
    assertEquals(visitor0, visitor0);
    assertNotEquals(visitor0, visitor1);
    assertNotEquals(visitor1, visitor0);
    assertEquals(visitor0.hashCode(), visitor0.hashCode());
    assertNotEquals(visitor0.hashCode(), visitor1.hashCode());
    assertNotEquals(visitor1.hashCode(), visitor0.hashCode());
}

@Test
public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePath() throws IOException {
    final Path archivePath = Paths.get(TEST_JAR_PATH);
    try (FileSystem archive = openArchive(archivePath, false)) {
        // relative jar -> absolute dir
        Path sourceDir = archive.getPath("dir1");
        PathUtils.copyDirectory(sourceDir, tempDirPath);
        assertTrue(Files.exists(tempDirPath.resolve("f1")));

        // absolute jar -> absolute dir
        sourceDir = archive.getPath("/next");
        PathUtils.copyDirectory(sourceDir, tempDirPath);
        assertTrue(Files.exists(tempDirPath.resolve("dir")));
    }
}

@Test
public void testCopyDirectoryForDifferentFilesystemsWithAbsolutePathReverse() throws IOException {
    try (FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
        // absolute dir -> relative jar
        Path targetDir = archive.getPath("target");
        Files.createDirectory(targetDir);
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2")
                .toAbsolutePath();
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));

        // absolute dir -> absolute jar
        targetDir = archive.getPath("/");
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
    }
}

@Test
public void testCopyDirectoryForDifferentFilesystemsWithRelativePath() throws IOException {
    final Path archivePath = Paths.get(TEST_JAR_PATH);
    try (FileSystem archive = openArchive(archivePath, false);
            FileSystem targetArchive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
        final Path targetDir = targetArchive.getPath("targetDir");
        Files.createDirectory(targetDir);
        // relative jar -> relative dir
        Path sourceDir = archive.getPath("next");
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir.resolve("dir")));

        // absolute jar -> relative dir
        sourceDir = archive.getPath("/dir1");
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir.resolve("f1")));
    }
}

@Test
public void testCopyDirectoryForDifferentFilesystemsWithRelativePathReverse() throws IOException {
    try (FileSystem archive = openArchive(tempDirPath.resolve(TEST_JAR_NAME), true)) {
        // relative dir -> relative jar
        Path targetDir = archive.getPath("target");
        Files.createDirectory(targetDir);
        final Path sourceDir = Paths.get("src/test/resources/org/apache/commons/io/dirs-2-file-size-2");
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));

        // relative dir -> absolute jar
        targetDir = archive.getPath("/");
        PathUtils.copyDirectory(sourceDir, targetDir);
        assertTrue(Files.exists(targetDir.resolve("dirs-a-file-size-1")));
    }
}

@Test
public void testCopyFile() throws IOException {
    final Path sourceFile = Paths.get("src/test/resources/org/apache/commons/io/dirs-1-file-size-1/file-size-1.bin");
    final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
    assertTrue(Files.exists(targetFile));
    assertEquals(Files.size(sourceFile), Files.size(targetFile));
}

@Test
public void testCopyFileTwoFileSystem() throws IOException {
    try (FileSystem archive = openArchive(Paths.get(TEST_JAR_PATH), false)) {
        final Path sourceFile = archive.getPath("next/dir/test.log");
        final Path targetFile = PathUtils.copyFileToDirectory(sourceFile, tempDirPath);
        assertTrue(Files.exists(targetFile));
        assertEquals(Files.size(sourceFile), Files.size(targetFile));
    }
}

@Test
public void testCreateDirectoriesNew() throws IOException {
    assertEquals(tempDirPath, PathUtils.createParentDirectories(tempDirPath.resolve("child")));
}

@Test
public void testCreateDirectoriesSymlink() throws IOException {
    final Path symlinkedDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
    final String leafDirName = "child";
    final Path newDirFollowed = PathUtils.createParentDirectories(symlinkedDir.resolve(leafDirName),
            PathUtils.NULL_LINK_OPTION);
    assertEquals(Files.readSymbolicLink(symlinkedDir), newDirFollowed);
}