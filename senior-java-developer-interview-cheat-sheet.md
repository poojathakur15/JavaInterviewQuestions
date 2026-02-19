# Senior Java Developer Interview Cheat Sheet (11+ Years Experience)

> A comprehensive guide covering essential topics for experienced Java developers

## Table of Contents
- [Core Java & Concurrency](#core-java--concurrency)
- [JVM & Memory Management](#jvm--memory-management)
- [REST & Web Services](#rest--web-services)
- [Security](#security)
- [Java 8+ Features](#java-8-features)
- [Design Patterns](#design-patterns)
- [Collections Framework](#collections-framework)
- [Spring Boot & Microservices](#spring-boot--microservices)
- [Distributed Systems](#distributed-systems)
- [Database & Performance](#database--performance)
- [DevOps & Deployment](#devops--deployment)
- [Advanced Java Concepts](#advanced-java-concepts)

---

## Core Java & Concurrency

### Q1: Simulate Concurrent Modification Through Stream API

#### Understanding Core Concepts

**What is ConcurrentModificationException?**

ConcurrentModificationException is a runtime exception thrown when a collection is **structurally modified** while being iterated, except through the iterator's own remove/add methods. This is a fail-fast behavior designed to detect bugs early.

**Key Terms:**

1. **Structural Modification**: Any operation that changes the size or structure of a collection (add, remove, clear)
   - Adding elements: `list.add()`
   - Removing elements: `list.remove()`
   - Clearing: `list.clear()`
   - NOT structural: Modifying existing element values

2. **Fail-Fast Iterator**: An iterator that immediately throws ConcurrentModificationException upon detecting modification
   - Uses `modCount` (modification count) internally
   - Each collection maintains a modification counter
   - Iterator takes a snapshot of modCount
   - On each iteration, compares current modCount with snapshot
   - If different → throws exception

3. **Stream API**: Introduced in Java 8, provides functional-style operations on collections
   - Sequential streams: Single thread processes elements
   - Parallel streams: Multiple threads process elements concurrently
   - Internally uses iterators or spliterators

**Why Does It Happen with Streams?**

Streams use **fail-fast iterators** internally. When you modify the source collection during stream operations:
- The underlying iterator detects the change via modCount
- Throws ConcurrentModificationException to prevent unpredictable behavior
- Protects against corrupted data or infinite loops

**ModCount Mechanism:**
```java
// Internal implementation (simplified)
public class ArrayList<E> {
    private transient int modCount = 0;  // Modification counter
    
    public boolean add(E e) {
        modCount++;  // Increment on structural change
        // ... add logic
    }
    
    private class Itr implements Iterator<E> {
        int expectedModCount = modCount;  // Snapshot
        
        public E next() {
            if (modCount != expectedModCount)  // Check on each iteration
                throw new ConcurrentModificationException();
            // ... return next element
        }
    }
}
```

#### Answering the Question: How to Simulate Concurrent Modification?

Let me demonstrate various scenarios where ConcurrentModificationException occurs with Stream API:

#### Scenario 1: Structural Modification During Stream

```java
// ❌ Throws ConcurrentModificationException
List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

numbers.stream()
    .forEach(num -> {
        if (num % 2 == 0) {
            numbers.remove(num); // Modifying during iteration
        }
    });
```

#### Scenario 2: Parallel Stream with Non-Thread-Safe Collection

```java
// ❌ ConcurrentModificationException with parallel stream
List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

numbers.parallelStream()
    .forEach(num -> {
        numbers.add(num * 10); // Multiple threads modifying
    });
```

#### Scenario 3: Multiple Threads Modifying Collection

```java
// ❌ Concurrent modification
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");

// Thread 1: Iterating
Thread t1 = new Thread(() -> {
    list.stream().forEach(System.out::println);
});

// Thread 2: Modifying
Thread t2 = new Thread(() -> {
    list.add("C");
});

t1.start();
t2.start();
```

#### Solutions to Avoid Concurrent Modification

**Solution 1: Use Thread-Safe Collections**
```java
// ✅ Using CopyOnWriteArrayList
List<Integer> numbers = new CopyOnWriteArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

numbers.parallelStream()
    .forEach(num -> {
        if (num > 3) {
            numbers.add(num * 10); // Safe with CopyOnWriteArrayList
        }
    });

// ✅ Using Collections.synchronizedList
List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
```

**Solution 2: Collect Results and Modify After Stream**
```java
// ✅ Best Practice
List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

List<Integer> toRemove = numbers.stream()
    .filter(num -> num % 2 == 0)
    .collect(Collectors.toList());

numbers.removeAll(toRemove); // Modify after stream completes
```

**Solution 3: Use removeIf (Fail-Safe Iterator)**
```java
// ✅ Built-in safe removal
List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
numbers.removeIf(num -> num % 2 == 0);
```

**Solution 4: Use Concurrent Collections**
```java
// ✅ For parallel processing
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("A", 1);
map.put("B", 2);

map.entrySet().parallelStream()
    .forEach(entry -> {
        map.put(entry.getKey() + "_new", entry.getValue() * 10);
    });
```

**Demonstration Code:**
```java
public class ConcurrentModificationDemo {
    
    // ❌ Causes exception
    public static void demonstrateException() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        try {
            list.stream().forEach(item -> {
                if (item.equals("B")) {
                    list.remove(item); // ConcurrentModificationException
                }
            });
        } catch (ConcurrentModificationException e) {
            System.out.println("Exception caught: " + e.getMessage());
        }
    }
    
    // ✅ Safe approach
    public static void safeDemonstration() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        // Collect items to remove
        List<String> toRemove = list.stream()
            .filter(item -> item.equals("B"))
            .collect(Collectors.toList());
        
        // Remove after stream completes
        list.removeAll(toRemove);
        
        System.out.println("Result: " + list); // [A, C, D]
    }
}
```

---

### Q2: Parallel Stream Threading & Internal Working

#### Understanding Core Concepts

**What is a Stream?**

A Stream in Java is a **sequence of elements** supporting sequential and parallel aggregate operations. It's NOT a data structure but a **pipeline of operations** on data.

**Key Characteristics:**
- **No storage**: Streams don't store elements; they convey elements from a source
- **Functional**: Operations produce results but don't modify the source
- **Lazy**: Computation happens only when needed (terminal operation)
- **Possibly infinite**: Can represent infinite sequences
- **Consumable**: Can be traversed only once

**Sequential vs Parallel Streams:**

```java
// Sequential Stream - Single thread processes all elements
list.stream()
    .filter(x -> x > 10)
    .map(x -> x * 2)
    .collect(Collectors.toList());

// Parallel Stream - Multiple threads process elements concurrently
list.parallelStream()
    .filter(x -> x > 10)
    .map(x -> x * 2)
    .collect(Collectors.toList());
```

**What is ForkJoinPool?**

ForkJoinPool is a specialized ExecutorService designed for **work-stealing algorithms**:
- Introduced in Java 7 as part of Fork/Join Framework
- Optimized for tasks that can be recursively split into smaller subtasks
- Each worker thread has its own **double-ended queue (deque)**
- Idle threads "steal" work from busy threads
- Used by parallel streams under the hood

**Fork/Join Framework Concept:**
```
FORK: Split large task into smaller subtasks recursively
      until task is small enough to solve directly
      
JOIN: Combine results of subtasks to produce final result
```

**What is a Spliterator?**

Spliterator = **Split** + **Iterator**
- An iterator that can partition elements for parallel processing
- Has `trySplit()` method to divide elements into chunks
- Each chunk can be processed by different threads
- More advanced than Iterator (supports parallel traversal)

```java
// Spliterator methods
trySplit()        // Split into two parts
tryAdvance()      // Process one element
forEachRemaining()// Process all remaining elements
characteristics() // Describe element properties (ORDERED, SIZED, etc.)
```

**Work-Stealing Algorithm:**

A load-balancing technique where:
1. Each thread maintains a work queue (deque)
2. Thread pushes new tasks to its own queue (LIFO - Last In First Out)
3. Thread processes tasks from its own queue
4. When queue is empty, thread "steals" from another thread's queue (FIFO - First In First Out)
5. Minimizes contention and maximizes CPU utilization

```
Thread-1 Queue: [T1, T2, T3, T4]  ← Push (LIFO)
                                   ↑ Pop (LIFO)
                                   
Thread-2 Queue: []  (idle)
                 ↓
    Steals from Thread-1 (FIFO) → Takes T1
```

#### Answering the Question: Thread Count & Internal Working

**How Many Threads Does Parallel Stream Use?**

**Default Thread Pool Size:**
```java
int threads = Runtime.getRuntime().availableProcessors();
// Typically: CPU cores - 1 for worker threads
// Example: 8-core machine = 7 worker threads + 1 main thread
```

**Check Parallel Stream Thread Count:**
```java
List<Integer> numbers = IntStream.range(0, 100)
    .boxed()
    .collect(Collectors.toList());

Set<String> threadNames = Collections.synchronizedSet(new HashSet<>());

numbers.parallelStream()
    .forEach(num -> {
        threadNames.add(Thread.currentThread().getName());
    });

System.out.println("Threads used: " + threadNames.size());
System.out.println("Thread names: " + threadNames);
// Output: ForkJoinPool.commonPool-worker-1, worker-2, ..., main
```

#### Internal Working of Parallel Streams

**1. Fork/Join Framework**
- Uses `ForkJoinPool.commonPool()`
- Work-stealing algorithm
- Divide-and-conquer approach

```java
// Parallel stream uses common ForkJoin pool
ForkJoinPool commonPool = ForkJoinPool.commonPool();
System.out.println("Parallelism: " + commonPool.getParallelism());
// Typically: Number of CPU cores - 1
```

**2. Spliterator (Split + Iterator)**
- Divides source into chunks
- Each chunk processed by different thread

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

// Sequential
numbers.stream()
    .forEach(num -> System.out.println(Thread.currentThread().getName()));
// All on main thread

// Parallel - splits into chunks
numbers.parallelStream()
    .forEach(num -> System.out.println(Thread.currentThread().getName()));
// Multiple threads: main, ForkJoinPool.commonPool-worker-X
```

**3. Work-Stealing Algorithm**
```
Initial Split:
[1,2,3,4,5,6,7,8]
      ↓
  [1,2,3,4]  [5,6,7,8]
      ↓           ↓
  [1,2] [3,4]  [5,6] [7,8]

Thread-1 → [1,2]
Thread-2 → [3,4]
Thread-3 → [5,6]
Thread-4 → [7,8]

If Thread-1 finishes early, it "steals" work from Thread-4's queue
```

**4. Custom ForkJoinPool**
```java
// Override default parallelism
ForkJoinPool customPool = new ForkJoinPool(4); // 4 threads

try {
    customPool.submit(() -> {
        list.parallelStream()
            .forEach(item -> {
                // Process with custom pool (4 threads)
                System.out.println(Thread.currentThread().getName());
            });
    }).get();
} catch (Exception e) {
    e.printStackTrace();
} finally {
    customPool.shutdown();
}
```

**5. Configure Common Pool Size**
```java
// JVM argument
-Djava.util.concurrent.ForkJoinPool.common.parallelism=8

// Or programmatically (must be set before first use)
System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8");
```

#### Internal Architecture

```
                ParallelStream
                      ↓
            ForkJoinPool.commonPool()
                      ↓
        ┌─────────────┴─────────────┐
        ↓                           ↓
   Spliterator                Work Stealing
   (Split Data)               (Load Balance)
        ↓                           ↓
    ┌───┴───┬───────┬───────┐      ↓
    ↓       ↓       ↓       ↓      ↓
 Thread1 Thread2 Thread3 Thread4   ↓
    │       │       │       │       ↓
    └───────┴───────┴───────┴───────→ Combine Results
```

#### Performance Considerations

```java
public class ParallelStreamPerformance {
    
    // ✅ Good for parallel: CPU-intensive, large dataset
    public static void goodCandidate() {
        List<Integer> numbers = IntStream.range(1, 1_000_000)
            .boxed()
            .collect(Collectors.toList());
        
        long sum = numbers.parallelStream()
            .mapToInt(i -> i * i) // CPU-intensive
            .sum();
    }
    
    // ❌ Bad for parallel: Small dataset, I/O bound
    public static void badCandidate() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        
        // Overhead > benefit for small lists
        numbers.parallelStream().forEach(System.out::println);
    }
    
    // ❌ Bad: Synchronized operation negates parallelism
    public static void synchronizedOperation() {
        List<Integer> numbers = IntStream.range(0, 1000)
            .boxed()
            .collect(Collectors.toList());
        
        List<Integer> result = new ArrayList<>();
        
        numbers.parallelStream()
            .forEach(num -> {
                synchronized(result) { // Bottleneck!
                    result.add(num);
                }
            });
    }
}
```

**When to Use Parallel Streams:**
- ✅ Large datasets (thousands+ elements)
- ✅ CPU-intensive operations
- ✅ Stateless operations
- ✅ Thread-safe operations
- ❌ Small datasets (< 1000 elements)
- ❌ I/O bound operations
- ❌ Operations requiring order
- ❌ Stateful operations

---

### Q3: Thread Pool Executor Internal Working

#### Understanding Core Concepts

**What is a Thread Pool?**

A Thread Pool is a **collection of pre-initialized, reusable worker threads** that execute submitted tasks. Instead of creating a new thread for each task (expensive operation), threads are reused from the pool.

**Why Use Thread Pools?**

1. **Performance**: Thread creation/destruction is expensive (system calls, memory allocation)
   - Creating thread: ~1-2ms overhead
   - Thread pool: Threads already exist, just assign work

2. **Resource Management**: Prevents unbounded thread creation
   - Without pool: 10,000 requests = 10,000 threads = System crash
   - With pool: 10,000 requests = Fixed threads (e.g., 100) = Stable system

3. **Better Control**: Configure thread lifecycle, rejection policies, queue strategies

**Executor Framework Hierarchy:**
```
        Executor (interface)
             ↓
    ExecutorService (interface)
             ↓
   AbstractExecutorService (abstract class)
             ↓
    ThreadPoolExecutor (concrete class)
             ↓
   Executors (factory methods)
```

**Key Components of ThreadPoolExecutor:**

1. **Core Pool Size**: Minimum number of threads kept alive even if idle
   - Always maintained (unless allowCoreThreadTimeOut = true)
   - Example: corePoolSize = 5 means 5 threads always ready

2. **Maximum Pool Size**: Maximum number of threads allowed
   - New threads created only when queue is full
   - Example: maxPoolSize = 10 means at most 10 concurrent threads

3. **Keep-Alive Time**: How long excess threads wait for work before dying
   - Applies to threads beyond corePoolSize
   - Example: keepAliveTime = 60s means idle thread dies after 60s

4. **Work Queue**: Holds tasks waiting for thread availability
   - **SynchronousQueue**: No capacity, direct handoff (Executors.newCachedThreadPool())
   - **LinkedBlockingQueue**: Unbounded queue (Executors.newFixedThreadPool())
   - **ArrayBlockingQueue**: Bounded queue with fixed capacity
   - **PriorityBlockingQueue**: Priority-based ordering

5. **Thread Factory**: Creates new threads
   - Can set thread names, daemon status, priority
   - Default: Executors.defaultThreadFactory()

6. **Rejection Policy**: What to do when queue is full and max threads reached

**Thread Lifecycle in Pool:**
```
       NEW
        ↓
    RUNNABLE ←──────┐
        ↓           │
     RUNNING        │
        ↓           │
    BLOCKED/       │
    WAITING     (Get task
        ↓        from queue)
    RUNNABLE ───────┘
        ↓
   (No more tasks
    & timeout)
        ↓
   TERMINATED
```

**AtomicInteger `ctl` (Control State):**

ThreadPoolExecutor uses a single AtomicInteger to encode TWO values:
```
32-bit Integer (ctl)
├─ Upper 3 bits: runState (pool lifecycle state)
└─ Lower 29 bits: workerCount (number of active threads)

Example:
 ctl = 11100000 00000000 00000000 00000101
       ├──┘                          └──┘
       │                              │
   runState = RUNNING (-1)      workerCount = 5

Why? Atomic updates of both values in single CAS operation
```

**Worker Thread Concept:**

Each thread in the pool is wrapped in a `Worker` object:
- Worker extends AbstractQueuedSynchronizer (AQS) for lock management
- Contains the actual Thread object
- Tracks completed task count
- Has its own lock for interrupt control

```java
private final class Worker extends AQS implements Runnable {
    final Thread thread;        // Actual thread
    Runnable firstTask;        // Initial task
    volatile long completedTasks; // Tasks completed by this worker
}
```

**Blocking Queue Role:**

Producer-Consumer pattern:
- **Producer**: Main thread submits tasks → `queue.offer(task)`
- **Consumer**: Worker threads take tasks → `queue.take()` or `queue.poll(timeout)`
- `take()`: Blocks until task available (keeps thread alive)
- `poll(timeout)`: Waits for specified time, returns null if no task (thread may die)

#### Answering the Question: How ThreadPoolExecutor Checks Thread State

**Internal Mechanism for Tracking Active/Dead Threads:**

#### Architecture Overview

```java
public class ThreadPoolExecutor extends AbstractExecutorService {
    // Core components
    private final BlockingQueue<Runnable> workQueue;
    private final HashSet<Worker> workers;
    private volatile int corePoolSize;
    private volatile int maximumPoolSize;
    private volatile long keepAliveTime;
    private volatile ThreadFactory threadFactory;
    private volatile RejectedExecutionHandler handler;
}
```

#### How Thread Pool Checks Active/Dead Threads

**1. Worker Thread Management**
```java
// Internal Worker class (simplified)
private final class Worker implements Runnable {
    final Thread thread;
    Runnable firstTask;
    
    Worker(Runnable firstTask) {
        this.thread = getThreadFactory().newThread(this);
        this.firstTask = firstTask;
    }
    
    public void run() {
        runWorker(this);
    }
}

// Worker tracking
private final HashSet<Worker> workers = new HashSet<>();
```

**2. Thread State Tracking**
```java
// ThreadPoolExecutor maintains thread count atomically
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

// ctl encodes two values:
// - workerCount: Number of active threads (lower 29 bits)
// - runState: Lifecycle state (upper 3 bits)

private static int workerCountOf(int c) { 
    return c & CAPACITY; // Extract worker count
}

private static int runStateOf(int c) {
    return c & ~CAPACITY; // Extract run state
}
```

**3. Thread Lifecycle States**
```java
// Pool states
private static final int RUNNING    = -1 << COUNT_BITS; // Accept new tasks
private static final int SHUTDOWN   =  0 << COUNT_BITS; // No new tasks
private static final int STOP       =  1 << COUNT_BITS; // Interrupt threads
private static final int TIDYING    =  2 << COUNT_BITS; // All tasks done
private static final int TERMINATED =  3 << COUNT_BITS; // terminated() complete
```

#### Internal Working Flow

**Step 1: Task Submission**
```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    
    int c = ctl.get();
    
    // Step 1: If threads < corePoolSize, create new thread
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    
    // Step 2: Try to queue task
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        if (!isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    // Step 3: Try to create thread up to maximumPoolSize
    else if (!addWorker(command, false))
        reject(command); // Reject if can't create thread
}
```

**Step 2: Adding Worker Thread**
```java
private boolean addWorker(Runnable firstTask, boolean core) {
    // Atomically increment worker count
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        
        // Check if pool is shutting down
        if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty()))
            return false;
        
        for (;;) {
            int wc = workerCountOf(c);
            if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            
            // Atomically increment worker count
            if (compareAndIncrementWorkerCount(c))
                break retry;
            
            c = ctl.get();
            if (runStateOf(c) != rs)
                continue retry;
        }
    }
    
    // Create and start worker
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    
    try {
        w = new Worker(firstTask);
        final Thread t = w.thread;
        
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                workers.add(w); // Add to worker set
                workerAdded = true;
            } finally {
                mainLock.unlock();
            }
            
            if (workerAdded) {
                t.start(); // Start the thread
                workerStarted = true;
            }
        }
    } finally {
        if (!workerStarted)
            addWorkerFailed(w);
    }
    
    return workerStarted;
}
```

**Step 3: Worker Run Loop**
```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    
    try {
        // Keep getting tasks from queue
        while (task != null || (task = getTask()) != null) {
            w.lock();
            
            try {
                beforeExecute(wt, task); // Hook
                try {
                    task.run(); // Execute task
                    afterExecute(task, null); // Hook
                } catch (RuntimeException x) {
                    afterExecute(task, x);
                    throw x;
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
    } finally {
        processWorkerExit(w, completedAbruptly); // Thread dies here
    }
}
```

**Step 4: Getting Tasks from Queue**
```java
private Runnable getTask() {
    boolean timedOut = false;
    
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        
        // Check if pool is shutting down
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null; // Thread will die
        }
        
        int wc = workerCountOf(c);
        
        // Determine if thread should timeout
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        
        if (wc <= maximumPoolSize && !(timedOut && timed))
            break;
        
        if (compareAndDecrementWorkerCount(c))
            return null; // Thread will die
        
        try {
            // Block waiting for task (with/without timeout)
            Runnable r = timed ?
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                workQueue.take();
            
            if (r != null)
                return r;
            
            timedOut = true; // No task within keepAliveTime
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```

**Step 5: Thread Death (Cleanup)**
```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    if (completedAbruptly) // If abrupt, adjust counts
        decrementWorkerCount();
    
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        completedTaskCount += w.completedTasks;
        workers.remove(w); // Remove dead worker
    } finally {
        mainLock.unlock();
    }
    
    tryTerminate(); // Try to terminate pool
    
    // Replace dead thread if needed
    int c = ctl.get();
    if (runStateLessThan(c, STOP)) {
        if (!completedAbruptly) {
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            if (min == 0 && !workQueue.isEmpty())
                min = 1;
            if (workerCountOf(c) >= min)
                return; // Replacement not needed
        }
        addWorker(null, false); // Add replacement
    }
}
```

#### Thread Pool Configuration Example

```java
public class ThreadPoolExample {
    
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                      // corePoolSize
            4,                      // maximumPoolSize
            60L,                    // keepAliveTime
            TimeUnit.SECONDS,       // time unit
            new LinkedBlockingQueue<>(10),  // work queue
            Executors.defaultThreadFactory(), // thread factory
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
        
        // Monitor thread pool
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("Task " + taskId + 
                    " executed by " + Thread.currentThread().getName());
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Monitor pool state
            System.out.println("Active threads: " + executor.getActiveCount());
            System.out.println("Pool size: " + executor.getPoolSize());
            System.out.println("Queue size: " + executor.getQueue().size());
            System.out.println("Completed tasks: " + executor.getCompletedTaskCount());
            System.out.println("---");
        }
        
        executor.shutdown();
    }
}
```

#### Rejection Policies

```java
// 1. AbortPolicy (default) - Throws RejectedExecutionException
new ThreadPoolExecutor.AbortPolicy()

// 2. CallerRunsPolicy - Caller thread executes task
new ThreadPoolExecutor.CallerRunsPolicy()

// 3. DiscardPolicy - Silently discard task
new ThreadPoolExecutor.DiscardPolicy()

// 4. DiscardOldestPolicy - Discard oldest unhandled request
new ThreadPoolExecutor.DiscardOldestPolicy()

// 5. Custom policy
new RejectedExecutionHandler() {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // Log rejection, queue to database, etc.
        System.out.println("Task rejected: " + r.toString());
    }
}
```

#### Thread Pool Lifecycle

```
           execute(task)
                ↓
    workerCount < corePoolSize?
         ↓ Yes          ↓ No
    Create Thread   Queue Full?
         ↓           ↓ Yes    ↓ No
    Execute      maxPool?    Enqueue
                ↓ No  ↓ Yes      ↓
           Create   Reject    Wait
           Thread    Task     in Queue
                                ↓
                         Thread Available
                                ↓
                    Execute & Remove from Queue
                                ↓
                    Idle > keepAliveTime?
                         ↓ Yes    ↓ No
                    Thread Dies   Continue
```

**Key Monitoring Methods:**
```java
executor.getActiveCount()      // Currently executing tasks
executor.getPoolSize()         // Current number of threads
executor.getCorePoolSize()     // Core pool size
executor.getMaximumPoolSize()  // Maximum pool size
executor.getTaskCount()        // Total tasks submitted
executor.getCompletedTaskCount() // Completed tasks
executor.getQueue().size()     // Tasks in queue
executor.isShutdown()          // Shutdown initiated?
executor.isTerminated()        // All tasks completed?
```

---

### Q4: Java Memory Model (JMM)

#### Understanding Core Concepts

**What is the Java Memory Model (JMM)?**

The Java Memory Model is a **specification** that defines:
1. How threads interact through memory
2. What behaviors are allowed in concurrent execution
3. Which operations are atomic
4. When changes made by one thread become visible to other threads
5. Rules for reordering operations by compiler and CPU

**Why Do We Need JMM?**

**Problem**: Modern systems have complex memory hierarchies:
```
CPU1          CPU2          CPU3
 ├─ L1 Cache  ├─ L1 Cache  ├─ L1 Cache   (Fastest, Private)
 ├─ L2 Cache  ├─ L2 Cache  ├─ L2 Cache   (Fast, Private)
 └─────────────┴────────────┴───────────
              ↓
         L3 Cache (Shared Cache)         (Medium speed)
              ↓
         Main Memory (RAM)               (Slower)
```

**Challenges**:
1. **Cache Coherency**: CPU1 modifies variable in its cache, CPU2 still sees old value
2. **Instruction Reordering**: Compiler/CPU reorders instructions for optimization
3. **Write Buffers**: CPU writes may be buffered before reaching main memory
4. **Read Ahead**: CPU may read values into cache before needed

**JMM Solution**: Provides guarantees about when writes become visible and ordering constraints

**Key JMM Concepts:**

**1. Atomicity**

An operation is **atomic** if it completes entirely or not at all - no intermediate state visible.

```java
// ✅ Atomic operations (single machine instruction)
int x = 5;           // Simple read/write of primitive ≤ 32 bits
boolean flag = true;
reference = object;  // Reference assignment

// ❌ NOT atomic (multiple operations)
x++;                 // Read, increment, write (3 operations)
long l = 5L;        // 64-bit may require 2 operations on 32-bit systems
y = x + 1;          // Read x, add 1, write to y
```

**2. Visibility**

A write by one thread is **visible** to a read by another thread.

**Problem Example**:
```java
// Thread 1               // Thread 2
sharedVar = 42;          while (sharedVar != 42) {
                             // May loop forever!
                         }
```

**Why?** Thread 2 might read from CPU cache, not seeing Thread 1's write to main memory.

**3. Ordering (Reordering)**

Compiler and CPU can reorder instructions for optimization if no dependencies exist.

```java
int a = 1;  // Statement 1
int b = 2;  // Statement 2
int c = a;  // Statement 3

// Compiler might reorder to:
// Statement 2, Statement 1, Statement 3
// (Because statements 1 & 2 don't depend on each other)
```

**4. Happens-Before Relationship**

A **happens-before** edge between two actions guarantees:
- If action A happens-before action B
- Then results of A are visible to B
- And A is ordered before B

**Transitivity**: If A happens-before B, and B happens-before C, then A happens-before C

**5. Memory Barriers (Fences)**

Instructions that prevent reordering and ensure visibility:

- **LoadLoad Barrier**: Ensures loads before barrier complete before loads after
- **StoreStore Barrier**: Ensures stores before barrier complete before stores after
- **LoadStore Barrier**: Ensures loads before barrier complete before stores after
- **StoreLoad Barrier**: Ensures stores before barrier complete before loads after (most expensive)

**6. Cache Coherence**

Mechanism ensuring all CPUs see consistent memory state:
- **MESI Protocol**: Modified, Exclusive, Shared, Invalid states for cache lines
- When one CPU writes, other CPUs' cached copies are invalidated
- Next read by other CPU fetches latest value from main memory

**7. volatile Keyword**

Marks variable for special treatment:
```java
private volatile int counter;
```

**Effects**:
- **Visibility**: Every read sees most recent write
- **Ordering**: Establishes happens-before relationship
- **No Reordering**: Operations before volatile write can't be reordered after it
- **Cache Invalidation**: Writes immediately flushed to main memory
- **NOT Atomic**: `counter++` still requires synchronization

**Volatile vs Synchronized:**
```java
// volatile: Lightweight, visibility only
private volatile boolean flag;

// synchronized: Heavyweight, atomicity + visibility + mutual exclusion
private int counter;
public synchronized void increment() {
    counter++;  // Protected from concurrent access
}
```

**8. synchronized Keyword**

Provides:
- **Mutual Exclusion**: Only one thread in synchronized block
- **Visibility**: Changes made before unlock visible to subsequent lock acquirers
- **Atomicity**: Operations in block appear atomic
- **Memory Barrier**: Full memory barrier on entry and exit

**Monitor Lock**:
- Every Java object has an intrinsic lock (monitor)
- `synchronized(object)` acquires object's monitor
- Only one thread can hold monitor at a time

**9. final Keyword**

For fields:
```java
public class ImmutablePoint {
    private final int x;  // Guaranteed visible after constructor
    private final int y;
    
    public ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
        // After constructor, all threads see initialized values
    }
}
```

**Final Field Guarantee**: Once constructor completes, all threads see initialized final field values (no need for synchronization to read).

**10. ThreadLocal**

Provides thread-local variables:
```java
private static ThreadLocal<Integer> threadId = ThreadLocal.withInitial(() -> 0);

// Each thread has its own copy
threadId.set(123);  // Only visible to current thread
int id = threadId.get();
```

#### Memory Architecture Deep Dive

**Overview**

The **Java Memory Model (JMM)** defines how threads interact through memory and what behaviors are allowed in concurrent execution.

#### Memory Architecture

```
┌────────────────────────────────────────────────────────┐
│                    Physical Memory                      │
└────────────────────────────────────────────────────────┘
                         ↑
                         │
┌────────────────────────┴───────────────────────────────┐
│                   Main Memory (Heap)                    │
│  - Shared between all threads                           │
│  - Objects, Instance variables, Static variables        │
└────────────────────────────────────────────────────────┘
            ↑                    ↑                    ↑
            │                    │                    │
    ┌───────┴──────┐    ┌───────┴──────┐    ┌───────┴──────┐
    │   Thread 1   │    │   Thread 2   │    │   Thread N   │
    │              │    │              │    │              │
    │ Working      │    │ Working      │    │ Working      │
    │ Memory       │    │ Memory       │    │ Memory       │
    │ (CPU Cache)  │    │ (CPU Cache)  │    │ (CPU Cache)  │
    │              │    │              │    │              │
    │ - Registers  │    │ - Registers  │    │ - Registers  │
    │ - L1 Cache   │    │ - L1 Cache   │    │ - L1 Cache   │
    │ - L2 Cache   │    │ - L2 Cache   │    │ - L2 Cache   │
    │ - Local vars │    │ - Local vars │    │ - Local vars │
    └──────────────┘    └──────────────┘    └──────────────┘
```

#### JVM Memory Structure

```
┌─────────────────────────────────────────────────────────┐
│                    JVM Memory                            │
├─────────────────────────────────────────────────────────┤
│  1. HEAP MEMORY (Shared)                                │
│     ├─ Young Generation                                 │
│     │   ├─ Eden Space                                   │
│     │   └─ Survivor Spaces (S0, S1)                     │
│     └─ Old Generation (Tenured)                         │
│                                                          │
│  2. METASPACE (Shared) - Java 8+                        │
│     └─ Class metadata, Method metadata                  │
│                                                          │
│  3. STACK MEMORY (Per Thread)                           │
│     ├─ Local variables                                  │
│     ├─ Method call frames                               │
│     └─ Partial results                                  │
│                                                          │
│  4. PROGRAM COUNTER (PC) REGISTER (Per Thread)          │
│     └─ Current instruction address                      │
│                                                          │
│  5. NATIVE METHOD STACK (Per Thread)                    │
│     └─ Native method information                        │
│                                                          │
│  6. CODE CACHE                                          │
│     └─ JIT compiled code                                │
└─────────────────────────────────────────────────────────┘
```

#### Detailed Breakdown

**1. Heap Memory**
```java
// Objects stored in heap
public class MemoryExample {
    private String name;        // Instance variable → Heap
    private static int count;   // Static variable → Heap (special area)
    
    public void method() {
        String local = "hello"; // Reference → Stack, Object → Heap
        int num = 10;          // Primitive → Stack
    }
}
```

**Configuration:**
```bash
-Xms2g          # Initial heap size
-Xmx4g          # Maximum heap size
-XX:NewRatio=2  # Old:Young ratio
-XX:SurvivorRatio=8  # Eden:Survivor ratio
```

**2. Stack Memory**
```java
public class StackExample {
    public void method1() {
        int a = 10;              // Stored in method1's frame
        method2(a);
    }
    
    public void method2(int b) { // Stored in method2's frame
        int c = b + 10;
    }
}

// Stack Frame Structure:
// ┌─────────────────┐
// │   method2()     │ ← Top
// │   - b, c        │
// ├─────────────────┤
// │   method1()     │
// │   - a           │
// ├─────────────────┤
// │   main()        │
// └─────────────────┘
```

**Configuration:**
```bash
-Xss1m  # Stack size per thread (default 1MB)
```

**3. MetaSpace (Java 8+)**
```java
// Class metadata stored here
public class MyClass {
    private int field;
    
    public void method() {
        // Method bytecode stored in MetaSpace
    }
}
```

**Configuration:**
```bash
-XX:MetaspaceSize=128m
-XX:MaxMetaspaceSize=512m
```

#### Visibility and Happens-Before Relationship

**Problem: Visibility**
```java
public class VisibilityProblem {
    private boolean flag = false;  // Not volatile
    
    // Thread 1
    public void writer() {
        flag = true;  // May not be visible to Thread 2
    }
    
    // Thread 2
    public void reader() {
        while (!flag) {  // May loop forever!
            // Thread 2 reads from its cache
        }
    }
}
```

**Solution: volatile**
```java
public class VisibilitySolution {
    private volatile boolean flag = false;  // Ensures visibility
    
    // Thread 1
    public void writer() {
        flag = true;  // Immediately flushed to main memory
    }
    
    // Thread 2
    public void reader() {
        while (!flag) {  // Reads from main memory
            // Will see the update
        }
    }
}
```

#### Happens-Before Rules

**1. Program Order Rule**
```java
int a = 1;  // Happens-before
int b = 2;  // This
```

**2. Monitor Lock Rule**
```java
synchronized (lock) {
    // All operations here happen-before
    // operations after unlock
}
```

**3. Volatile Variable Rule**
```java
volatile int v = 0;

// Thread 1
v = 1;  // Write to volatile happens-before

// Thread 2
int x = v;  // Read from volatile
```

**4. Thread Start Rule**
```java
Thread t = new Thread(() -> {
    // Operations here happen-after t.start()
});
t.start();  // Happens-before thread execution
```

**5. Thread Join Rule**
```java
Thread t = new Thread(() -> {
    // Operations here happen-before t.join() returns
});
t.start();
t.join();  // All thread operations visible after join
```

#### Memory Barriers / Fences

```java
public class MemoryBarriers {
    private int a = 0;
    private volatile int b = 0;
    
    // Thread 1
    public void writer() {
        a = 1;           // Regular write
        b = 2;           // Volatile write (STORE barrier)
        // All writes before volatile write are flushed to memory
    }
    
    // Thread 2
    public void reader() {
        int x = b;       // Volatile read (LOAD barrier)
        // All reads after volatile read see latest values
        int y = a;       // Guaranteed to see a = 1
    }
}
```

#### Atomicity, Visibility, Ordering

```java
public class JMMConcepts {
    
    // 1. ATOMICITY - Operation completes entirely or not at all
    private int counter = 0;
    
    // ❌ Not atomic (read-modify-write)
    public void increment() {
        counter++;  // Three operations: read, add, write
    }
    
    // ✅ Atomic
    private AtomicInteger atomicCounter = new AtomicInteger(0);
    public void atomicIncrement() {
        atomicCounter.incrementAndGet();  // Single atomic operation
    }
    
    // 2. VISIBILITY - Changes visible to other threads
    private volatile boolean flag = false;  // Ensures visibility
    
    // 3. ORDERING - Order of operations
    private int x = 0, y = 0;
    private int r1 = 0, r2 = 0;
    
    // Thread 1
    public void thread1() {
        x = 1;  // May be reordered
        r1 = y;
    }
    
    // Thread 2
    public void thread2() {
        y = 1;  // May be reordered
        r2 = x;
    }
    // Possible result: r1 = 0, r2 = 0 (due to reordering)
}
```

#### Synchronization Mechanisms

**1. synchronized**
```java
public class SynchronizedExample {
    private int count = 0;
    
    // Method level
    public synchronized void increment() {
        count++;  // Atomicity + Visibility + Ordering
    }
    
    // Block level
    public void decrement() {
        synchronized(this) {
            count--;
        }
    }
}
```

**2. volatile**
```java
public class VolatileExample {
    private volatile int sharedVar;  // Visibility + Ordering (No atomicity)
    
    public void write(int value) {
        sharedVar = value;  // Visible immediately
    }
    
    public int read() {
        return sharedVar;  // Reads latest value
    }
}
```

**3. final**
```java
public class FinalExample {
    private final int immutable;
    
    public FinalExample(int value) {
        immutable = value;  // Guarantee: visible to all threads after construction
    }
}
```

**4. Locks**
```java
public class LockExample {
    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0;
    
    public void increment() {
        lock.lock();
        try {
            count++;  // Protected by lock
        } finally {
            lock.unlock();
        }
    }
}
```

#### Safe Publication

```java
// ❌ Unsafe publication
public class UnsafePublication {
    public static User user;
    
    public static void initialize() {
        user = new User("John", 30);  // Not thread-safe
    }
}

// ✅ Safe publication with volatile
public class SafePublication1 {
    public static volatile User user;
    
    public static void initialize() {
        user = new User("John", 30);  // Thread-safe
    }
}

// ✅ Safe publication with synchronization
public class SafePublication2 {
    private static User user;
    
    public static synchronized User getUser() {
        if (user == null) {
            user = new User("John", 30);
        }
        return user;
    }
}

// ✅ Safe publication with final
public class SafePublication3 {
    private static final User user = new User("John", 30);  // Thread-safe
}
```

#### Double-Checked Locking (Correct Implementation)

```java
public class Singleton {
    private static volatile Singleton instance;  // Must be volatile!
    
    public static Singleton getInstance() {
        if (instance == null) {              // First check (no locking)
            synchronized (Singleton.class) {
                if (instance == null) {      // Second check (with locking)
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

#### Memory Model Guarantees Summary

| Feature | synchronized | volatile | final | AtomicXXX |
|---------|-------------|----------|-------|-----------|
| **Atomicity** | ✅ | ❌ | ❌ | ✅ |
| **Visibility** | ✅ | ✅ | ✅ | ✅ |
| **Ordering** | ✅ | ✅ | ✅ | ✅ |
| **Mutual Exclusion** | ✅ | ❌ | ❌ | ❌ |
| **Performance** | Slow | Fast | Fast | Medium |

#### Best Practices

```java
// 1. Use immutable objects when possible
public final class ImmutableUser {
    private final String name;
    private final int age;
    
    public ImmutableUser(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// 2. Use concurrent collections
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// 3. Prefer atomic variables for counters
AtomicInteger counter = new AtomicInteger(0);

// 4. Use volatile for flags
private volatile boolean shutdown = false;

// 5. Minimize synchronization scope
public void method() {
    // Non-synchronized code
    
    synchronized(this) {
        // Only critical section
    }
    
    // More non-synchronized code
}
```

---

## JVM & Memory Management

### Q5: PermGen vs MetaSpace (JDK 8)

#### Understanding Core Concepts

**What is JVM Memory Structure?**

The Java Virtual Machine (JVM) divides memory into several regions, each serving a specific purpose:

1. **Heap Memory**: Where objects are allocated
2. **Stack Memory**: Where method calls and local variables are stored
3. **Method Area**: Where class metadata is stored
4. **PC Registers**: Program counter for each thread
5. **Native Method Stack**: For native method calls

**What is the Method Area?**

The Method Area is a **logical concept** (part of JVM specification) that stores:
- Class structures (metadata)
- Method data and method code
- Runtime constant pool
- Field data
- Static variables
- Code for methods and constructors

**Implementation Evolution**:
- **Before Java 8**: Method Area implemented as **PermGen** (Permanent Generation)
- **Java 8 onwards**: Method Area implemented as **MetaSpace**

**What is Class Metadata?**

Metadata about classes loaded by JVM:
```java
public class Employee {
    private String name;        // Field metadata
    private int age;
    
    public void work() {        // Method metadata
        // Method bytecode
    }
}

// Metadata stored:
// - Class name: Employee
// - Superclass: Object
// - Fields: name (String), age (int)
// - Methods: work() (bytecode)
// - Access modifiers, annotations, etc.
```

**What is String Pool (String Interning)?**

A special memory area where String literals are stored:
```java
String s1 = "hello";  // Stored in String Pool
String s2 = "hello";  // Reuses same reference from pool
String s3 = new String("hello");  // Creates new object in heap

System.out.println(s1 == s2);  // true (same reference)
System.out.println(s1 == s3);  // false (different references)
```

**Why String Pool?**
- Memory efficiency (reuse common strings)
- Performance (comparison via reference equality)
- Strings are immutable, safe to share

**Understanding Garbage Collection Context**

Garbage Collection (GC) in Java:
- **Young Generation**: Short-lived objects (Eden, Survivor spaces)
- **Old Generation**: Long-lived objects
- **GC Types**: 
  - **Minor GC**: Cleans Young Generation
  - **Major GC**: Cleans Old Generation
  - **Full GC**: Cleans entire heap + Method Area

**Problem with PermGen**: Fixed size, part of heap, frequent Full GCs

#### Answering the Question: PermGen vs MetaSpace

#### PermGen (Permanent Generation) - Before Java 8
- **Location**: Heap memory
- **Stores**: Class metadata, static variables, string pool
- **Size**: Fixed (default 64MB)
- **Config**: `-XX:PermSize`, `-XX:MaxPermSize`
- **Problem**: `OutOfMemoryError: PermGen space`

#### MetaSpace (Java 8+)
- **Location**: Native memory (not heap)
- **Stores**: Class metadata only
- **Size**: Dynamic, auto-grows
- **Config**: `-XX:MetaspaceSize`, `-XX:MaxMetaspaceSize`
- **Benefits**: Eliminates PermGen OOM errors, better for dynamic class loading

**Key Changes:**
- String pool moved to heap memory
- Better memory management for applications using reflection, proxies, dynamic class generation

---

## REST & Web Services

### Q6: REST Service vs RESTful Web Service

#### Understanding Core Concepts

**What is an Architectural Style?**

An architectural style is a set of **principles, constraints, and patterns** that guide how software systems are designed. It's NOT a protocol or implementation.

**What is REST?**

REST (Representational State Transfer) - An **architectural style** by Roy Fielding (2000) defining constraints for web services.

**Key Philosophy**: Resources (nouns) + Standard HTTP methods = Scalable APIs

**What is a Resource?**
```
Examples:
- User: /users/123
- Order: /orders/456  
- User's Orders: /users/123/orders
```

**REST Constraints (6 Principles)**:
1. **Client-Server**: Separation of concerns
2. **Stateless**: No client context on server
3. **Cacheable**: Responses define cacheability
4. **Uniform Interface**: Standard HTTP, URIs, HATEOAS
5. **Layered System**: Intermediaries allowed
6. **Code on Demand** (optional): Server sends executable code

**What is HATEOAS?**
```json
// Hypermedia As The Engine Of Application State
{
  "id": 123,
  "name": "John",
  "_links": {
    "self": {"/users/123"},
    "orders": {"/users/123/orders"}
  }
}
```

#### Answering the Question

**Terms are largely interchangeable**, but:

| REST Service | RESTful Web Service |
|--------------|---------------------|
| May partially follow REST principles | Strictly adheres to ALL REST constraints |
| Informal implementation | Formal compliance with REST architecture |

**RESTful Principles (Must Follow All):**
1. ✅ **Stateless** - Each request is independent
2. ✅ **Client-Server** - Separation of concerns
3. ✅ **Cacheable** - Responses marked as cacheable/non-cacheable
4. ✅ **Uniform Interface** - Standard HTTP methods, URIs
5. ✅ **Layered System** - Can have intermediary layers
6. ✅ **Code on Demand** (optional) - Server can send executable code

---

### Q7: HTTP Methods

#### Understanding Core Concepts

**What is HTTP?**

HTTP (HyperText Transfer Protocol) - Application-layer protocol for data communication on the web.

**Request-Response Model**:
```
Client --[HTTP Request]-->  Server
Client <--[HTTP Response]-- Server
```

**What are HTTP Methods (Verbs)?**

HTTP methods indicate the **desired action** on a resource.

**CRUD Mapping**:
```
Create  →  POST    →  INSERT
Read    →  GET     →  SELECT
Update  →  PUT/PATCH → UPDATE
Delete  →  DELETE  →  DELETE
```

**What is Idempotency?**

Multiple identical requests = Same effect as single request

```java
// ✅ Idempotent
PUT /users/123 {"name":"John"}  // Always results in name="John"

// ❌ NOT Idempotent  
POST /users {"name":"John"}     // Creates new user each time
```

**Why Idempotency Matters**: Safe to retry failed requests

**What is Safety?**

A method is **safe** if it doesn't modify resource state (read-only).

```
Safe ⊂ Idempotent
- All safe methods are idempotent
- Not all idempotent methods are safe
```

**PUT vs PATCH**:
```java
// PUT: Complete replacement
PUT /users/123 {"name":"Jane"}  // Replaces entire resource

// PATCH: Partial update  
PATCH /users/123 {"name":"Jane"} // Updates only name field
```

#### Answering the Question

| Method | Purpose | Idempotent | Safe | Body | Response |
|--------|---------|-----------|------|------|----------|
| **GET** | Retrieve resource | ✅ | ✅ | ❌ | ✅ |
| **POST** | Create resource | ❌ | ❌ | ✅ | ✅ |
| **PUT** | Update/Replace entire | ✅ | ❌ | ✅ | ✅ |
| **PATCH** | Partial update | ❌ | ❌ | ✅ | ✅ |
| **DELETE** | Remove resource | ✅ | ❌ | ❌ | Optional |
| **HEAD** | Headers only | ✅ | ✅ | ❌ | ❌ |
| **OPTIONS** | Communication options | ✅ | ✅ | ❌ | ✅ |

**Key Concepts:**
- **Idempotent**: Multiple identical requests = same effect as single request
- **Safe**: Doesn't modify resource state

---

## Security

### Q8: DDoS Prevention

#### Understanding Core Concepts

**What is a Denial of Service (DoS) Attack?**

A DoS attack attempts to make a system **unavailable to legitimate users** by overwhelming it with malicious traffic or requests.

**Goal**: Exhaust system resources:
- CPU cycles
- Memory
- Network bandwidth
- Database connections
- Thread pools

**Simple DoS Example**:
```java
// Attacker sends many requests from single source
for (int i = 0; i < 1000000; i++) {
    httpClient.get("https://victim.com/expensive-operation");
}
// Server overwhelmed, can't serve legitimate users
```

**What is a DDoS (Distributed Denial of Service) Attack?**

DDoS is a DoS attack from **multiple sources simultaneously** (distributed).

**Why Distributed?**
- Single IP easy to block
- Multiple IPs (thousands/millions) hard to block
- More traffic volume
- Uses botnet (network of compromised machines)

**Botnet Visualization**:
```
        Attacker (Command & Control)
               |
    ┌──────────┼──────────┐
    ↓          ↓          ↓
  Bot1       Bot2       Bot3
  (PC)       (IoT)      (Server)
  1000s      1000s      1000s
    ↓          ↓          ↓
    └──────────┴──────────┘
           ↓
    Victim Server (overwhelmed)
```

**Types of DDoS Attacks**

**1. Volumetric Attacks** (Network Layer)
- Flood network with massive traffic
- Consumes bandwidth
- Examples:
  - **UDP Flood**: Sends UDP packets to random ports
  - **ICMP Flood (Ping Flood)**: Overwhelms with ICMP Echo requests
  - **DNS Amplification**: Uses DNS servers to amplify traffic

**2. Protocol Attacks** (Transport Layer)
- Exploits protocol weaknesses
- Consumes server resources (connection tables)
- Examples:
  - **SYN Flood**: Sends TCP SYN requests, never completes handshake
  ```
  Client → SYN → Server (creates connection entry, waits)
  Client ← SYN-ACK ← Server
  [Client never sends ACK, connection hangs]
  Repeat 1000s times → Server connection table full
  ```
  - **Ping of Death**: Sends malformed packets

**3. Application Layer Attacks** (Layer 7)
- Targets application itself
- Appears as legitimate traffic (hardest to detect)
- Consumes application resources
- Examples:
  - **HTTP Flood**: Legitimate-looking HTTP GET/POST requests
  - **Slowloris**: Opens connections, sends partial requests slowly
  - **API Abuse**: Calls expensive operations (search, report generation)

**Attack Example**:
```java
// Attacker targets expensive endpoint
GET /api/users/search?query=*&export=pdf  // Scans all users, generates PDF
GET /api/reports/generate?year=all        // Processes years of data
GET /api/images/resize?size=10000x10000   // CPU-intensive operation

// Legitimate users can't access application
```

**What is Rate Limiting?**

Rate limiting restricts the **number of requests** a user can make in a time window.

**Token Bucket Algorithm**:
```
Bucket capacity: 100 tokens
Refill rate: 10 tokens/second

Request arrives:
- If tokens available: Consume 1 token, allow request
- If no tokens: Reject request (429 Too Many Requests)
- Tokens refill over time
```

**Example**:
```java
// User can make:
// - 100 requests immediately (burst)
// - 10 requests per second sustained
// - If exceeded: HTTP 429 (Too Many Requests)
```

**What is a Circuit Breaker?**

Circuit breaker prevents **cascading failures** by stopping calls to failing service.

**States**:
```
CLOSED → Requests flow normally
  ↓ (Failure threshold exceeded)
OPEN → Requests fail immediately (no calls to service)
  ↓ (After timeout)
HALF_OPEN → Try one request
  ↓ Success?        ↓ Failure?
CLOSED            OPEN
```

**What is a Web Application Firewall (WAF)?**

WAF filters, monitors, and blocks HTTP traffic to/from web application.

**Functions**:
- Blocks malicious requests
- Detects attack patterns (SQL injection, XSS, DDoS)
- Rate limiting
- Geo-blocking
- Bot detection

**Popular WAFs**: AWS WAF, Cloudflare, ModSecurity

#### Answering the Question: DDoS Prevention Strategies

**DDoS (Distributed Denial of Service)**: Overwhelming system with traffic from multiple sources to make it unavailable

**Prevention Strategies:**

```java
// 1. Rate Limiting
@RateLimiter(name = "apiRateLimiter")
public Response getResource() { }

// 2. Circuit Breaker
@CircuitBreaker(name = "service", fallbackMethod = "fallback")
public Response callService() { }
```

**Infrastructure Level:**
- API Gateway (AWS API Gateway, Kong, Nginx)
- CDN & Load Balancers (CloudFlare, AWS CloudFront)
- WAF (Web Application Firewall)
- AWS Shield, Cloudflare DDoS Protection
- IP Whitelisting/Blacklisting
- CAPTCHA for critical endpoints

**Application Level:**
- Rate limiting (Resilience4j, Bucket4j)
- Throttling concurrent connections
- Traffic analysis (ELK, Prometheus)
- Circuit breaker pattern

---

### Q12: API Security Best Practices

```java
// 1. Authentication & Authorization
@PreAuthorize("hasRole('ADMIN')")
@Secured("ROLE_ADMIN")
public Response secureEndpoint() { }

// 2. Rate Limiting
@RateLimiter(name = "default")
public Response limitedEndpoint() { }

// 3. CORS Configuration
@CrossOrigin(origins = "https://trusted-domain.com")
public Response getData() { }
```

**Security Checklist:**
- ✅ OAuth 2.0 / OpenID Connect / JWT
- ✅ HTTPS/TLS encryption
- ✅ Input validation & sanitization
- ✅ API Gateway with centralized security
- ✅ Rate limiting & throttling
- ✅ Request signing (HMAC)
- ✅ Security headers (X-Frame-Options, CSP, HSTS)
- ✅ Logging & monitoring
- ✅ Least privilege principle
- ✅ IP whitelisting

---

## Java 8+ Features

### Q9: Method Reference & Functional Interface

#### Functional Interface
```java
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);
    default void print() { }    // Allowed
    static void display() { }   // Allowed
}

// Usage
Calculator add = (a, b) -> a + b;
```

**Built-in Functional Interfaces:**
- `Predicate<T>` - boolean test(T t)
- `Function<T,R>` - R apply(T t)
- `Consumer<T>` - void accept(T t)
- `Supplier<T>` - T get()
- `BiFunction<T,U,R>` - R apply(T t, U u)

#### Method Reference Types

```java
// 1. Static method reference
Function<String, Integer> parser = Integer::parseInt;

// 2. Instance method of particular object
String str = "Hello";
Supplier<Integer> lengthSupplier = str::length;

// 3. Instance method of arbitrary object
Function<String, String> upperCase = String::toUpperCase;

// 4. Constructor reference
Supplier<ArrayList<String>> listSupplier = ArrayList::new;
```

---

### Q10: Default/Static Method Override Rules (Java 8)

#### Default Methods

```java
interface A { 
    default void show() { System.out.println("A"); } 
}

interface B { 
    default void show() { System.out.println("B"); } 
}

// Diamond Problem Resolution
class C implements A, B {
    @Override
    public void show() {
        A.super.show(); // Must explicitly resolve ambiguity
        // Or B.super.show();
    }
}
```

**Rules:**
1. Class implementation > Interface default
2. Most specific interface default wins
3. Must use `InterfaceName.super.method()` for ambiguity

#### Static Methods

```java
interface MyInterface {
    static void staticMethod() { 
        System.out.println("Static method"); 
    }
}

// Call: MyInterface.staticMethod()
// NOT: ClassName.staticMethod() ❌
```

**Rules:**
- Cannot be overridden
- Belong to interface, not inherited
- Called using interface name only

---

### Q43: String.join() (Java 8)

```java
// Basic usage
String result = String.join(", ", "Apple", "Banana", "Orange");
// "Apple, Banana, Orange"

// Join collection
List<String> fruits = Arrays.asList("Apple", "Banana", "Orange");
String result = String.join(", ", fruits);

// Different delimiters
String csv = String.join(",", "John", "Doe", "30");
String path = String.join("/", "home", "user", "documents");

// With Streams & prefix/suffix
String result = list.stream()
    .collect(Collectors.joining(", ", "[", "]"));
// "[Apple, Banana, Orange]"
```

---

### Q26: Java 8 IO Improvements

```java
// 1. Files.lines() - Stream API
try (Stream<String> lines = Files.lines(Paths.get("file.txt"))) {
    lines.filter(line -> line.contains("error"))
         .forEach(System.out::println);
}

// 2. Files.walk() - Directory traversal
Files.walk(Paths.get("/data"))
     .filter(Files::isRegularFile)
     .forEach(System.out::println);

// 3. Files.list() - List directory
Files.list(Paths.get("/data"))
     .forEach(System.out::println);

// 4. BufferedReader.lines()
try (BufferedReader br = Files.newBufferedReader(Paths.get("file.txt"))) {
    br.lines().forEach(System.out::println);
}
```

---

## Design Patterns

### Q11: Design Patterns Overview

#### Creational Patterns

**1. Singleton** - Single instance
```java
// Thread-safe Bill Pugh method
public class Singleton {
    private Singleton() {}
    
    private static class Holder {
        private static final Singleton INSTANCE = new Singleton();
    }
    
    public static Singleton getInstance() { 
        return Holder.INSTANCE; 
    }
}

// Best: Enum Singleton (prevents reflection, serialization attacks)
public enum Singleton {
    INSTANCE;
    public void doSomething() { }
}
```

**2. Builder** - Construct complex objects
```java
User user = User.builder()
    .name("John")
    .email("john@example.com")
    .age(25)
    .build();

// Lombok
@Builder
public class User {
    private String name;
    private String email;
    private int age;
}
```

**3. Factory** - Create objects without specifying class
```java
public interface Shape { }

public class ShapeFactory {
    public Shape getShape(String type) {
        if ("CIRCLE".equals(type)) return new Circle();
        if ("SQUARE".equals(type)) return new Square();
        return null;
    }
}
```

**4. Prototype** - Clone existing objects
```java
public class Document implements Cloneable {
    @Override
    public Document clone() { 
        return (Document) super.clone(); 
    }
}
```

#### Structural Patterns

**1. Adapter** - Convert interface
```java
public class PaymentAdapter implements PaymentGateway {
    private ThirdPartyPayment thirdParty;
    
    public void processPayment() { 
        thirdParty.pay(); 
    }
}
```

**2. Proxy** - Control access
```java
public class ProxyImage implements Image {
    private RealImage realImage;
    
    public void display() {
        if (realImage == null) 
            realImage = new RealImage();
        realImage.display();
    }
}
```

**3. Decorator** - Add responsibilities dynamically
```java
public class EncryptedDataSource implements DataSource {
    private DataSource wrapper;
    
    public void writeData(String data) { 
        wrapper.writeData(encrypt(data)); 
    }
}
```

#### Behavioral Patterns

**1. Chain of Responsibility** - Pass request through handler chain
```java
abstract class Handler {
    protected Handler next;
    public abstract void handle(Request req);
}
```

**2. Observer** - Notify dependents
```java
public interface Observer {
    void update(Event event);
}
```

**3. Strategy** - Select algorithm at runtime
```java
public interface PaymentStrategy {
    void pay(int amount);
}
```

---

### Q39: Builder vs Constructor

**Constructor Problems:**
```java
// ❌ Telescoping constructors
public User(String name) { }
public User(String name, String email) { }
public User(String name, String email, int age) { }

// ❌ Unclear parameters
User user = new User("John", "john@email.com", 25, "123");
```

**Builder Advantages:**
```java
// ✅ Readable & self-documenting
User user = User.builder()
    .name("John")
    .email("john@email.com")
    .age(25)
    .build();
```

**Benefits:**
- ✅ Readable code
- ✅ Optional parameters
- ✅ Immutability support
- ✅ Fluent API
- ✅ Validation in build()

---

### Q37: Breaking Singleton & Creation Strategies

#### Ways to Break Singleton

```java
// 1. Reflection
Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
constructor.setAccessible(true);
Singleton instance2 = constructor.newInstance(); // ❌ Breaks singleton

// Prevention
private Singleton() {
    if (instance != null) {
        throw new RuntimeException("Use getInstance()");
    }
}

// 2. Serialization
// Prevention
protected Object readResolve() {
    return getInstance();
}

// 3. Cloning
// Prevention
@Override
protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
}
```

#### Creation Strategies

| Strategy | Thread-Safe | Lazy | Best For |
|----------|-------------|------|----------|
| Eager | ✅ | ❌ | Always needed |
| Lazy | ❌ | ✅ | Rarely used |
| Synchronized | ✅ | ✅ | Simple, slow |
| Double-Checked | ✅ | ✅ | Performance critical |
| Bill Pugh | ✅ | ✅ | **Recommended** |
| Enum | ✅ | ❌ | **Best - Bulletproof** |

**Best Implementation (Bill Pugh):**
```java
public class Singleton {
    private Singleton() {}
    
    private static class SingletonHolder {
        private static final Singleton INSTANCE = new Singleton();
    }
    
    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

---

### Q38: Deep vs Shallow Cloning

```java
// Shallow Copy - References copied
class Person implements Cloneable {
    String name;
    Address address;
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); // Shallow
    }
}

// Deep Copy - Objects recursively copied
class Person implements Cloneable {
    String name;
    Address address;
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        Person cloned = (Person) super.clone();
        cloned.address = (Address) this.address.clone(); // Deep
        return cloned;
    }
}
```

**Cloneable Interface:**
- Marker interface (no methods)
- Must override `clone()` method
- Throws `CloneNotSupportedException` if not implemented

**Better Alternatives:**
- Copy constructor: `public Person(Person other) { }`
- Builder pattern
- Serialization/Deserialization

---

## Collections Framework

### Q13: hashCode() and equals()

#### Understanding Core Concepts

**What is Object Identity vs Object Equality?**

**Identity**: Are they the same object in memory?
```java
Employee e1 = new Employee(1, "John");
Employee e2 = new Employee(1, "John");

System.out.println(e1 == e2);  // false (different objects)
```

**Equality**: Do they have the same content/value?
```java
System.out.println(e1.equals(e2));  // Should be true if properly implemented
```

**What is equals() Method?**

Defined in `Object` class, determines if two objects are "equal".

**Default Implementation** (from Object class):
```java
public boolean equals(Object obj) {
    return (this == obj);  // Reference equality only
}
```

**Problem**: Default compares references, not content!

**What is hashCode() Method?**

Returns an integer hash code representing the object.

**Default Implementation** (from Object class):
```java
public native int hashCode();  // Based on memory address
```

**Purpose**: Used by hash-based collections (HashMap, HashSet) for:
1. **Quick lookup**: Determine which bucket to check
2. **Performance**: O(1) average lookup time
3. **Distribution**: Spread objects across buckets

**How HashMap Uses hashCode()**:
```java
// Simplified HashMap logic
public V put(K key, V value) {
    int hash = key.hashCode();        // 1. Get hash code
    int index = hash % buckets.length; // 2. Find bucket index
    
    // 3. In bucket, use equals() to find exact match
    for (Entry<K,V> e : buckets[index]) {
        if (e.key.equals(key)) {
            e.value = value;  // Update existing
            return;
        }
    }
    buckets[index].add(new Entry<>(key, value));  // Add new
}
```

**Why Both Methods?**

**Two-step process**:
1. **hashCode()**: Narrows down search space (find bucket)
2. **equals()**: Exact match within bucket

**Analogy**:
```
Finding person in large company:
1. hashCode() = Department (narrows to 50 people)
2. equals() = Check name match (finds exact person)
```

**The Contract Between equals() and hashCode()**

**Rule 1**: If `a.equals(b)` is `true`, then `a.hashCode() == b.hashCode()` **MUST** be true
```java
// If two objects are equal, they must have same hash code
if (obj1.equals(obj2)) {
    assert obj1.hashCode() == obj2.hashCode();  // Must be true
}
```

**Rule 2**: If `a.hashCode() == b.hashCode()`, `a.equals(b)` **MAY** be true or false
```java
// Same hash code doesn't mean objects are equal (hash collision)
if (obj1.hashCode() == obj2.hashCode()) {
    // obj1.equals(obj2) might be true or false
}
```

**Rule 3**: If `a.equals(b)` is `false`, hash codes **CAN** be same or different
```java
// Different objects can have same hash code (collision)
if (!obj1.equals(obj2)) {
    // obj1.hashCode() might equal obj2.hashCode() (collision)
    // or might be different
}
```

**What is a Hash Collision?**

Different objects with same hash code.

**Example**:
```java
"Aa".hashCode() == "BB".hashCode()  // true! (both = 2112)
```

**Why Collisions Happen**:
- Infinite possible objects
- Finite hash codes (32-bit integer: ~4 billion values)
- By pigeonhole principle, collisions inevitable

**How HashMap Handles Collisions**:
- Multiple objects in same bucket (linked list or tree)
- Use `equals()` to find exact match

**What Happens If Only One is Overridden?**

**Scenario 1: Only equals() overridden**
```java
class Employee {
    int id;
    
    @Override
    public boolean equals(Object o) {
        return this.id == ((Employee) o).id;
    }
    // hashCode() not overridden - uses Object's default (memory address)
}

Employee e1 = new Employee(1);
Employee e2 = new Employee(1);

e1.equals(e2)  // true
e1.hashCode() == e2.hashCode()  // false! (different memory addresses)

// HashMap behavior
Map<Employee, String> map = new HashMap<>();
map.put(e1, "John");
map.get(e2);  // ❌ null! (different buckets due to different hash codes)
```

**Problem**: HashMap can't find logically equal objects!

**Scenario 2: Only hashCode() overridden**
```java
class Employee {
    int id;
    
    @Override
    public int hashCode() {
        return id;
    }
    // equals() not overridden - uses Object's default (reference equality)
}

Employee e1 = new Employee(1);
Employee e2 = new Employee(1);

e1.hashCode() == e2.hashCode()  // true
e1.equals(e2)  // false! (different references)

// HashMap behavior
Map<Employee, String> map = new HashMap<>();
map.put(e1, "John");
map.get(e2);  // ❌ null! (same bucket, but equals() fails)
```

**Problem**: Objects end up in same bucket but can't find each other!

#### Answering the Question

**Contract Rules:**
1. If `a.equals(b)` → `a.hashCode() == b.hashCode()` (MUST)
2. If `a.hashCode() == b.hashCode()` → `a.equals(b)` (MAY or MAY NOT)
3. Always override both together

```java
public class Employee {
    private int id;
    private String name;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee emp = (Employee) o;
        return id == emp.id && Objects.equals(name, emp.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
```

**Override Scenarios:**

| Scenario | HashMap Behavior | Issue |
|----------|------------------|-------|
| Only equals() | ❌ Malfunction | Won't find objects correctly |
| Only hashCode() | ❌ Malfunction | More collisions, wrong equality |
| Both | ✅ Works | Correct behavior |
| Neither | ⚠️ Reference equality | Only finds same instance |

---

### Q14: HashMap and HashSet Internals

#### Understanding Core Concepts

**What is a Hash Table?**

A data structure that implements an **associative array** (map) using a **hash function** to compute an index.

**Basic Concept**:
```
Key → Hash Function → Index → Value

"John" → hash() → 5 → Array[5] = {age: 30}
```

**Components**:
1. **Array** (buckets): Stores entries
2. **Hash Function**: Converts key to array index
3. **Collision Resolution**: Handles multiple keys mapping to same index

**Why Use Hash Table?**

**Performance**:
```
Array:      get(index) = O(1), search(value) = O(n)
LinkedList: get(index) = O(n), search(value) = O(n)
HashMap:    get(key) = O(1) average, search = O(1) average
```

**Hash Function Requirements**:
1. **Deterministic**: Same input → same output
2. **Fast**: Quick to compute
3. **Uniform distribution**: Spread keys evenly

**What is a Collision?**

When **different keys** hash to the **same index**.

**Example**:
```java
hash("John") % 16 = 5
hash("Jane") % 16 = 5  // Collision!
```

**Collision Resolution Strategies**:

**1. Separate Chaining** (used by Java HashMap)
- Each bucket is a linked list or tree
- Store all colliding entries in same bucket
```
Bucket 0: []
Bucket 1: [Entry1 → Entry2 → Entry3]  // Collision chain
Bucket 2: [Entry4]
```

**2. Open Addressing** (alternative)
- Find next empty slot
- Linear probing, quadratic probing

**What is Load Factor?**

Ratio of filled buckets to total capacity.

```
Load Factor = Number of entries / Capacity

Example:
16 buckets, 12 entries → Load Factor = 12/16 = 0.75
```

**Why Important?**
- **Low load factor**: Fast lookup, wasted space
- **High load factor**: Slow lookup (more collisions), efficient space
- **Default 0.75**: Good balance

**When Load Factor Exceeded**: **Rehashing**
1. Double capacity (16 → 32 → 64...)
2. Create new bigger array
3. Recompute indices for all entries
4. Copy entries to new array

**Rehashing Example**:
```java
// Before: capacity = 4, size = 3, load factor = 0.75
Bucket 0: [A]
Bucket 1: [B]
Bucket 2: [C]
Bucket 3: []

// Add 4th element → load factor = 4/4 = 1.0 > 0.75
// Trigger rehash!

// After: capacity = 8, size = 4
Bucket 0: [A]
Bucket 1: []
Bucket 2: [B]
Bucket 3: []
Bucket 4: [C]
Bucket 5: [D]
Bucket 6: []
Bucket 7: []
```

**Cost**: Rehashing is O(n) operation!

**What is Treeification (Java 8)?**

When bucket chain grows too long (≥8 elements), **convert to red-black tree**.

**Why?**
- Linked list: O(n) lookup
- Tree: O(log n) lookup

**When Does It Happen?**
```java
// Conditions for treeification:
1. Bucket has ≥ 8 entries (TREEIFY_THRESHOLD)
2. Total capacity ≥ 64 (MIN_TREEIFY_CAPACITY)

// If capacity < 64, resize instead of treeify
```

**Conversion**:
```
Linked List (8+ elements):     Tree:
[E1 → E2 → E3 → ... → E10]       E5
                              / \
                            E3   E8
                           /    / \
                          E2  E6  E10
```

**What is a Red-Black Tree?**

Self-balancing binary search tree:
- Every node: Red or Black
- Root: Always Black
- No two adjacent Red nodes
- All paths from root to leaf have same number of Black nodes

**Properties**:
- Height: O(log n)
- Search: O(log n)
- Insert: O(log n)

#### Answering the Question

#### HashMap (Java 8+)

**Data Structure:**
- Array of `Node<K,V>` (buckets)
- Bucket: LinkedList (< 8 elements) or Red-Black Tree (≥ 8 elements)

**Working:**
```
1. hash = key.hashCode() → apply hash function
2. index = hash & (n-1) → find bucket
3. If collision → traverse list/tree using equals()
4. Load Factor (0.75) → rehash when 75% full
5. Capacity: Default 16, doubles on rehash
```

**Complexity:**
- `put()`: O(1) average, O(log n) worst (tree)
- `get()`: O(1) average, O(log n) worst
- Rehashing: O(n)

#### HashSet

```java
// HashSet uses HashMap internally
private transient HashMap<E, Object> map;
private static final Object PRESENT = new Object();

public boolean add(E e) {
    return map.put(e, PRESENT) == null;
}
```

---

### Q24: Collection Complexities & Data Structures

| Collection | Add | Remove | Get | Contains | Data Structure |
|------------|-----|--------|-----|----------|----------------|
| **ArrayList** | O(1)* | O(n) | O(1) | O(n) | Dynamic Array |
| **LinkedList** | O(1) | O(1)** | O(n) | O(n) | Doubly Linked List |
| **HashMap** | O(1)* | O(1)* | O(1)* | O(1)* | Array + List/Tree |
| **TreeMap** | O(log n) | O(log n) | O(log n) | O(log n) | Red-Black Tree |
| **HashSet** | O(1)* | O(1)* | - | O(1)* | HashMap |
| **TreeSet** | O(log n) | O(log n) | - | O(log n) | TreeMap |
| **PriorityQueue** | O(log n) | O(log n) | O(1) | O(n) | Binary Heap |
| **ArrayDeque** | O(1)* | O(1)* | O(1) | O(n) | Circular Array |

\* Amortized, \*\* If reference available

---

### Q25: Concurrent Collections & Java 8 Improvements

#### Concurrent Collections

```java
// 1. ConcurrentHashMap - Segmented/CAS locking
ConcurrentHashMap<String, User> map = new ConcurrentHashMap<>();

// 2. CopyOnWriteArrayList - Read-heavy scenarios
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

// 3. ConcurrentLinkedQueue - Lock-free
ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();

// 4. BlockingQueue
ArrayBlockingQueue<Task> blockingQueue = new ArrayBlockingQueue<>(100);
```

#### Java 8+ Collection Enhancements

```java
// forEach
list.forEach(System.out::println);

// removeIf
list.removeIf(s -> s.length() < 3);

// replaceAll
list.replaceAll(String::toUpperCase);

// Stream API
List<String> filtered = list.stream()
    .filter(s -> s.startsWith("A"))
    .map(String::toUpperCase)
    .collect(Collectors.toList());

// Collectors
Map<Integer, List<String>> grouped = list.stream()
    .collect(Collectors.groupingBy(String::length));

// ConcurrentHashMap enhancements
map.forEach((k, v) -> System.out.println(k + ": " + v));
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
map.merge(key, 1, Integer::sum);
```

---

## Spring Boot & Microservices

### Q15: Caching in Spring Boot

```java
// 1. Enable Caching
@SpringBootApplication
@EnableCaching
public class Application { }

// 2. Annotations
@Cacheable(value = "users", key = "#id")
public User getUserById(Long id) { }

@CachePut(value = "users", key = "#user.id")
public User updateUser(User user) { }

@CacheEvict(value = "users", key = "#id")
public void deleteUser(Long id) { }

@CacheEvict(value = "users", allEntries = true)
public void clearCache() { }

@Caching(
    cacheable = {@Cacheable("users")},
    evict = {@CacheEvict("oldUsers")}
)
```

**Cache Providers:**
- Simple (ConcurrentHashMap) - Default
- Caffeine
- Redis
- Hazelcast
- EhCache

**Redis Configuration:**
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)))
            .build();
    }
}
```

---

### Q22: WebFlux and Mono

**Spring WebFlux:**
- Reactive, non-blocking web framework
- Built on Project Reactor
- Event-driven, handles massive concurrency

```java
// Mono - 0 or 1 element
@GetMapping("/user/{id}")
public Mono<User> getUser(@PathVariable Long id) {
    return userService.findById(id);
}

// Flux - 0 to N elements
@GetMapping("/users")
public Flux<User> getAllUsers() {
    return userService.findAll();
}

// Operators
Mono.just("Hello")
    .map(String::toUpperCase)
    .flatMap(s -> callExternalService(s))
    .defaultIfEmpty("Default")
    .subscribe(System.out::println);
```

**When to Use:**
- High concurrency requirements
- Streaming data
- Non-blocking I/O
- Backpressure handling

**WebMVC vs WebFlux:**
- WebMVC: Thread per request (blocking)
- WebFlux: Event loop (non-blocking, fewer threads)

---

### Q27: Enable/Disable Auto-Configuration

```java
// Method 1: @SpringBootApplication
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class Application { }

// Method 2: application.properties
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

// Method 3: Conditional
@Configuration
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
public class FeatureConfig { }

// Check auto-configuration report
debug=true
```

---

### Q28: Spring Boot Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoints.web.base-path=/actuator
management.server.port=9090
```

**Custom Health Indicator:**
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        boolean isHealthy = checkService();
        if (isHealthy) {
            return Health.up()
                .withDetail("service", "available")
                .build();
        }
        return Health.down()
            .withDetail("service", "unavailable")
            .build();
    }
}
```

**Custom Endpoint:**
```java
@Component
@Endpoint(id = "custom")
public class CustomEndpoint {
    @ReadOperation
    public Map<String, String> customEndpoint() {
        return Map.of("status", "active");
    }
}
```

**Custom Metrics:**
```java
@Service
public class MetricsService {
    private final MeterRegistry registry;
    
    public void recordMetric() {
        Counter.builder("custom.requests")
            .tag("type", "api")
            .register(registry)
            .increment();
    }
}
```

---

### Q29: Custom Annotation

```java
// 1. Define Annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogExecutionTime {
    String value() default "";
}

// 2. Create Aspect
@Aspect
@Component
public class LogExecutionTimeAspect {
    @Around("@annotation(logExecutionTime)")
    public Object logTime(ProceedingJoinPoint joinPoint, 
                         LogExecutionTime logExecutionTime) 
            throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + 
                          " executed in " + executionTime + "ms");
        return result;
    }
}

// 3. Usage
@Service
public class UserService {
    @LogExecutionTime
    public User getUser(Long id) {
        // Business logic
    }
}
```

**Validation Annotation:**
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidator.class)
public @interface ValidEmail {
    String message() default "Invalid email";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class CustomValidator 
    implements ConstraintValidator<ValidEmail, String> {
    @Override
    public boolean isValid(String value, 
                          ConstraintValidatorContext context) {
        return value != null && 
               value.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

---

### Q35: Prevent Cyclic Dependency

```java
// Problem: A → B, B → A

// Solution 1: @Lazy
@Service
public class ServiceA {
    private final ServiceB serviceB;
    
    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

// Solution 2: @PostConstruct
@Service
public class ServiceA {
    @Autowired
    private ApplicationContext context;
    private ServiceB serviceB;
    
    @PostConstruct
    public void init() {
        this.serviceB = context.getBean(ServiceB.class);
    }
}

// Solution 3: Refactor - Extract common to third class
// A → C, B → C

// Solution 4: Use Events
applicationEventPublisher.publishEvent(new CustomEvent());
```

**Best Practice:** Fix design - cyclic dependencies indicate architectural issues

---

### Q40: Resolve Ambiguous URLs

```java
// Problem: Same URL, same HTTP method

// Solution 1: Request Parameters
@GetMapping(value = "/user", params = "type=student")
public String getStudent() { }

@GetMapping(value = "/user", params = "type=teacher")
public String getTeacher() { }

// Solution 2: Headers
@GetMapping(value = "/user", headers = "X-User-Type=student")
public String getStudent() { }

// Solution 3: Consumes (Content-Type)
@PostMapping(value = "/user", consumes = "application/json")
public String createUserJson(@RequestBody User user) { }

@PostMapping(value = "/user", consumes = "application/xml")
public String createUserXml(@RequestBody User user) { }

// Solution 4: Produces (Accept Header)
@GetMapping(value = "/user", produces = "application/json")
public User getUserJson() { }

@GetMapping(value = "/user", produces = "application/xml")
public User getUserXml() { }
```

---

## Distributed Systems

### Q16: Kafka & Consumer Groups

**Apache Kafka Components:**
- **Producer**: Publishes messages to topics
- **Consumer**: Reads messages from topics
- **Topic**: Category/feed name
- **Partition**: Topic divided for parallelism
- **Broker**: Kafka server
- **ZooKeeper/KRaft**: Cluster coordination

**Consumer Groups:**
- Group of consumers working together
- **Each partition → ONE consumer in group**
- Enables parallel processing & load balancing

```
Topic: orders (3 partitions)
Consumer Group: order-processors (3 consumers)

Partition 0 → Consumer 1
Partition 1 → Consumer 2
Partition 2 → Consumer 3
```

```java
@KafkaListener(topics = "orders", groupId = "order-processors")
public void consume(String message) {
    // Process message
}
```

**Benefits:**
- ✅ Scalability - Add consumers = faster processing
- ✅ Fault tolerance - Rebalancing on failure
- ✅ Offset management - Each group tracks own offset

---

### Q17: Microservices Design Patterns

#### 1. Circuit Breaker
```java
@CircuitBreaker(name = "userService", fallbackMethod = "fallbackUser")
public User getUser(Long id) {
    return userClient.getUser(id);
}

public User fallbackUser(Long id, Exception e) {
    return new User(); // Fallback response
}
```

**States:** CLOSED → OPEN → HALF_OPEN

#### 2. SAGA Pattern
- Manages distributed transactions
- **Choreography**: Event-driven, each service publishes events
- **Orchestration**: Central coordinator manages flow
- Compensating transactions for rollback

#### 3. CQRS (Command Query Responsibility Segregation)
- Separate models for read and write
- Commands: Modify state
- Queries: Read state
- Often used with Event Sourcing

#### 4. Two-Phase Commit (2PC)
- **Phase 1 (Prepare)**: All participants vote
- **Phase 2 (Commit/Rollback)**: Based on votes
- **Issues**: Blocking, single point of failure
- ⚠️ Less preferred (use SAGA instead)

**Other Patterns:**
- API Gateway
- Service Discovery
- Event Sourcing
- Bulkhead
- Retry Pattern
- BFF (Backend for Frontend)

---

### Q21: TraceId and SpanId

**Distributed Tracing:** Track requests across microservices

```java
// Spring Cloud Sleuth
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

**TraceId:**
- Unique identifier for entire request flow
- Same across all services
- Format: `2f9c1d7e8a3b4c5d`

**SpanId:**
- Unique identifier for single operation/service call
- Each service creates new span
- Parent-child relationship

**Example Flow:**
```
TraceId: 123456
├─ SpanId: A (API Gateway)
   ├─ SpanId: B (User Service)
   └─ SpanId: C (Order Service)
      └─ SpanId: D (Payment Service)
```

**Logs:**
```
[app-name,traceId,spanId,exportable]
[user-service,123456,A,true] Processing request
```

**Tools:** Zipkin, Jaeger, AWS X-Ray

---

### Q31: Application Resilience

```java
// 1. Circuit Breaker
@CircuitBreaker(name = "service", fallbackMethod = "fallback")
public String callService() { }

// 2. Retry
@Retry(name = "retryService", fallbackMethod = "fallback")
public String unstableService() { }

// 3. Rate Limiter
@RateLimiter(name = "rateLimiter")
public String limitedEndpoint() { }

// 4. Bulkhead - Isolate resources
@Bulkhead(name = "service", type = Bulkhead.Type.THREADPOOL)

// 5. Timeout
@TimeLimiter(name = "timeLimiter")
public CompletableFuture<String> slowService() { }
```

**Infrastructure:**
- ✅ Health checks (Spring Actuator)
- ✅ Load balancing (Spring Cloud LoadBalancer)
- ✅ Database connection pooling (HikariCP)
- ✅ Monitoring (Prometheus, Grafana, ELK)
- ✅ Auto-scaling (Kubernetes HPA)
- ✅ Chaos engineering (Chaos Monkey)

---

## Database & Performance

### Q18: Database Sharding

**Definition:** Horizontal partitioning - splitting database across multiple servers

**Types:**

```java
// 1. Range-based
// Users 1-10000 → Shard 1
// Users 10001-20000 → Shard 2

// 2. Hash-based
int shardId = userId.hashCode() % totalShards;
DataSource shard = shardMap.get(shardId);

// 3. Geographic
// US users → US shard
// EU users → EU shard

// 4. Directory-based
// Lookup service maps keys to shards
```

**Benefits:**
- ✅ Scalability - Distribute load
- ✅ Performance - Smaller datasets per shard
- ✅ High availability

**Challenges:**
- ❌ Cross-shard queries expensive
- ❌ Rebalancing complexity
- ❌ Application logic complexity

---

### Q19: SQL vs NoSQL

| Aspect | SQL (RDBMS) | NoSQL |
|--------|-------------|-------|
| **Schema** | Fixed, predefined | Flexible, dynamic |
| **Structure** | Tables, rows, columns | Documents, key-value, graphs |
| **Scalability** | Vertical (scale up) | Horizontal (scale out) |
| **Transactions** | ACID compliant | BASE (Eventually consistent) |
| **Joins** | Complex joins | Limited/no joins |
| **Examples** | MySQL, PostgreSQL, Oracle | MongoDB, Cassandra, Redis |
| **Use Cases** | Financial, ERP | Big data, real-time web apps |

**When to use SQL:**
- Complex transactions
- Relationships and joins
- ACID compliance critical

**When to use NoSQL:**
- Massive scale
- Flexible schema
- Horizontal scaling
- Fast reads/writes

---

### Q32: Find Even Rows SQL Query

```sql
-- Method 1: ROW_NUMBER() (Best)
SELECT id, name
FROM (
    SELECT id, name, 
           ROW_NUMBER() OVER (ORDER BY id) AS row_num
    FROM employee
) AS temp
WHERE row_num % 2 = 0;

-- Method 2: Using MOD (if id is sequential)
SELECT id, name
FROM employee
WHERE MOD(id, 2) = 0;

-- Method 3: Self Join (less efficient)
SELECT e1.id, e1.name
FROM employee e1
JOIN employee e2 ON e1.id >= e2.id
GROUP BY e1.id, e1.name
HAVING COUNT(*) % 2 = 0;
```

---

## DevOps & Deployment

### Q30: Kubernetes Essentials

**What is Kubernetes?**
- Container orchestration platform
- Automates deployment, scaling, and management

**Why Use K8s?**
- ✅ Auto-scaling (HPA, VPA)
- ✅ Self-healing
- ✅ Load balancing
- ✅ Rollouts/Rollbacks
- ✅ Service discovery
- ✅ Secret & config management

**Core Components:**

```yaml
# 1. Pod - Smallest unit
apiVersion: v1
kind: Pod
metadata:
  name: my-app
spec:
  containers:
  - name: app
    image: myapp:1.0

# 2. Deployment - Manages ReplicaSets
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: app
        image: myapp:1.0

# 3. Service - Network endpoint
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector:
    app: my-app
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

**Architecture:**
- **Cluster**: Collection of nodes
- **Node**: Physical/virtual machine running containers
- **Pod**: One or more containers sharing network/storage
- **Deployment**: Manages desired state
- **Service**: Stable network endpoint (ClusterIP, NodePort, LoadBalancer)
- **Namespace**: Virtual clusters
- **ConfigMap**: Configuration data
- **Secret**: Sensitive data

---

### Q36: Reduce Docker Image Size

```dockerfile
# ✅ 1. Multi-Stage Builds (Best Practice)
# Build stage
FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim
COPY --from=build /app/target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

# ✅ 2. Use Alpine/Slim Base Images
FROM openjdk:11-jre-slim  # Instead of openjdk:11
FROM node:18-alpine       # Instead of node:18

# ✅ 3. Minimize Layers
RUN apt-get update && apt-get install -y \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# ✅ 4. .dockerignore
node_modules
target
.git
*.md
tests
```

**Spring Boot Layered JARs:**
```dockerfile
FROM openjdk:11-jre-slim
COPY --from=build app/dependencies/ ./
COPY --from=build app/spring-boot-loader/ ./
COPY --from=build app/snapshot-dependencies/ ./
COPY --from=build app/application/ ./
```

**Result:** 600MB+ → 150MB or less

---

### Q41: Application Deployment Structure

```
┌─────────────────────────────────────────────┐
│         AWS Cloud / Azure / GCP             │
├─────────────────────────────────────────────┤
│  Load Balancer (AWS ALB / ELB)              │
├─────────────────────────────────────────────┤
│  API Gateway (Kong / AWS API Gateway)       │
├─────────────────────────────────────────────┤
│       Kubernetes Cluster (EKS/AKS/GKE)      │
│  ┌─────────────────────────────────────┐   │
│  │  Ingress Controller (Nginx)         │   │
│  ├─────────────────────────────────────┤   │
│  │  Microservices (Pods)               │   │
│  │  - User Service (3 replicas)        │   │
│  │  - Order Service (3 replicas)       │   │
│  │  - Payment Service (2 replicas)     │   │
│  └─────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│  Service Discovery (Eureka / Consul)        │
│  Config Server (Spring Cloud Config)        │
│  Distributed Tracing (Zipkin / Jaeger)      │
├─────────────────────────────────────────────┤
│  Message Queue (Kafka / RabbitMQ)           │
├─────────────────────────────────────────────┤
│  Databases                                   │
│  - PostgreSQL (RDS), MongoDB, Redis         │
├─────────────────────────────────────────────┤
│  Monitoring & Logging                       │
│  - Prometheus, Grafana, ELK, CloudWatch     │
└─────────────────────────────────────────────┘
```

**CI/CD Pipeline:**
```
GitHub → Jenkins/GitLab CI → Maven Build → 
Docker Build → Push to ECR → Deploy to K8s → 
Health Check → Monitor
```

---

## Advanced Java Concepts

### Q23: Custom Immutable Class

**Rules:**
1. Declare class `final`
2. All fields `private` and `final`
3. No setter methods
4. Initialize via constructor
5. Deep copy mutable objects
6. Return copies of mutable objects

```java
public final class ImmutableEmployee {
    private final int id;
    private final String name;
    private final Date joiningDate;
    private final List<String> skills;
    
    public ImmutableEmployee(int id, String name, 
                            Date joiningDate, 
                            List<String> skills) {
        this.id = id;
        this.name = name;
        // Deep copy mutable objects
        this.joiningDate = new Date(joiningDate.getTime());
        this.skills = new ArrayList<>(skills);
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    
    public Date getJoiningDate() {
        return new Date(joiningDate.getTime());
    }
    
    public List<String> getSkills() {
        return Collections.unmodifiableList(new ArrayList<>(skills));
    }
}
```

**Proving String Immutability:**
```java
String str = "Hello";
System.out.println("Original hashCode: " + 
                  System.identityHashCode(str));

str = str.concat(" World");
System.out.println("After concat hashCode: " + 
                  System.identityHashCode(str));
// Different hashCode → new object created
```

---

### Q33: Sorting Algorithms

**Arrays.sort():**
- **Primitives**: Dual-Pivot Quicksort (Java 7+)
  - O(n log n) average
- **Objects**: TimSort (Merge + Insertion)
  - O(n log n) guaranteed
  - Stable sort

**Collections.sort():**
- Uses Arrays.sort() internally
- **TimSort** algorithm
- O(n log n) time
- **Stable** (preserves order of equal elements)

```java
// Primitives
int[] arr = {3, 1, 2};
Arrays.sort(arr); // Dual-Pivot Quicksort

// Objects
List<String> list = Arrays.asList("C", "A", "B");
Collections.sort(list); // TimSort
```

---

### Q34: serialVersionUID

**Purpose:** Version control for serialization

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private transient String password; // Won't be serialized
}
```

**How It Works:**
- During serialization: JVM writes `serialVersionUID`
- During deserialization: JVM compares versions
- If mismatch → `InvalidClassException`

**Why Use:**
1. ✅ Explicit version control
2. ✅ Avoid auto-generation (fragile)
3. ✅ Prevent deserialization errors

**Best Practices:**
- Always declare explicitly
- Start with `1L`
- Increment for incompatible changes
- Use `transient` for non-serializable fields

---

### Q42: CompletableFuture

**Purpose:** Asynchronous, non-blocking programming

```java
// 1. supplyAsync / runAsync
CompletableFuture<String> future = 
    CompletableFuture.supplyAsync(() -> "Hello");

CompletableFuture<Void> future2 = 
    CompletableFuture.runAsync(() -> System.out.println("Done"));

// 2. thenApply - Transform result
CompletableFuture<Integer> result = 
    CompletableFuture.supplyAsync(() -> "123")
        .thenApply(Integer::parseInt)
        .thenApply(num -> num * 2);

// 3. thenAccept - Consume result
future.thenAccept(result -> System.out.println(result));

// 4. thenCompose - Flatten nested futures
CompletableFuture<User> user = getUserAsync(id)
    .thenCompose(u -> getAddressAsync(u.getId()));

// 5. thenCombine - Combine two futures
CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 10);
CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> 20);
CompletableFuture<Integer> combined = f1.thenCombine(f2, (a, b) -> a + b);

// 6. allOf / anyOf
CompletableFuture<Void> all = 
    CompletableFuture.allOf(future1, future2, future3);

// 7. Exception Handling
future.exceptionally(ex -> {
    System.err.println("Error: " + ex.getMessage());
    return "Default";
});

future.handle((result, ex) -> {
    if (ex != null) return "Error";
    return result;
});
```

**Real-World Example:**
```java
@Service
public class OrderService {
    public CompletableFuture<OrderDetails> getOrderDetails(Long orderId) {
        CompletableFuture<Order> orderFuture = 
            CompletableFuture.supplyAsync(() -> orderRepo.findById(orderId));
        
        CompletableFuture<User> userFuture = 
            CompletableFuture.supplyAsync(() -> userService.getUser(userId));
        
        CompletableFuture<Payment> paymentFuture = 
            CompletableFuture.supplyAsync(() -> paymentService.getPayment(orderId));
        
        return CompletableFuture.allOf(orderFuture, userFuture, paymentFuture)
            .thenApply(v -> new OrderDetails(
                orderFuture.join(),
                userFuture.join(),
                paymentFuture.join()
            ));
    }
}
```

---

## Bonus Topics

### Q20: Apache Spark

**What is Spark?**
- Unified analytics engine for large-scale data processing
- In-memory processing (100x faster than Hadoop)
- Supports batch, streaming, ML, graph processing

**Components:**
- Spark Core, Spark SQL, Spark Streaming, MLlib, GraphX

**With Spring Boot:**
```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-core_2.12</artifactId>
</dependency>
```

```java
@Service
public class SparkService {
    public void processData() {
        SparkConf conf = new SparkConf()
            .setAppName("app")
            .setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        
        JavaRDD<String> data = sc.textFile("data.txt");
        long count = data.filter(line -> line.contains("error")).count();
        
        sc.close();
    }
}
```

**Use Cases:**
- ETL pipelines
- Real-time analytics
- Machine learning at scale
- Log processing

---

## Quick Reference Tables

### HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | Successful request |
| 201 | Created | Resource created |
| 204 | No Content | Successful, no response body |
| 400 | Bad Request | Invalid input |
| 401 | Unauthorized | Authentication required |
| 403 | Forbidden | No permission |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Server overloaded |

### Spring Boot Annotations Quick Reference

```java
// Core
@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
@RestController = @Controller + @ResponseBody
@Service, @Repository, @Component

// Web
@GetMapping, @PostMapping, @PutMapping, @DeleteMapping, @PatchMapping
@RequestBody, @PathVariable, @RequestParam, @RequestHeader

// Validation
@Valid, @NotNull, @NotEmpty, @Size, @Min, @Max, @Email, @Pattern

// JPA
@Entity, @Table, @Id, @GeneratedValue, @Column, @OneToMany, @ManyToOne

// Configuration
@Configuration, @Bean, @Value, @ConfigurationProperties
@Profile, @Conditional

// Async & Scheduling
@Async, @Scheduled, @EnableAsync, @EnableScheduling

// Security
@Secured, @PreAuthorize, @PostAuthorize, @EnableWebSecurity

// Testing
@SpringBootTest, @WebMvcTest, @DataJpaTest, @MockBean, @Mock
```

### Common Maven Dependencies

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<!-- Resilience -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Interview Tips

### Behavioral Questions Approach
1. **Situation**: Set the context
2. **Task**: Describe the challenge
3. **Action**: What you did
4. **Result**: Outcome and learning

### Technical Discussion Framework
1. Start with high-level overview
2. Dive into implementation details
3. Discuss trade-offs
4. Mention alternatives
5. Share real-world experience

### Problem-Solving Approach
1. Clarify requirements
2. Think aloud
3. Consider edge cases
4. Discuss complexity
5. Optimize solution

### Common Follow-up Areas
- **Scalability**: How would you scale this?
- **Performance**: How would you optimize?
- **Security**: How would you secure this?
- **Monitoring**: How would you monitor this?
- **Testing**: How would you test this?

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Java Documentation](https://docs.oracle.com/en/java/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Design Patterns](https://refactoring.guru/design-patterns)

---

## Contributing

Feel free to contribute by:
- Adding more questions
- Improving explanations
- Fixing errors
- Adding examples

---

## License

This cheat sheet is provided as-is for educational purposes.


