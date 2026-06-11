// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetBaseNamePathBaseCases() {
    assertEquals("bar", FileNameUtils.getBaseName(Paths.get("a/b/c/bar.foo")));
    assertEquals("foo", FileNameUtils.getBaseName(Paths.get("foo")));
    assertEquals("", FileNameUtils.getBaseName(Paths.get("")));
    assertEquals("", FileNameUtils.getBaseName(Paths.get(".")));
    for (final File f : File.listRoots()) {
        assertNull(FileNameUtils.getBaseName(f.toPath()));
    }
    if (SystemUtils.IS_OS_WINDOWS) {
        assertNull(FileNameUtils.getBaseName(Paths.get("C:\\")));
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

// commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/HtmlEmailTest.java
/**
 * @throws EmailException when bad addresses and attachments are used
 * @throws IOException    if creating a temp file, URL or sending fails
 */
@Test
void testSend() throws EmailException, IOException {
    final EmailAttachment attachment = new EmailAttachment();

    /* File to used to test file attachments (Must be valid) */
    final File testFile = File.createTempFile("commons-email-testfile", ".txt");
    testFile.deleteOnExit();
    // Test Success
    getMailServer();

    String strSubject = "Test HTML Send #1 Subject (w charset)";

    email = new MockHtmlEmailConcrete();
    email.setHostName(strTestMailServer);
    email.setSmtpPort(getMailServerPort());
    email.setFrom(strTestMailFrom);
    email.addTo(strTestMailTo);

    /* File to used to test file attachments (Must be valid) */
    attachment.setName("Test Attachment");
    attachment.setDescription("Test Attachment Desc");
    attachment.setPath(testFile.getAbsolutePath());
    email.attach(attachment);

    // email.setAuthentication(strTestUser, strTestPasswd);

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

    // validate attachment
    validateSend(fakeMailServer, strSubject, attachment.getName(), email.getFromAddress(), email.getToAddresses(),
            email.getCcAddresses(),
            email.getBccAddresses(), false);

    getMailServer();

    email = new MockHtmlEmailConcrete();
    email.setHostName(strTestMailServer);
    email.setSmtpPort(getMailServerPort());
    email.setFrom(strTestMailFrom);
    email.addTo(strTestMailTo);

    if (strTestUser != null && strTestPasswd != null) {
        email.setAuthentication(strTestUser, strTestPasswd);
    }

    strSubject = "Test HTML Send #1 Subject (wo charset)";
    email.setSubject(strSubject);
    email.setTextMsg("Test message");

    email.send();
    fakeMailServer.stop();
    // validate txt message
    validateSend(fakeMailServer, strSubject, email.getText(), email.getFromAddress(), email.getToAddresses(),
            email.getCcAddresses(),
            email.getBccAddresses(), true);
}

// commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/resolver/DataSourceUrlResolverTest.java
/**
 * Shows how the DataSourceUrlResolver can resolve files as well but this should
 * be done using a DataSourceFileResolver.
 *
 * @throws Exception the test failed
 */
@Test
void testResolvingFilesLenient() throws Exception {
    final DataSourceResolver dataSourceResolver = new DataSourceUrlResolver(
            new File("./src/test/resources").toURI().toURL(), true);
    assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("images/asf_logo_wide.gif")).length);
    assertEquals(IMG_SIZE, toByteArray(dataSourceResolver.resolve("./images/asf_logo_wide.gif")).length);
    assertNull(dataSourceResolver.resolve("./images/does-not-exist.gif"));
    assertNull(dataSourceResolver.resolve("/images/asf_logo_wide.gif"));
}

// commons-io/src/test/java/org/apache/commons/io/FileUtilsCopyDirectoryToDirectoryTest.java
@Test
void testCopyDirectoryToDirectoryThrowsIllegalArgumentExceptionWithCorrectMessageWhenDstDirIsNotDirectory()
        throws IOException {
    final File srcDir = new File(temporaryFolder, "sourceDirectory");
    srcDir.mkdir();
    final File destDir = new File(temporaryFolder, "notadirectory");
    destDir.createNewFile();
    final String expectedMessage = String.format("Parameter 'destinationDir' is not a directory: '%s'",
            destDir);
    assertExceptionTypeAndMessage(srcDir, destDir, IllegalArgumentException.class, expectedMessage);
}

// commons-io/src/test/java/org/apache/commons/io/input/ReversedLinesFileReaderSimpleTest.java
/*
 * Tests IO-639.
 */
@ParameterizedTest
@MethodSource("org.apache.commons.io.input.ReversedLinesFileReaderParamBlockSizeTest#blockSizes")
void testEmptyFirstLine(final int blockSize) throws Exception {
    final File testFileEmptyFirstLine = TestResources.getFile("/empty-first-line.bin");
    try (ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(testFileEmptyFirstLine,
            blockSize, StandardCharsets.US_ASCII.name())) {
        assertEqualsAndNoLineBreaks("test2", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("test1", reversedLinesFileReader.readLine());
        assertEqualsAndNoLineBreaks("", reversedLinesFileReader.readLine());
    }
}

// commons-io/src/test/java/org/apache/commons/io/AbstractFileUtilsDeleteDirectoryTest.java
@Test
void testDeleteDirWithASymbolicLinkDir2() throws Exception {

    final File realOuter = new File(top, "realouter");
    assertTrue(realOuter.mkdirs());

    final File realInner = new File(realOuter, "realinner");
    assertTrue(realInner.mkdirs());

    FileUtils.touch(new File(realInner, "file1"));
    assertEquals(1, realInner.list().length);

    final File randomDirectory = new File(top, "randomDir");
    assertTrue(randomDirectory.mkdirs());

    FileUtils.touch(new File(randomDirectory, "randomfile"));
    assertEquals(1, randomDirectory.list().length);

    final File symlinkDirectory = new File(realOuter, "fakeinner");
    Files.createSymbolicLink(symlinkDirectory.toPath(), randomDirectory.toPath());

    assertEquals(1, symlinkDirectory.list().length);

    // assert contents of the real directory were removed including the symlink
    FileUtils.deleteDirectory(realOuter);
    assertEquals(1, top.list().length);

    // ensure that the contents of the symlink were NOT removed.
    assertEquals(1, randomDirectory.list().length, "Contents of symbolic link should not have been removed");
}

// commons-io/src/test/java/org/apache/commons/io/IORandomAccessFileTest.java
@ParameterizedTest
@EnumSource(RandomAccessFileMode.class)
void testFile(final RandomAccessFileMode mode) throws IOException {
    final File file = newFileFixture();
    final String modeStr = mode.getMode();
    try (IORandomAccessFile raf = new IORandomAccessFile(file, modeStr)) {
        assertEquals(file, raf.getFile());
        assertEquals(modeStr, raf.getMode());
    }
}

// commons-io/src/test/java/org/apache/commons/io/FileUtilsCleanSymlinksTest.java
@Test
void testCleanDirWithASymlinkDir() throws Exception {
    if (SystemProperties.getOsName().startsWith("Win")) {
        // Can't use "ln" for symlinks on the command line in Windows.
        return;
    }

    final File realOuter = new File(top, "realouter");
    assertTrue(realOuter.mkdirs());

    final File realInner = new File(realOuter, "realinner");
    assertTrue(realInner.mkdirs());

    FileUtils.touch(new File(realInner, "file1"));
    assertEquals(1, realInner.list().length);

    final File randomDirectory = new File(top, "randomDir");
    assertTrue(randomDirectory.mkdirs());

    FileUtils.touch(new File(randomDirectory, "randomfile"));
    assertEquals(1, randomDirectory.list().length);

    final File symlinkDirectory = new File(realOuter, "fakeinner");
    assertTrue(setupSymlink(randomDirectory, symlinkDirectory));

    assertEquals(1, symlinkDirectory.list().length);

    // assert contents of the real directory were removed including the symlink
    FileUtils.cleanDirectory(realOuter);
    assertTrue(realOuter.exists());
    assertEquals(0, realOuter.list().length);

    // ensure that the contents of the symlink were NOT removed.
    assertEquals(1, randomDirectory.list().length, "Contents of symbolic link should not have been removed");
}

// commons-compress/src/test/java/org/apache/commons/compress/compressors/LZMATest.java
@Test
void testLzmaRoundtrip() throws Exception {
    final Path input = getPath("test1.xml");
    final File compressed = newTempFile("test1.xml.xz");
    try (OutputStream out = Files.newOutputStream(compressed.toPath())) {
        try (CompressorOutputStream<?> cos = new CompressorStreamFactory().createCompressorOutputStream("lzma", out)) {
            cos.write(input);
        }
    }
    final byte[] orig = Files.readAllBytes(input);
    final byte[] uncompressed;
    try (InputStream is = Files.newInputStream(compressed.toPath());
            CompressorInputStream in = new LZMACompressorInputStream(is)) {
        uncompressed = IOUtils.toByteArray(in);
    }
    assertArrayEquals(orig, uncompressed);
}

// ================================ SEM O RESOURCE OPTIMISM ================================

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/DynaPropertyTest.java
@Test
void testHashCode() {

    final int initialHashCode = testPropertyWithNameAndTypeAndContentType.hashCode();
    assertEquals(testPropertyWithName.hashCode(), testProperty1Duplicate.hashCode());
    assertEquals(testPropertyWithNameAndType.hashCode(), testProperty2Duplicate.hashCode());
    assertEquals(testPropertyWithNameAndTypeAndContentType.hashCode(), testProperty3Duplicate.hashCode());
    assertEquals(initialHashCode, testPropertyWithNameAndTypeAndContentType.hashCode());
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/ArrayRealVectorTest.java
@Test
public void testPredicates() {

    Assert.assertEquals(create(new double[] { Double.NaN, 1, 2 }).hashCode(),
            create(new double[] { 0, Double.NaN, 2 }).hashCode());

    Assert.assertTrue(
            create(new double[] { Double.NaN, 1, 2 }).hashCode() != create(new double[] { 0, 1, 2 }).hashCode());
}

// commons-lang/src/test/java/org/apache/commons/lang3/SystemUtilsTest.java
/**
 * Assumes no security manager exists.
 */
@Test
void testGetJavaHome() {
    final File dir = SystemUtils.getJavaHome();
    assertNotNull(dir);
    assertTrue(dir.exists());
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
}

@Test
public void testScale() {
    final double[] test = new double[] { -2.5, -1, 0, 1, 2.5 };
    final double[] correctTest = Arrays.copyOf(test, test.length);
    final double[] correctScaled = new double[] { 5.25, 2.1, 0, -2.1, -5.25 };

    final double[] scaled = MathArrays.scale(-2.1, test);

    // Make sure test has not changed
    for (int i = 0; i < test.length; i++) {
        Assert.assertEquals(correctTest[i], test[i], 0);
    }

    // Test scaled values
    for (int i = 0; i < scaled.length; i++) {
        Assert.assertEquals(correctScaled[i], scaled[i], 0);
    }
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

// commons-lang/src/test/java/org/apache/commons/lang3/text/StrLookupTest.java
@Test
void testSystemPropertiesLookupReplacedProperties() {
    final Properties oldProperties = System.getProperties();
    final String osName = "os.name";
    final String newOsName = oldProperties.getProperty(osName) + "_changed";

    final StrLookup<String> sysLookup = StrLookup.systemPropertiesLookup();
    final Properties newProps = new Properties();
    newProps.setProperty(osName, newOsName);
    System.setProperties(newProps);
    try {
        assertEquals(newOsName, sysLookup.lookup(osName), "Changed properties not detected");
    } finally {
        System.setProperties(oldProperties);
    }
}

/**
 * Positive getPropertyDescriptor on property {@code doubleProperty}.
 */
@Test
void testGetDescriptorDouble() throws Exception {
    testGetDescriptorBase("doubleProperty", "getDoubleProperty", "setDoubleProperty");
}
