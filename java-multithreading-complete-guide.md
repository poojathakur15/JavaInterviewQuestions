# 🧵 Java Multithreading - Complete Interview Guide

**Author:** Java Interview Preparation Guide  
**Target Audience:** Java Developers preparing for interviews  
**Last Updated:** February 19, 2026

---

## 📋 Table of Contents

### Part 1: Fundamentals
1. [What is Multithreading?](#1-what-is-multithreading)
2. [Process vs Thread](#2-process-vs-thread)
3. [Thread Lifecycle](#3-thread-lifecycle)
4. [Creating Threads](#4-creating-threads)
5. [Thread Class vs Runnable Interface](#5-thread-class-vs-runnable-interface)

### Part 2: Thread Management
6. [Thread Priority](#6-thread-priority)
7. [Thread Sleep, Yield, and Join](#7-thread-sleep-yield-and-join)
8. [Daemon Threads](#8-daemon-threads)
9. [Thread Naming](#9-thread-naming)
10. [Thread Groups](#10-thread-groups)

### Part 3: Synchronization
11. [What is Synchronization?](#11-what-is-synchronization)
12. [Synchronized Method](#12-synchronized-method)
13. [Synchronized Block](#13-synchronized-block)
14. [Static Synchronization](#14-static-synchronization)
15. [Deadlock](#15-deadlock)

### Part 4: Inter-Thread Communication
16. [wait(), notify(), and notifyAll()](#16-wait-notify-and-notifyall)
17. [Producer-Consumer Problem](#17-producer-consumer-problem)
18. [Thread Communication Best Practices](#18-thread-communication-best-practices)

### Part 5: Advanced Concurrency
19. [Volatile Keyword](#19-volatile-keyword)
20. [Atomic Variables](#20-atomic-variables)
21. [Thread Local](#21-thread-local)
22. [Executor Framework](#22-executor-framework)
23. [Callable and Future](#23-callable-and-future)

### Part 6: Concurrent Collections
24. [ConcurrentHashMap](#24-concurrenthashmap)
25. [CopyOnWriteArrayList](#25-copyonwritearraylist)
26. [BlockingQueue](#26-blockingqueue)
27. [Thread-Safe Collections](#27-thread-safe-collections)

### Part 7: Locks and Synchronizers
28. [ReentrantLock](#28-reentrantlock)
29. [ReadWriteLock](#29-readwritelock)
30. [Semaphore](#30-semaphore)
31. [CountDownLatch](#31-countdownlatch)
32. [CyclicBarrier](#32-cyclicbarrier)
33. [Exchanger](#33-exchanger)

### Part 8: Best Practices & Patterns
34. [Thread Pool Best Practices](#34-thread-pool-best-practices)
35. [Common Multithreading Issues](#35-common-multithreading-issues)
36. [Thread Safety Strategies](#36-thread-safety-strategies)
37. [Performance Optimization](#37-performance-optimization)

---

# Part 1: Fundamentals

## 1. What is Multithreading?

### Definition

**Multithreading** is a Java feature that allows concurrent execution of two or more parts of a program for maximum utilization of CPU. Each part of such a program is called a **thread**.

### Key Concepts

- **Thread**: Lightweight subprocess, smallest unit of processing
- **Multitasking**: Multiple tasks executing simultaneously
- **Concurrency**: Multiple threads making progress at the same time
- **Parallelism**: Multiple threads executing simultaneously on multiple cores

### Visual Representation

```
Single-Threaded Program:
Task 1 → Task 2 → Task 3 → Task 4
|______________________________|
        Sequential Execution

Multi-Threaded Program:
Task 1 ──┐
Task 2 ──┼─→ CPU
Task 3 ──┤
Task 4 ──┘
    Concurrent Execution
```

### Advantages

✅ **Better CPU Utilization** - Maximum use of CPU time  
✅ **Improved Performance** - Multiple operations simultaneously  
✅ **Responsiveness** - UI remains responsive during long operations  
✅ **Resource Sharing** - Threads share memory space  
✅ **Simplified Program Structure** - For concurrent operations  

### Disadvantages

❌ **Complex Debugging** - Hard to reproduce and fix bugs  
❌ **Synchronization Issues** - Race conditions, deadlocks  
❌ **Context Switching Overhead** - CPU time spent switching threads  
❌ **Increased Memory Consumption** - Each thread needs stack space  

### Real-World Examples

```java
// Example 1: Simple Multithreading Demo
class MultiThreadingDemo {
    public static void main(String[] args) {
        // Creating multiple threads
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Thread 1: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Thread 2: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Start both threads
        t1.start();
        t2.start();
    }
}
```

**Output** (may vary):
```
Thread 1: 1
Thread 2: 1
Thread 1: 2
Thread 2: 2
Thread 1: 3
Thread 2: 3
...
```

---

## 2. Process vs Thread

### Visual Comparison

```
┌─────────────────────────────────────┐
│          PROCESS                    │
│  ┌─────────────────────────────┐   │
│  │   Code Section              │   │
│  ├─────────────────────────────┤   │
│  │   Data Section              │   │
│  ├─────────────────────────────┤   │
│  │   Heap Memory               │   │
│  └─────────────────────────────┘   │
│                                     │
│  Thread 1   Thread 2   Thread 3    │
│  [Stack]    [Stack]    [Stack]     │
│  [Register] [Register] [Register]  │
│  [Counter]  [Counter]  [Counter]   │
└─────────────────────────────────────┘
```

### Detailed Comparison Table

| Feature | Process | Thread |
|---------|---------|--------|
| **Definition** | Independent program in execution | Lightweight sub-process within a process |
| **Memory** | Separate memory space | Shares memory with other threads |
| **Creation Cost** | Expensive (more resources) | Cheap (fewer resources) |
| **Context Switching** | Slower | Faster |
| **Communication** | Inter-Process Communication (IPC) | Direct communication (shared memory) |
| **Independence** | Fully independent | Dependent on parent process |
| **Isolation** | Changes don't affect other processes | Changes affect all threads in process |
| **Example** | Multiple Java applications | Multiple tasks in one Java application |

### Code Example

```java
// Process Example - Running separate JVM instances
class ProcessDemo {
    public static void main(String[] args) throws Exception {
        // Creating a new process
        ProcessBuilder pb = new ProcessBuilder("java", "-version");
        Process process = pb.start();
        
        System.out.println("Process ID: " + process.pid());
        System.out.println("Is Alive: " + process.isAlive());
        
        int exitCode = process.waitFor();
        System.out.println("Exit Code: " + exitCode);
    }
}
```

**Output:**
```
Process ID: 12345
Is Alive: true
Exit Code: 0
```

```java
// Thread Example - Within same JVM
class ThreadDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> 
            System.out.println("Thread: " + Thread.currentThread().getName())
        );
        
        Thread t2 = new Thread(() -> 
            System.out.println("Thread: " + Thread.currentThread().getName())
        );
        
        t1.start();
        t2.start();
        
        // All threads share the same process memory
    }
}
```

**Output:**
```
Thread: Thread-0
Thread: Thread-1
```

---

## 3. Thread Lifecycle

### Thread States

A thread can be in one of the following states:

1. **NEW** - Thread created but not started (using `new Thread()`)
2. **RUNNABLE** - Thread is ready to run, waiting for CPU time (after calling `start()`)
3. **RUNNING** - Thread is currently executing (CPU assigned by scheduler)
4. **BLOCKED** - Waiting for monitor lock (trying to enter synchronized block/method)
5. **WAITING** - Waiting indefinitely for another thread (using `wait()`, `join()`)
6. **TIMED_WAITING** - Waiting for a specified time (using `sleep()`, `wait(timeout)`, `join(timeout)`)
7. **TERMINATED** - Thread has completed execution (after `run()` completes)

### State Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          THREAD LIFECYCLE STATES                            │
└─────────────────────────────────────────────────────────────────────────────┘

    ┌─────────┐
    │   NEW   │  ← Thread object created (new Thread())
    └────┬────┘
         │
         │ start()
         ↓
    ┌──────────┐
    │ RUNNABLE │  ← Thread is ready to run (in ready queue)
    └────┬─────┘
         │
         │ CPU Scheduler assigns CPU time
         ↓
    ┌──────────┐
    │ RUNNING  │  ← Thread is currently executing
    └────┬─────┘
         │
         ├──────────────────────────────────────────────────────────────┐
         │                                                               │
         │ run() completes                                               │
         │ or exception                                                  │
         ↓                                                               │
    ┌─────────────┐                                                     │
    │ TERMINATED  │  ← Thread finished execution (dead)                │
    └─────────────┘                                                     │
                                                                         │
    ┌────────────────────────────────────────────────────────────────────┘
    │
    │  From RUNNING state, thread can transition to:
    │
    ├─→ sleep(ms) / wait(ms) / join(ms)
    │   ┌─────────────────┐
    │   │ TIMED_WAITING   │  ← Waiting for specific time
    │   └────────┬────────┘
    │            │ timeout expires
    │            └────────────────────┐
    │                                 │
    ├─→ wait() / join()               │
    │   ┌──────────┐                  │
    │   │ WAITING  │  ← Waiting until notified
    │   └────┬─────┘                  │
    │        │ notify() / notifyAll() │
    │        └────────────────────────┤
    │                                 │
    ├─→ synchronized (lock not available)
    │   ┌──────────┐                  │
    │   │ BLOCKED  │  ← Waiting for monitor lock
    │   └────┬─────┘                  │
    │        │ lock acquired          │
    │        └────────────────────────┤
    │                                 │
    ├─→ yield()                       │
    │   │                             │
    │   └─────────────────────────────┤
    │                                 ↓
    │                            ┌──────────┐
    └────────────────────────────│ RUNNABLE │ ← Back to ready queue
                                 └──────────┘
                                      │
                                      │ CPU Scheduler
                                      ↓
                                 ┌──────────┐
                                 │ RUNNING  │ ← Resume execution
                                 └──────────┘
```

### Visual Summary

```
                    ┌──────────────────────────────────┐
                    │   Thread Creation & Execution    │
                    └──────────────────────────────────┘
                                    │
              NEW  ─────start()────→ RUNNABLE ─────┬───→ RUNNING
                                       ↑            │       │
                                       │   CPU      │       │
                                       │  Scheduler │       │
                                       │            │       ↓
                                       │            │   TERMINATED
                                       │            │   (completed)
                                       │            │
              ┌────────────────────────┴────────────┴───────┐
              │    Thread can be temporarily suspended:     │
              └──────────────────────────────────────────────┘
                     │              │              │
                     │              │              │
                     ↓              ↓              ↓
              TIMED_WAITING    WAITING        BLOCKED
              (sleep/wait)   (wait/join)   (lock waiting)
                     │              │              │
                     └──────┬───────┴──────┬───────┘
                            │              │
                            ↓              ↓
                       RUNNABLE ─────→ RUNNING
```

### State Transition Methods

| State Transition | Method | Description |
|------------------|--------|-------------|
| **NEW → RUNNABLE** | `start()` | Thread becomes ready to run |
| **RUNNABLE → RUNNING** | CPU Scheduler | JVM thread scheduler assigns CPU |
| **RUNNING → RUNNABLE** | `yield()` | Thread voluntarily gives up CPU |
| **RUNNING → TIMED_WAITING** | `sleep(ms)` | Thread sleeps for specified milliseconds |
| **RUNNING → TIMED_WAITING** | `wait(ms)` | Wait with timeout (must hold lock) |
| **RUNNING → TIMED_WAITING** | `join(ms)` | Wait for another thread with timeout |
| **RUNNING → WAITING** | `wait()` | Wait until notified (must hold lock) |
| **RUNNING → WAITING** | `join()` | Wait for another thread to complete |
| **RUNNING → BLOCKED** | synchronized | Waiting to acquire monitor lock |
| **WAITING → RUNNABLE** | `notify()` | Wake up one waiting thread |
| **WAITING → RUNNABLE** | `notifyAll()` | Wake up all waiting threads |
| **TIMED_WAITING → RUNNABLE** | timeout expires | Automatically returns after timeout |
| **BLOCKED → RUNNABLE** | lock acquired | Gets the monitor lock |
| **RUNNING → TERMINATED** | `run()` completes | Thread execution finishes |

### Complete Example

```java
class ThreadLifecycleDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Thread is RUNNABLE");
                
                // TIMED_WAITING
                Thread.sleep(2000);
                System.out.println("Thread woke up from sleep");
                
                // Synchronized block
                synchronized (ThreadLifecycleDemo.class) {
                    System.out.println("Thread in synchronized block");
                    ThreadLifecycleDemo.class.wait(1000); // TIMED_WAITING
                }
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread is about to TERMINATE");
        });
        
        // State: NEW
        System.out.println("1. State: " + thread.getState());
        
        thread.start();
        // State: RUNNABLE
        System.out.println("2. State: " + thread.getState());
        
        Thread.sleep(500);
        // State: TIMED_WAITING
        System.out.println("3. State: " + thread.getState());
        
        thread.join(); // Wait for thread to complete
        // State: TERMINATED
        System.out.println("4. State: " + thread.getState());
    }
}
```

**Output:**
```
1. State: NEW
2. State: RUNNABLE
Thread is RUNNABLE
3. State: TIMED_WAITING
Thread woke up from sleep
Thread in synchronized block
Thread is about to TERMINATE
4. State: TERMINATED
```

---

## 4. Creating Threads

### Method 1: Extending Thread Class

```java
// Step 1: Create a class that extends Thread
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread is running: " + Thread.currentThread().getName());
        
        for (int i = 1; i <= 5; i++) {
            System.out.println(Thread.currentThread().getName() + ": " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Step 2: Create and start the thread
class ThreadMethodOne {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();
        
        t1.setName("Worker-1");
        t2.setName("Worker-2");
        
        t1.start(); // Don't call run() directly!
        t2.start();
        
        System.out.println("Main thread: " + Thread.currentThread().getName());
    }
}
```

**Output:**
```
Main thread: main
Thread is running: Worker-1
Thread is running: Worker-2
Worker-1: 1
Worker-2: 1
Worker-1: 2
Worker-2: 2
Worker-1: 3
Worker-2: 3
Worker-1: 4
Worker-2: 4
Worker-1: 5
Worker-2: 5
```
> **Note:** The actual order may vary as threads execute concurrently and their scheduling is determined by the JVM.

---

### Method 2: Implementing Runnable Interface

```java
// Step 1: Create a class that implements Runnable
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable is running: " + Thread.currentThread().getName());
        
        for (int i = 1; i <= 5; i++) {
            System.out.println(Thread.currentThread().getName() + ": " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Step 2: Create Thread object with Runnable
class ThreadMethodTwo {
    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        
        Thread t1 = new Thread(runnable, "Worker-1");
        Thread t2 = new Thread(runnable, "Worker-2");
        
        t1.start();
        t2.start();
        
        System.out.println("Main thread: " + Thread.currentThread().getName());
    }
}
```

**Output:**
```
Main thread: main
Runnable is running: Worker-1
Runnable is running: Worker-2
Worker-1: 1
Worker-2: 1
Worker-1: 2
Worker-2: 2
Worker-1: 3
Worker-2: 3
Worker-1: 4
Worker-2: 4
Worker-1: 5
Worker-2: 5
```
> **Note:** Both threads share the same Runnable instance but execute independently.

---

### Method 3: Using Anonymous Class

> **Introduced in:** Java 1.1 (1997)  
> **Still Present in:** Java 17 and all modern Java versions  
> **Purpose:** Create one-time-use classes without explicitly naming them  
> **Note:** While still fully supported, lambda expressions (Java 8+) provide a more concise syntax for functional interfaces

```java
class AnonymousThreadDemo {
    public static void main(String[] args) {
        // Anonymous Thread class
        Thread t1 = new Thread() {
            @Override
            public void run() {
                System.out.println("Anonymous Thread: " + getName());
            }
        };
        
        // Anonymous Runnable interface
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Runnable: " + Thread.currentThread().getName());
            }
        });
        
        t1.start();
        t2.start();
    }
}
```

**Output:**
```
Anonymous Thread: Thread-0
Anonymous Runnable: Thread-1
```
> **Note:** Default thread names are Thread-0, Thread-1, etc., unless explicitly set.

---

### Method 4: Using Lambda Expression (Java 8+)

```java
class LambdaThreadDemo {
    public static void main(String[] args) {
        // Lambda with Runnable
        Thread t1 = new Thread(() -> {
            System.out.println("Lambda Thread 1: " + Thread.currentThread().getName());
        });
        
        // Lambda with multiple statements
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("Lambda Thread 2: " + i);
            }
        });
        
        // Lambda with name
        Thread t3 = new Thread(() -> 
            System.out.println("Lambda Thread 3"), "CustomName"
        );
        
        t1.start();
        t2.start();
        t3.start();
    }
}
```

**Output:**
```
Lambda Thread 1: Thread-0
Lambda Thread 2: 1
Lambda Thread 2: 2
Lambda Thread 2: 3
Lambda Thread 3
```
> **Note:** Thread t3 will show "CustomName" if you print Thread.currentThread().getName() inside the lambda.

---

### Method 5: Using Executor Framework (Modern Approach)

```java
import java.util.concurrent.*;

class ExecutorThreadDemo {
    public static void main(String[] args) {
        // Create a thread pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Submit tasks
        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " executed by: " + 
                                   Thread.currentThread().getName());
            });
        }
        
        // Shutdown executor
        executor.shutdown();
    }
}
```

**Output:**
```
Task 1 executed by: pool-1-thread-1
Task 2 executed by: pool-1-thread-2
Task 3 executed by: pool-1-thread-3
Task 4 executed by: pool-1-thread-1
Task 5 executed by: pool-1-thread-2
```
> **Note:** With a pool of 3 threads, tasks 4 and 5 are executed by reusing threads from the pool. The order may vary based on thread scheduling.

---

### Comparison Table

| Method | Advantage | Disadvantage | When to Use |
|--------|-----------|--------------|-------------|
| **Extend Thread** | Simple, direct access to Thread methods | Can't extend other classes | Simple, standalone threads |
| **Implement Runnable** | Can extend other classes, better OOP | Need to wrap in Thread object | Preferred for most cases |
| **Anonymous Class** | Quick for one-time use | Verbose syntax | Quick prototyping |
| **Lambda Expression** | Concise, modern syntax | Less readable for complex logic | Simple tasks (Java 8+) |
| **Executor Framework** | Resource management, reusability | More complex setup | Production applications |

---

## 5. Thread Class vs Runnable Interface

### Key Differences

```java
// Thread Class Approach
class ThreadApproach extends Thread {
    @Override
    public void run() {
        System.out.println("Using Thread Class");
    }
}

// Runnable Interface Approach
class RunnableApproach implements Runnable {
    @Override
    public void run() {
        System.out.println("Using Runnable Interface");
    }
}
```

### Detailed Comparison

| Aspect | Thread Class | Runnable Interface |
|--------|--------------|-------------------|
| **Inheritance** | Can't extend another class | Can extend other classes |
| **Coupling** | Tightly coupled | Loosely coupled |
| **Reusability** | Less reusable | More reusable |
| **Object Creation** | Creates new thread object | Creates task object |
| **Multiple Threads** | Need multiple objects | Can share one Runnable |
| **Resource Efficiency** | More memory per thread | Less memory overhead |
| **Best Practice** | ❌ Not recommended | ✅ Recommended |

### Why Runnable is Better

**Problem with Thread Class:**
```java
// ❌ BAD: Can't extend both Thread and another class
class MyTask extends Thread { // Already extending Thread
    // Cannot extend another class!
    // class MyTask extends Thread, SomeOtherClass { } // NOT POSSIBLE!
    
    @Override
    public void run() {
        System.out.println("Task running");
    }
}
```

**Solution with Runnable:**
```java
// ✅ GOOD: Can extend other classes
class MyTask extends SomeBaseClass implements Runnable {
    @Override
    public void run() {
        System.out.println("Task running");
    }
}

class SomeBaseClass {
    void someMethod() {
        System.out.println("Method from base class");
    }
}
```

### Resource Sharing Example

```java
// Sharing same Runnable instance
class TicketBooking implements Runnable {
    private int availableTickets = 5;
    
    @Override
    public void run() {
        synchronized (this) {
            if (availableTickets > 0) {
                System.out.println(Thread.currentThread().getName() + 
                                   " booked ticket. Remaining: " + (--availableTickets));
            } else {
                System.out.println(Thread.currentThread().getName() + 
                                   " - No tickets available");
            }
        }
    }
}

class BookingDemo {
    public static void main(String[] args) {
        // Single Runnable instance shared by multiple threads
        TicketBooking booking = new TicketBooking();
        
        Thread t1 = new Thread(booking, "User-1");
        Thread t2 = new Thread(booking, "User-2");
        Thread t3 = new Thread(booking, "User-3");
        Thread t4 = new Thread(booking, "User-4");
        Thread t5 = new Thread(booking, "User-5");
        Thread t6 = new Thread(booking, "User-6");
        
        t1.start(); t2.start(); t3.start();
        t4.start(); t5.start(); t6.start();
    }
}
```

**Output:**
```
User-1 booked ticket. Remaining: 4
User-2 booked ticket. Remaining: 3
User-3 booked ticket. Remaining: 2
User-4 booked ticket. Remaining: 1
User-5 booked ticket. Remaining: 0
User-6 - No tickets available
```
> **Note:** The order may vary due to thread scheduling, but the ticket count will always be correctly synchronized.

---

### Complete Comparison Example

```java
// Scenario: Download files from multiple sources

// Using Thread Class
class DownloadThread extends Thread {
    private String url;
    
    public DownloadThread(String url) {
        this.url = url;
    }
    
    @Override
    public void run() {
        System.out.println("Downloading from: " + url);
        // Download logic
    }
}

// Using Runnable Interface
class DownloadTask implements Runnable {
    private String url;
    
    public DownloadTask(String url) {
        this.url = url;
    }
    
    @Override
    public void run() {
        System.out.println("Downloading from: " + url);
        // Download logic
    }
}

class DownloadDemo {
    public static void main(String[] args) {
        // Thread approach - Creates 3 thread objects
        DownloadThread dt1 = new DownloadThread("url1");
        DownloadThread dt2 = new DownloadThread("url2");
        DownloadThread dt3 = new DownloadThread("url3");
        
        dt1.start();
        dt2.start();
        dt3.start();
        
        // Runnable approach - Better separation of concern
        Thread t1 = new Thread(new DownloadTask("url1"));
        Thread t2 = new Thread(new DownloadTask("url2"));
        Thread t3 = new Thread(new DownloadTask("url3"));
        
        t1.start();
        t2.start();
        t3.start();
        
        // Or use Executor (Best approach)
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(new DownloadTask("url1"));
        executor.submit(new DownloadTask("url2"));
        executor.submit(new DownloadTask("url3"));
        executor.shutdown();
    }
}
```

**Output:**
```
Downloading from: url1
Downloading from: url2
Downloading from: url3
Downloading from: url1
Downloading from: url2
Downloading from: url3
```
> **Note:** First three lines from Thread approach, next three from Runnable approach. Actual order may vary.

---

### Best Practice Recommendation

✅ **Always prefer Runnable interface over Thread class**

**Reasons:**
1. Follows composition over inheritance principle
2. Allows extending other classes
3. Better separation of task and thread management
4. More flexible and reusable
5. Works well with modern Executor framework

---

## 5A. Can We Override the start() Method?

### Short Answer

**Yes, you CAN override start(), but you SHOULD NOT!**

Overriding `start()` defeats the entire purpose of multithreading because it prevents the creation of a new thread.

---

### What Happens Normally?

When you call `thread.start()`:

1. **JVM creates a new thread** (a new call stack)
2. **Thread enters RUNNABLE state**
3. **JVM internally calls run() method** in the new thread
4. **Concurrent execution** happens

```java
// Normal flow
MyThread t = new MyThread();
t.start(); // ✅ Creates new thread → calls run() in new thread
```

---

### What Happens When You Override start()?

If you override `start()` without calling `super.start()`, **NO new thread is created!**

```java
class WrongThread extends Thread {
    @Override
    public void start() {
        // ❌ Overridden without super.start()
        System.out.println("start() overridden - No new thread created!");
        run(); // Called in SAME thread (main thread)
    }
    
    @Override
    public void run() {
        System.out.println("Running in thread: " + Thread.currentThread().getName());
    }
}

class OverrideStartWrong {
    public static void main(String[] args) {
        WrongThread t = new WrongThread();
        
        System.out.println("Main thread: " + Thread.currentThread().getName());
        t.start(); // Looks like starting thread, but...
        System.out.println("After start() call");
    }
}
```

**Output:**
```
Main thread: main
start() overridden - No new thread created!
Running in thread: main
After start() call
```

**❌ Problem:** Everything runs in the **main thread**! No concurrency!

---

### Correct Way to Override start() (If You Must)

If you have a valid reason to override `start()`, **always call super.start()**:

```java
class CustomThread extends Thread {
    @Override
    public void start() {
        System.out.println("Custom start() called");
        // Add your custom logic here (before thread starts)
        
        super.start(); // ✅ MUST call this to create new thread!
        
        // Add logic after thread starts (runs in calling thread)
        System.out.println("Thread has been started");
    }
    
    @Override
    public void run() {
        System.out.println("Running in thread: " + Thread.currentThread().getName());
    }
}

class OverrideStartCorrect {
    public static void main(String[] args) {
        System.out.println("Main thread: " + Thread.currentThread().getName());
        
        CustomThread t = new CustomThread();
        t.start(); // Now creates new thread properly
        
        System.out.println("Main continues...");
    }
}
```

**Output:**
```
Main thread: main
Custom start() called
Thread has been started
Main continues...
Running in thread: Thread-0
```

**✅ Correct:** New thread is created because of `super.start()`

---

### Comparison: start() vs run()

```java
class StartVsRun {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            System.out.println("Thread-1 executing in: " + 
                             Thread.currentThread().getName());
        });
        
        Thread t2 = new Thread(() -> {
            System.out.println("Thread-2 executing in: " + 
                             Thread.currentThread().getName());
        });
        
        System.out.println("=== Using start() - Creates New Thread ===");
        t1.start(); // ✅ New thread created
        
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        
        System.out.println("\n=== Calling run() Directly - No New Thread ===");
        t2.run(); // ❌ No new thread - runs in main thread
    }
}
```

**Output:**
```
=== Using start() - Creates New Thread ===
Thread-1 executing in: Thread-0

=== Calling run() Directly - No New Thread ===
Thread-2 executing in: main
```

**Key Difference:**
- `start()` → Creates **new thread** → JVM calls `run()` in that thread
- `run()` → **No new thread** → Just a normal method call

---

### What If You Call start() Twice?

```java
class DoubleStartDemo {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            System.out.println("Thread running: " + Thread.currentThread().getName());
        });
        
        t.start(); // First call - OK
        System.out.println("First start() succeeded");
        
        try {
            Thread.sleep(100);
            t.start(); // Second call - Exception!
        } catch (IllegalThreadStateException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
```

**Output:**
```
Thread running: Thread-0
First start() succeeded
❌ Error: Thread already started
```

**Rule:** `start()` can be called **only ONCE** per thread object. Second call throws `IllegalThreadStateException`.

---

### Real-World Example: Adding Logging to start()

**Valid Use Case:** Add logging/monitoring when threads start

```java
class LoggingThread extends Thread {
    private static int threadCount = 0;
    private int threadId;
    
    public LoggingThread(Runnable target, String name) {
        super(target, name);
    }
    
    @Override
    public void start() {
        threadId = ++threadCount;
        System.out.println("🚀 Starting thread #" + threadId + 
                         " (" + getName() + ") at " + System.currentTimeMillis());
        
        super.start(); // ✅ Create actual thread
        
        System.out.println("✅ Thread #" + threadId + " started successfully");
    }
    
    @Override
    public void run() {
        super.run();
        System.out.println("🏁 Thread #" + threadId + " completed");
    }
}

class LoggingThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        LoggingThread t1 = new LoggingThread(() -> {
            System.out.println("   Task 1 executing...");
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }, "Worker-1");
        
        LoggingThread t2 = new LoggingThread(() -> {
            System.out.println("   Task 2 executing...");
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }, "Worker-2");
        
        t1.start();
        Thread.sleep(100);
        t2.start();
        
        t1.join();
        t2.join();
    }
}
```

**Output:**
```
🚀 Starting thread #1 (Worker-1) at 1708777234567
✅ Thread #1 started successfully
   Task 1 executing...
🚀 Starting thread #2 (Worker-2) at 1708777234670
✅ Thread #2 started successfully
   Task 2 executing...
🏁 Thread #2 completed
🏁 Thread #1 completed
```

---

### Common Mistake: Overriding start() for Custom Logic

```java
// ❌ WRONG: Trying to add custom behavior
class WrongApproach extends Thread {
    @Override
    public void start() {
        System.out.println("Initializing thread...");
        // Forgot super.start() - BIG MISTAKE!
        run(); // Runs in calling thread, NOT a new thread
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            System.out.println("Count: " + i + " in " + 
                             Thread.currentThread().getName());
        }
    }
}

// ✅ CORRECT: Use initialization in constructor or separate method
class CorrectApproach extends Thread {
    public CorrectApproach() {
        System.out.println("Initializing thread...");
        // Initialization logic here
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            System.out.println("Count: " + i + " in " + 
                             Thread.currentThread().getName());
        }
    }
}

class ComparisonDemo {
    public static void main(String[] args) {
        System.out.println("=== Wrong Approach ===");
        WrongApproach wrong = new WrongApproach();
        wrong.start();
        
        System.out.println("\n=== Correct Approach ===");
        CorrectApproach correct = new CorrectApproach();
        correct.start();
    }
}
```

**Output:**
```
=== Wrong Approach ===
Initializing thread...
Count: 0 in main
Count: 1 in main
Count: 2 in main

=== Correct Approach ===
Initializing thread...
Count: 0 in Thread-0
Count: 1 in Thread-0
Count: 2 in Thread-0
```

---

### Visual Representation

**Normal start() Flow:**
```
Main Thread                         New Thread (Thread-0)
    │                                       │
    │ thread.start()                        │
    ├──────────────────────────────────────►│
    │                                       │
    │                                 JVM creates thread
    │                                       │
    │                                 JVM calls run()
    │                                       │
    │ (continues)                      (executes run())
    │                                       │
    ▼                                       ▼
```

**Overridden start() without super.start():**
```
Main Thread
    │
    │ thread.start()
    │
    │ (overridden start() called)
    │
    │ run() called directly
    │
    │ (NO new thread created!)
    │
    ▼
```

---

### Interview Q&A

**Q1: Can we override start() method?**
**A:** Yes, but you should call `super.start()` to create the actual thread. Without it, no new thread is created.

**Q2: What happens if we don't call super.start()?**
**A:** The `run()` method executes in the calling thread (e.g., main thread), defeating the purpose of multithreading.

**Q3: Why would we override start()?**
**A:** Valid reasons include:
- Adding logging/monitoring
- Performing initialization checks
- Tracking thread lifecycle
- Adding security checks

**Q4: What's the difference between start() and run()?**
**A:**
- `start()`: Creates a new thread and JVM calls `run()` in that thread
- `run()`: Just a normal method call in the current thread

**Q5: Can we call start() multiple times on the same thread?**
**A:** No! It throws `IllegalThreadStateException` on the second call.

---

### Best Practices

✅ **DO:**
- Override `run()` for thread logic
- Call `super.start()` if you override `start()`
- Use `start()` to begin thread execution

❌ **DON'T:**
- Override `start()` without calling `super.start()`
- Call `run()` directly (use `start()` instead)
- Call `start()` more than once on same thread
- Add heavy logic in overridden `start()` before `super.start()`

---

### Summary

| Scenario | Result | New Thread? | Recommended? |
|----------|--------|-------------|--------------|
| `t.start()` (normal) | Creates new thread, JVM calls run() | ✅ Yes | ✅ Yes |
| `t.run()` (direct call) | Method call in current thread | ❌ No | ❌ Never |
| Override `start()` with `super.start()` | Creates new thread + custom logic | ✅ Yes | ⚠️ If needed |
| Override `start()` without `super.start()` | No new thread created | ❌ No | ❌ Never |
| `t.start()` twice | IllegalThreadStateException | ❌ N/A | ❌ Never |

---

**🎯 Key Takeaway:** You CAN override `start()`, but you MUST call `super.start()` to create the actual thread. Otherwise, you lose all benefits of multithreading!

---

# Part 2: Thread Management

## 6. Thread Priority

### Understanding Priority

Java threads have priorities ranging from **1 to 10**:
- `MIN_PRIORITY = 1`
- `NORM_PRIORITY = 5` (default)
- `MAX_PRIORITY = 10`

### Visual Representation

```
Priority Level:
1 ──────────────────────────────────────→ 10
MIN                 NORM                MAX
(Lowest)          (Default)           (Highest)
```

### Important Points

⚠️ **Thread priority is just a hint to the scheduler**  
⚠️ **Does NOT guarantee execution order**  
⚠️ **Platform-dependent behavior**  
⚠️ **Higher priority = Higher probability of execution**

### Setting Priority

```java
class PriorityDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + 
                                   " - Priority: " + Thread.currentThread().getPriority() + 
                                   " - Count: " + i);
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + 
                                   " - Priority: " + Thread.currentThread().getPriority() + 
                                   " - Count: " + i);
            }
        });
        
        Thread t3 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + 
                                   " - Priority: " + Thread.currentThread().getPriority() + 
                                   " - Count: " + i);
            }
        });
        
        // Set priorities
        t1.setPriority(Thread.MIN_PRIORITY);  // 1
        t2.setPriority(Thread.NORM_PRIORITY); // 5
        t3.setPriority(Thread.MAX_PRIORITY);  // 10
        
        t1.setName("Low-Priority");
        t2.setName("Normal-Priority");
        t3.setName("High-Priority");
        
        t1.start();
        t2.start();
        t3.start();
    }
}
```

**Output:**
```
High-Priority - Priority: 10 - Count: 1
High-Priority - Priority: 10 - Count: 2
High-Priority - Priority: 10 - Count: 3
High-Priority - Priority: 10 - Count: 4
High-Priority - Priority: 10 - Count: 5
Normal-Priority - Priority: 5 - Count: 1
Normal-Priority - Priority: 5 - Count: 2
Normal-Priority - Priority: 5 - Count: 3
Normal-Priority - Priority: 5 - Count: 4
Normal-Priority - Priority: 5 - Count: 5
Low-Priority - Priority: 1 - Count: 1
Low-Priority - Priority: 1 - Count: 2
Low-Priority - Priority: 1 - Count: 3
Low-Priority - Priority: 1 - Count: 4
Low-Priority - Priority: 1 - Count: 5
```
> **Note:** Priority is a hint, not a guarantee. Actual execution order may vary based on OS scheduler. Higher priority threads tend to execute first but it's not guaranteed.

---

### Default Priority Behavior

```java
class DefaultPriorityDemo {
    public static void main(String[] args) {
        System.out.println("Main thread priority: " + 
                           Thread.currentThread().getPriority()); // 5
        
        Thread child = new Thread(() -> {
            System.out.println("Child thread priority: " + 
                               Thread.currentThread().getPriority()); // 5 (inherits from parent)
        });
        
        child.start();
        
        // Change parent priority
        Thread.currentThread().setPriority(7);
        
        Thread child2 = new Thread(() -> {
            System.out.println("Child2 thread priority: " + 
                               Thread.currentThread().getPriority()); // 7 (inherits from parent)
        });
        
        child2.start();
    }
}
```

**Output:**
```
Main thread priority: 5
Child thread priority: 5
Child2 thread priority: 7
```
> **Note:** Child threads inherit priority from their parent thread at creation time.

---

### Real-World Example

```java
class DownloadManager {
    public static void main(String[] args) {
        Thread criticalDownload = new Thread(() -> {
            System.out.println("Downloading critical system update...");
            // Simulate download
            for (int i = 0; i <= 100; i += 10) {
                System.out.println("Critical: " + i + "%");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread regularDownload = new Thread(() -> {
            System.out.println("Downloading regular file...");
            // Simulate download
            for (int i = 0; i <= 100; i += 10) {
                System.out.println("Regular: " + i + "%");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread backgroundDownload = new Thread(() -> {
            System.out.println("Downloading background updates...");
            // Simulate download
            for (int i = 0; i <= 100; i += 10) {
                System.out.println("Background: " + i + "%");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        // Set priorities based on importance
        criticalDownload.setPriority(Thread.MAX_PRIORITY);   // 10
        regularDownload.setPriority(Thread.NORM_PRIORITY);   // 5
        backgroundDownload.setPriority(Thread.MIN_PRIORITY); // 1
        
        criticalDownload.start();
        regularDownload.start();
        backgroundDownload.start();
    }
}
```

**Output:**
```
Downloading critical system update...
Downloading regular file...
Downloading background updates...
Critical: 0%
Regular: 0%
Background: 0%
Critical: 10%
Regular: 10%
Background: 10%
Critical: 20%
Regular: 20%
Background: 20%
Critical: 30%
Regular: 30%
Background: 30%
Critical: 40%
Regular: 40%
Background: 40%
Critical: 50%
Regular: 50%
Background: 50%
Critical: 60%
Regular: 60%
Background: 60%
Critical: 70%
Regular: 70%
Background: 70%
Critical: 80%
Regular: 80%
Background: 80%
Critical: 90%
Regular: 90%
Background: 90%
Critical: 100%
Regular: 100%
Background: 100%
```
> **Note:** Critical download (MAX_PRIORITY) tends to get more CPU time, but execution order varies by OS. All downloads progress concurrently.

---

## 7. Thread Sleep, Yield, and Join

### 1. sleep() Method

Causes the current thread to sleep for the specified time.

```java
class SleepDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + ": " + i);
                try {
                    // Sleep for 1 second
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        t1.start();
    }
}
```

**Output:**
```
Thread-0: 1
Thread-0: 2
Thread-0: 3
Thread-0: 4
Thread-0: 5
```
> **Note:** Each line appears after a 1-second delay. Total execution time: ~5 seconds.

---

**Key Points:**
- ✅ Thread moves to TIMED_WAITING state
- ✅ Does NOT release lock (if holding any)
- ✅ Throws InterruptedException (checked exception)
- ✅ Static method - always sleeps current thread

### 2. yield() Method

Hints to scheduler that current thread is willing to yield its current use of CPU.

```java
class YieldDemo {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + ": " + i);
                Thread.yield(); // Give chance to other threads
            }
        }, "Thread-1");
        
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(Thread.currentThread().getName() + ": " + i);
            }
        }, "Thread-2");
        
        t1.start();
        t2.start();
    }
}
```

**Output:**
```
Thread-1: 1
Thread-2: 1
Thread-1: 2
Thread-2: 2
Thread-1: 3
Thread-2: 3
Thread-1: 4
Thread-2: 4
Thread-1: 5
Thread-2: 5
```
> **Note:** Thread-1 yields after each iteration, giving Thread-2 a chance to execute. However, yield() is just a hint and actual behavior is platform-dependent.

---

**Key Points:**
- ✅ Only a hint to scheduler
- ✅ May or may not have any effect
- ✅ Does NOT release lock
- ✅ Thread remains in RUNNABLE state
- ✅ Static method

### 3. join() Method

Wait for a thread to die/complete.

```java
class JoinDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Thread-1: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Thread-2: " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        t1.start();
        t1.join(); // Wait for t1 to complete
        
        t2.start();
        t2.join(); // Wait for t2 to complete
        
        System.out.println("All threads completed!");
    }
}
```

**Output:**
```
Thread-1: 1
Thread-1: 2
Thread-1: 3
Thread-1: 4
Thread-1: 5
Thread-2: 1
Thread-2: 2
Thread-2: 3
Thread-2: 4
Thread-2: 5
All threads completed!
```
> **Note:** Thread-1 completes entirely before Thread-2 starts (due to join). Main thread waits for both to finish before printing "All threads completed!".

---

**Key Points:**
- ✅ Current thread waits for specified thread to die
- ✅ Throws InterruptedException
- ✅ Three variants: `join()`, `join(long millis)`, `join(long millis, int nanos)`
- ✅ Instance method

### Comparison Table

| Feature | sleep() | yield() | join() |
|---------|---------|---------|--------|
| **Purpose** | Pause execution | Give chance to others | Wait for thread completion |
| **Type** | Static | Static | Instance |
| **Exception** | InterruptedException | None | InterruptedException |
| **Lock Release** | ❌ No | ❌ No | ❌ No |
| **State** | TIMED_WAITING | RUNNABLE | WAITING/TIMED_WAITING |
| **Guarantee** | ✅ Yes | ❌ No (just hint) | ✅ Yes |

---

### sleep() vs yield() - Detailed Comparison

#### Visual State Diagram

**sleep() - Thread State Changes:**
```
Thread Running
    ↓
thread.sleep(1000)
    ↓
[TIMED_WAITING] ──→ Guaranteed to wait 1000ms
    ↓
Time expires
    ↓
[RUNNABLE] ──→ Ready to run (waits for CPU)
    ↓
Scheduler assigns CPU
    ↓
[RUNNING]
```

**yield() - Thread State Changes:**
```
Thread Running
    ↓
thread.yield()
    ↓
[RUNNABLE] ──→ Gives up CPU voluntarily (maybe!)
    ↓
Scheduler may or may not give CPU to another thread
    ↓
[RUNNING] or stays [RUNNABLE]
```

---

#### Key Difference #1: Guaranteed Pause vs Hint

**sleep() - Guaranteed Pause:**
```java
class SleepGuaranteed {
    public static void main(String[] args) {
        System.out.println("Start: " + System.currentTimeMillis());
        
        try {
            Thread.sleep(2000); // GUARANTEED 2-second pause
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("End: " + System.currentTimeMillis());
        System.out.println("Elapsed: ~2000ms (guaranteed)");
    }
}
```

**Output:**
```
Start: 1708777890123
End: 1708777892125
Elapsed: ~2000ms (guaranteed)
```

**yield() - Just a Hint (No Guarantee):**
```java
class YieldNoGuarantee {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("Thread-1: " + i);
                Thread.yield(); // Hint only - may be ignored!
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("Thread-2: " + i);
            }
        });
        
        t1.start();
        t2.start();
    }
}
```

**Output (Varies by Run):**
```
Thread-1: 0
Thread-1: 1
Thread-1: 2
Thread-1: 3
Thread-1: 4
Thread-2: 0
Thread-2: 1
Thread-2: 2
Thread-2: 3
Thread-2: 4
```
**Note:** yield() may have no effect! Threads might run sequentially anyway.

---

#### Key Difference #2: Thread State

**sleep() Changes State:**
```java
class SleepStateDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                System.out.println("Before sleep: " + Thread.currentThread().getState());
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        worker.start();
        Thread.sleep(100); // Let worker thread start
        
        System.out.println("During sleep: " + worker.getState());
        
        worker.join();
        System.out.println("After sleep: " + worker.getState());
    }
}
```

**Output:**
```
Before sleep: RUNNABLE
During sleep: TIMED_WAITING
After sleep: TERMINATED
```

**yield() Doesn't Change State:**
```java
class YieldStateDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("Before yield: " + Thread.currentThread().getState());
            Thread.yield();
            System.out.println("After yield: " + Thread.currentThread().getState());
        });
        
        worker.start();
        worker.join();
    }
}
```

**Output:**
```
Before yield: RUNNABLE
After yield: RUNNABLE
```
**Note:** State remains RUNNABLE throughout!

---

#### Key Difference #3: Time-Based vs Voluntary

**sleep() - Time-Based Pause:**
```java
class TimedPauseExample {
    public static void main(String[] args) {
        Thread counter = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Count: " + i + " at " + 
                                 System.currentTimeMillis());
                try {
                    Thread.sleep(1000); // Exactly 1 second between counts
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        counter.start();
    }
}
```

**Output:**
```
Count: 1 at 1708777890123
Count: 2 at 1708777891125
Count: 3 at 1708777892127
Count: 4 at 1708777893129
Count: 5 at 1708777894131
```
**Note:** Consistent ~1000ms intervals!

**yield() - Voluntary CPU Release:**
```java
class VoluntaryReleaseExample {
    public static void main(String[] args) throws InterruptedException {
        Thread polite = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Polite thread: " + i);
                Thread.yield(); // "After you, other threads!"
            }
        });
        
        Thread eager = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("Eager thread: " + i);
                // No yield - keeps running
            }
        });
        
        polite.start();
        eager.start();
        
        polite.join();
        eager.join();
    }
}
```

**Output (One Possible Run):**
```
Polite thread: 1
Eager thread: 1
Eager thread: 2
Eager thread: 3
Eager thread: 4
Eager thread: 5
Polite thread: 2
Polite thread: 3
Polite thread: 4
Polite thread: 5
```
**Note:** Eager thread may dominate because polite thread yields!

---

#### Key Difference #4: Exception Handling

**sleep() - Checked Exception:**
```java
class SleepException {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000); // MUST handle InterruptedException
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted!");
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        });
        
        t.start();
        
        try {
            Thread.sleep(500);
            t.interrupt(); // Interrupt the sleeping thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

**Output:**
```
Sleep interrupted!
```

**yield() - No Exception:**
```java
class YieldNoException {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            Thread.yield(); // No try-catch needed!
            System.out.println("Yield completed (no exception handling)");
        });
        
        t.start();
    }
}
```

**Output:**
```
Yield completed (no exception handling)
```

---

#### Key Difference #5: Use Cases

**When to Use sleep():**

```java
// ✅ Use Case 1: Polling/Waiting
class PollingExample {
    private static volatile boolean dataReady = false;
    
    public static void main(String[] args) throws InterruptedException {
        Thread consumer = new Thread(() -> {
            while (!dataReady) {
                System.out.println("Waiting for data...");
                try {
                    Thread.sleep(500); // Poll every 500ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Data is ready! Processing...");
        });
        
        consumer.start();
        
        // Producer prepares data
        Thread.sleep(2000);
        dataReady = true;
    }
}
```

**Output:**
```
Waiting for data...
Waiting for data...
Waiting for data...
Waiting for data...
Data is ready! Processing...
```

```java
// ✅ Use Case 2: Rate Limiting
class RateLimitingExample {
    public static void main(String[] args) {
        Thread apiCaller = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("API Call #" + i + " at " + System.currentTimeMillis());
                try {
                    Thread.sleep(1000); // Rate limit: 1 call per second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        apiCaller.start();
    }
}
```

**Output:**
```
API Call #1 at 1708777890123
API Call #2 at 1708777891125
API Call #3 at 1708777892127
API Call #4 at 1708777893129
API Call #5 at 1708777894131
```

**When to Use yield():**

```java
// ✅ Use Case 1: Fairness in CPU Sharing
class FairCPUSharing {
    public static void main(String[] args) throws InterruptedException {
        Thread worker1 = new Thread(() -> {
            int sum = 0;
            for (int i = 0; i < 1000; i++) {
                sum += i;
                if (i % 100 == 0) {
                    Thread.yield(); // Give others a chance
                }
            }
            System.out.println("Worker-1 sum: " + sum);
        });
        
        Thread worker2 = new Thread(() -> {
            int product = 1;
            for (int i = 1; i <= 10; i++) {
                product *= i;
                Thread.yield(); // Be cooperative
            }
            System.out.println("Worker-2 product: " + product);
        });
        
        worker1.start();
        worker2.start();
        
        worker1.join();
        worker2.join();
    }
}
```

**Output:**
```
Worker-2 product: 3628800
Worker-1 sum: 499500
```

```java
// ✅ Use Case 2: Cooperative Multitasking
class CooperativeTask {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (char c = 'A'; c <= 'E'; c++) {
                System.out.print(c + " ");
                Thread.yield(); // Let others print
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.print(i + " ");
                Thread.yield(); // Let others print
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
    }
}
```

**Output (One Possible Run):**
```
A 1 B 2 C 3 D 4 E 5
```

---

#### Performance Comparison

**sleep() Performance Cost:**
```java
class SleepPerformance {
    public static void main(String[] args) {
        long start = System.nanoTime();
        
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1); // Even 1ms sleep has overhead
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long end = System.nanoTime();
        System.out.println("10 sleeps of 1ms each took: " + 
                         (end - start) / 1_000_000 + "ms");
    }
}
```

**Output:**
```
10 sleeps of 1ms each took: 15ms
```
**Note:** Actual time > requested time due to context switching overhead!

**yield() Performance Cost:**
```java
class YieldPerformance {
    public static void main(String[] args) {
        long start = System.nanoTime();
        
        for (int i = 0; i < 10000; i++) {
            Thread.yield(); // Very lightweight
        }
        
        long end = System.nanoTime();
        System.out.println("10000 yields took: " + 
                         (end - start) / 1_000_000 + "ms");
    }
}
```

**Output:**
```
10000 yields took: 2ms
```
**Note:** Much faster than sleep()! May be ignored entirely.

---

#### Comprehensive Comparison Table

| Aspect | sleep() | yield() |
|--------|---------|---------|
| **Method Type** | `static void sleep(long millis)` | `static void yield()` |
| **Purpose** | Pause thread for specific time | Give CPU to equal/higher priority threads |
| **State Change** | ✅ RUNNABLE → TIMED_WAITING | ❌ Stays RUNNABLE |
| **Guarantee** | ✅ Always pauses for specified time | ❌ Scheduler may ignore |
| **Exception** | ✅ Throws InterruptedException | ❌ No exception |
| **Lock Release** | ❌ Keeps locks | ❌ Keeps locks |
| **Precision** | ✅ Time-based (milliseconds) | ❌ No time control |
| **CPU Usage** | ✅ Releases CPU completely | ⚠️ May get CPU back immediately |
| **Context Switch** | ✅ Always switches | ⚠️ May or may not switch |
| **Platform Dependent** | ❌ No | ✅ Yes (very!) |
| **Overhead** | ⚠️ Higher (context switch cost) | ✅ Lower (hint only) |
| **Use When** | Need timed pause, polling | Want fairness, cooperative tasks |
| **Interrupt Handling** | ✅ Can be interrupted | ❌ Cannot be interrupted |
| **Thread Priority** | ❌ Ignores priority | ✅ Respects priority (maybe) |

---

#### Interview Questions & Answers

**Q1: What's the main difference between sleep() and yield()?**

**A:** 
- `sleep()` **guarantees** the thread will pause for the specified time and moves to TIMED_WAITING state
- `yield()` is just a **hint** to the scheduler suggesting it's willing to give up CPU, but may be completely ignored. Thread stays in RUNNABLE state.

**Q2: Can we use sleep(0) and yield() interchangeably?**

**A:** No!
- `sleep(0)` causes a context switch but thread may immediately get CPU back
- `yield()` suggests giving CPU to threads of equal or higher priority only
- `sleep(0)` changes state to TIMED_WAITING (briefly), `yield()` doesn't change state

**Q3: Which is more expensive - sleep() or yield()?**

**A:** `sleep()` is more expensive:
- Requires kernel-level context switch
- State change overhead
- Timer management overhead
- `yield()` is just a hint, may have zero overhead if ignored

**Q4: When should I use sleep() vs yield()?**

**A:**

**Use sleep():**
- Need guaranteed pause time
- Implementing polling/waiting logic
- Rate limiting
- Animation/UI updates with timing
- Simulating delays in testing

**Use yield():**
- Want cooperative multitasking
- Fairness among similar-priority threads
- CPU-intensive loop that should share CPU
- Non-critical scheduling hints

**Q5: Does yield() make my program faster or slower?**

**A:** **Neither guaranteed!**
- May make it *slower* if threads keep yielding unnecessarily
- May make it *faster* by allowing other threads to progress
- May have *no effect* if scheduler ignores it
- Platform and JVM-dependent behavior

**Q6: Can sleep() or yield() release locks?**

**A:** **NO! Both methods keep all locks held by the thread.**
- If you need to release locks while waiting, use `wait()` instead
- Common misconception - neither releases synchronized locks

**Q7: Why does sleep(0) exist?**

**A:** `sleep(0)` is used to:
- Trigger context switch without actual delay
- Allow other threads to run
- Different from yield() - sleep(0) may give CPU to lower-priority threads too

---

#### Common Mistakes

**❌ Mistake 1: Using yield() for timing**
```java
// WRONG!
for (int i = 0; i < 1000; i++) {
    doWork();
    Thread.yield(); // This is NOT a delay!
}
```

**✅ Correct:**
```java
for (int i = 0; i < 1000; i++) {
    doWork();
    Thread.sleep(10); // Use sleep for timing
}
```

---

**❌ Mistake 2: Expecting yield() to always work**
```java
// WRONG! May not alternate at all!
Thread t1 = new Thread(() -> {
    for (int i = 0; i < 5; i++) {
        System.out.println("T1: " + i);
        Thread.yield(); // May be ignored!
    }
});
```

**✅ Correct - Use proper synchronization if order matters:**
```java
Object lock = new Object();
// Use wait/notify for guaranteed coordination
```

---

**❌ Mistake 3: Relying on yield() for thread safety**
```java
// WRONG! yield() doesn't provide atomicity
int counter = 0;
Thread t = new Thread(() -> {
    for (int i = 0; i < 1000; i++) {
        counter++; // Race condition!
        Thread.yield();
    }
});
```

**✅ Correct:**
```java
AtomicInteger counter = new AtomicInteger();
// Or use synchronized block
```

---

#### Best Practices

**✅ DO:**
- Use `sleep()` when you need **guaranteed timing**
- Use `yield()` for **cooperative scheduling** in CPU-intensive loops
- Handle `InterruptedException` properly with `sleep()`
- Use `Thread.sleep(0)` if you want to trigger context switch to any thread

**❌ DON'T:**
- Don't use `yield()` for timing or synchronization
- Don't rely on `yield()` behavior across platforms
- Don't use `sleep()` in tight loops (use wait/notify instead)
- Don't ignore `InterruptedException` from `sleep()`

---

#### Summary Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    sleep() vs yield()                         │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  sleep(1000):                    yield():                    │
│                                                               │
│  Thread Running                  Thread Running              │
│      ↓                              ↓                        │
│  TIMED_WAITING                   RUNNABLE                    │
│  (guaranteed 1s)                 (hint only)                 │
│      ↓                              ↓                        │
│  Wait exactly 1000ms             May or may not              │
│      ↓                           give up CPU                 │
│  RUNNABLE                            ↓                       │
│      ↓                           RUNNABLE/RUNNING            │
│  Wait for scheduler                  ↓                       │
│      ↓                           Continues execution         │
│  RUNNING                                                     │
│                                                               │
│  Use Case:                       Use Case:                   │
│  • Timed delays                  • Fair CPU sharing          │
│  • Polling                       • Cooperative tasks         │
│  • Rate limiting                 • Hints to scheduler        │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

---

**🎯 Key Takeaway:**  
- Use **sleep()** when you need **control over timing** (guaranteed pause)
- Use **yield()** when you want to be **"polite" to other threads** (hint only, no guarantee)
- **Never use yield() for timing or synchronization!**

---

### Complete Real-World Example

```java
class DataProcessor {
    public static void main(String[] args) {
        // Step 1: Download data
        Thread downloadThread = new Thread(() -> {
            System.out.println("Starting download...");
            for (int i = 0; i <= 100; i += 20) {
                System.out.println("Downloaded: " + i + "%");
                try {
                    Thread.sleep(1000); // Simulate download time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Download complete!");
        });
        
        // Step 2: Process data (must wait for download)
        Thread processThread = new Thread(() -> {
            System.out.println("Starting processing...");
            for (int i = 0; i <= 100; i += 25) {
                System.out.println("Processed: " + i + "%");
                try {
                    Thread.sleep(800);
                    Thread.yield(); // Give chance to other threads
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Processing complete!");
        });
        
        // Step 3: Upload data (must wait for processing)
        Thread uploadThread = new Thread(() -> {
            System.out.println("Starting upload...");
            for (int i = 0; i <= 100; i += 33) {
                System.out.println("Uploaded: " + i + "%");
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Upload complete!");
        });
        
        try {
            // Sequential execution using join
            downloadThread.start();
            downloadThread.join(); // Wait for download to finish
            
            processThread.start();
            processThread.join(); // Wait for processing to finish
            
            uploadThread.start();
            uploadThread.join(); // Wait for upload to finish
            
            System.out.println("All operations completed successfully!");
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

**Output:**
```
Starting download...
Downloaded: 0%
Downloaded: 20%
Downloaded: 40%
Downloaded: 60%
Downloaded: 80%
Downloaded: 100%
Download complete!
Starting processing...
Processed: 0%
Processed: 25%
Processed: 50%
Processed: 75%
Processed: 100%
Processing complete!
Starting upload...
Uploaded: 0%
Uploaded: 33%
Uploaded: 66%
Uploaded: 99%
Upload complete!
All operations completed successfully!
```
> **Note:** Operations execute sequentially due to join(). Each step waits for the previous to complete. Total time: download + process + upload times.

---

## 8. Daemon Threads

### What is a Daemon Thread?

A **daemon thread** is a low-priority thread that runs in the background to provide services to user threads. The JVM exits when only daemon threads are running.

---

### 🔑 Understanding "JVM Exits When Only Daemon Threads Are Running"

#### The Rule Explained:

**Java has two types of threads:**

1. **User Threads (Non-Daemon Threads)**
   - Regular threads created by default
   - JVM **WAITS** for these threads to complete before shutting down
   - Application stays alive as long as at least ONE user thread is running

2. **Daemon Threads**
   - Background service threads
   - JVM **DOES NOT WAIT** for these threads to complete
   - Automatically killed when all user threads finish
   - Cannot keep JVM alive on their own

#### The Key Point:

```
JVM Shutdown Logic:
┌─────────────────────────────────────────────────────────────┐
│  IF (All user threads are finished)                         │
│      THEN: JVM exits immediately                            │
│            → Kills all remaining daemon threads             │
│            → Application terminates                         │
│                                                              │
│  IF (At least 1 user thread is still running)              │
│      THEN: JVM keeps running                                │
│            → Waits for user threads                         │
│            → Daemon threads keep working                    │
└─────────────────────────────────────────────────────────────┘
```

---

### 📊 Visual Timeline Examples

#### Example 1: User Thread Keeps JVM Alive

```
Time  →  0s          1s          2s          3s          4s
         │           │           │           │           │
Main     │ Start     │ End       │           │           │
         ├───────────┤           │           │           │
         │                       │           │           │
User     │ Start ────────────────────────────────────────┤ End
Thread   │           Working     Working     Working     │
         │                                               │
Daemon   │ Start ────────────────────────────────────────┤ Alive
Thread   │           Working     Working     Working     │
         │                                               │
JVM      ├───────────────────────────────────────────────┤ RUNNING
Status   │                                               └─ EXIT!
         
Result: JVM stays alive until User Thread completes at 4s
        Daemon thread works the entire time
        Both threads finish naturally
```

#### Example 2: Daemon Thread CANNOT Keep JVM Alive

```
Time  →  0s          1s          2s          3s
         │           │           │           │
Main     │ Start     │ End       │           │
         ├───────────┤           │           │
         │                       │           │
User     │ Start ────────────────┤ End       │
Thread   │           Working     │           │
         │                       │           │
Daemon   │ Start ────────────────────────────► KILLED!
Thread   │           Working     Working (wants to continue)
         │                       │           
JVM      ├───────────────────────┤ RUNNING   
Status   │                       └─ EXIT! (Only daemon left)

Result: JVM exits at 2s when User Thread ends
        Daemon thread is killed abruptly (even though it wanted to continue)
        Daemon thread CANNOT prevent JVM shutdown
```

---

### 💡 Real Code Examples

#### Example 1: JVM Waits for User Thread

```java
class UserThreadExample {
    public static void main(String[] args) {
        System.out.println("Main starts");
        
        // User thread (non-daemon)
        Thread userThread = new Thread(() -> {
            System.out.println("User thread running...");
            try {
                Thread.sleep(5000); // 5 seconds of work
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("User thread finished!");
        });
        
        userThread.start(); // User thread by default
        
        System.out.println("Main ends");
        // But JVM WAITS! It won't exit until userThread completes
    }
}
```

**Output:**
```
Main starts
Main ends
User thread running...
(5 second pause)
User thread finished!
(NOW JVM exits)
```

**Timeline:**
- 0s: Main thread starts and ends immediately
- 0s-5s: **JVM keeps running** (waiting for user thread)
- 5s: User thread finishes
- 5s: **JVM exits**

---

#### Example 2: JVM Does NOT Wait for Daemon Thread

```java
class DaemonThreadExample {
    public static void main(String[] args) {
        System.out.println("Main starts");
        
        // Daemon thread
        Thread daemonThread = new Thread(() -> {
            System.out.println("Daemon thread running...");
            try {
                Thread.sleep(5000); // Wants to work for 5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Daemon thread finished!"); // NEVER PRINTS!
        });
        
        daemonThread.setDaemon(true); // Set as daemon
        daemonThread.start();
        
        System.out.println("Main ends");
        // JVM exits immediately! Daemon thread is KILLED!
    }
}
```

**Output:**
```
Main starts
Main ends
Daemon thread running...
(JVM exits immediately - daemon thread KILLED)
```

**Timeline:**
- 0s: Main thread starts and ends immediately
- 0s: Daemon thread starts
- 0s: **JVM exits immediately** (only daemon thread left)
- Daemon thread is killed (never finishes its 5-second sleep)

---

#### Example 3: Combined - JVM Waits for User, Kills Daemon

```java
class CombinedExample {
    public static void main(String[] args) {
        System.out.println("Main starts");
        
        // User thread - 3 seconds
        Thread userThread = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("User: " + i);
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            System.out.println("User finished!");
        });
        
        // Daemon thread - wants 10 seconds but will be killed
        Thread daemonThread = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                System.out.println("  Daemon: " + i);
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            System.out.println("  Daemon finished!"); // NEVER PRINTS!
        });
        
        daemonThread.setDaemon(true);
        
        userThread.start();
        daemonThread.start();
        
        System.out.println("Main ends");
    }
}
```

**Output:**
```
Main starts
Main ends
User: 1
  Daemon: 1
User: 2
  Daemon: 2
User: 3
  Daemon: 3
User finished!
(JVM exits - daemon thread killed at iteration 3)
```

**Timeline:**
- 0s: Main ends, but JVM keeps running (user thread alive)
- 1s: User=1, Daemon=1
- 2s: User=2, Daemon=2
- 3s: User=3, Daemon=3, User finishes
- 3s: **Only daemon thread left → JVM exits!**
- Daemon wanted to run until 10, but got killed at 3

---

### 🎯 Key Takeaways

| Aspect | User Thread | Daemon Thread |
|--------|------------|---------------|
| **JVM Waits?** | ✅ YES - JVM waits for completion | ❌ NO - JVM kills it |
| **Can Keep JVM Alive?** | ✅ YES | ❌ NO |
| **Completion Guaranteed?** | ✅ YES - Always finishes | ❌ NO - May be killed abruptly |
| **Use Case** | Main application logic | Background services |
| **Examples** | API requests, data processing | Garbage collection, auto-save, monitoring |

---

### 🚨 Common Pitfall

```java
// ❌ DANGER: Daemon thread might not finish critical work!
class DangerousExample {
    public static void main(String[] args) {
        Thread saveThread = new Thread(() -> {
            System.out.println("Saving important data to database...");
            // Critical database save operation
            try {
                Thread.sleep(2000); // Simulate save
            } catch (InterruptedException e) {}
            System.out.println("Data saved!"); // Might NEVER print!
        });
        
        saveThread.setDaemon(true); // ❌ BAD! Don't use daemon for critical work
        saveThread.start();
        
        // Main ends immediately → JVM exits → Data NOT saved!
    }
}
```

**Solution:** Use user thread for critical operations:
```java
// ✅ SAFE: User thread ensures data is saved
saveThread.setDaemon(false); // or don't set at all (default is false)
saveThread.start();
saveThread.join(); // Wait for save to complete
```

---

### Characteristics

✅ **Background service threads**  
✅ **JVM doesn't wait for them to complete**  
✅ **Automatically terminated when all user threads finish**  
✅ **Cannot prevent JVM shutdown**  
✅ **Examples: Garbage Collector, Finalizer**

### Visual Representation

```
User Thread         Daemon Thread
    │                   │
    │ Working           │ Working
    │                   │
    │ Working           │ Working
    │                   │
    │ Completes         │ Working
    │                   │
  [EXIT]             [KILLED]
         JVM Shutdown
```

### Creating Daemon Thread

```java
class DaemonDemo {
    public static void main(String[] args) {
        Thread userThread = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("User Thread: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("User Thread completed!");
        });
        
        Thread daemonThread = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                System.out.println("Daemon Thread: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Daemon Thread completed!"); // May not print
        });
        
        // Set as daemon BEFORE starting
        daemonThread.setDaemon(true);
        
        // Check if daemon
        System.out.println("Is userThread daemon? " + userThread.isDaemon());     // false
        System.out.println("Is daemonThread daemon? " + daemonThread.isDaemon()); // true
        
        userThread.start();
        daemonThread.start();
        
        System.out.println("Main thread ending...");
        // JVM will exit when userThread completes
        // daemonThread will be killed abruptly
    }
}
```

**Output:**
```
Is userThread daemon? false
Is daemonThread daemon? true
Main thread ending...
User Thread: 1
Daemon Thread: 1
User Thread: 2
Daemon Thread: 2
User Thread: 3
Daemon Thread: 3
User Thread completed!
[JVM exits - Daemon thread killed]
```

### Important Rules

```java
class DaemonRules {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            System.out.println("Thread running");
        });
        
        // ✅ CORRECT: Set daemon before start()
        t.setDaemon(true);
        t.start();
        
        Thread t2 = new Thread(() -> {
            System.out.println("Thread2 running");
        });
        
        t2.start();
        // ❌ WRONG: Cannot set daemon after start()
        // t2.setDaemon(true); // IllegalThreadStateException
    }
}
```

### Daemon Thread Inheritance

```java
class DaemonInheritanceDemo {
    public static void main(String[] args) {
        Thread parent = new Thread(() -> {
            System.out.println("Parent is daemon: " + Thread.currentThread().isDaemon());
            
            // Child thread inherits daemon status
            Thread child = new Thread(() -> {
                System.out.println("Child is daemon: " + Thread.currentThread().isDaemon());
            });
            
            child.start();
        });
        
        parent.setDaemon(true);
        parent.start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

**Output:**
```
Parent is daemon: true
Child is daemon: true
```

### Real-World Example: Auto-Save Feature

```java
class AutoSaveDemo {
    private static volatile String document = "";
    
    public static void main(String[] args) throws InterruptedException {
        // Daemon thread for auto-save
        Thread autoSaveThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); // Auto-save every 5 seconds
                    if (!document.isEmpty()) {
                        System.out.println("Auto-saving document: " + document);
                        // Save logic here
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        
        autoSaveThread.setDaemon(true); // Set as daemon
        autoSaveThread.start();
        
        // User thread simulating user typing
        Thread userThread = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                document += "Line " + i + ". ";
                System.out.println("User typed: Line " + i);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("User finished typing.");
        });
        
        userThread.start();
        userThread.join();
        
        System.out.println("Application closing...");
        // Auto-save thread will be killed when user thread completes
    }
}
```

**Output:**
```
User typed: Line 1
User typed: Line 2
Auto-saving document: Line 1. Line 2. 
User typed: Line 3
User typed: Line 4
Auto-saving document: Line 1. Line 2. Line 3. Line 4. 
User typed: Line 5
User finished typing.
Application closing...
```
> **Note:** Auto-save daemon thread runs every 5 seconds but is killed when the user thread completes, even if it's in the middle of execution.

---

### Common Daemon Thread Examples

| Daemon Thread | Purpose |
|---------------|---------|
| **Garbage Collector** | Automatic memory management |
| **Finalizer** | Cleanup before object destruction |
| **Signal Dispatcher** | Handle OS signals |
| **Reference Handler** | Manage weak/soft/phantom references |
| **Auto-Save** | Periodic document saving |
| **Log Writer** | Background log writing |
| **Health Monitor** | System health checks |

---

## 9. Thread Naming

### Default Thread Names

```java
class DefaultNamesDemo {
    public static void main(String[] args) {
        System.out.println("Main thread: " + Thread.currentThread().getName()); // main
        
        Thread t1 = new Thread(() -> {
            System.out.println("Thread 1: " + Thread.currentThread().getName()); // Thread-0
        });
        
        Thread t2 = new Thread(() -> {
            System.out.println("Thread 2: " + Thread.currentThread().getName()); // Thread-1
        });
        
        Thread t3 = new Thread(() -> {
            System.out.println("Thread 3: " + Thread.currentThread().getName()); // Thread-2
        });
        
        t1.start();
        t2.start();
        t3.start();
    }
}
```

**Output:**
```
Main thread: main
Thread 1: Thread-0
Thread 2: Thread-1
Thread 3: Thread-2
```
> **Note:** Default naming pattern is "Thread-N" where N starts from 0. Main thread is always named "main".

---

### Setting Thread Names

```java
class NamingDemo {
    public static void main(String[] args) {
        // Method 1: Using setName()
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " is running");
        });
        t1.setName("Worker-1");
        
        // Method 2: Using constructor
        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " is running");
        }, "Worker-2");
        
        // Method 3: Using constructor with Runnable
        Runnable task = () -> {
            System.out.println(Thread.currentThread().getName() + " is running");
        };
        Thread t3 = new Thread(task, "Worker-3");
        
        t1.start();
        t2.start();
        t3.start();
    }
}
```

**Output:**
```
Worker-1 is running
Worker-2 is running
Worker-3 is running
```
> **Note:** Custom names make debugging and log analysis much easier than default Thread-0, Thread-1, etc.

---

### Getting Thread Information

```java
class ThreadInfoDemo {
    public static void main(String[] args) {
        Thread currentThread = Thread.currentThread();
        
        System.out.println("Thread Name: " + currentThread.getName());
        System.out.println("Thread ID: " + currentThread.getId());
        System.out.println("Thread Priority: " + currentThread.getPriority());
        System.out.println("Thread State: " + currentThread.getState());
        System.out.println("Is Alive: " + currentThread.isAlive());
        System.out.println("Is Daemon: " + currentThread.isDaemon());
        System.out.println("Thread Group: " + currentThread.getThreadGroup().getName());
    }
}
```

**Output:**
```
Thread Name: main
Thread ID: 1
Thread Priority: 5
Thread State: RUNNABLE
Is Alive: true
Is Daemon: false
Thread Group: main
```
> **Note:** Main thread has ID 1, priority 5 (NORM_PRIORITY), and belongs to "main" thread group.

---

### Real-World Example: Named Worker Threads

```java
class TaskProcessor {
    public static void main(String[] args) {
        // Database operations
        Thread dbThread1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " - Inserting records...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Insert complete");
        }, "DB-Insert-Worker");
        
        Thread dbThread2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " - Fetching records...");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Fetch complete");
        }, "DB-Fetch-Worker");
        
        // Network operations
        Thread networkThread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " - Sending data...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Send complete");
        }, "Network-Worker");
        
        // File operations
        Thread fileThread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " - Writing file...");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Write complete");
        }, "File-IO-Worker");
        
        dbThread1.start();
        dbThread2.start();
        networkThread.start();
        fileThread.start();
    }
}
```

**Output:**
```
DB-Insert-Worker - Inserting records...
DB-Fetch-Worker - Fetching records...
Network-Worker - Sending data...
File-IO-Worker - Writing file...
Network-Worker - Send complete
DB-Fetch-Worker - Fetch complete
DB-Insert-Worker - Insert complete
File-IO-Worker - Write complete
```
> **Note:** Named threads make logs readable and debugging much easier. Operations complete in order: Network (1s), Fetch (1.5s), Insert (2s), File (2.5s).

---

### Thread Naming Best Practices

✅ **Use descriptive names** - Helps in debugging  
✅ **Follow naming convention** - Component-Purpose-Number  
✅ **Include counter/ID** - For multiple similar threads  
✅ **Set name before start()** - More predictable  
✅ **Use thread pools naming** - ExecutorService allows custom naming  

```java
class BestPracticeNaming {
    public static void main(String[] args) {
        // Good naming conventions
        Thread emailSender = new Thread(() -> {
            // Email sending logic
        }, "Email-Sender-Thread-1");
        
        Thread reportGenerator = new Thread(() -> {
            // Report generation logic
        }, "Report-Generator-Thread-1");
        
        Thread cacheUpdater = new Thread(() -> {
            // Cache update logic
        }, "Cache-Updater-Thread-1");
        
        emailSender.start();
        reportGenerator.start();
        cacheUpdater.start();
    }
}
```

---

## 10. Thread Groups

### What is a Thread Group?

A **ThreadGroup** represents a set of threads and can also include other thread groups, forming a tree structure. It's a legacy mechanism from Java 1.0 for organizing and managing threads collectively.

---

### ⚠️ IMPORTANT: ThreadGroup Status in JDK 17+

#### Deprecation and Modern Status

**ThreadGroup has been largely deprecated and discouraged in modern Java:**

| Aspect | Status in JDK 17+ |
|--------|-------------------|
| **API Status** | **Deprecated (but not removed)** |
| **Recommendation** | ❌ **NOT recommended for new code** |
| **Why?** | Design flaws, better alternatives exist |
| **Removal Plans** | Considered for removal in future releases |
| **JEP Reference** | [JEP 425](https://openjdk.org/jeps/425) discusses Virtual Threads (replacement) |

#### Why ThreadGroup is Discouraged in JDK 17+

```java
/*
 * ThreadGroup Problems:
 * 
 * 1. Race Conditions - Methods like enumerate() are not thread-safe
 * 2. Ineffective Control - Cannot truly control thread lifecycle
 * 3. Poor Encapsulation - Violates object-oriented principles
 * 4. Limited Usefulness - Most methods deprecated or ineffective
 * 5. Better Alternatives - ExecutorService, Thread pools, Virtual Threads
 */
```

---

### 🔴 Deprecated Methods in JDK 17+

Many ThreadGroup methods are **deprecated for removal**:

```java
@Deprecated(since="16", forRemoval=true)
public void stop()

@Deprecated(since="16", forRemoval=true)
public void suspend()

@Deprecated(since="16", forRemoval=true)
public void resume()

@Deprecated(since="1.2")
public boolean allowThreadSuspension(boolean b)
```

**Warning:** Using these methods will generate compiler warnings and may cause `UnsupportedOperationException` at runtime.

---

### Visual Representation

```
┌────────────────────────────────────────────────────────────┐
│                    THREAD GROUP HIERARCHY                   │
└────────────────────────────────────────────────────────────┘

                    System ThreadGroup
                           │
                ┌──────────┴──────────┐
                │                     │
           main ThreadGroup      system ThreadGroup
                │                     │
    ┌───────────┼───────────┐         ├── Reference Handler
    │           │           │         ├── Finalizer
Thread-0    Thread-1   Custom Group   └── Signal Dispatcher
                           │
                    ┌──────┴──────┐
                    │             │
               Worker-1      Worker-2
```

---

### Basic Thread Group Creation (Legacy)

```java
class ThreadGroupBasics {
    public static void main(String[] args) {
        // Get current thread group
        ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();
        System.out.println("Main group: " + mainGroup.getName());
        System.out.println("Parent: " + mainGroup.getParent().getName());
        
        // Create custom thread group
        ThreadGroup workerGroup = new ThreadGroup("Worker-Group");
        
        // Create threads in the group
        Thread t1 = new Thread(workerGroup, () -> {
            System.out.println(Thread.currentThread().getName() + 
                               " in group: " + Thread.currentThread().getThreadGroup().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Worker-1");
        
        Thread t2 = new Thread(workerGroup, () -> {
            System.out.println(Thread.currentThread().getName() + 
                               " in group: " + Thread.currentThread().getThreadGroup().getName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Worker-2");
        
        t1.start();
        t2.start();
        
        // Get thread count in group
        System.out.println("Active threads in workerGroup: " + workerGroup.activeCount());
        
        // List all threads in group
        System.out.println("\nThreads in group:");
        workerGroup.list();
    }
}
```

**Output:**
```
Main group: main
Parent: system
Worker-1 in group: Worker-Group
Worker-2 in group: Worker-Group
Active threads in workerGroup: 2

Threads in group:
java.lang.ThreadGroup[name=Worker-Group,maxpri=10]
    Thread[Worker-1,5,Worker-Group]
    Thread[Worker-2,5,Worker-Group]
```

---

### Thread Group Methods (Legacy API)

```java
class ThreadGroupMethods {
    public static void main(String[] args) throws InterruptedException {
        ThreadGroup group = new ThreadGroup("MyGroup");
        
        Thread t1 = new Thread(group, () -> {
            try {
                Thread.sleep(3000);
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Thread-1");
        
        Thread t2 = new Thread(group, () -> {
            try {
                Thread.sleep(3000);
                System.out.println(Thread.currentThread().getName() + " finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Thread-2");
        
        t1.start();
        t2.start();
        
        // Group information
        System.out.println("Group name: " + group.getName());
        System.out.println("Active count: " + group.activeCount());
        System.out.println("Max priority: " + group.getMaxPriority());
        System.out.println("Is daemon: " + group.isDaemon());
        System.out.println("Is destroyed: " + group.isDestroyed());
        System.out.println("Parent: " + group.getParent().getName());
        
        // List all threads
        System.out.println("\nThread list:");
        group.list();
        
        // Enumerate threads (better than list())
        Thread[] threads = new Thread[group.activeCount()];
        int count = group.enumerate(threads);
        System.out.println("\nEnumerated threads:");
        for (int i = 0; i < count; i++) {
            System.out.println("  - " + threads[i].getName() + " [State: " + threads[i].getState() + "]");
        }
        
        // Wait for threads to complete
        t1.join();
        t2.join();
        
        System.out.println("\nAfter completion:");
        System.out.println("Active count: " + group.activeCount());
    }
}
```

**Output:**
```
Group name: MyGroup
Active count: 2
Max priority: 10
Is daemon: false
Is destroyed: false
Parent: main

Thread list:
java.lang.ThreadGroup[name=MyGroup,maxpri=10]
    Thread[Thread-1,5,MyGroup]
    Thread[Thread-2,5,MyGroup]

Enumerated threads:
  - Thread-1 [State: TIMED_WAITING]
  - Thread-2 [State: TIMED_WAITING]

Thread-1 finished
Thread-2 finished

After completion:
Active count: 0
```

---

### ThreadGroup Methods Reference

| Method | Description | Status in JDK 17+ |
|--------|-------------|-------------------|
| `getName()` | Get group name | ✅ Active |
| `getParent()` | Get parent group | ✅ Active |
| `activeCount()` | Number of active threads | ✅ Active (but inaccurate) |
| `enumerate(Thread[])` | Copy active threads to array | ✅ Active (race conditions) |
| `list()` | Print thread info to System.out | ✅ Active (debug only) |
| `getMaxPriority()` | Get max priority | ✅ Active |
| `setMaxPriority()` | Set max priority | ✅ Active |
| `isDaemon()` | Check if daemon group | ✅ Active |
| `setDaemon()` | Set daemon status | ✅ Active |
| `interrupt()` | Interrupt all threads | ✅ Active |
| `destroy()` | Destroy empty group | ✅ Active |
| `stop()` | Stop all threads | 🔴 **Deprecated, removed** |
| `suspend()` | Suspend all threads | 🔴 **Deprecated, removed** |
| `resume()` | Resume all threads | 🔴 **Deprecated, removed** |

---

### Problems with ThreadGroup

#### Problem 1: Race Conditions

```java
class RaceConditionProblem {
    public static void main(String[] args) {
        ThreadGroup group = new ThreadGroup("Test");
        
        // Problem: activeCount() is not atomic with enumerate()
        Thread[] threads = new Thread[group.activeCount()]; // ❌ May be wrong!
        
        // By the time enumerate() is called, count might have changed
        int count = group.enumerate(threads);
        
        // Threads array might be too small or have nulls!
        for (Thread t : threads) {
            if (t != null) { // Need null check!
                System.out.println(t.getName());
            }
        }
    }
}
```

**Solution:** Use proper synchronization or better alternatives (ExecutorService).

#### Problem 2: Ineffective Control

```java
class InefficientControl {
    public static void main(String[] args) {
        ThreadGroup group = new ThreadGroup("Workers");
        
        // Create threads
        for (int i = 1; i <= 5; i++) {
            new Thread(group, () -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName() + " working...");
                    } catch (InterruptedException e) {
                        break; // Proper way to stop
                    }
                }
            }, "Worker-" + i).start();
        }
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
        
        // ❌ BAD: This doesn't actually stop threads!
        // group.stop(); // Deprecated and doesn't work
        
        // ✅ CORRECT: Interrupt all threads
        group.interrupt(); // This only sets interrupt flag
        
        // Still need to handle interruption properly in thread code
    }
}
```

#### Problem 3: No Automatic Cleanup

```java
class NoCleanupProblem {
    public static void main(String[] args) {
        ThreadGroup group = new ThreadGroup("Temp");
        
        Thread t = new Thread(group, () -> {
            System.out.println("Task completed");
        });
        
        t.start();
        
        try {
            t.join();
        } catch (InterruptedException e) {}
        
        // Group still exists even after all threads are done!
        System.out.println("Active count: " + group.activeCount()); // 0
        
        // Must manually destroy (and can only do so if empty)
        if (group.activeCount() == 0) {
            group.destroy();
        }
    }
}
```

---

### 🚀 Modern Alternatives to ThreadGroup (JDK 17+)

#### Alternative 1: ExecutorService (Best Practice)

```java
import java.util.concurrent.*;
import java.util.*;

class ExecutorServiceAlternative {
    public static void main(String[] args) throws InterruptedException {
        // ✅ MODERN: Use ExecutorService instead of ThreadGroup
        
        // Create named thread pool
        ThreadFactory factory = new ThreadFactory() {
            private int counter = 0;
            
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Worker-" + (++counter));
            }
        };
        
        ExecutorService executor = Executors.newFixedThreadPool(3, factory);
        
        // Submit tasks
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            Future<String> future = executor.submit(() -> {
                Thread.sleep(1000);
                return "Task " + taskId + " by " + Thread.currentThread().getName();
            });
            futures.add(future);
        }
        
        // Get results
        for (Future<String> future : futures) {
            try {
                System.out.println(future.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        // Proper shutdown
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("All tasks completed!");
    }
}
```

**Output:**
```
Task 1 by Worker-1
Task 2 by Worker-2
Task 3 by Worker-3
Task 4 by Worker-1
Task 5 by Worker-2
All tasks completed!
```

**Benefits over ThreadGroup:**
- ✅ Proper lifecycle management
- ✅ Task queuing and scheduling
- ✅ Return values from tasks
- ✅ Exception handling
- ✅ Graceful shutdown
- ✅ Thread reuse

---

#### Alternative 2: Virtual Threads (JDK 19+)

```java
// JDK 19+ with Virtual Threads (Preview in 19, Standard in 21)
class VirtualThreadsAlternative {
    public static void main(String[] args) throws InterruptedException {
        // ✅ FUTURE: Virtual Threads - Lightweight, millions of threads possible
        
        List<Thread> threads = new ArrayList<>();
        
        // Create 10,000 virtual threads (not possible with ThreadGroup!)
        for (int i = 1; i <= 10_000; i++) {
            final int taskId = i;
            Thread vt = Thread.ofVirtual().name("VT-" + taskId).start(() -> {
                try {
                    Thread.sleep(1000);
                    if (taskId % 1000 == 0) {
                        System.out.println("Task " + taskId + " by " + 
                                           Thread.currentThread().getName());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(vt);
        }
        
        // Wait for all to complete
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("All 10,000 virtual threads completed!");
    }
}
```

**Benefits over ThreadGroup:**
- ✅ Millions of threads possible (lightweight)
- ✅ No OS thread overhead
- ✅ Better for I/O-bound tasks
- ✅ Structured concurrency support

---

#### Alternative 3: Thread Pool with Monitoring

```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class MonitoredThreadPool {
    public static void main(String[] args) throws InterruptedException {
        // ✅ MODERN: Custom thread pool with monitoring
        
        AtomicInteger counter = new AtomicInteger(0);
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            3,                              // core pool size
            5,                              // max pool size
            60L,                            // keep alive time
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("Worker-" + counter.incrementAndGet());
                    t.setUncaughtExceptionHandler((thread, throwable) -> {
                        System.err.println("Exception in " + thread.getName() + 
                                           ": " + throwable.getMessage());
                    });
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // Submit tasks
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                System.out.println("Task " + taskId + " executing by " + 
                                   Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // Monitor thread pool
        System.out.println("\nThread Pool Stats:");
        System.out.println("Active threads: " + executor.getActiveCount());
        System.out.println("Pool size: " + executor.getPoolSize());
        System.out.println("Queue size: " + executor.getQueue().size());
        System.out.println("Completed tasks: " + executor.getCompletedTaskCount());
        
        // Shutdown
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("\nFinal Stats:");
        System.out.println("Completed tasks: " + executor.getCompletedTaskCount());
    }
}
```

**Output:**
```
Task 1 executing by Worker-1
Task 2 executing by Worker-2
Task 3 executing by Worker-3

Thread Pool Stats:
Active threads: 3
Pool size: 3
Queue size: 7
Completed tasks: 0

Task 4 executing by Worker-1
Task 5 executing by Worker-2
...
Final Stats:
Completed tasks: 10
```

---

#### Alternative 4: Structured Concurrency (JDK 19+)

```java
// JDK 19+ with Structured Concurrency (Preview)
import java.util.concurrent.*;

class StructuredConcurrencyExample {
    
    record Result(String user, String order) {}
    
    public static void main(String[] args) throws Exception {
        Result result = processOrder("user123", "order456");
        System.out.println("User: " + result.user);
        System.out.println("Order: " + result.order);
    }
    
    static Result processOrder(String userId, String orderId) throws Exception {
        // ✅ MODERN: Structured Concurrency ensures all tasks complete together
        
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            // Fork tasks (similar to ThreadGroup but better)
            Future<String> userTask = scope.fork(() -> {
                Thread.sleep(1000);
                return "User: " + userId;
            });
            
            Future<String> orderTask = scope.fork(() -> {
                Thread.sleep(1000);
                return "Order: " + orderId;
            });
            
            // Wait for all or fail fast
            scope.join();           // Wait for all
            scope.throwIfFailed();  // Propagate exceptions
            
            return new Result(userTask.resultNow(), orderTask.resultNow());
        }
        // Automatic cleanup - all threads stopped when leaving scope
    }
}
```

**Benefits:**
- ✅ Automatic cleanup (no leaked threads)
- ✅ Fail-fast behavior
- ✅ Easier error handling
- ✅ Better structured code

---

### 📊 Comparison: ThreadGroup vs Modern Alternatives

| Feature | ThreadGroup | ExecutorService | Virtual Threads | Structured Concurrency |
|---------|-------------|-----------------|-----------------|------------------------|
| **JDK Version** | 1.0 | 5+ | 19+ | 19+ |
| **Status** | Deprecated | ✅ Recommended | ✅ Modern | ✅ Future |
| **Thread Management** | Manual | Automatic | Automatic | Automatic |
| **Resource Control** | Poor | Excellent | Excellent | Excellent |
| **Lifecycle Management** | Manual | Built-in | Built-in | Built-in |
| **Exception Handling** | Poor | Good | Good | Excellent |
| **Scalability** | Low | Medium | Very High | Very High |
| **Task Scheduling** | None | Yes | Limited | Yes |
| **Return Values** | No | Yes | Yes | Yes |
| **Cancellation** | Poor | Good | Good | Excellent |
| **Monitoring** | Basic | Advanced | Advanced | Advanced |
| **Best For** | ❌ Nothing | General purpose | I/O-bound | Structured tasks |

---

### 🎯 When to Use What (JDK 17+)

```java
// ❌ NEVER use ThreadGroup
ThreadGroup group = new ThreadGroup("workers"); // Don't do this!

// ✅ Use ExecutorService for most cases
ExecutorService executor = Executors.newFixedThreadPool(10);

// ✅ Use Virtual Threads for I/O-bound work (JDK 19+)
Thread.ofVirtual().start(() -> { /* I/O task */ });

// ✅ Use Structured Concurrency for scoped parallel tasks (JDK 19+)
try (var scope = new StructuredTaskScope<String>()) {
    // Scoped tasks
}

// ✅ Use CompletableFuture for async composition
CompletableFuture.supplyAsync(() -> "data")
                 .thenApply(data -> process(data))
                 .thenAccept(System.out::println);
```

---

### Real-World Migration Example

#### Before (ThreadGroup - Deprecated):

```java
// ❌ OLD WAY with ThreadGroup
class OldWay {
    public static void main(String[] args) {
        ThreadGroup workers = new ThreadGroup("Workers");
        
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            new Thread(workers, () -> {
                System.out.println("Processing task " + id);
            }).start();
        }
        
        // Poor control, no proper shutdown
        workers.interrupt(); // Hope this works!
    }
}
```

#### After (ExecutorService - Modern):

```java
// ✅ MODERN WAY with ExecutorService
class ModernWay {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int id = i;
            Future<?> future = executor.submit(() -> {
                System.out.println("Processing task " + id);
            });
            futures.add(future);
        }
        
        // Proper shutdown
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
```

---

### Key Takeaways for JDK 17+

1. ⚠️ **ThreadGroup is deprecated** - Don't use it in new code
2. ✅ **Use ExecutorService** - Best practice for thread management
3. 🚀 **Virtual Threads (JDK 19+)** - Future of lightweight concurrency
4. 🔄 **Structured Concurrency (JDK 19+)** - For scoped parallel tasks
5. 📝 **Migration needed** - Update legacy ThreadGroup code

---

### Interview Questions on ThreadGroup

**Q1: Should we use ThreadGroup in modern Java?**
- ❌ **No.** It's deprecated and has design flaws. Use ExecutorService instead.

**Q2: What are the problems with ThreadGroup?**
- Race conditions in enumerate()
- Poor lifecycle control
- Deprecated methods (stop, suspend, resume)
- Better alternatives exist

**Q3: What replaced ThreadGroup?**
- ExecutorService (JDK 5+)
- Virtual Threads (JDK 19+)
- Structured Concurrency (JDK 19+)

**Q4: Can ThreadGroup be removed from Java?**
- Yes, it's being considered for removal in future JDK releases as part of legacy cleanup.

**Q5: What should I do with existing ThreadGroup code?**
- Migrate to ExecutorService
- Use proper shutdown mechanisms
- Add exception handling
- Consider Virtual Threads for I/O-bound work

---

# Part 3: Synchronization

## 11. What is Synchronization?

### The Problem: Race Condition

When multiple threads access shared resources simultaneously, it can lead to **data inconsistency**.

### Visual Representation

```
Without Synchronization:

Thread 1                Thread 2
  │                       │
  ├─ Read balance (100)   │
  │                       ├─ Read balance (100)
  ├─ Add 50               │
  │                       ├─ Add 30
  ├─ Write balance (150)  │
  │                       ├─ Write balance (130)  ❌ WRONG!
  │                       │
Expected: 180            Got: 130


With Synchronization:

Thread 1                Thread 2
  │                       │
  ├─ LOCK ────────────┐   │
  ├─ Read (100)       │   │
  ├─ Add 50           │   │ (Waiting...)
  ├─ Write (150)      │   │
  ├─ UNLOCK ──────────┘   │
  │                       ├─ LOCK ────────────┐
  │                       ├─ Read (150)       │
  │                       ├─ Add 30           │
  │                       ├─ Write (180)      │
  │                       ├─ UNLOCK ──────────┘
                          │
Result: 180 ✅ CORRECT!
```

### Example Without Synchronization (Race Condition)

```java
class Counter {
    private int count = 0;
    
    public void increment() {
        count++; // NOT atomic! (read, increment, write)
    }
    
    public int getCount() {
        return count;
    }
}

class RaceConditionDemo {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        
        // Create 1000 threads, each incrementing counter
        Thread[] threads = new Thread[1000];
        
        for (int i = 0; i < 1000; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 100000");
        System.out.println("Actual: " + counter.getCount()); // Will be less than 100000!
    }
}
```

**Output:**
```
Expected: 100000
Actual: 97834    ❌ Data inconsistency!
```

### Solution: Synchronization

```java
class SynchronizedCounter {
    private int count = 0;
    
    // Synchronized method
    public synchronized void increment() {
        count++; // Now thread-safe!
    }
    
    public int getCount() {
        return count;
    }
}

class SynchronizedDemo {
    public static void main(String[] args) throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        
        Thread[] threads = new Thread[1000];
        
        for (int i = 0; i < 1000; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 100000");
        System.out.println("Actual: " + counter.getCount()); // Will be exactly 100000!
    }
}
```

**Output:**
```
Expected: 100000
Actual: 100000   ✅ Correct!
```

### Key Concepts

**Synchronized Block/Method:**
- Only one thread can execute synchronized code at a time
- Other threads must wait (BLOCKED state)
- Uses intrinsic lock (monitor lock) of the object
- Prevents race conditions

**Lock Mechanism:**
```
Object Lock
    │
    ├─ Thread 1 (Holding lock) ✅ Executing
    │
    └─ Waiting Queue
        ├─ Thread 2 (Waiting) 🔴
        ├─ Thread 3 (Waiting) 🔴
        └─ Thread 4 (Waiting) 🔴
```

---

## 12. Synchronized Method

### Syntax

```java
// Instance method synchronization
public synchronized void methodName() {
    // Thread-safe code
}

// Static method synchronization
public static synchronized void methodName() {
    // Thread-safe code
}
```

### How It Works

When a thread invokes a synchronized method:
1. **Acquires lock** on the object (or class for static)
2. **Executes** the method
3. **Releases lock** when method completes

### Example: Bank Account

```java
class BankAccount {
    private int balance = 1000;
    
    // Synchronized method
    public synchronized void withdraw(int amount) {
        System.out.println(Thread.currentThread().getName() + " attempting to withdraw " + amount);
        
        if (balance >= amount) {
            System.out.println(Thread.currentThread().getName() + " checking balance: " + balance);
            
            try {
                Thread.sleep(100); // Simulate processing time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            balance -= amount;
            System.out.println(Thread.currentThread().getName() + " completed. New balance: " + balance);
        } else {
            System.out.println(Thread.currentThread().getName() + " insufficient balance!");
        }
    }
    
    public synchronized int getBalance() {
        return balance;
    }
}

class BankDemo {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();
        
        // Multiple threads trying to withdraw simultaneously
        Thread t1 = new Thread(() -> account.withdraw(600), "Thread-1");
        Thread t2 = new Thread(() -> account.withdraw(600), "Thread-2");
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        System.out.println("Final balance: " + account.getBalance());
    }
}
```

**Output:**
```
Thread-1 attempting to withdraw 600
Thread-1 checking balance: 1000
Thread-1 completed. New balance: 400
Thread-2 attempting to withdraw 600
Thread-2 insufficient balance!
Final balance: 400
```

### Multiple Synchronized Methods

```java
class SharedResource {
    private int value = 0;
    
    public synchronized void increment() {
        value++;
        System.out.println(Thread.currentThread().getName() + " incremented: " + value);
    }
    
    public synchronized void decrement() {
        value--;
        System.out.println(Thread.currentThread().getName() + " decremented: " + value);
    }
    
    public synchronized int getValue() {
        return value;
    }
}

class MultiMethodDemo {
    public static void main(String[] args) {
        SharedResource resource = new SharedResource();
        
        // Thread calling increment
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                resource.increment();
            }
        }, "Incrementer");
        
        // Thread calling decrement
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                resource.decrement();
            }
        }, "Decrementer");
        
        t1.start();
        t2.start();
    }
}
```

### Important Points

✅ **Lock is on object** - Same object's synchronized methods share lock  
✅ **Only one at a time** - Only one thread can execute ANY synchronized method  
✅ **Other threads block** - Wait until lock is released  
✅ **Non-synchronized methods** - Can be called simultaneously  
✅ **Reentr ant** - Same thread can acquire lock multiple times  

### Synchronized vs Non-Synchronized

```java
class MixedMethods {
    public synchronized void synchronizedMethod() {
        System.out.println(Thread.currentThread().getName() + " in synchronized method");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void nonSynchronizedMethod() {
        System.out.println(Thread.currentThread().getName() + " in non-synchronized method");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MixedDemo {
    public static void main(String[] args) {
        MixedMethods obj = new MixedMethods();
        
        Thread t1 = new Thread(() -> obj.synchronizedMethod(), "Thread-1");
        Thread t2 = new Thread(() -> obj.synchronizedMethod(), "Thread-2");
        Thread t3 = new Thread(() -> obj.nonSynchronizedMethod(), "Thread-3");
        Thread t4 = new Thread(() -> obj.nonSynchronizedMethod(), "Thread-4");
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
```

**Output:**
```
Thread-1 in synchronized method
Thread-3 in non-synchronized method
Thread-4 in non-synchronized method
(Thread-2 waits for Thread-1 to release lock)
Thread-2 in synchronized method
```

---

## 13. Synchronized Block

### Why Synchronized Block?

Synchronized methods lock the entire method. Synchronized blocks allow **fine-grained control** - lock only the critical section.

### Syntax

```java
synchronized(object) {
    // Critical section - only one thread at a time
}
```

### Example: Synchronized Method vs Block

```java
class SynchronizedComparison {
    private int count = 0;
    
    // Method 1: Synchronized method (locks entire method)
    public synchronized void incrementMethod() {
        System.out.println("Non-critical section 1");
        count++; // Critical section
        System.out.println("Non-critical section 2");
    }
    
    // Method 2: Synchronized block (locks only critical section)
    public void incrementBlock() {
        System.out.println("Non-critical section 1"); // Not locked
        
        synchronized(this) {
            count++; // Only this is locked
        }
        
        System.out.println("Non-critical section 2"); // Not locked
    }
    
    public int getCount() {
        return count;
    }
}
```

### Lock on Different Objects

```java
class MultiLockDemo {
    private Object lock1 = new Object();
    private Object lock2 = new Object();
    
    private int resource1 = 0;
    private int resource2 = 0;
    
    public void updateResource1() {
        synchronized(lock1) {
            resource1++;
            System.out.println(Thread.currentThread().getName() + " updated resource1: " + resource1);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void updateResource2() {
        synchronized(lock2) {
            resource2++;
            System.out.println(Thread.currentThread().getName() + " updated resource2: " + resource2);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MultiLockTest {
    public static void main(String[] args) {
        MultiLockDemo demo = new MultiLockDemo();
        
        // These can run in parallel (different locks)
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                demo.updateResource1();
            }
        }, "Thread-1");
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                demo.updateResource2();
            }
        }, "Thread-2");
        
        t1.start();
        t2.start();
    }
}
```

### Synchronized Block on Class

```java
class ClassLockDemo {
    private static int staticCounter = 0;
    private int instanceCounter = 0;
    
    // Lock on class object
    public void incrementStatic() {
        synchronized(ClassLockDemo.class) {
            staticCounter++;
            System.out.println(Thread.currentThread().getName() + " - Static: " + staticCounter);
        }
    }
    
    // Lock on instance object
    public void incrementInstance() {
        synchronized(this) {
            instanceCounter++;
            System.out.println(Thread.currentThread().getName() + " - Instance: " + instanceCounter);
        }
    }
}
```

### Real-World Example: Print Shop

```java
class PrintShop {
    private Object printerLock = new Object();
    private Object scannerLock = new Object();
    
    public void printDocument(String doc) {
        synchronized(printerLock) {
            System.out.println(Thread.currentThread().getName() + " printing: " + doc);
            try {
                Thread.sleep(1000); // Simulate printing time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " completed printing: " + doc);
        }
    }
    
    public void scanDocument(String doc) {
        synchronized(scannerLock) {
            System.out.println(Thread.currentThread().getName() + " scanning: " + doc);
            try {
                Thread.sleep(1000); // Simulate scanning time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " completed scanning: " + doc);
        }
    }
}

class PrintShopDemo {
    public static void main(String[] args) {
        PrintShop shop = new PrintShop();
        
        // Multiple threads can print and scan simultaneously
        Thread t1 = new Thread(() -> shop.printDocument("Doc1"), "Printer-1");
        Thread t2 = new Thread(() -> shop.printDocument("Doc2"), "Printer-2");
        Thread t3 = new Thread(() -> shop.scanDocument("Doc3"), "Scanner-1");
        Thread t4 = new Thread(() -> shop.scanDocument("Doc4"), "Scanner-2");
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
```

### Synchronized Block Best Practices

✅ **Minimize critical section** - Lock only what's necessary  
✅ **Use specific locks** - Different locks for different resources  
✅ **Avoid nesting** - Can lead to deadlock  
✅ **Keep it short** - Release lock quickly  
✅ **Use final locks** - `private final Object lock = new Object();`  

---

## 14. Static Synchronization

### What is Static Synchronization?

When you synchronize a **static method** or use a **class-level lock**, you're implementing static synchronization.

### Class Lock vs Object Lock

```
Object Lock (Instance):
Object1 ──> Lock1
Object2 ──> Lock2
(Different locks for different objects)

Class Lock (Static):
MyClass.class ──> Single Lock
(One lock for all objects of the class)
```

### Example: Static Synchronized Method

```java
class StaticSyncDemo {
    private static int staticCounter = 0;
    private int instanceCounter = 0;
    
    // Static synchronized method (class-level lock)
    public static synchronized void incrementStatic() {
        staticCounter++;
        System.out.println(Thread.currentThread().getName() + 
                           " - Static counter: " + staticCounter);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // Instance synchronized method (object-level lock)
    public synchronized void incrementInstance() {
        instanceCounter++;
        System.out.println(Thread.currentThread().getName() + 
                           " - Instance counter: " + instanceCounter);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class StaticSyncTest {
    public static void main(String[] args) {
        StaticSyncDemo obj1 = new StaticSyncDemo();
        StaticSyncDemo obj2 = new StaticSyncDemo();
        
        // Thread 1 & 2: Static method on different objects
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                StaticSyncDemo.incrementStatic();
            }
        }, "Thread-1");
        
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                StaticSyncDemo.incrementStatic();
            }
        }, "Thread-2");
        
        // Thread 3 & 4: Instance method on different objects
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                obj1.incrementInstance();
            }
        }, "Thread-3");
        
        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                obj2.incrementInstance();
            }
        }, "Thread-4");
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
```

**Output:**
```
Thread-1 - Static counter: 1     // Static methods are synchronized
Thread-1 - Static counter: 2
Thread-3 - Instance counter: 1   // Instance methods run in parallel
Thread-4 - Instance counter: 1   // Different objects = different locks
Thread-2 - Static counter: 3
Thread-3 - Instance counter: 2
...
```

### Static Synchronized Block

```java
class StaticBlockDemo {
    private static int sharedResource = 0;
    
    public void updateResource() {
        // Synchronized block on class object
        synchronized(StaticBlockDemo.class) {
            sharedResource++;
            System.out.println(Thread.currentThread().getName() + 
                               " updated: " + sharedResource);
        }
    }
    
    // Equivalent to:
    public static synchronized void updateResourceMethod() {
        sharedResource++;
        System.out.println(Thread.currentThread().getName() + 
                           " updated: " + sharedResource);
    }
}
```

### Real-World Example: Database Connection Pool

```java
class DatabaseConnectionPool {
    private static int totalConnections = 0;
    private static final int MAX_CONNECTIONS = 10;
    
    // Static synchronized - ensures thread-safe connection count
    public static synchronized boolean requestConnection() {
        if (totalConnections < MAX_CONNECTIONS) {
            totalConnections++;
            System.out.println(Thread.currentThread().getName() + 
                               " got connection. Total: " + totalConnections);
            return true;
        } else {
            System.out.println(Thread.currentThread().getName() + 
                               " - No connections available!");
            return false;
        }
    }
    
    public static synchronized void releaseConnection() {
        if (totalConnections > 0) {
            totalConnections--;
            System.out.println(Thread.currentThread().getName() + 
                               " released connection. Total: " + totalConnections);
        }
    }
    
    public static synchronized int getConnectionCount() {
        return totalConnections;
    }
}

class ConnectionPoolDemo {
    public static void main(String[] args) throws InterruptedException {
        // Multiple threads requesting connections
        Thread[] threads = new Thread[15];
        
        for (int i = 0; i < 15; i++) {
            threads[i] = new Thread(() -> {
                if (DatabaseConnectionPool.requestConnection()) {
                    try {
                        Thread.sleep(1000); // Simulate work
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DatabaseConnectionPool.releaseConnection();
                }
            }, "Thread-" + (i + 1));
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("\nFinal connection count: " + 
                           DatabaseConnectionPool.getConnectionCount());
    }
}
```

### Key Differences

| Feature | Object Lock | Class Lock |
|---------|-------------|------------|
| **Scope** | Instance level | Class level |
| **Lock Object** | `this` | `ClassName.class` |
| **Method Type** | Instance method | Static method |
| **Multiple Objects** | Different locks | Same lock |
| **Syntax** | `synchronized void method()` | `static synchronized void method()` |
| **Block Syntax** | `synchronized(this)` | `synchronized(ClassName.class)` |

---

## 15. Deadlock

### What is Deadlock?

**Deadlock** occurs when two or more threads are blocked forever, each waiting for the other to release a resource.

### Visual Representation

```
Thread-1                     Thread-2
   │                           │
   ├─ Acquires Lock A          │
   │                           ├─ Acquires Lock B
   │                           │
   ├─ Needs Lock B (waits)     │
   │  ⏰ Waiting...           │
   │                           ├─ Needs Lock A (waits)
   │                           │  ⏰ Waiting...
   │                           │
   └─ DEADLOCK! ❌            └─ DEADLOCK! ❌
```

### Classic Deadlock Example

```java
class DeadlockDemo {
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();
    
    public static void main(String[] args) {
        // Thread 1: Acquires lock1 first, then lock2
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-1: Holding lock1...");
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Thread-1: Waiting for lock2...");
                synchronized (lock2) {
                    System.out.println("Thread-1: Holding lock1 & lock2");
                }
            }
        }, "Thread-1");
        
        // Thread 2: Acquires lock2 first, then lock1
        Thread t2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread-2: Holding lock2...");
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Thread-2: Waiting for lock1...");
                synchronized (lock1) {
                    System.out.println("Thread-2: Holding lock1 & lock2");
                }
            }
        }, "Thread-2");
        
        t1.start();
        t2.start();
        
        // Program will hang here! Deadlock occurred!
    }
}
```

**Output:**
```
Thread-1: Holding lock1...
Thread-2: Holding lock2...
Thread-1: Waiting for lock2...
Thread-2: Waiting for lock1...
[Program hangs - DEADLOCK!]
```

### Solution 1: Lock Ordering

```java
class DeadlockSolution1 {
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();
    
    public static void main(String[] args) {
        // Thread 1: Acquires locks in same order
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread-1: Holding lock1...");
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Thread-1: Waiting for lock2...");
                synchronized (lock2) {
                    System.out.println("Thread-1: Holding lock1 & lock2");
                }
            }
        }, "Thread-1");
        
        // Thread 2: Acquires locks in SAME order (not reversed)
        Thread t2 = new Thread(() -> {
            synchronized (lock1) { // Same order as Thread-1
                System.out.println("Thread-2: Holding lock1...");
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                System.out.println("Thread-2: Waiting for lock2...");
                synchronized (lock2) {
                    System.out.println("Thread-2: Holding lock1 & lock2");
                }
            }
        }, "Thread-2");
        
        t1.start();
        t2.start();
        
        // No deadlock! ✅
    }
}
```

### Solution 2: Using tryLock() with ReentrantLock

```java
import java.util.concurrent.locks.*;

class DeadlockSolution2 {
    private static Lock lock1 = new ReentrantLock();
    private static Lock lock2 = new ReentrantLock();
    
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            boolean lock1Acquired = false;
            boolean lock2Acquired = false;
            
            try {
                while (true) {
                    lock1Acquired = lock1.tryLock();
                    lock2Acquired = lock2.tryLock();
                    
                    if (lock1Acquired && lock2Acquired) {
                        System.out.println("Thread-1: Got both locks");
                        break;
                    } else {
                        System.out.println("Thread-1: Could not get both locks, releasing...");
                        if (lock1Acquired) lock1.unlock();
                        if (lock2Acquired) lock2.unlock();
                        Thread.sleep(50); // Back off
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (lock1Acquired) lock1.unlock();
                if (lock2Acquired) lock2.unlock();
            }
        }, "Thread-1");
        
        Thread t2 = new Thread(() -> {
            boolean lock1Acquired = false;
            boolean lock2Acquired = false;
            
            try {
                while (true) {
                    lock2Acquired = lock2.tryLock();
                    lock1Acquired = lock1.tryLock();
                    
                    if (lock1Acquired && lock2Acquired) {
                        System.out.println("Thread-2: Got both locks");
                        break;
                    } else {
                        System.out.println("Thread-2: Could not get both locks, releasing...");
                        if (lock1Acquired) lock1.unlock();
                        if (lock2Acquired) lock2.unlock();
                        Thread.sleep(50); // Back off
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (lock1Acquired) lock1.unlock();
                if (lock2Acquired) lock2.unlock();
            }
        }, "Thread-2");
        
        t1.start();
        t2.start();
    }
}
```

### Deadlock Conditions (Coffman Conditions)

Deadlock occurs when ALL four conditions are met:

1. **Mutual Exclusion** - Resources cannot be shared
2. **Hold and Wait** - Thread holds resource while waiting for another
3. **No Preemption** - Resources cannot be forcibly taken
4. **Circular Wait** - Circular chain of threads waiting for resources

### Preventing Deadlock

✅ **Lock Ordering** - Always acquire locks in same order  
✅ **Lock Timeout** - Use tryLock() with timeout  
✅ **Deadlock Detection** - Monitor and detect deadlock  
✅ **Avoid Nested Locks** - Minimize nested synchronized blocks  
✅ **Use Higher-Level Constructs** - Use ExecutorService, CountDownLatch  

### Real-World Example: Bank Transfer

```java
class BankAccountDeadlock {
    private int balance;
    private int accountNumber;
    
    public BankAccountDeadlock(int accountNumber, int balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
    
    // WRONG: Can cause deadlock
    public synchronized void transferWrong(BankAccountDeadlock target, int amount) {
        synchronized (target) {
            this.balance -= amount;
            target.balance += amount;
            System.out.println("Transferred " + amount + " from " + 
                               this.accountNumber + " to " + target.accountNumber);
        }
    }
    
    // CORRECT: Lock ordering prevents deadlock
    public void transferCorrect(BankAccountDeadlock target, int amount) {
        BankAccountDeadlock first, second;
        
        // Determine lock order based on account number
        if (this.accountNumber < target.accountNumber) {
            first = this;
            second = target;
        } else {
            first = target;
            second = this;
        }
        
        synchronized (first) {
            synchronized (second) {
                this.balance -= amount;
                target.balance += amount;
                System.out.println("Transferred " + amount + " from " + 
                                   this.accountNumber + " to " + target.accountNumber);
            }
        }
    }
    
    public synchronized int getBalance() {
        return balance;
    }
}
```

---

*This is Part 1-3 of the Multithreading Guide. The document continues with Parts 4-8 covering Inter-Thread Communication, Advanced Concurrency, Concurrent Collections, Locks & Synchronizers, and Best Practices.*

---

---

# 🔄 PART 5: ADVANCED CONCURRENCY

---

## 22. Executor Framework

### What is Executor Framework?

> **Introduced in:** Java 5 (2004)  
> **Package:** `java.util.concurrent`  
> **Purpose:** High-level thread management API that abstracts thread creation, management, and scheduling

**The Executor Framework is a powerful abstraction that:**
- ✅ Manages thread pools automatically
- ✅ Reuses threads instead of creating new ones
- ✅ Provides task queuing and scheduling
- ✅ Handles thread lifecycle management
- ✅ Improves application performance and scalability

---

### Why Use Executor Framework?

#### Problem with Traditional Thread Creation

```java
// ❌ BAD: Creating threads manually
public class BadThreadManagement {
    public static void main(String[] args) {
        // Creating 1000 threads!
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            Thread thread = new Thread(() -> {
                System.out.println("Task " + taskId);
            });
            thread.start(); // Each creates a new OS thread
        }
    }
}
```

**Problems:**
- 🔴 Creates 1000 OS threads (expensive: ~1MB stack per thread)
- 🔴 Thread creation overhead (~1-2ms per thread)
- 🔴 Context switching overhead
- 🔴 No control over resource usage
- 🔴 Can crash JVM with OutOfMemoryError

#### Solution with Executor Framework

```java
// ✅ GOOD: Using Executor Framework
import java.util.concurrent.*;

public class GoodThreadManagement {
    public static void main(String[] args) {
        // Create a pool of 10 threads
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // Submit 1000 tasks - only 10 threads used!
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " by " + 
                                   Thread.currentThread().getName());
            });
        }
        
        executor.shutdown(); // Graceful shutdown
    }
}
```

**Benefits:**
- ✅ Only 10 threads created (90% reduction)
- ✅ Threads are reused (no creation overhead)
- ✅ Controlled resource usage
- ✅ Built-in task queuing
- ✅ Graceful shutdown support

---

### Executor Framework Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                    EXECUTOR FRAMEWORK                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
                    ┌─────────────────┐
                    │    Executor     │ ← Basic interface
                    │  (interface)    │   void execute(Runnable)
                    └────────┬────────┘
                             │
                             ↓
                ┌────────────────────────┐
                │   ExecutorService      │ ← Extended interface
                │     (interface)        │   - submit(), invokeAll()
                └───────────┬────────────┘   - shutdown(), awaitTermination()
                            │
                            ↓
          ┌─────────────────────────────────┐
          │  AbstractExecutorService        │ ← Base implementation
          │     (abstract class)            │
          └────────────┬────────────────────┘
                       │
                       ↓
          ┌────────────────────────────┐
          │   ThreadPoolExecutor       │ ← Core implementation
          │   (concrete class)         │   - Most flexible
          └────────────┬───────────────┘   - Configurable pools
                       │
                       ↓
          ┌────────────────────────────┐
          │      Executors             │ ← Factory class
          │   (factory methods)        │   - newFixedThreadPool()
          └────────────────────────────┘   - newCachedThreadPool()
                                            - newSingleThreadExecutor()
                                            - newScheduledThreadPool()
```

---

### Key Interfaces and Classes

#### 1. Executor Interface

The simplest interface with just one method:

```java
public interface Executor {
    void execute(Runnable command);
}
```

**Example:**

```java
import java.util.concurrent.*;

public class ExecutorExample {
    public static void main(String[] args) {
        Executor executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                // Custom execution logic
                new Thread(command).start();
            }
        };
        
        executor.execute(() -> System.out.println("Task executed"));
    }
}
```

#### 2. ExecutorService Interface

Extended interface with lifecycle management:

```java
public interface ExecutorService extends Executor {
    // Lifecycle methods
    void shutdown();
    List<Runnable> shutdownNow();
    boolean isShutdown();
    boolean isTerminated();
    boolean awaitTermination(long timeout, TimeUnit unit);
    
    // Task submission methods
    <T> Future<T> submit(Callable<T> task);
    Future<?> submit(Runnable task);
    <T> Future<T> submit(Runnable task, T result);
    
    // Bulk execution methods
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks);
    <T> T invokeAny(Collection<? extends Callable<T>> tasks);
}
```

---

### Types of Thread Pools

#### 1. Fixed Thread Pool

**Creates a pool with a fixed number of threads.**

```java
import java.util.concurrent.*;

public class FixedThreadPoolExample {
    public static void main(String[] args) {
        // Create pool with 3 threads
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Submit 10 tasks
        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + 
                                   " executed by " + Thread.currentThread().getName() +
                                   " at " + System.currentTimeMillis());
                
                try {
                    Thread.sleep(1000); // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
    }
}
```

**Output:**
```
Task 1 executed by pool-1-thread-1 at 1708617600000
Task 2 executed by pool-1-thread-2 at 1708617600001
Task 3 executed by pool-1-thread-3 at 1708617600002
Task 4 executed by pool-1-thread-1 at 1708617601003
Task 5 executed by pool-1-thread-2 at 1708617601004
Task 6 executed by pool-1-thread-3 at 1708617601005
Task 7 executed by pool-1-thread-1 at 1708617602006
Task 8 executed by pool-1-thread-2 at 1708617602007
Task 9 executed by pool-1-thread-3 at 1708617602008
Task 10 executed by pool-1-thread-1 at 1708617603009
```

**Characteristics:**
- ✅ Fixed number of threads (3 in this example)
- ✅ Threads are reused (pool-1-thread-1 handles tasks 1, 4, 7, 10)
- ✅ Unbounded queue (LinkedBlockingQueue)
- ✅ Good for predictable workload
- ⚠️ Queue can grow indefinitely if tasks arrive faster than processed

**Use Case:** Web servers with predictable load

---

#### 2. Cached Thread Pool

**Creates new threads as needed, reuses previously created threads.**

```java
import java.util.concurrent.*;

public class CachedThreadPoolExample {
    public static void main(String[] args) {
        // Create cached thread pool
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // Submit 5 tasks with delay
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + 
                                   " executed by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
    }
}
```

**Output:**
```
Task 1 executed by pool-1-thread-1
Task 2 executed by pool-1-thread-2
Task 3 executed by pool-1-thread-3
Task 4 executed by pool-1-thread-4
Task 5 executed by pool-1-thread-5
```

**Characteristics:**
- ✅ Creates threads on demand
- ✅ Reuses idle threads (60s keep-alive)
- ✅ No queue (SynchronousQueue - direct handoff)
- ✅ Good for many short-lived tasks
- ⚠️ Can create unlimited threads (resource exhaustion risk)

**Use Case:** Applications with many short async tasks

---

#### 3. Single Thread Executor

**Uses a single worker thread.**

```java
import java.util.concurrent.*;

public class SingleThreadExecutorExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + 
                                   " executed by " + Thread.currentThread().getName());
            });
        }
        
        executor.shutdown();
    }
}
```

**Output:**
```
Task 1 executed by pool-1-thread-1
Task 2 executed by pool-1-thread-1
Task 3 executed by pool-1-thread-1
Task 4 executed by pool-1-thread-1
Task 5 executed by pool-1-thread-1
```

**Characteristics:**
- ✅ Exactly one thread
- ✅ Tasks executed sequentially (FIFO order)
- ✅ Thread replaced if it dies
- ✅ Unbounded queue

**Use Case:** Tasks that must execute sequentially (event processing, logging)

---

#### 4. Scheduled Thread Pool

**Supports delayed and periodic task execution.**

```java
import java.util.concurrent.*;

public class ScheduledThreadPoolExample {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        
        // Task 1: Execute once after 2 seconds
        scheduler.schedule(() -> {
            System.out.println("Task executed after 2 seconds");
        }, 2, TimeUnit.SECONDS);
        
        // Task 2: Execute every 1 second with initial delay of 0 seconds
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Repeating task at " + System.currentTimeMillis());
        }, 0, 1, TimeUnit.SECONDS);
        
        // Task 3: Execute with fixed delay of 1 second between completions
        scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("Task with fixed delay at " + System.currentTimeMillis());
            try {
                Thread.sleep(500); // Task takes 500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 1, TimeUnit.SECONDS);
        
        // Let it run for 5 seconds
        Thread.sleep(5000);
        scheduler.shutdown();
    }
}
```

**Output:**
```
Repeating task at 1708617600000
Task with fixed delay at 1708617600001
Repeating task at 1708617601000
Task with fixed delay at 1708617601502
Task executed after 2 seconds
Repeating task at 1708617602000
Task with fixed delay at 1708617603003
Repeating task at 1708617603000
Task with fixed delay at 1708617604505
Repeating task at 1708617604000
```

**Methods:**
- **schedule()** - Execute once after delay
- **scheduleAtFixedRate()** - Execute periodically (fixed rate from start time)
- **scheduleWithFixedDelay()** - Execute periodically (fixed delay after completion)

**Use Case:** Scheduled tasks, polling, periodic data sync

---

### ThreadPoolExecutor - The Core Implementation

**Most flexible and configurable thread pool.**

```java
import java.util.concurrent.*;

public class ThreadPoolExecutorExample {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                              // corePoolSize
            4,                              // maximumPoolSize
            60L,                            // keepAliveTime
            TimeUnit.SECONDS,               // time unit
            new ArrayBlockingQueue<>(10),   // work queue (bounded)
            Executors.defaultThreadFactory(), // thread factory
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
        
        // Submit 20 tasks
        for (int i = 1; i <= 20; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("Task " + taskId + 
                                       " executed by " + Thread.currentThread().getName() +
                                       " | Active: " + executor.getActiveCount() +
                                       " | Pool Size: " + executor.getPoolSize() +
                                       " | Queue Size: " + executor.getQueue().size());
                    try {
                        Thread.sleep(2000); // Simulate work
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " rejected!");
            }
        }
        
        executor.shutdown();
    }
}
```

**Output:**
```
Task 1 executed by pool-1-thread-1 | Active: 2 | Pool Size: 2 | Queue Size: 9
Task 2 executed by pool-1-thread-2 | Active: 2 | Pool Size: 2 | Queue Size: 9
Task 3 executed by pool-1-thread-3 | Active: 4 | Pool Size: 4 | Queue Size: 7
Task 4 executed by pool-1-thread-4 | Active: 4 | Pool Size: 4 | Queue Size: 7
Task 17 executed by main | Active: 4 | Pool Size: 4 | Queue Size: 10
Task 18 executed by main | Active: 4 | Pool Size: 4 | Queue Size: 10
Task 19 executed by main | Active: 4 | Pool Size: 4 | Queue Size: 10
Task 20 executed by main | Active: 4 | Pool Size: 4 | Queue Size: 10
...
```

---

### ThreadPoolExecutor Configuration Explained

#### 1. Core Pool Size

**Minimum number of threads kept alive even if idle.**

```
corePoolSize = 2
```

- Always maintained (unless allowCoreThreadTimeOut = true)
- Threads created up to this number immediately
- Example: With core=2, first 2 tasks create 2 threads

#### 2. Maximum Pool Size

**Maximum number of threads allowed.**

```
maximumPoolSize = 4
```

- New threads created only when queue is full
- Example: With max=4, at most 4 threads can exist

#### 3. Keep-Alive Time

**How long excess threads wait before termination.**

```
keepAliveTime = 60 seconds
```

- Applies to threads beyond corePoolSize
- Idle thread dies after 60 seconds of inactivity

#### 4. Work Queue

**Holds tasks waiting for thread availability.**

**Options:**

**a) SynchronousQueue** (no capacity - direct handoff)
```java
new SynchronousQueue<>()
```
- No storage
- Task must be handed to available thread or rejected
- Used by Executors.newCachedThreadPool()

**b) LinkedBlockingQueue** (unbounded)
```java
new LinkedBlockingQueue<>()
```
- Unlimited capacity
- New threads created only up to corePoolSize
- Used by Executors.newFixedThreadPool()
- ⚠️ Can cause OutOfMemoryError if tasks accumulate

**c) ArrayBlockingQueue** (bounded)
```java
new ArrayBlockingQueue<>(100)
```
- Fixed capacity (100 tasks in this example)
- Good for bounded resource usage
- Tasks rejected when queue is full and max threads reached

**d) PriorityBlockingQueue** (priority-based)
```java
new PriorityBlockingQueue<>()
```
- Tasks executed based on priority
- Requires tasks to implement Comparable

#### 5. Thread Factory

**Creates new threads.**

```java
// Default factory
Executors.defaultThreadFactory()

// Custom factory
ThreadFactory factory = new ThreadFactory() {
    private int counter = 0;
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("MyWorker-" + counter++);
        thread.setDaemon(false);
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
};
```

#### 6. Rejection Policies

**What to do when queue is full and max threads reached.**

```java
// 1. AbortPolicy (default) - Throws RejectedExecutionException
new ThreadPoolExecutor.AbortPolicy()

// 2. CallerRunsPolicy - Caller thread executes task (backpressure)
new ThreadPoolExecutor.CallerRunsPolicy()

// 3. DiscardPolicy - Silently discard task
new ThreadPoolExecutor.DiscardPolicy()

// 4. DiscardOldestPolicy - Discard oldest task in queue
new ThreadPoolExecutor.DiscardOldestPolicy()

// 5. Custom policy
new RejectedExecutionHandler() {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        System.out.println("Task rejected: " + r.toString());
        // Log, queue to database, etc.
    }
}
```

---

### Thread Pool Execution Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    TASK SUBMISSION                           │
└──────────────────────────────────────────────────────────────┘
                            │
                            ↓
                   ┌────────────────┐
                   │  New Task      │
                   └────────┬───────┘
                            │
                            ↓
              ┌─────────────────────────────┐
              │ Pool size < corePoolSize?   │
              └──────┬──────────────┬───────┘
                YES  │              │  NO
                     ↓              ↓
            ┌──────────────┐   ┌──────────────────┐
            │ Create new   │   │ Queue full?      │
            │ thread       │   └────┬────────┬────┘
            └──────────────┘     YES│        │NO
                                    │        │
                                    │        ↓
                                    │   ┌──────────────┐
                                    │   │ Add to queue │
                                    │   └──────────────┘
                                    ↓
                     ┌───────────────────────────────┐
                     │ Pool size < maximumPoolSize?  │
                     └────┬─────────────────┬────────┘
                       YES│                 │NO
                          ↓                 ↓
                  ┌──────────────┐   ┌─────────────────┐
                  │ Create new   │   │ Apply rejection │
                  │ thread       │   │ policy          │
                  └──────────────┘   └─────────────────┘
```

**Example Flow:**

```
Configuration: core=2, max=4, queue=10

Task 1  → Create thread-1 (pool size: 1)
Task 2  → Create thread-2 (pool size: 2, reached core size)
Task 3  → Add to queue (queue size: 1)
Task 4  → Add to queue (queue size: 2)
...
Task 12 → Add to queue (queue size: 10, queue full!)
Task 13 → Create thread-3 (pool size: 3)
Task 14 → Create thread-4 (pool size: 4, reached max!)
Task 15 → Queue still full, max threads reached → REJECT!
```

---

### Executor Service Lifecycle

```java
import java.util.concurrent.*;

public class ExecutorLifecycleExample {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // 1. RUNNING state (can accept new tasks)
        System.out.println("Is shutdown? " + executor.isShutdown()); // false
        System.out.println("Is terminated? " + executor.isTerminated()); // false
        
        // Submit tasks
        executor.submit(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Task 1 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        executor.submit(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Task 2 completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // 2. SHUTDOWN state (no new tasks, existing tasks continue)
        executor.shutdown();
        System.out.println("Is shutdown? " + executor.isShutdown()); // true
        System.out.println("Is terminated? " + executor.isTerminated()); // false
        
        // Try to submit new task after shutdown
        try {
            executor.submit(() -> System.out.println("Task 3"));
        } catch (RejectedExecutionException e) {
            System.out.println("Cannot submit new tasks after shutdown");
        }
        
        // 3. Wait for termination
        boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Terminated? " + terminated); // true
        System.out.println("Is terminated? " + executor.isTerminated()); // true
    }
}
```

**Output:**
```
Is shutdown? false
Is terminated? false
Is shutdown? true
Is terminated? false
Cannot submit new tasks after shutdown
Task 2 completed
Task 1 completed
Terminated? true
Is terminated? true
```

**Lifecycle Methods:**

| Method | Description | New Tasks? | Running Tasks? |
|--------|-------------|-----------|----------------|
| `shutdown()` | Graceful shutdown | ❌ Rejected | ✅ Complete | 
| `shutdownNow()` | Immediate shutdown | ❌ Rejected | ❌ Interrupted |
| `awaitTermination()` | Wait for completion | - | - |
| `isShutdown()` | Check if shutdown initiated | - | - |
| `isTerminated()` | Check if all tasks completed | - | - |

---

### shutdown() vs shutdownNow()

```java
import java.util.concurrent.*;
import java.util.*;

public class ShutdownComparison {
    
    // Example 1: shutdown() - Graceful
    public static void gracefulShutdown() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("Task 1 completed");
            } catch (InterruptedException e) {
                System.out.println("Task 1 interrupted");
            }
        });
        
        executor.submit(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Task 2 completed");
            } catch (InterruptedException e) {
                System.out.println("Task 2 interrupted");
            }
        });
        
        executor.shutdown(); // Graceful: Wait for tasks to complete
        System.out.println("Shutdown initiated");
    }
    
    // Example 2: shutdownNow() - Immediate
    public static void immediateShutdown() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("Task 1 completed");
            } catch (InterruptedException e) {
                System.out.println("Task 1 interrupted");
            }
        });
        
        executor.submit(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Task 2 completed");
            } catch (InterruptedException e) {
                System.out.println("Task 2 interrupted");
            }
        });
        
        Thread.sleep(500); // Let tasks start
        
        List<Runnable> notExecutedTasks = executor.shutdownNow();
        System.out.println("Shutdown now! Not executed: " + notExecutedTasks.size());
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Graceful Shutdown ===");
        gracefulShutdown();
        Thread.sleep(4000);
        
        System.out.println("\n=== Immediate Shutdown ===");
        immediateShutdown();
        Thread.sleep(1000);
    }
}
```

**Output:**
```
=== Graceful Shutdown ===
Shutdown initiated
Task 2 completed
Task 1 completed

=== Immediate Shutdown ===
Shutdown now! Not executed: 0
Task 1 interrupted
Task 2 interrupted
```

---

### Best Practices

#### 1. Always Shutdown Executors

```java
ExecutorService executor = Executors.newFixedThreadPool(5);

try {
    // Submit tasks
    executor.submit(() -> System.out.println("Task"));
} finally {
    // Always shutdown
    executor.shutdown();
    
    try {
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

#### 2. Use Try-With-Resources (Java 19+)

```java
// Java 19+ ExecutorService implements AutoCloseable
try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
    executor.submit(() -> System.out.println("Task"));
} // Automatically calls close() which does shutdown()
```

#### 3. Choose Right Pool Type

```java
// CPU-intensive tasks: Fixed pool sized to CPU cores
int cores = Runtime.getRuntime().availableProcessors();
ExecutorService cpuIntensive = Executors.newFixedThreadPool(cores);

// I/O-intensive tasks: Larger pool or cached pool
ExecutorService ioIntensive = Executors.newFixedThreadPool(cores * 2);

// Scheduled tasks: Scheduled pool
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
```

#### 4. Use Bounded Queues

```java
// ✅ GOOD: Bounded queue prevents memory exhaustion
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100), // Bounded!
    new ThreadPoolExecutor.CallerRunsPolicy()
);

// ❌ BAD: Unbounded queue can cause OutOfMemoryError
ExecutorService bad = Executors.newFixedThreadPool(5); // Uses LinkedBlockingQueue (unbounded)
```

#### 5. Monitor Thread Pool

```java
@Scheduled(fixedDelay = 10000)
public void monitorThreadPool() {
    ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
    
    log.info("Thread Pool Stats: " +
        "Active={}, " +
        "Pool Size={}, " +
        "Core Pool Size={}, " +
        "Max Pool Size={}, " +
        "Queue Size={}, " +
        "Completed Tasks={}",
        pool.getActiveCount(),
        pool.getPoolSize(),
        pool.getCorePoolSize(),
        pool.getMaximumPoolSize(),
        pool.getQueue().size(),
        pool.getCompletedTaskCount()
    );
}
```

---

### Real-World Example: Image Processing Service

```java
import java.util.concurrent.*;
import java.util.*;

public class ImageProcessingService {
    
    private final ExecutorService executor;
    
    public ImageProcessingService() {
        int cores = Runtime.getRuntime().availableProcessors();
        
        // CPU-intensive: Use core count
        this.executor = new ThreadPoolExecutor(
            cores,                          // corePoolSize
            cores * 2,                      // maximumPoolSize
            60L,                            // keepAliveTime
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),  // bounded queue
            new ThreadFactory() {
                private int counter = 0;
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("image-processor-" + counter++);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // backpressure
        );
    }
    
    public List<Future<ProcessedImage>> processImages(List<String> imageUrls) {
        List<Future<ProcessedImage>> futures = new ArrayList<>();
        
        for (String url : imageUrls) {
            Future<ProcessedImage> future = executor.submit(() -> {
                System.out.println("Processing " + url + 
                                   " by " + Thread.currentThread().getName());
                
                // Simulate image processing
                Thread.sleep(2000);
                
                return new ProcessedImage(url, "processed");
            });
            
            futures.add(future);
        }
        
        return futures;
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static void main(String[] args) throws Exception {
        ImageProcessingService service = new ImageProcessingService();
        
        List<String> images = Arrays.asList(
            "image1.jpg", "image2.jpg", "image3.jpg",
            "image4.jpg", "image5.jpg", "image6.jpg"
        );
        
        List<Future<ProcessedImage>> futures = service.processImages(images);
        
        // Wait for all to complete
        for (Future<ProcessedImage> future : futures) {
            ProcessedImage result = future.get(); // Blocking call
            System.out.println("Completed: " + result.url);
        }
        
        service.shutdown();
    }
    
    static class ProcessedImage {
        String url;
        String status;
        
        ProcessedImage(String url, String status) {
            this.url = url;
            this.status = status;
        }
    }
}
```

**Output:**
```
Processing image1.jpg by image-processor-0
Processing image2.jpg by image-processor-1
Processing image3.jpg by image-processor-2
Processing image4.jpg by image-processor-3
Processing image5.jpg by image-processor-0
Processing image6.jpg by image-processor-1
Completed: image1.jpg
Completed: image2.jpg
Completed: image3.jpg
Completed: image4.jpg
Completed: image5.jpg
Completed: image6.jpg
```

---

### Common Interview Questions

**Q1: What is the difference between execute() and submit()?**

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

// execute() - void, no return value, only Runnable
executor.execute(() -> System.out.println("Task"));

// submit() - returns Future, can use Callable or Runnable
Future<String> future = executor.submit(() -> {
    return "Result";
});

String result = future.get(); // Get result
```

**Q2: What happens if we don't call shutdown()?**

- Executor keeps running (threads don't die)
- JVM doesn't exit (non-daemon threads keep it alive)
- Resource leak (threads consume memory)

**Q3: What is the difference between shutdown() and shutdownNow()?**

| Method | New Tasks | Running Tasks | Returns |
|--------|-----------|---------------|---------|
| `shutdown()` | Rejected | Complete | void |
| `shutdownNow()` | Rejected | Interrupted | List<Runnable> |

**Q4: How to configure thread pool size?**

```java
// CPU-intensive tasks
int poolSize = Runtime.getRuntime().availableProcessors();

// I/O-intensive tasks
int poolSize = Runtime.getRuntime().availableProcessors() * 2;

// Mixed workload
int poolSize = Runtime.getRuntime().availableProcessors() * (1 + waitTime/computeTime);
```

**Q5: What are the disadvantages of Executors factory methods?**

- `newFixedThreadPool()` - Unbounded queue (OutOfMemoryError risk)
- `newCachedThreadPool()` - Unbounded threads (resource exhaustion)
- `newSingleThreadExecutor()` - Unbounded queue

**Solution:** Use `ThreadPoolExecutor` with bounded queue!

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

**End of Part 5 - Section 22** - Executor Framework Complete! ✅

---

---


# 📦 PART 6: CONCURRENT COLLECTIONS & BLOCKING QUEUES

---

## 26. BlockingQueue

### What is BlockingQueue?

**BlockingQueue** is a thread-safe queue interface that supports operations that **wait** for the queue to become non-empty when retrieving an element, and **wait** for space to become available when storing an element.

**Key Characteristics:**
- ✅ Thread-safe (no need for external synchronization)
- ✅ Blocking operations (automatic waiting)
- ✅ Bounded or unbounded capacity
- ✅ Supports producer-consumer pattern
- ✅ Null elements not allowed

---

### BlockingQueue Methods

| Method | Throws Exception | Returns Special Value | Blocks | Times Out |
|--------|-----------------|----------------------|--------|-----------|
| **Insert** | `add(e)` | `offer(e)` | `put(e)` | `offer(e, time, unit)` |
| **Remove** | `remove()` | `poll()` | `take()` | `poll(time, unit)` |
| **Examine** | `element()` | `peek()` | N/A | N/A |

**Method Behavior:**

1. **Throws Exception**: `add()`, `remove()`, `element()`
   - Throws exception if operation cannot be performed immediately

2. **Special Value**: `offer()`, `poll()`, `peek()`
   - Returns `true`/`false` or `null` instead of throwing exception

3. **Blocks**: `put()`, `take()`
   - Waits indefinitely until operation succeeds

4. **Times Out**: `offer(e, timeout)`, `poll(timeout)`
   - Waits for specified time, then gives up

---

### BlockingQueue Example

```java
import java.util.concurrent.*;

class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                queue.put("Item-1");
                System.out.println("Produced: Item-1");
                
                queue.put("Item-2");
                System.out.println("Produced: Item-2");
                
                queue.put("Item-3");
                System.out.println("Produced: Item-3");
                
                // This will block until space is available
                queue.put("Item-4");
                System.out.println("Produced: Item-4");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait before consuming
                
                String item = queue.take();
                System.out.println("Consumed: " + item);
                
                item = queue.take();
                System.out.println("Consumed: " + item);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
    }
}
```

**Output:**
```
Produced: Item-1
Produced: Item-2
Produced: Item-3
(waits 2 seconds...)
Consumed: Item-1
Consumed: Item-2
Produced: Item-4
```

**Explanation:**
- Queue capacity is 3
- After producing 3 items, `put("Item-4")` blocks
- Once consumer removes items, producer continues

---

## 27. ArrayBlockingQueue vs LinkedBlockingQueue

### ArrayBlockingQueue

**Definition:** Bounded blocking queue backed by an **array**.

**Key Features:**
- ✅ Fixed capacity (must specify at creation)
- ✅ FIFO ordering
- ✅ Single lock for both put and take operations
- ✅ Fair or unfair access policy
- ✅ Better performance for high contention

**When to Use:**
- Known maximum capacity
- Need to prevent memory exhaustion
- High contention scenarios

---

### ArrayBlockingQueue Example

```java
import java.util.concurrent.*;

class ArrayBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        // Bounded queue with capacity 5
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        
        // Producer
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    queue.put(i);
                    System.out.println("Produced: " + i + " | Queue size: " + queue.size());
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(1000); // Slow consumer
                
                for (int i = 1; i <= 10; i++) {
                    int item = queue.take();
                    System.out.println("Consumed: " + item + " | Queue size: " + queue.size());
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("All tasks completed!");
    }
}
```

**Output:**
```
Produced: 1 | Queue size: 1
Produced: 2 | Queue size: 2
Produced: 3 | Queue size: 3
Produced: 4 | Queue size: 4
Produced: 5 | Queue size: 5
(Producer blocks, queue is full)
Consumed: 1 | Queue size: 4
Produced: 6 | Queue size: 5
Consumed: 2 | Queue size: 4
Produced: 7 | Queue size: 5
...
All tasks completed!
```

---

### LinkedBlockingQueue

**Definition:** Optionally bounded blocking queue backed by a **linked list**.

**Key Features:**
- ✅ Optionally bounded (can be unbounded)
- ✅ FIFO ordering
- ✅ Two separate locks (one for put, one for take) - better throughput
- ✅ Higher throughput under concurrent access
- ✅ More memory overhead (linked nodes)

**When to Use:**
- Need higher throughput
- Don't know maximum capacity in advance
- Low to medium contention

---

### LinkedBlockingQueue Example

```java
import java.util.concurrent.*;

class LinkedBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        // Optionally bounded with capacity 100
        BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);
        
        // Or unbounded:
        // BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
        
        // Multiple producers
        for (int i = 1; i <= 3; i++) {
            final int producerId = i;
            new Thread(() -> {
                try {
                    for (int j = 1; j <= 5; j++) {
                        Task task = new Task("P" + producerId + "-T" + j);
                        queue.put(task);
                        System.out.println("Producer " + producerId + " produced: " + task.name);
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        // Multiple consumers
        for (int i = 1; i <= 2; i++) {
            final int consumerId = i;
            new Thread(() -> {
                try {
                    while (true) {
                        Task task = queue.take();
                        System.out.println("Consumer " + consumerId + " consumed: " + task.name);
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        Thread.sleep(5000);
        System.out.println("Demo completed!");
    }
}

class Task {
    String name;
    
    Task(String name) {
        this.name = name;
    }
}
```

**Output:**
```
Producer 1 produced: P1-T1
Producer 2 produced: P2-T1
Producer 3 produced: P3-T1
Consumer 1 consumed: P1-T1
Consumer 2 consumed: P2-T1
Producer 1 produced: P1-T2
Producer 2 produced: P2-T2
Consumer 1 consumed: P3-T1
...
Demo completed!
```

---

### ArrayBlockingQueue vs LinkedBlockingQueue - Comparison

| Feature | ArrayBlockingQueue | LinkedBlockingQueue |
|---------|-------------------|-------------------|
| **Structure** | Array-based | Linked list-based |
| **Capacity** | ✅ Always bounded | ✅ Optionally bounded |
| **Locks** | Single lock | Two locks (put/take) |
| **Throughput** | Lower | Higher |
| **Memory** | Less overhead | More overhead (node objects) |
| **Fairness** | Optional fair mode | Always unfair |
| **Use Case** | High contention | High throughput needed |
| **Iterator** | Strongly consistent | Weakly consistent |

**Summary:**
- Use **ArrayBlockingQueue** when you need bounded capacity and fairness
- Use **LinkedBlockingQueue** when you need better throughput with many producers/consumers

---

## 28. PriorityBlockingQueue

### What is PriorityBlockingQueue?

**PriorityBlockingQueue** is an **unbounded** blocking queue that uses priority ordering. Elements are ordered by their natural ordering or by a custom `Comparator`.

**Key Characteristics:**
- ✅ Unbounded capacity (never blocks on put)
- ✅ Priority-based ordering (not FIFO)
- ✅ Thread-safe
- ✅ Blocks only on take() when queue is empty
- ✅ Uses binary heap internally
- ✅ Null elements not allowed

---

### PriorityBlockingQueue Example

```java
import java.util.concurrent.*;

class PriorityBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        // Natural ordering (Priority class implements Comparable)
        BlockingQueue<Task> queue = new PriorityBlockingQueue<>();
        
        // Producer - adds tasks with different priorities
        Thread producer = new Thread(() -> {
            try {
                queue.put(new Task("Low priority task", 3));
                System.out.println("Added: Low priority task (3)");
                
                queue.put(new Task("High priority task", 1));
                System.out.println("Added: High priority task (1)");
                
                queue.put(new Task("Medium priority task", 2));
                System.out.println("Added: Medium priority task (2)");
                
                queue.put(new Task("Critical task", 0));
                System.out.println("Added: Critical task (0)");
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer - retrieves tasks by priority
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(1000); // Let producer add all tasks
                
                while (!queue.isEmpty()) {
                    Task task = queue.take();
                    System.out.println("Processing: " + task);
                    Thread.sleep(500);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("All tasks processed!");
    }
}

class Task implements Comparable<Task> {
    String name;
    int priority; // Lower number = higher priority
    
    Task(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
    
    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }
    
    @Override
    public String toString() {
        return name + " (Priority: " + priority + ")";
    }
}
```

**Output:**
```
Added: Low priority task (3)
Added: High priority task (1)
Added: Medium priority task (2)
Added: Critical task (0)
Processing: Critical task (Priority: 0)
Processing: High priority task (Priority: 1)
Processing: Medium priority task (Priority: 2)
Processing: Low priority task (Priority: 3)
All tasks processed!
```

**Explanation:**
- Tasks are processed by priority, not insertion order
- Lower priority number = higher priority
- Critical task (0) processed first, despite being added last

---

## 29. DelayQueue

### What is DelayQueue?

**DelayQueue** is an **unbounded** blocking queue where elements can only be taken when their **delay has expired**.

**Key Characteristics:**
- ✅ Unbounded capacity
- ✅ Elements must implement `Delayed` interface
- ✅ Ordered by delay time (shortest delay first)
- ✅ Element available only after its delay expires
- ✅ Thread-safe
- ✅ Used for scheduling tasks

---

### DelayQueue Example

```java
import java.util.concurrent.*;

class DelayQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        DelayQueue<DelayedTask> queue = new DelayQueue<>();
        
        // Add tasks with different delays
        long now = System.currentTimeMillis();
        
        queue.put(new DelayedTask("Task 1", now + 3000)); // 3 seconds
        queue.put(new DelayedTask("Task 2", now + 1000)); // 1 second
        queue.put(new DelayedTask("Task 3", now + 5000)); // 5 seconds
        queue.put(new DelayedTask("Task 4", now + 2000)); // 2 seconds
        
        System.out.println("All tasks added at: " + now);
        System.out.println("Waiting for tasks to become available...\n");
        
        // Consumer thread
        while (!queue.isEmpty()) {
            DelayedTask task = queue.take(); // Blocks until delay expires
            System.out.println(System.currentTimeMillis() + " - Retrieved: " + task.name);
        }
        
        System.out.println("\nAll tasks completed!");
    }
}

class DelayedTask implements Delayed {
    String name;
    long executeTime; // Time when task should be executed
    
    DelayedTask(String name, long executeTime) {
        this.name = name;
        this.executeTime = executeTime;
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        long diff = executeTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.executeTime, ((DelayedTask) o).executeTime);
    }
    
    @Override
    public String toString() {
        return name + " (delay: " + getDelay(TimeUnit.MILLISECONDS) + "ms)";
    }
}
```

**Output:**
```
All tasks added at: 1708776543210
Waiting for tasks to become available...

1708776544210 - Retrieved: Task 2
1708776545210 - Retrieved: Task 4
1708776546210 - Retrieved: Task 1
1708776548210 - Retrieved: Task 3

All tasks completed!
```

**Explanation:**
- Tasks retrieved in order of delay expiration, not insertion order
- Task 2 (1 sec delay) retrieved first
- `take()` blocks until each task's delay expires

---

## 30. SynchronousQueue

### What is SynchronousQueue?

**SynchronousQueue** is a blocking queue where each **insert** operation must wait for a corresponding **remove** operation by another thread, and vice versa.

**Key Characteristics:**
- ✅ Zero capacity (no storage)
- ✅ Direct handoff between threads
- ✅ No peek() operation (nothing to peek at)
- ✅ Used by `Executors.newCachedThreadPool()`
- ✅ Useful for handoff designs
- ✅ Fair or unfair mode

---

### SynchronousQueue Example

```java
import java.util.concurrent.*;

class SynchronousQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        SynchronousQueue<String> queue = new SynchronousQueue<>();
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                String[] items = {"Item-1", "Item-2", "Item-3"};
                
                for (String item : items) {
                    System.out.println("Producer trying to put: " + item);
                    queue.put(item); // Blocks until consumer takes it
                    System.out.println("Producer successfully handed off: " + item);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(2000); // Slow consumer
                    System.out.println("Consumer ready to take item...");
                    String item = queue.take(); // Blocks until producer provides
                    System.out.println("Consumer received: " + item);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        producer.start();
        Thread.sleep(500); // Let producer start first
        consumer.start();
        
        producer.join();
        consumer.join();
        
        System.out.println("\nQueue size: " + queue.size()); // Always 0
    }
}
```

**Output:**
```
Producer trying to put: Item-1
(Producer blocks, waiting for consumer)
Consumer ready to take item...
Producer successfully handed off: Item-1
Consumer received: Item-1
Producer trying to put: Item-2
(Producer blocks again)
Consumer ready to take item...
Producer successfully handed off: Item-2
Consumer received: Item-2
Producer trying to put: Item-3
Consumer ready to take item...
Producer successfully handed off: Item-3
Consumer received: Item-3

Queue size: 0
```

**Explanation:**
- Producer blocks on `put()` until consumer calls `take()`
- No internal storage - direct handoff
- Queue size is always 0

---

## 31. Exchanger

### What is Exchanger?

**Exchanger** is a synchronization point where **two threads** can exchange objects. Each thread presents an object to the other thread and receives the partner's object in return.

**Key Characteristics:**
- ✅ Synchronization point for exactly 2 threads
- ✅ Bidirectional data exchange
- ✅ Thread-safe
- ✅ Blocks until both threads reach exchange point
- ✅ Useful for pipeline designs

---

### Exchanger Example

```java
import java.util.concurrent.*;

class ExchangerDemo {
    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                String myData = "Data from Thread-1";
                System.out.println("Thread-1 has: " + myData);
                System.out.println("Thread-1 waiting to exchange...");
                
                // Exchange data - blocks until Thread-2 arrives
                String receivedData = exchanger.exchange(myData);
                
                System.out.println("Thread-1 received: " + receivedData);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(2000); // Arrive late
                
                String myData = "Data from Thread-2";
                System.out.println("Thread-2 has: " + myData);
                System.out.println("Thread-2 ready to exchange...");
                
                // Exchange data - blocks until Thread-1 arrives
                String receivedData = exchanger.exchange(myData);
                
                System.out.println("Thread-2 received: " + receivedData);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\nExchange completed!");
    }
}
```

**Output:**
```
Thread-1 has: Data from Thread-1
Thread-1 waiting to exchange...
(Thread-1 blocks for 2 seconds)
Thread-2 has: Data from Thread-2
Thread-2 ready to exchange...
Thread-1 received: Data from Thread-2
Thread-2 received: Data from Thread-1

Exchange completed!
```

**Explanation:**
- Thread-1 arrives first and blocks at `exchange()`
- Thread-2 arrives after 2 seconds
- Both exchange data simultaneously
- Both receive each other's data

---

## 32. Busy Spinning (Spin-Wait)

### What is Busy Spinning?

**Busy spinning** (or spin-waiting) is a technique where a thread repeatedly checks a condition in a tight loop instead of using blocking mechanisms like `wait()` or `sleep()`.

**Characteristics:**
- ✅ Thread actively checks condition (doesn't release CPU)
- ✅ Very low latency (no context switch)
- ✅ High CPU usage
- ✅ Used in high-performance scenarios

---

### When to Use Busy Spinning?

**✅ Use Busy Spinning When:**
1. Wait time is **very short** (nanoseconds to microseconds)
2. Need **ultra-low latency** (e.g., high-frequency trading)
3. Context switching cost is too high
4. Running on dedicated CPU cores

**❌ Avoid Busy Spinning When:**
1. Wait time is long (wastes CPU)
2. Limited CPU resources
3. Sharing CPU with other applications
4. Power efficiency is important

---

### Busy Spinning Example

```java
class BusySpinningDemo {
    private volatile boolean ready = false;
    private String message;
    
    public static void main(String[] args) throws InterruptedException {
        BusySpinningDemo demo = new BusySpinningDemo();
        
        // Consumer thread - busy spinning
        Thread consumer = new Thread(() -> {
            System.out.println("Consumer: Waiting for message (busy spinning)...");
            long start = System.nanoTime();
            
            // Busy spin until ready
            while (!demo.ready) {
                // Actively checking - burning CPU
                // No context switch overhead
            }
            
            long end = System.nanoTime();
            System.out.println("Consumer: Received message: " + demo.message);
            System.out.println("Consumer: Wait time: " + (end - start) + " nanoseconds");
        });
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate work
                System.out.println("Producer: Preparing message...");
                demo.message = "Hello from Producer!";
                demo.ready = true; // Signal consumer
                System.out.println("Producer: Message ready!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        consumer.start();
        Thread.sleep(10); // Ensure consumer starts first
        producer.start();
        
        consumer.join();
        producer.join();
    }
}
```

**Output:**
```
Consumer: Waiting for message (busy spinning)...
Producer: Preparing message...
Producer: Message ready!
Consumer: Received message: Hello from Producer!
Consumer: Wait time: 102437584 nanoseconds
```

---

### Optimized Busy Spinning with Thread.onSpinWait()

Java 9 introduced `Thread.onSpinWait()` - a hint to the JVM that the thread is in a spin-wait loop.

```java
class OptimizedBusySpinning {
    private volatile boolean ready = false;
    private String message;
    
    public static void main(String[] args) throws InterruptedException {
        OptimizedBusySpinning demo = new OptimizedBusySpinning();
        
        // Consumer with optimized spinning
        Thread consumer = new Thread(() -> {
            System.out.println("Consumer: Optimized busy spinning...");
            
            while (!demo.ready) {
                Thread.onSpinWait(); // JVM hint - can reduce power, help HyperThreading
            }
            
            System.out.println("Consumer: Received: " + demo.message);
        });
        
        // Producer
        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(100);
                demo.message = "Optimized message!";
                demo.ready = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        consumer.start();
        producer.start();
        
        consumer.join();
        producer.join();
    }
}
```

**Output:**
```
Consumer: Optimized busy spinning...
Consumer: Received: Optimized message!
```

**Benefits of `Thread.onSpinWait()`:**
- Reduces power consumption
- Improves performance on HyperThreading CPUs
- Allows other threads on same core to make progress

---

### Comparison: Busy Spinning vs Blocking

| Aspect | Busy Spinning | Blocking (wait/notify) |
|--------|--------------|----------------------|
| **CPU Usage** | ⚠️ High (100% while waiting) | ✅ Low (released) |
| **Latency** | ✅ Very low (nanoseconds) | ⚠️ Higher (context switch) |
| **Context Switch** | ✅ None | ⚠️ Yes (expensive) |
| **Power Efficiency** | ❌ Poor | ✅ Good |
| **Use Case** | Short waits (<1μs) | Long waits (>1ms) |
| **CPU Cores** | Need dedicated cores | Can share cores |

---

## 33. join() vs wait() - The Differences

### Key Differences

| Aspect | `join()` | `wait()` |
|--------|---------|---------|
| **Purpose** | Wait for **thread** to complete | Wait for **notification** from another thread |
| **Belongs to** | `Thread` class | `Object` class |
| **Usage** | Thread coordination | Inter-thread communication |
| **Lock Release** | Does NOT release any lock | Releases monitor lock |
| **Who calls** | Any thread waiting for another thread | Thread holding object's lock |
| **Wakeup** | Automatic (when thread dies) | Manual (`notify()`/`notifyAll()`) |
| **Synchronized** | No synchronization needed | Must be in synchronized block |
| **Return** | When target thread completes | When notified or interrupted |

---

### join() - Detailed Explanation

**Purpose:** Wait for a thread to complete execution.

```java
class JoinExample {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("Worker: Starting work...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Worker: Work completed!");
        });
        
        worker.start();
        
        System.out.println("Main: Waiting for worker to complete...");
        worker.join(); // Main thread waits here
        System.out.println("Main: Worker finished, continuing...");
    }
}
```

**Output:**
```
Worker: Starting work...
Main: Waiting for worker to complete...
(3 seconds pass)
Worker: Work completed!
Main: Worker finished, continuing...
```

**Key Points:**
- `join()` makes current thread wait until the target thread completes
- Does **NOT** require synchronization
- Does **NOT** release any locks held by calling thread

---

### wait() - Detailed Explanation

**Purpose:** Inter-thread communication - wait for notification.

```java
class WaitExample {
    private static final Object lock = new Object();
    private static boolean dataReady = false;
    
    public static void main(String[] args) {
        Thread consumer = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Consumer: Waiting for data...");
                try {
                    while (!dataReady) {
                        lock.wait(); // Releases lock and waits
                    }
                    System.out.println("Consumer: Data received, processing...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(2000);
                synchronized (lock) {
                    System.out.println("Producer: Data ready!");
                    dataReady = true;
                    lock.notify(); // Wake up consumer
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        consumer.start();
        producer.start();
    }
}
```

**Output:**
```
Consumer: Waiting for data...
(2 seconds pass)
Producer: Data ready!
Consumer: Data received, processing...
```

**Key Points:**
- `wait()` **MUST** be in synchronized block
- **Releases** the monitor lock
- Waits until `notify()` or `notifyAll()` is called

---

### Side-by-Side Comparison Example

```java
class JoinVsWaitComparison {
    private static final Object lock = new Object();
    private static boolean ready = false;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demonstrating join() ===\n");
        demonstrateJoin();
        
        System.out.println("\n=== Demonstrating wait() ===\n");
        demonstrateWait();
    }
    
    private static void demonstrateJoin() throws InterruptedException {
        Thread worker = new Thread(() -> {
            System.out.println("Worker: Processing...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Worker: Done!");
        });
        
        worker.start();
        System.out.println("Main: Calling join() - no synchronization needed");
        worker.join(); // Wait for worker to finish
        System.out.println("Main: Worker completed, continuing...");
    }
    
    private static void demonstrateWait() {
        Thread consumer = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Consumer: Calling wait() - must be synchronized");
                try {
                    while (!ready) {
                        lock.wait(); // Releases lock
                    }
                    System.out.println("Consumer: Notified, continuing...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        Thread producer = new Thread(() -> {
            try {
                Thread.sleep(2000);
                synchronized (lock) {
                    System.out.println("Producer: Setting ready and calling notify()");
                    ready = true;
                    lock.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        consumer.start();
        producer.start();
    }
}
```

**Output:**
```
=== Demonstrating join() ===

Main: Calling join() - no synchronization needed
Worker: Processing...
Worker: Done!
Main: Worker completed, continuing...

=== Demonstrating wait() ===

Consumer: Calling wait() - must be synchronized
Producer: Setting ready and calling notify()
Consumer: Notified, continuing...
```

---

### Common Interview Question: Can join() release locks?

**Answer: NO!**

```java
class JoinDoesNotReleaseLock {
    private static final Object lock = new Object();
    
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Worker: Acquired lock, trying to work...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Worker: Releasing lock");
            }
        });
        
        worker.start();
        Thread.sleep(100); // Let worker acquire lock first
        
        synchronized (lock) {
            System.out.println("Main: Acquired lock, calling join()");
            worker.join(); // ⚠️ DEADLOCK! join() doesn't release lock
            System.out.println("Main: This will never print!");
        }
    }
}
```

**Output:**
```
Worker: Acquired lock, trying to work...
Main: Acquired lock, calling join()
(DEADLOCK - program hangs forever)
```

**Explanation:**
- Worker holds `lock` and sleeps
- Main acquires `lock` later and calls `worker.join()`
- `join()` does NOT release `lock`, so main waits forever
- Worker can't finish because it needs `lock` (which main holds)
- **DEADLOCK!**

**Fix: Don't call join() while holding locks needed by the joined thread!**

---

### Summary Table

| Feature | `join()` | `wait()` |
|---------|---------|---------|
| Class | `Thread` | `Object` |
| Synchronization | ❌ Not required | ✅ **Required** |
| Lock Release | ❌ No | ✅ **Yes** |
| Purpose | Wait for thread completion | Wait for notification |
| Wakeup | Automatic (thread dies) | Manual (`notify()`) |
| Use Case | Thread coordination | Producer-Consumer |
| Timeout | `join(timeout)` | `wait(timeout)` |

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

**End of Part 6** - Concurrent Collections & Blocking Queues Complete! ✅

---

