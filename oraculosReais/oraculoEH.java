class OraculoEH {

    // commons-beanutils/src/test/java/org/apache/commons/beanutils2/converters/DateLocaleConverterTest.java
    @Test
    void testInvalidDate() {

        converter = DateLocaleConverter.builder().setLocale(defaultLocale).get();

        try {
            converter.convert("01/10/2004", "dd-MM-yyyy");
        } catch (final ConversionException e) {
            assertEquals("Error parsing date '01/10/2004' at position = 2", e.getMessage(), "Parse Error");
        }

        try {
            converter.convert("01-10-2004X", "dd-MM-yyyy");
        } catch (final ConversionException e) {
            assertEquals("Date '01-10-2004X' contains unparsed characters from position = 10", e.getMessage(),
                    "Parse Length");
        }

    }

    // commons-collections/src/test/java/org/apache/commons/collections4/map/CaseInsensitiveMapTest.java
    @Test
    void testLocaleIndependence() {
        final Locale orig = Locale.getDefault();

        final Locale[] locales = { Locale.ENGLISH, new Locale("tr", StringUtils.EMPTY, StringUtils.EMPTY),
                Locale.getDefault() };

        final String[][] data = {
                { "i", "I" },
                { "\u03C2", "\u03C3" },
                { "\u03A3", "\u03C2" },
                { "\u03A3", "\u03C3" },
        };

        try {
            for (final Locale locale : locales) {
                Locale.setDefault(locale);
                for (int j = 0; j < data.length; j++) {
                    assertTrue(data[j][0].equalsIgnoreCase(data[j][1]), "Test data corrupt: " + j);
                    final CaseInsensitiveMap<String, String> map = new CaseInsensitiveMap<>();
                    map.put(data[j][0], "value");
                    assertEquals("value", map.get(data[j][1]), Locale.getDefault() + ": " + j);
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
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

    // commons-csv/src/test/java/org/apache/commons/csv/CSVRecordTest.java
    @Test
    void testCSVRecordNULLValues() throws IOException {
        try (CSVParser parser = CSVParser.parse("A,B\r\nONE,TWO", CSVFormat.DEFAULT.withHeader())) {
            final CSVRecord csvRecord = new CSVRecord(parser, null, null, 0L, 0L, 0L);
            assertEquals(0, csvRecord.size());
            assertThrows(IllegalArgumentException.class, () -> csvRecord.get("B"));
        }
    }

    // commons-email/commons-email2-jakarta/src/test/java/org/apache/commons/mail2/jakarta/InputStreamDataSourceTest.java
    @Test
    void testGetInputStream() throws IOException {
        final byte[] testData = "Test data for InputStream".getBytes();
        final InputStream testInputStream = new ByteArrayInputStream(testData);
        final InputStreamDataSource dataSource = new InputStreamDataSource(testInputStream, "application/octet-stream");
        try (InputStream inputStream = dataSource.getInputStream()) {
            final byte[] readData = new byte[testData.length];
            final int bytesRead = inputStream.read(readData);
            assertEquals(testData.length, bytesRead);
            assertArrayEquals(testData, readData);
        }
    }

    // commons-io/src/test/java/org/apache/commons/io/input/ReaderInputStreamTest.java
    @Test
    public void testAvailableAfterClose() throws IOException {
        try (InputStream inputStream = createInputStream()) {
            inputStream.close();
            assertEquals(0, inputStream.available());
        }
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

    // commons-pool/src/test/java/org/apache/commons/pool3/impl/TestDefaultPooledObjectInfo.java
    @Test
    void testGetLastBorrowTrace() throws Exception {
        final AbandonedConfig abandonedConfig = new AbandonedConfig();

        abandonedConfig.setRemoveAbandonedOnBorrow(true);
        abandonedConfig.setRemoveAbandonedTimeout(TestConstants.ONE_SECOND_DURATION);
        abandonedConfig.setLogAbandoned(true);
        try (GenericObjectPool<String, TestException> pool = new GenericObjectPool<>(new SimpleFactory(),
                new GenericObjectPoolConfig<>(), abandonedConfig)) {

            pool.borrowObject();
            // pool.returnObject(s1); // Object not returned, causes abandoned object
            // created exception

            final Set<DefaultPooledObjectInfo> strings = pool.listAllObjects();
            final DefaultPooledObjectInfo s1Info = strings.iterator().next();
            final String lastBorrowTrace = s1Info.getLastBorrowTrace();

            assertTrue(lastBorrowTrace.startsWith("Pooled object created"));
        }
    }

    // commons-text/src/test/java/org/apache/commons/text/translate/NumericEntityUnescaperTest.java
    @Test
    void testUnfinishedEntity() {
        // parse it
        NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional);
        String input = "Test &#x30 not test";
        String expected = "Test \u0030 not test";

        String result = neu.translate(input);
        assertEquals(expected, result, "Failed to support unfinished entities (i.e. missing semicolon)");

        // ignore it
        neu = new NumericEntityUnescaper();
        input = "Test &#x30 not test";
        expected = input;

        result = neu.translate(input);
        assertEquals(expected, result, "Failed to ignore unfinished entities (i.e. missing semicolon)");

        // fail it
        neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon);
        input = "Test &#x30 not test";

        try {
            result = neu.translate(input);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException iae) {
            // expected
        }
    }

    // commons-text/src/test/java/org/apache/commons/text/StringEscapeUtilsTest.java
    @Test
    void testUnescapeJava() throws IOException {
        assertNull(StringEscapeUtils.unescapeJava(null));
        try {
            StringEscapeUtils.UNESCAPE_JAVA.translate(null, null);
            fail("Exception expected!");
        } catch (final IOException ex) {
            fail("Exception expected!");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        try {
            StringEscapeUtils.UNESCAPE_JAVA.translate("", null);
            fail("Exception expected!");
        } catch (final IOException ex) {
            fail("Exception expected!");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        assertThrows(RuntimeException.class, () -> StringEscapeUtils.unescapeJava("\\u02-3"));

        assertUnescapeJava("", "");
        assertUnescapeJava("test", "test");
        assertUnescapeJava("\ntest\b", "\\ntest\\b");
        assertUnescapeJava("\u123425foo\ntest\b", "\\u123425foo\\ntest\\b");
        assertUnescapeJava("'\foo\teste\r", "\\'\\foo\\teste\\r");
        assertUnescapeJava("", "\\");
        // foo
        assertUnescapeJava("\uABCDx", "\\uabcdx", "lowercase Unicode");
        assertUnescapeJava("\uABCDx", "\\uABCDx", "uppercase Unicode");
        assertUnescapeJava("\uABCD", "\\uabcd", "Unicode as final character");
    }
}

// =========================== SEM O EXCEPTION HANDLING ======================================

@Test
public void testReadEofTwice() throws IOException {
    try (ReaderInputStream reader = ReaderInputStream.builder().setCharset(StandardCharsets.UTF_8)
            .setReader(new StringReader("123")).get()) {
        assertEquals('1', reader.read());
        assertEquals('2', reader.read());
        assertEquals('3', reader.read());
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read());
    }
}

@Test
private void testReadZero(final String inStr, final ReaderInputStream inputStream) throws IOException {
    final byte[] bytes = new byte[30];
    assertEquals(0, inputStream.read(bytes, 0, 0));
    assertEquals(inStr.length(), inputStream.read(bytes, 0, inStr.length() + 1));
    // Should always return 0 for length == 0
    assertEquals(0, inputStream.read(bytes, 0, 0));
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
public void testCloseHandleIOException() throws IOException {
    ProxyInputStreamTest.testCloseHandleIOException(ThrottledInputStream.builder());
}

@Test
public void testClose() throws IOException {
    final Reader reader = new CharSequenceReader("FooBar");
    checkRead(reader, "Foo");
    reader.close();
    checkRead(reader, "Foo");

    final Reader subReader = new CharSequenceReader("xFooBarx", 1, 7);
    checkRead(subReader, "Foo");
    subReader.close();
    checkRead(subReader, "Foo");
}
