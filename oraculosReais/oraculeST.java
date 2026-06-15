// commons-collections/src/test/java/org/apache/commons/collections4/bloomfilter/LayeredBloomFilterTest.java
@Test
void testExpiration() throws InterruptedException {
    // this test uses the instrumentation noted above to track changes for debugging
    // purposes.

    // list of timestamps that are expected to be expired.
    final List<Instant> lst = new ArrayList<>();
    final Shape shape = Shape.fromNM(4, 64);

    // create a filter that removes filters that are 4 seconds old
    // and quantises time to 1 second intervals.
    final LayeredBloomFilter<TimestampedBloomFilter<SimpleBloomFilter>> underTest = createTimedLayeredFilter(shape,
            Duration.ofMillis(600),
            Duration.ofMillis(150));

    for (int i = 0; i < 10; i++) {
        underTest.merge(TestingHashers.randomHasher());
    }
    underTest.processBloomFilters(dbg.and(x -> lst.add(((TimestampedBloomFilter) x).timestamp)));
    assertTrue(underTest.getDepth() > 1);

    Thread.sleep(300);
    for (int i = 0; i < 10; i++) {
        underTest.merge(TestingHashers.randomHasher());
    }
    dbgInstrument.add("=== AFTER 300 milliseconds ====\n");
    underTest.processBloomFilters(dbg);

    Thread.sleep(150);
    for (int i = 0; i < 10; i++) {
        underTest.merge(TestingHashers.randomHasher());
    }
    dbgInstrument.add("=== AFTER 450 milliseconds ====\n");
    underTest.processBloomFilters(dbg);

    // sleep 200 milliseconds to ensure we cross the 600 millisecond boundary
    Thread.sleep(200);
    underTest.merge(TestingHashers.randomHasher());
    dbgInstrument.add("=== AFTER 600 milliseconds ====\n");
    assertTrue(underTest.processBloomFilters(dbg.and(x -> !lst.contains(((TimestampedBloomFilter) x).timestamp))),
            "Found filter that should have been deleted: " + dbgInstrument.get(dbgInstrument.size() - 1));
}

// commons-compress/src/test/java/org/apache/commons/compress/archivers/tar/TarArchiveOutputStreamTest.java
@Test
void testLongNameMd5Hash() throws Exception {
    // @formatter:off
        final String longFileName =
            "a/considerably/longer/file/name/which/forces/use/of/the/long/link/header/which/appears/to/always/use/the/current/time/as/modification/date";
        // @formatter:on
    final Date modificationDate = new Date();

    final byte[] archive1 = createTarArchiveContainingOneDirectory(longFileName, modificationDate);
    final byte[] digest1 = MessageDigest.getInstance("MD5").digest(archive1);

    // let a second elapse otherwise the modification dates will be equal
    Thread.sleep(1000L);

    // now recreate exactly the same tar file
    final byte[] archive2 = createTarArchiveContainingOneDirectory(longFileName, modificationDate);
    // and I would expect the MD5 hash to be the same, but for long names it isn't
    final byte[] digest2 = MessageDigest.getInstance("MD5").digest(archive2);

    assertArrayEquals(digest1, digest2);

    // do I still have the correct modification date?
    // let a second elapse, so we don't get the current time
    Thread.sleep(1000);
    try (TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(archive2))) {
        final ArchiveEntry nextEntry = tarIn.getNextEntry();
        assertEquals(longFileName, nextEntry.getName());
        // tar archive stores modification time to second granularity only (floored)
        assertEquals(modificationDate.getTime() / 1000, nextEntry.getLastModifiedDate().getTime() / 1000);
    }
}

// commons-io/src/test/java/org/apache/commons/io/input/UnsynchronizedBufferedInputStreamTest.java
@Test
public void test_close() throws IOException {
    builder().setInputStream(isFile).get().close();

    try (InputStream in = new InputStream() {
        Object lock = new Object();

        @Override
        public void close() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public int read() {
            return 1;
        }

        @Override
        public int read(final byte[] buf, final int offset, final int length) {
            synchronized (lock) {
                try {
                    lock.wait(3000);
                } catch (final InterruptedException e) {
                    // Ignore
                }
            }
            return 1;
        }
    }) {
        final UnsynchronizedBufferedInputStream bufin = builder().setInputStream(in).get();
        final Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                bufin.close();
            } catch (final Exception e) {
                // Ignored
            }
        });
        thread.start();
        assertThrows(IOException.class, () -> bufin.read(new byte[100], 0, 99), "Should throw IOException");
    }
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

// commons-math/commons-math-legacy/src/test/java/org/apache/commons/math4/legacy/genetics/FixedElapsedTimeTest.java
@Test
public void testIsSatisfied() {
    final Population pop = new Population() {
        @Override
        public void addChromosome(final Chromosome chromosome) {
            // unimportant
        }

        @Override
        public Chromosome getFittestChromosome() {
            // unimportant
            return null;
        }

        @Override
        public int getPopulationLimit() {
            // unimportant
            return 0;
        }

        @Override
        public int getPopulationSize() {
            // unimportant
            return 0;
        }

        @Override
        public Population nextGeneration() {
            // unimportant
            return null;
        }

        @Override
        public Iterator<Chromosome> iterator() {
            // unimportant
            return null;
        }
    };

    final long start = System.nanoTime();
    final long duration = 3;
    final FixedElapsedTime tec = new FixedElapsedTime(duration, TimeUnit.SECONDS);

    while (!tec.isSatisfied(pop)) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    final long end = System.nanoTime();
    final long elapsedTime = end - start;
    final long diff = JdkMath.abs(elapsedTime - TimeUnit.SECONDS.toNanos(duration));

    Assert.assertTrue(diff < TimeUnit.MILLISECONDS.toNanos(100));
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

// commons-collections/src/test/java/org/apache/commons/collections4/map/LRUMapTest.java
@Test
void testSynchronizedRemoveFromEntrySet() throws InterruptedException {

    final Map<Object, Thread> map = new LRUMap<>(10000);

    final Map<Throwable, String> exceptions = new HashMap<>();
    final ThreadGroup tg = new ThreadGroup(getName()) {
        @Override
        public void uncaughtException(final Thread t, final Throwable e) {
            exceptions.put(e, t.getName());
            super.uncaughtException(t, e);
        }
    };

    final int[] counter = new int[1];
    counter[0] = 0;
    final Thread[] threads = new Thread[50];
    for (int i = 0; i < threads.length; ++i) {
        threads[i] = new Thread(tg, "JUnit Thread " + i) {

            @Override
            public void run() {
                int i = 0;
                try {
                    synchronized (this) {
                        notifyAll();
                        wait();
                    }
                    final Thread thread = Thread.currentThread();
                    while (i < 1000 && !interrupted()) {
                        synchronized (map) {
                            map.put(thread.getName() + "[" + ++i + "]", thread);
                        }
                    }
                    synchronized (map) {
                        map.entrySet().removeIf(entry -> entry.getValue() == this);
                    }
                } catch (final InterruptedException e) {
                    fail("Unexpected InterruptedException");
                }
                if (i > 0) {
                    synchronized (counter) {
                        counter[0]++;
                    }
                }
            }

        };
    }

    for (final Thread thread : threads) {
        synchronized (thread) {
            thread.start();
            thread.wait();
        }
    }

    for (final Thread thread : threads) {
        synchronized (thread) {
            thread.notifyAll();
        }
    }

    Thread.sleep(1000);

    for (final Thread thread : threads) {
        thread.interrupt();
    }
    for (final Thread thread : threads) {
        synchronized (thread) {
            thread.join();
        }
    }

    assertEquals(0, exceptions.size(), "Exceptions have been thrown: " + exceptions);
    assertTrue(counter[0] >= threads.length,
            "Each thread should have put at least 1 element into the map, but only " + counter[0] + " did succeed");
}

// commons-lang/src/test/java/org/apache/commons/lang3/concurrent/TimedSemaphoreTest.java
@Test
void testStartTimer() throws InterruptedException {
    final TimedSemaphoreTestImpl semaphore = new TimedSemaphoreTestImpl(PERIOD_MILLIS,
            UNIT, LIMIT);
    final ScheduledFuture<?> future = semaphore.startTimer();
    assertNotNull(future, "No future returned");
    ThreadUtils.sleepQuietly(DURATION);
    final int trials = 10;
    int count = 0;
    do {
        Thread.sleep(PERIOD_MILLIS);
        assertFalse(count++ > trials, "endOfPeriod() not called!");
    } while (semaphore.getPeriodEnds() <= 0);
    semaphore.shutdown();
}

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestDefaultPooledObjectInfo.java
@Test
void testTiming() throws Exception {
    try (GenericObjectPool<String, TestException> pool = new GenericObjectPool<>(new SimpleFactory())) {

        final long t1Millis = System.currentTimeMillis();

        Thread.sleep(50);
        final String s1 = pool.borrowObject();
        Thread.sleep(50);

        final long t2Millis = System.currentTimeMillis();

        Thread.sleep(50);
        pool.returnObject(s1);
        Thread.sleep(50);

        final long t3Millis = System.currentTimeMillis();

        Thread.sleep(50);
        pool.borrowObject();
        Thread.sleep(50);

        final long t4Millis = System.currentTimeMillis();

        final Set<DefaultPooledObjectInfo> strings = pool.listAllObjects();

        assertEquals(1, strings.size());

        final DefaultPooledObjectInfo s1Info = strings.iterator().next();

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

        assertTrue(s1Info.getCreateTime() > t1Millis);
        assertEquals(sdf.format(Long.valueOf(s1Info.getCreateTime())), s1Info.getCreateTimeFormatted());
        assertTrue(s1Info.getCreateTime() < t2Millis);

        assertTrue(s1Info.getLastReturnTime() > t2Millis);
        assertEquals(sdf.format(Long.valueOf(s1Info.getLastReturnTime())),
                s1Info.getLastReturnTimeFormatted());
        assertTrue(s1Info.getLastReturnTime() < t3Millis);

        assertTrue(s1Info.getLastBorrowTime() > t3Millis);
        assertEquals(sdf.format(Long.valueOf(s1Info.getLastBorrowTime())),
                s1Info.getLastBorrowTimeFormatted());
        assertTrue(s1Info.getLastBorrowTime() < t4Millis);
    }
}

// commons-pool/src/test/java/org/apache/commons/pool3/impl/TestGenericObjectPool.java
@SuppressWarnings("deprecation")
@Test
void testAbandonedPool() throws TestException, InterruptedException {
    final GenericObjectPoolConfig<String> config = new GenericObjectPoolConfig<>();
    config.setJmxEnabled(false);
    GenericObjectPool<String, TestException> abandoned = new GenericObjectPool<>(simpleFactory, config);
    abandoned.setDurationBetweenEvictionRuns(Duration.ofMillis(100)); // Starts evictor
    assertEquals(abandoned.getRemoveAbandonedTimeoutDuration(), abandoned.getRemoveAbandonedTimeoutDuration());

    // This is ugly, but forces GC to hit the pool
    final WeakReference<GenericObjectPool<String, TestException>> ref = new WeakReference<>(abandoned);
    abandoned = null;
    while (ref.get() != null) {
        System.gc();
        Thread.sleep(100);
    }
}

// ===================== SEM O SLEEP TEST ==========================

@Test
void testClearEndOfLineCharacters() {
    assertNull(EmailUtils.replaceEndOfLineCharactersWithSpaces(null));
    assertEquals("", EmailUtils.replaceEndOfLineCharactersWithSpaces(""));
    assertEquals("   ", EmailUtils.replaceEndOfLineCharactersWithSpaces("   "));
    assertEquals("abcdefg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abcdefg"));
    assertEquals("abc defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\rdefg"));
    assertEquals("abc defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\ndefg"));
    assertEquals("abc  defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\r\ndefg"));
    assertEquals("abc  defg", EmailUtils.replaceEndOfLineCharactersWithSpaces("abc\n\rdefg"));
}

@Test
void testIsEmptyMap() {
    assertTrue(EmailUtils.isEmpty((Map<?, ?>) null));
    final HashMap<String, String> map = new HashMap<>();
    assertTrue(EmailUtils.isEmpty(map));
    map.put("k", "v");
    assertFalse(EmailUtils.isEmpty(map));
}

@Test
void testIsEmptyString() {
    assertTrue(EmailUtils.isEmpty((String) null));
    assertTrue(EmailUtils.isEmpty(""));
    assertFalse(EmailUtils.isEmpty("a"));
}

@Test
void testUrlEncoding() {
    assertNull(EmailUtils.encodeUrl(null));
    assertEquals("abcdefg", EmailUtils.encodeUrl("abcdefg"));
    assertEquals("0123456789", EmailUtils.encodeUrl("0123456789"));
    assertEquals("Test%20CID", EmailUtils.encodeUrl("Test CID"));
    assertEquals("joe.doe@apache.org", EmailUtils.encodeUrl("joe.doe@apache.org"));
    assertEquals("joe+doe@apache.org", EmailUtils.encodeUrl("joe+doe@apache.org"));
    assertEquals("peter%26paul%26mary@oldmusic.org", EmailUtils.encodeUrl("peter&paul&mary@oldmusic.org"));
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

@Test(expected = DimensionMismatchException.class)
public void testEbeSubtractPrecondition() {
    MathArrays.ebeSubtract(new double[3], new double[4]);
}

@Test(expected = DimensionMismatchException.class)
public void testEbeMultiplyPrecondition() {
    MathArrays.ebeMultiply(new double[3], new double[4]);
}

@Test(expected = DimensionMismatchException.class)
public void testEbeDividePrecondition() {
    MathArrays.ebeDivide(new double[3], new double[4]);
}