package br.ufba.arieslinter.oracles;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Oracle for Eager Test smell.
 * Detection: A test method contains multiple calls to multiple production methods.
 * (Production methods are non-framework, non-object, non-getter/setter methods).
 */
public class EagerTestOracle {

    @Test
    public void test1_EagerBehavior() {
        Calculator calc = new Calculator();
        // Multiple different production methods
        calc.add(10);
        calc.subtract(5);
        calc.multiply(2);
        assertEquals(10, calc.getResult());
    }

    @Test
    public void test2_UserProcess() {
        UserService service = new UserService();
        // Several methods of the production object
        service.register("user");
        service.authenticate("user", "pass");
        service.updateProfile("user", "new data");
        assertTrue(service.isLoggedIn("user"));
    }

    @Test
    public void test3_FileOperations() {
        FileManager fm = new FileManager();
        fm.createFile("test.txt");
        fm.writeFile("test.txt", "content");
        fm.readLines("test.txt");
        fm.deleteFile("test.txt");
    }

    @Test
    public void test4_DatabaseEager() {
        Database db = new Database();
        db.connect();
        db.executeQuery("SELECT 1");
        db.commit();
        db.disconnect();
    }

    @Test
    public void test5_OrderSystem() {
        Order order = new Order();
        order.addItem("item1");
        order.calculateTotal();
        order.applyDiscount(0.1);
        order.place();
    }

    @Test
    public void test6_ComplexCalculation() {
        Engine engine = new Engine();
        engine.start();
        engine.adjustPressure(10.5);
        engine.checkStatus();
        engine.stop();
    }

    @Test
    public void test7_ListProcessing() {
        DataProcessor dp = new DataProcessor();
        dp.loadData();
        dp.filterValues();
        dp.sort();
        dp.saveResults();
    }

    @Test
    public void test8_AuthFlow() {
        Authenticator auth = new Authenticator();
        auth.validateToken("token");
        auth.checkPermissions("admin");
        auth.logAccess();
    }

    @Test
    public void test9_GraphicsSetup() {
        Renderer r = new Renderer();
        r.initialize();
        r.loadTexture("bg.png");
        r.setResolution(1920, 1080);
        r.drawFrame();
    }

    @Test
    public void test10_MultiObjectEager() {
        // Eager Test often involves one production object, but the detection 
        // counts multiple production method calls in general.
        ServiceA a = new ServiceA();
        ServiceB b = new ServiceB();
        a.doWork();
        b.process();
    }

    /**
     * NEGATIVE TEST: These should NOT be detected as Eager Test
     */
    @Test
    public void testValid_SingleProductionMethod() {
        Calculator calc = new Calculator();
        calc.add(10); // Only one production method call
        assertEquals(10, calc.getResult()); // getResult is a getter (ignored)
    }

    @Test
    public void testValid_MultipleCallsToSameMethod() {
        Calculator calc = new Calculator();
        calc.add(10);
        calc.add(20);
        calc.add(30); // Same production method multiple times
        assertEquals(60, calc.getResult());
    }

    // Helper classes for the examples
    private static class Calculator {
        void add(int v) {}
        void subtract(int v) {}
        void multiply(int v) {}
        int getResult() { return 0; }
    }
    private static class UserService {
        void register(String u) {}
        void authenticate(String u, String p) {}
        void updateProfile(String u, String d) {}
        boolean isLoggedIn(String u) { return true; }
    }
    private static class FileManager {
        void createFile(String f) {}
        void writeFile(String f, String c) {}
        void readLines(String f) {}
        void deleteFile(String f) {}
    }
    private static class Database {
        void connect() {}
        void executeQuery(String q) {}
        void commit() {}
        void disconnect() {}
    }
    private static class Order {
        void addItem(String i) {}
        void calculateTotal() {}
        void applyDiscount(double d) {}
        void place() {}
    }
    private static class Engine {
        void start() {}
        void stop() {}
        void adjustPressure(double p) {}
        void checkStatus() {}
    }
    private static class DataProcessor {
        void loadData() {}
        void filterValues() {}
        void sort() {}
        void saveResults() {}
    }
    private static class Authenticator {
        void validateToken(String t) {}
        void checkPermissions(String p) {}
        void logAccess() {}
    }
    private static class Renderer {
        void initialize() {}
        void loadTexture(String t) {}
        void setResolution(int w, int h) {}
        void drawFrame() {}
    }
    private static class ServiceA { void doWork() {} }
    private static class ServiceB { void process() {} }
}
