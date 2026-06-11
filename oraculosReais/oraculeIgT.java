// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/RealVectorTest.java
@Test
@Ignore("Abstract class RealVector does not implement append(RealVector).")
@Override
public void testAppendVector() {
    // Do nothing
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/EigenDecompositionTest.java
@Test
@Ignore
public void testRandomUnsymmetricMatrix() {
    for (int run = 0; run < 100; run++) {
        Random r = new Random(System.currentTimeMillis());

        // matrix size
        int size = r.nextInt(20) + 4;

        double[][] data = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                data[i][j] = r.nextInt(100);
            }
        }

        RealMatrix m = MatrixUtils.createRealMatrix(data);
        checkUnsymmetricMatrix(m);
    }
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/analysis/interpolation/AkimaSplineInterpolatorTest.java
@Ignore
@Test
public void testMath1635() {
    final double[] x = {
            5994, 6005, 6555, 6588, 6663,
            6760, 6770, 6792, 6856, 6964,
            7028, 7233, 7426, 7469, 7619,
            7910, 8038, 8178, 8414, 8747,
            8983, 9316, 9864, 9875
    };

    final double[] y = {
            3.0, 2.0, 2.0, 2.0, 2.0,
            2.0, 2.0, 2.0, 2.0, 2.0,
            2.0, 2.0, 2.0, 2.0, 2.0,
            2.0, 2.0, 2.0, 2.0, 2.0,
            2.0, 2.0, 2.0, 3.0
    };

    final AkimaSplineInterpolator interpolator = new AkimaSplineInterpolator(true);
    final PolynomialSplineFunction interpolate = interpolator.interpolate(x, y);
    final double value = interpolate.value(9584);
    final double expected = 2;
    Assert.assertEquals(expected, value, 1e-4);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/distribution/MixtureMultivariateNormalDistributionTest.java
@Ignore
@Test
public void testSampling() {
    final double[] weights = { 0.3, 0.7 };
    final double[][] means = { { -1.5, 2.0 },
            { 4.0, 8.2 } };
    final double[][][] covariances = { { { 2.0, -1.1 },
            { -1.1, 2.0 } },
            { { 3.5, 1.5 },
                    { 1.5, 3.5 } } };
    final MixtureMultivariateNormalDistribution d = new MixtureMultivariateNormalDistribution(weights, means,
            covariances);
    final MultivariateRealDistribution.Sampler sampler = d.createSampler(RandomSource.WELL_19937_C.create(50));

    final double[][] correctSamples = getCorrectSamples();
    final int n = correctSamples.length;
    final double[][] samples = AbstractMultivariateRealDistribution.sample(n, sampler);

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < samples[i].length; j++) {
            Assert.assertEquals("sample[" + j + "]",
                    correctSamples[i][j], samples[i][j], 1e-16);
        }
    }
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/optim/nonlinear/scalar/noderiv/SimplexOptimizerMultiDirectionalTest.java
@Ignore("See MATH-1552")
@Test
public void testElliRotated() {
    doTest(new OptimTestUtils.ElliRotated(),
            OptimTestUtils.point(DIM, 1.0, 1e-1),
            GoalType.MINIMIZE,
            911,
            new PointValuePair(OptimTestUtils.point(DIM, 0.0), 0.0),
            1e-9);
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/optim/nonlinear/scalar/noderiv/BOBYQAOptimizerTest.java
@Ignore
@Test
public void testConstrainedRosenWithMoreInterpolationPoints() {
    final int dim = 12;
    final double[] startPoint = OptimTestUtils.point(dim, 0.1);
    final double[][] boundaries = boundaries(dim, -1, 2);
    final PointValuePair expected = new PointValuePair(OptimTestUtils.point(dim, 1.0), 0.0);

    // This should have been 78 because in the code the hard limit is
    // said to be
    // ((DIM + 1) * (DIM + 2)) / 2 - (2 * DIM + 1)
    // i.e. 78 in this case, but the test fails for 48, 59, 62, 63, 64,
    // 65, 66, ...
    final int maxAdditionalPoints = 47;

    for (int num = 1; num <= maxAdditionalPoints; num++) {
        doTest(TestFunction.ROSENBROCK.withDimension(dim), startPoint, boundaries,
                GoalType.MINIMIZE,
                1e-12, 1e-6, 2000,
                num,
                expected,
                "num=" + num);
    }
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/RealVectorTest.java
@Test
@Ignore("Abstract class RealVector does not implement getSubvector(int, int)")
@Override
public void testGetSubVector() {
    // Do nothing
}

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/linear/RealVectorTest.java
@Test
@Ignore("Abstract class RealVector does not implement setSubvector(int, RealVector)")
@Override
public void testSetSubVectorSameType() {
    // Do nothing
}

// =================================== SEM O IGNORED TEST ==========================================

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
@SuppressWarnings("resource") // not necessary to close these resources
public void testConstructor2() {
    final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
    final byte[] one = new byte[1];
    final byte[] some = new byte[25];

    UnsynchronizedByteArrayInputStream is = newStream(empty, 0);
    assertEquals(empty.length, is.available());
    is = newStream(empty, 1);
    assertEquals(0, is.available());

    is = newStream(one, 0);
    assertEquals(one.length, is.available());
    is = newStream(one, 1);
    assertEquals(0, is.available());
    is = newStream(one, 2);
    assertEquals(0, is.available());

    is = newStream(some, 0);
    assertEquals(some.length, is.available());
    is = newStream(some, 1);
    assertEquals(some.length - 1, is.available());
    is = newStream(some, 10);
    assertEquals(some.length - 10, is.available());
    is = newStream(some, some.length);
    assertEquals(0, is.available());
}

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

@Test
public void testInvalidConstructor2OffsetUnder() {
    assertThrows(IllegalArgumentException.class, () -> {
        newStream(IOUtils.EMPTY_BYTE_ARRAY, -1);
    });
}

@Test
public void testConstructor1() throws IOException {
    final byte[] empty = IOUtils.EMPTY_BYTE_ARRAY;
    final byte[] one = new byte[1];
    final byte[] some = new byte[25];

    try (UnsynchronizedByteArrayInputStream is = newStream(empty)) {
        assertEquals(empty.length, is.available());
    }
    try (UnsynchronizedByteArrayInputStream is = newStream(one)) {
        assertEquals(one.length, is.available());
    }
    try (UnsynchronizedByteArrayInputStream is = newStream(some)) {
        assertEquals(some.length, is.available());
    }
}
