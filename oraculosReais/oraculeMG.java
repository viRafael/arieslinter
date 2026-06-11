// commons-email/commons-email2-javax/src/test/java/org/apache/commons/mail2/javax/HtmlEmailTest.java
@Test
void testEmbedDataSource() throws Exception {
    final File tmpFile = File.createTempFile("testEmbedDataSource", "txt");
    tmpFile.deleteOnExit();
    final FileDataSource dataSource = new FileDataSource(tmpFile);

    // does embedding a datasource without a name fail?
    assertThrows(EmailException.class, () -> email.embed(dataSource, ""));

    // properly embed the datasource
    final String cid = email.embed(dataSource, "testname");

    // does embedding the same datasource under the same name return
    // the original cid?
    final String sameCid = email.embed(dataSource, "testname");

    assertEquals(cid, sameCid, "didn't get same CID for embedding same datasource twice");

    // does embedding another datasource under the same name fail?
    final File anotherFile = File.createTempFile("testEmbedDataSource2", "txt");
    anotherFile.deleteOnExit();
    final FileDataSource anotherDS = new FileDataSource(anotherFile);
    assertThrows(EmailException.class, () -> email.embed(anotherDS, "testname"));
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

// commons-lang/src/test/java/org/apache/commons/lang3/SystemUtilsTest.java
/**
 * Assumes no security manager exists.
 */
@Test
void testGetJavaIoTmpDir() {
    final File dir = SystemUtils.getJavaIoTmpDir();
    assertNotNull(dir);
    assertTrue(dir.exists());
}

// commons-lang/src/test/java/org/apache/commons/lang3/SystemUtilsTest.java
/**
 * Assumes no security manager exists.
 */
@Test
void testGetUserDir() {
    final File dir = SystemUtils.getUserDir();
    assertNotNull(dir);
    assertTrue(dir.exists());
}

// commons-lang/src/test/java/org/apache/commons/lang3/SystemUtilsTest.java
/**
 * Assumes no security manager exists.
 */
@Test
void testGetUserHome() {
    final File dir = SystemUtils.getUserHome();
    assertNotNull(dir);
    assertTrue(dir.exists());
}

// commons-csv/src/test/java/org/apache/commons/csv/CSVParserTest.java
@Test
void testParseFileCharsetNullFormat() throws IOException {
    final File file = new File("src/test/resources/org/apache/commons/csv/CSVFileParser/test.csv");
    try (CSVParser parser = CSVParser.parse(file, Charset.defaultCharset(), null)) {
        // null maps to DEFAULT.
        parseFully(parser);
    }
}

// commons-csv/src/test/java/org/apache/commons/csv/CSVParserTest.java
@Test
void testParseNullFileFormat() {
    assertThrows(NullPointerException.class,
            () -> CSVParser.parse((File) null, Charset.defaultCharset(), CSVFormat.DEFAULT));
}

// commons-compress/src/test/java/org/apache/commons/compress/archivers/tar/TarFileTest.java
@Test
void testDirectoryWithLongNameEndsWithSlash() throws IOException {
    final String rootPath = getTempDirFile().getAbsolutePath();
    final String dirDirectory = "COMPRESS-509";
    final int count = 100;
    final File root = new File(rootPath + "/" + dirDirectory);
    root.mkdirs();
    for (int i = 1; i < count; i++) {
        // create empty dirs with incremental length
        final String subDir = StringUtils.repeat('a', i);
        final File dir = new File(rootPath + "/" + dirDirectory, "/" + subDir);
        dir.mkdir();

        // tar these dirs
        final String fileName = "/" + dirDirectory + "/" + subDir;
        final File tarF = new File(rootPath + "/tar" + i + ".tar");
        try (OutputStream dest = Files.newOutputStream(tarF.toPath());
                TarArchiveOutputStream out = new TarArchiveOutputStream(new BufferedOutputStream(dest))) {
            out.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            final File file = new File(rootPath, fileName);
            final TarArchiveEntry entry = new TarArchiveEntry(file);
            entry.setName(fileName);
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
            out.flush();
        }
        // untar these tars
        try (TarFile tarFile = TarFile.builder().setFile(tarF).get()) {
            for (final TarArchiveEntry entry : tarFile.getEntries()) {
                assertTrue(entry.getName().endsWith("/"), "Entry name: " + entry.getName());
            }
        }
    }
}

// commons-compress/src/test/java/org/apache/commons/compress/harmony/pack200/ArchiveTest.java
@Test
void testAnnotations2() throws IOException, Pack200Exception, URISyntaxException {
    final File file = createTempFile("annotations", ".pack");
    try (JarFile in = new JarFile(new File(Archive.class.getResource("/pack200/annotations.jar").toURI()));
            FileOutputStream out = new FileOutputStream(file)) {
        final PackingOptions options = new PackingOptions();
        options.setGzip(false);
        new Archive(in, out, options).pack();
    }

    // now unpack
    final File file2 = createTempFile("annotationsout", ".jar");
    try (InputStream in2 = new FileInputStream(file);
            JarOutputStream out2 = new JarOutputStream(new FileOutputStream(file2))) {
        final org.apache.commons.compress.harmony.unpack200.Archive archive = new org.apache.commons.compress.harmony.unpack200.Archive(
                in2, out2);
        archive.unpack();
    }
    // TODO: This isn't quite right - to fix
    try (JarFile jarFile = new JarFile(file2);
            JarFile jarFile2 = new JarFile(new File(Archive.class.getResource("/pack200/annotationsRI.jar").toURI()))) {
        compareFiles(jarFile, jarFile2);
    }
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

// ================== sem o MYSTERY GUEST ==================

// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetBaseNamePathCornerCases() {
    assertNull(FileNameUtils.getBaseName((Path) null));
    assertEquals("foo", FileNameUtils.getBaseName(Paths.get("foo.")));
    assertEquals("", FileNameUtils.getBaseName(Paths.get("bar/.foo")));
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/ArrayRealVectorTest.java
@Test
public void testPredicates() {

    Assert.assertEquals(create(new double[] { Double.NaN, 1, 2 }).hashCode(),
            create(new double[] { 0, Double.NaN, 2 }).hashCode());

    Assert.assertTrue(
            create(new double[] { Double.NaN, 1, 2 }).hashCode() != create(new double[] { 0, 1, 2 }).hashCode());
}

// commons-collections/src/test/java/org/apache/commons/collections4/list/LazyListTest.java
@Test
@Override
public void testCanonicalFullCollectionExists() {
    // Factory and Transformer are not serializable
}

// commons-collections/src/test/java/org/apache/commons/collections4/list/LazyListTest.java
@Test
@Override
public void testSerializeDeserializeThenCompare() {
    // Factory and Transformer are not serializable
}

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

// commons-collections/src/test/java/org/apache/commons/collections4/IteratorUtilsTest.java
/**
 * Test empty iterator
 */
@Test
void testEmptyIterator() {
    assertSame(EmptyIterator.INSTANCE, IteratorUtils.EMPTY_ITERATOR);
    assertSame(EmptyIterator.RESETTABLE_INSTANCE, IteratorUtils.EMPTY_ITERATOR);
    assertTrue(IteratorUtils.EMPTY_ITERATOR instanceof Iterator);
    assertTrue(IteratorUtils.EMPTY_ITERATOR instanceof ResettableIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR instanceof OrderedIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR instanceof ListIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR instanceof MapIterator);
    assertFalse(IteratorUtils.EMPTY_ITERATOR.hasNext());
    IteratorUtils.EMPTY_ITERATOR.reset();
    assertSame(IteratorUtils.EMPTY_ITERATOR, IteratorUtils.EMPTY_ITERATOR);
    assertSame(IteratorUtils.EMPTY_ITERATOR, IteratorUtils.emptyIterator());

    assertThrows(NoSuchElementException.class, () -> IteratorUtils.EMPTY_ITERATOR.next());
    assertThrows(IllegalStateException.class, () -> IteratorUtils.EMPTY_ITERATOR.remove());
}

// commons-collections/src/test/java/org/apache/commons/collections4/functors/NullPredicateTest.java
@Test
void testNullPredicate() {
    assertSame(NullPredicate.nullPredicate(), NullPredicate.nullPredicate());
    assertPredicateTrue(NullPredicate.nullPredicate(), null);
}

@Test
public void testScaleInPlace() {
    final double[] test = new double[] { -2.5, -1, 0, 1, 2.5 };
    final double[] correctScaled = new double[] { 5.25, 2.1, 0, -2.1, -5.25 };
    MathArrays.scaleInPlace(-2.1, test);

    // Make sure test has changed
    for (int i = 0; i < test.length; i++) {
        Assert.assertEquals(correctScaled[i], test[i], 0);
    }
}

@Test(expected = DimensionMismatchException.class)
public void testEbeAddPrecondition() {
    MathArrays.ebeAdd(new double[3], new double[4]);
}