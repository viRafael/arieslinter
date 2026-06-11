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

// commons-lang/src/test/java/org/apache/commons/lang3/SystemUtilsTest.java
@Test
@SuppressWarnings("deprecation")
void test_IS_JAVA() throws Exception {
    final String javaVersion = SystemUtils.JAVA_VERSION;
    final int lastSupportedVersion = getLastSupportedJavaVersion();
    if (javaVersion == null) {
        assertFalse(SystemUtils.IS_JAVA_1_1);
        assertFalse(SystemUtils.IS_JAVA_1_2);
        assertFalse(SystemUtils.IS_JAVA_1_3);
        assertFalse(SystemUtils.IS_JAVA_1_4);
        assertFalse(SystemUtils.IS_JAVA_1_5);
        assertFalse(SystemUtils.IS_JAVA_1_6);
        assertFalse(SystemUtils.IS_JAVA_1_7);
        assertFalse(SystemUtils.IS_JAVA_1_8);
        assertFalse(SystemUtils.IS_JAVA_1_9);
        assertFalse(SystemUtils.IS_JAVA_10);
        assertFalse(SystemUtils.IS_JAVA_11);
        assertFalse(SystemUtils.IS_JAVA_12);
        assertFalse(SystemUtils.IS_JAVA_13);
        assertFalse(SystemUtils.IS_JAVA_14);
        assertFalse(SystemUtils.IS_JAVA_15);
        assertFalse(SystemUtils.IS_JAVA_16);
        assertFalse(SystemUtils.IS_JAVA_17);
        assertFalse(SystemUtils.IS_JAVA_18);
        assertFalse(SystemUtils.IS_JAVA_19);
        assertFalse(SystemUtils.IS_JAVA_20);
        assertFalse(SystemUtils.IS_JAVA_21);
        assertFalse(SystemUtils.IS_JAVA_22);
        assertFalse(SystemUtils.IS_JAVA_23);
        for (int version = 9; version <= lastSupportedVersion; version++) {
            assertFalse(getIS_JAVA(version));
        }
    } else if (javaVersion.startsWith("1.8")) {
        assertFalse(SystemUtils.IS_JAVA_1_1);
        assertFalse(SystemUtils.IS_JAVA_1_2);
        assertFalse(SystemUtils.IS_JAVA_1_3);
        assertFalse(SystemUtils.IS_JAVA_1_4);
        assertFalse(SystemUtils.IS_JAVA_1_5);
        assertFalse(SystemUtils.IS_JAVA_1_6);
        assertFalse(SystemUtils.IS_JAVA_1_7);
        assertTrue(SystemUtils.IS_JAVA_1_8);
        assertFalse(SystemUtils.IS_JAVA_1_9);
        assertFalse(SystemUtils.IS_JAVA_10);
        assertFalse(SystemUtils.IS_JAVA_11);
        assertFalse(SystemUtils.IS_JAVA_12);
        assertFalse(SystemUtils.IS_JAVA_13);
        assertFalse(SystemUtils.IS_JAVA_14);
        assertFalse(SystemUtils.IS_JAVA_15);
        assertFalse(SystemUtils.IS_JAVA_16);
        assertFalse(SystemUtils.IS_JAVA_17);
        assertFalse(SystemUtils.IS_JAVA_18);
        assertFalse(SystemUtils.IS_JAVA_19);
        assertFalse(SystemUtils.IS_JAVA_20);
        assertFalse(SystemUtils.IS_JAVA_21);
        assertFalse(SystemUtils.IS_JAVA_22);
        assertFalse(SystemUtils.IS_JAVA_23);
        for (int version = 9; version <= lastSupportedVersion; version++) {
            assertFalse(getIS_JAVA(version));
        }

    } else if (!javaVersion.startsWith("1.")) {
        assertFalse(SystemUtils.IS_JAVA_1_1);
        assertFalse(SystemUtils.IS_JAVA_1_2);
        assertFalse(SystemUtils.IS_JAVA_1_3);
        assertFalse(SystemUtils.IS_JAVA_1_4);
        assertFalse(SystemUtils.IS_JAVA_1_5);
        assertFalse(SystemUtils.IS_JAVA_1_6);
        assertFalse(SystemUtils.IS_JAVA_1_7);
        assertFalse(SystemUtils.IS_JAVA_1_8);
        assertEquals(javaVersion.startsWith("9"), SystemUtils.IS_JAVA_1_9);

        for (int version = 9; version <= lastSupportedVersion; version++) {
            assertEquals(javaVersion.startsWith("" + version), getIS_JAVA(version));
        }

    } else {
        System.out.println("Can't test IS_JAVA value: " + javaVersion);
    }
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/analysis/interpolation/BicubicInterpolatingFunctionTest.java
private void testInterpolation(double minimumX,
        double maximumX,
        double minimumY,
        double maximumY,
        int numberOfElements,
        int numberOfSamples,
        BivariateFunction f,
        BivariateFunction dfdx,
        BivariateFunction dfdy,
        BivariateFunction d2fdxdy,
        double meanTolerance,
        double maxTolerance,
        boolean print) {
    double expected;
    double actual;
    double currentX;
    double currentY;
    final double deltaX = (maximumX - minimumX) / numberOfElements;
    final double deltaY = (maximumY - minimumY) / numberOfElements;
    final double[] xValues = new double[numberOfElements];
    final double[] yValues = new double[numberOfElements];
    final double[][] zValues = new double[numberOfElements][numberOfElements];
    final double[][] dzdx = new double[numberOfElements][numberOfElements];
    final double[][] dzdy = new double[numberOfElements][numberOfElements];
    final double[][] d2zdxdy = new double[numberOfElements][numberOfElements];

    for (int i = 0; i < numberOfElements; i++) {
        xValues[i] = minimumX + deltaX * i;
        final double x = xValues[i];
        for (int j = 0; j < numberOfElements; j++) {
            yValues[j] = minimumY + deltaY * j;
            final double y = yValues[j];
            zValues[i][j] = f.value(x, y);
            dzdx[i][j] = dfdx.value(x, y);
            dzdy[i][j] = dfdy.value(x, y);
            d2zdxdy[i][j] = d2fdxdy.value(x, y);
        }
    }

    final BivariateFunction interpolation = new BicubicInterpolatingFunction(xValues,
            yValues,
            zValues,
            dzdx,
            dzdy,
            d2zdxdy);

    for (int i = 0; i < numberOfElements; i++) {
        currentX = xValues[i];
        for (int j = 0; j < numberOfElements; j++) {
            currentY = yValues[j];
            expected = f.value(currentX, currentY);
            actual = interpolation.value(currentX, currentY);
            Assert.assertTrue("On data point: " + expected + " != " + actual,
                    Precision.equals(expected, actual));
        }
    }

    final UniformRandomProvider rng = RandomSource.WELL_19937_C.create(1234567L);
    final ContinuousDistribution.Sampler distX = UniformContinuousDistribution
            .of(xValues[0], xValues[xValues.length - 1]).createSampler(rng);
    final ContinuousDistribution.Sampler distY = UniformContinuousDistribution
            .of(yValues[0], yValues[yValues.length - 1]).createSampler(rng);

    double sumError = 0;
    for (int i = 0; i < numberOfSamples; i++) {
        currentX = distX.sample();
        currentY = distY.sample();
        expected = f.value(currentX, currentY);

        if (print) {
            System.out.println(currentX + " " + currentY + " -> ");
        }

        actual = interpolation.value(currentX, currentY);
        sumError += JdkMath.abs(actual - expected);

        if (print) {
            System.out.println(actual + " (diff=" + (expected - actual) + ")");
        }

        Assert.assertEquals(expected, actual, maxTolerance);
    }

    final double meanError = sumError / numberOfSamples;
    Assert.assertEquals(0, meanError, meanTolerance);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/analysis/solvers/UnivariateSolverUtilsTest.java
@Test(expected = MathIllegalArgumentException.class)
public void testSolveBadEndpoints() {
    double root = UnivariateSolverUtils.solve(sin, 4.0, -0.1, 1e-6);
    System.out.println("root=" + root);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/analysis/solvers/MullerSolverTest.java
@Test
public void testParameters() {
    UnivariateFunction f = new Sin();
    UnivariateSolver solver = new MullerSolver();

    try {
        // bad interval
        double root = solver.solve(100, f, 1, -1);
        System.out.println("root=" + root);
        Assert.fail("Expecting NumberIsTooLargeException - bad interval");
    } catch (NumberIsTooLargeException ex) {
        // expected
    }
    try {
        // no bracketing
        solver.solve(100, f, 2, 3);
        Assert.fail("Expecting NoBracketingException - no bracketing");
    } catch (NoBracketingException ex) {
        // expected
    }
}

// ================================ SEM O REDUNDANT PRINT ==========================================

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

@Test
public void testCreateDirectoriesForRootsLinkOptionNull() throws IOException {
    for (final File f : File.listRoots()) {
        final Path path = f.toPath();
        assertEquals(path.getParent(), PathUtils.createParentDirectories(path, (LinkOption) null));
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

@Test
public void testCreateDirectoriesSymlinkClashing() throws IOException {
    final Path symlinkedDir = createTempSymbolicLinkedRelativeDir(tempDirPath);
    assertEquals(symlinkedDir, PathUtils.createParentDirectories(symlinkedDir.resolve("child")));
}

@Test
public void testGetBaseNamePathCornerCases() {
    assertNull(PathUtils.getBaseName((Path) null));
    assertEquals("foo", PathUtils.getBaseName(Paths.get("foo.")));
    assertEquals("", PathUtils.getBaseName(Paths.get("bar/.foo")));
}

@Test
public void testGetLastModifiedFileTime_File_Present() throws IOException {
    assertNotNull(PathUtils.getLastModifiedFileTime(current().toFile()));
}

@Test
public void testGetLastModifiedFileTime_Path_Absent() throws IOException {
    assertNull(PathUtils.getLastModifiedFileTime(getNonExistentPath()));
}
