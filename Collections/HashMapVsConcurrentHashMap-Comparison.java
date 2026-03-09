import java.util.*;
import java.util.concurrent.*;

/**
 * HashMap vs ConcurrentHashMap - Complete Comparison
 *
 * QUICK ANSWER:
 * - HashMap: ✅ Use for single-threaded applications (GENERAL CASE)
 * - ConcurrentHashMap: ✅ Use for multi-threaded applications
 *
 * ConcurrentHashMap is NOT better in general - it's better only for concurrent scenarios!
 */
public class HashMapVsConcurrentHashMapComparison {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  HashMap vs ConcurrentHashMap - Complete Comparison           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Demo 1: Single-threaded scenario
        singleThreadedComparison();

        // Demo 2: Multi-threaded scenario
        multiThreadedComparison();

        // Demo 3: Key differences
        keyDifferences();

        // Demo 4: When to use which
        recommendations();
    }

    // DEMO 1: Single-threaded - HashMap is simpler and faster
    public static void singleThreadedComparison() {
        System.out.println("\n📌 DEMO 1: Single-Threaded Scenario");
        System.out.println("════════════════════════════════════════════════════════════");

        // HashMap - Simple and fast for single thread
        HashMap<String, Integer> hashMap = new HashMap<>();
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            hashMap.put("Key" + i, i);
        }
        long hashMapTime = System.nanoTime() - start;

        // ConcurrentHashMap - Has overhead for thread-safety
        ConcurrentHashMap<String, Integer> concurrentMap = new ConcurrentHashMap<>();
        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            concurrentMap.put("Key" + i, i);
        }
        long concurrentMapTime = System.nanoTime() - start;

        System.out.println("HashMap time:            " + hashMapTime / 1000 + " μs");
        System.out.println("ConcurrentHashMap time:  " + concurrentMapTime / 1000 + " μs");
        System.out.println("\n✅ RESULT: HashMap is slightly faster for single thread!");
        System.out.println("   (Less overhead, simpler implementation)");
    }

    // DEMO 2: Multi-threaded - ConcurrentHashMap is thread-safe
    public static void multiThreadedComparison() throws InterruptedException {
        System.out.println("\n\n📌 DEMO 2: Multi-Threaded Scenario");
        System.out.println("════════════════════════════════════════════════════════════");

        // Test 1: HashMap - NOT thread-safe
        System.out.println("\n❌ Test 1: HashMap with multiple threads (UNSAFE)");
        HashMap<Integer, Integer> hashMap = new HashMap<>();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) hashMap.put(i, i);
        });
        Thread t2 = new Thread(() -> {
            for (int i = 5000; i < 10000; i++) hashMap.put(i, i);
        });

        long start = System.currentTimeMillis();
        t1.start(); t2.start();
        t1.join(); t2.join();
        long hashMapTime = System.currentTimeMillis() - start;

        System.out.println("   Expected size: 10000");
        System.out.println("   Actual size:   " + hashMap.size());
        System.out.println("   Time taken:    " + hashMapTime + " ms");
        System.out.println("   ⚠️  May have data loss or corruption!");

        // Test 2: ConcurrentHashMap - Thread-safe
        System.out.println("\n✅ Test 2: ConcurrentHashMap with multiple threads (SAFE)");
        ConcurrentHashMap<Integer, Integer> concurrentMap = new ConcurrentHashMap<>();

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) concurrentMap.put(i, i);
        });
        Thread t4 = new Thread(() -> {
            for (int i = 5000; i < 10000; i++) concurrentMap.put(i, i);
        });

        start = System.currentTimeMillis();
        t3.start(); t4.start();
        t3.join(); t4.join();
        long concurrentMapTime = System.currentTimeMillis() - start;

        System.out.println("   Expected size: 10000");
        System.out.println("   Actual size:   " + concurrentMap.size());
        System.out.println("   Time taken:    " + concurrentMapTime + " ms");
        System.out.println("   ✅ Always correct - thread-safe!");

        System.out.println("\n✅ RESULT: ConcurrentHashMap is REQUIRED for multi-threaded scenarios!");
    }

    // DEMO 3: Key Differences
    public static void keyDifferences() {
        System.out.println("\n\n📌 DEMO 3: Key Differences");
        System.out.println("════════════════════════════════════════════════════════════");

        // Difference 1: Null handling
        System.out.println("\n1️⃣ Null Key/Value Support:");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(null, "value");      // ✅ Works
        hashMap.put("key", null);        // ✅ Works
        System.out.println("   HashMap allows null key: " + hashMap.containsKey(null));
        System.out.println("   HashMap allows null value: " + hashMap.containsValue(null));

        ConcurrentHashMap<String, String> concurrentMap = new ConcurrentHashMap<>();
        try {
            concurrentMap.put(null, "value");  // ❌ Throws NullPointerException
        } catch (NullPointerException e) {
            System.out.println("   ConcurrentHashMap null key: ❌ NullPointerException");
        }
        try {
            concurrentMap.put("key", null);    // ❌ Throws NullPointerException
        } catch (NullPointerException e) {
            System.out.println("   ConcurrentHashMap null value: ❌ NullPointerException");
        }

        // Difference 2: Iteration behavior
        System.out.println("\n2️⃣ Iteration During Modification:");
        hashMap = new HashMap<>();
        hashMap.put("A", "1");
        hashMap.put("B", "2");

        System.out.print("   HashMap: ");
        try {
            for (String key : hashMap.keySet()) {
                hashMap.put("C", "3");  // Modify during iteration
            }
            System.out.println("No exception (lucky!)");
        } catch (ConcurrentModificationException e) {
            System.out.println("❌ ConcurrentModificationException (expected)");
        }

        concurrentMap = new ConcurrentHashMap<>();
        concurrentMap.put("A", "1");
        concurrentMap.put("B", "2");

        System.out.print("   ConcurrentHashMap: ");
        for (String key : concurrentMap.keySet()) {
            concurrentMap.put("C", "3");  // Safe modification
        }
        System.out.println("✅ No exception (fail-safe iterator)");

        // Difference 3: Atomic operations
        System.out.println("\n3️⃣ Atomic Operations:");
        System.out.println("   HashMap: ❌ No atomic operations");
        System.out.println("   ConcurrentHashMap: ✅ compute(), merge(), putIfAbsent()");

        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        counter.put("count", 0);

        // Atomic increment using merge
        counter.merge("count", 1, Integer::sum);
        System.out.println("   Atomic increment: " + counter.get("count"));
    }

    // DEMO 4: When to use which
    public static void recommendations() {
        System.out.println("\n\n📌 DEMO 4: When to Use Which?");
        System.out.println("════════════════════════════════════════════════════════════");

        System.out.println("\n✅ USE HASHMAP WHEN:");
        System.out.println("   1. Single-threaded application");
        System.out.println("   2. Method-level operations (no shared state)");
        System.out.println("   3. Local variables inside methods");
        System.out.println("   4. Need to store null keys or values");
        System.out.println("   5. Simple caching without concurrency");
        System.out.println("   6. 👉 THIS IS THE GENERAL SCENARIO! (Most common)");

        System.out.println("\n✅ USE CONCURRENTHASHMAP WHEN:");
        System.out.println("   1. Multi-threaded application");
        System.out.println("   2. Shared state across threads");
        System.out.println("   3. Web applications (multiple requests)");
        System.out.println("   4. Caching in concurrent environments");
        System.out.println("   5. Session storage");
        System.out.println("   6. Need atomic operations (compute, merge)");
        System.out.println("   7. High concurrent reads and writes");

        System.out.println("\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
        System.out.println("┃  🎯 FINAL ANSWER                                        ┃");
        System.out.println("┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫");
        System.out.println("┃  Q: Is ConcurrentHashMap better than HashMap?          ┃");
        System.out.println("┃                                                          ┃");
        System.out.println("┃  A: NO - They serve DIFFERENT purposes!                 ┃");
        System.out.println("┃                                                          ┃");
        System.out.println("┃  • HashMap: ✅ Better for single-threaded (GENERAL)     ┃");
        System.out.println("┃  • ConcurrentHashMap: ✅ Better for multi-threaded      ┃");
        System.out.println("┃                                                          ┃");
        System.out.println("┃  In GENERAL scenarios (single-thread), use HashMap!     ┃");
        System.out.println("┃  For concurrent scenarios, use ConcurrentHashMap!       ┃");
        System.out.println("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");

        System.out.println("\n\n📊 COMPARISON TABLE:");
        System.out.println("╔═══════════════════════╦═══════════════╦════════════════════════╗");
        System.out.println("║ Feature               ║ HashMap       ║ ConcurrentHashMap      ║");
        System.out.println("╠═══════════════════════╬═══════════════╬════════════════════════╣");
        System.out.println("║ Thread-Safe           ║ ❌ No         ║ ✅ Yes                 ║");
        System.out.println("║ Null Key             ║ ✅ Yes        ║ ❌ No                  ║");
        System.out.println("║ Null Value           ║ ✅ Yes        ║ ❌ No                  ║");
        System.out.println("║ Performance (Single)  ║ ⚡⚡⚡⚡⚡     ║ ⚡⚡⚡⚡                ║");
        System.out.println("║ Performance (Multi)   ║ ❌ Unsafe     ║ ⚡⚡⚡⚡⚡              ║");
        System.out.println("║ Iterator Type         ║ Fail-fast     ║ Fail-safe              ║");
        System.out.println("║ Atomic Operations     ║ ❌ No         ║ ✅ Yes                 ║");
        System.out.println("║ Use Case              ║ General/Local ║ Concurrent/Shared      ║");
        System.out.println("║ Memory Overhead       ║ Low           ║ Higher                 ║");
        System.out.println("╚═══════════════════════╩═══════════════╩════════════════════════╝");
    }
}

