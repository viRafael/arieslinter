// commons-beanutils/src/test/java/org/apache/commons/beanutils2/DynaPropertyTest.java
@Test
void testEqualsObject() {
    assertEquals(testPropertyWithName, testProperty1Duplicate);
    assertEquals(testPropertyWithNameAndType, testProperty2Duplicate);
    assertEquals(testPropertyWithNameAndTypeAndContentType, testProperty3Duplicate);
    assertFalse(testPropertyWithName.equals(testPropertyWithNameAndType));
    assertFalse(testPropertyWithNameAndType.equals(testPropertyWithNameAndTypeAndContentType));
    assertFalse(testPropertyWithName.equals(null));
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/DynaPropertyTest.java
@Test
void testHashCode() {
    final int initialHashCode = testPropertyWithNameAndTypeAndContentType.hashCode();
    assertEquals(testPropertyWithName.hashCode(), testProperty1Duplicate.hashCode());
    assertEquals(testPropertyWithNameAndType.hashCode(), testProperty2Duplicate.hashCode());
    assertEquals(testPropertyWithNameAndTypeAndContentType.hashCode(), testProperty3Duplicate.hashCode());
    assertEquals(initialHashCode, testPropertyWithNameAndTypeAndContentType.hashCode());
}

// commons-beanutils/src/test/java/org/apache/commons/beanutils2/BeanPropertyValueEqualsPredicateTest.java
@Test
void testEvaluateWithBooleanProperty() {
    final BeanPropertyValueEqualsPredicate<TestBean, Boolean> predicate = new BeanPropertyValueEqualsPredicate<>(
            "booleanProperty", expectedBooleanValue);
    assertTrue(predicate.test(new TestBean(expectedBooleanValue.booleanValue())));
    assertFalse(predicate.test(new TestBean(!expectedBooleanValue.booleanValue())));
}

// commons-collections/src/test/java/org/apache/commons/collections4/ArrayUtilsTest.java
@Test
void testContains() {
    final Object[] array = { "0", "1", "2", "3", null, "0" };
    assertFalse(ArrayUtils.contains(null, null));
    assertFalse(ArrayUtils.contains(null, "1"));
    assertTrue(ArrayUtils.contains(array, "0"));
    assertTrue(ArrayUtils.contains(array, "1"));
    assertTrue(ArrayUtils.contains(array, "2"));
    assertTrue(ArrayUtils.contains(array, "3"));
    assertTrue(ArrayUtils.contains(array, null));
    assertFalse(ArrayUtils.contains(array, "notInArray"));
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

// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetExtensionPathBaseCases() {
    assertEquals("foo", FileNameUtils.getExtension(Paths.get("a/b/c/bar.foo")));
    assertEquals("", FileNameUtils.getExtension(Paths.get("foo")));
    assertEquals("", FileNameUtils.getExtension(Paths.get("")));
    assertEquals("", FileNameUtils.getExtension(Paths.get(".")));
    for (final File f : File.listRoots()) {
        assertNull(FileNameUtils.getExtension(f.toPath()));
    }
    if (SystemUtils.IS_OS_WINDOWS) {
        assertNull(FileNameUtils.getExtension(Paths.get("C:\\")));
    }
}

// commons-compress/src/test/java/org/apache/commons/compress/utils/FileNameUtilsTest.java
@Test
void testGetExtensionPathCornerCases() {
    assertNull(FileNameUtils.getExtension((String) null));
    assertEquals("", FileNameUtils.getExtension(Paths.get("foo.")));
    assertEquals("foo", FileNameUtils.getExtension(Paths.get("bar/.foo")));
}

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

// commons-io/src/test/java/org/apache/commons/io/FilenameUtilsTest.java
@Test
void testGetBaseName() {
    assertNull(FilenameUtils.getBaseName(null));
    assertEquals("noseparator", FilenameUtils.getBaseName("noseparator.inthispath"));
    assertEquals("c", FilenameUtils.getBaseName("a/b/c.txt"));
    assertEquals("c", FilenameUtils.getBaseName("a/b/c"));
    assertEquals("", FilenameUtils.getBaseName("a/b/c/"));
    assertEquals("c", FilenameUtils.getBaseName("a\\b\\c"));
    assertEquals("file.txt", FilenameUtils.getBaseName("file.txt.bak"));
}

// ==================== SEM O ASSERTION ROULETTE ====================
// commons-io/src/test/java/org/apache/commons/io/input/ReaderInputStreamTest.java
@Test
public void testAvailableAfterClose() throws IOException {
    try (InputStream inputStream = createInputStream()) {
        inputStream.close();
        assertEquals(0, inputStream.available());
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

// commons-collections/src/test/java/org/apache/commons/collections4/iterators/ExtendedIteratorTest.java
@Test
void testAndThen() {
    final Iterator<Integer> iter1 = Arrays.asList(1, 2, 3).iterator();
    final Iterator<Integer> iter2 = Arrays.asList(4, 5, 6).iterator();

    final ExtendedIterator<Integer> underTest = ExtendedIterator.create(iter1).andThen(iter2);
    final List<Integer> actual = new ArrayList<>();
    underTest.forEachRemaining(actual::add);
    assertEquals(collectionA, actual);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/optim/nonlinear/scalar/noderiv/SimplexOptimizerNelderMeadTest.java
@Ignore("See MATH-1552")
@Test
public void testElliRotated() {
    doTest(new OptimTestUtils.ElliRotated(),
            OptimTestUtils.point(DIM, 1.0, 1e-1),
            GoalType.MINIMIZE,
            7467,
            new PointValuePair(OptimTestUtils.point(DIM, 0.0), 0.0),
            1e-14);
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

// commons-text/src/test/java/org/apache/commons/text/lookup/DefaultStringLookupTest.java
@Test
void testEnumValues() {
    final Map<String, StringLookup> stringLookupMap = new HashMap<>();
    StringLookupFactory.INSTANCE.addDefaultStringLookups(stringLookupMap);
    // Loop through all enums
    for (final DefaultStringLookup stringLookup : DefaultStringLookup.values()) {
        assertSame(stringLookupMap.get(stringLookup.getKey()), stringLookupMap.get(stringLookup.getKey()));
    }
}

// commons-lang/src/test/java/org/apache/commons/lang3/text/translate/EntityArraysTest.java
@Test
void testISO8859_1_ESCAPE() {
    final Set<String> col0 = new HashSet<>();
    final Set<String> col1 = new HashSet<>();
    final String[][] sa = EntityArrays.ISO8859_1_ESCAPE();
    boolean success = true;
    for (int i = 0; i < sa.length; i++) {
        final boolean add0 = col0.add(sa[i][0]);
        final boolean add1 = col1.add(sa[i][1]);
        if (!add0) {
            success = false;
            System.out.println("Already added entry 0: " + i + " " + sa[i][0] + " " + sa[i][1]);
        }
        if (!add1) {
            success = false;
            System.out.println("Already added entry 1: " + i + " " + sa[i][0] + " " + sa[i][1]);
        }
    }
    assertTrue(success, "One or more errors detected");
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

    // .
    // .
    // .

}