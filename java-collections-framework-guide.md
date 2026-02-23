# 📚 Java Collections Framework - Complete Guide

**Author:** Java Interview Preparation Guide  
**Target Audience:** Java Developers preparing for interviews

---

## 📋 Table of Contents

### Part 1: Collection Framework Overview
1. [What is Java Collections Framework?](#1-what-is-java-collections-framework)
2. [Collection Hierarchy](#2-collection-hierarchy)
3. [Collection vs Collections](#3-collection-vs-collections)

### Part 2: List Interface
4. [ArrayList](#4-arraylist)
5. [LinkedList](#5-linkedlist)
6. [Vector](#6-vector)
7. [ArrayList vs LinkedList vs Vector](#7-arraylist-vs-linkedlist-vs-vector)

### Part 3: Set Interface
8. [HashSet](#8-hashset)
9. [LinkedHashSet](#9-linkedhashset)
10. [TreeSet](#10-treeset)
11. [HashSet vs LinkedHashSet vs TreeSet](#11-hashset-vs-linkedhashset-vs-treeset)

### Part 4: Queue Interface
12. [Queue](#12-queue)
13. [PriorityQueue](#13-priorityqueue)
14. [Deque](#14-deque)
15. [ArrayDeque](#15-arraydeque)

### Part 5: Map Interface
16. [HashMap](#16-hashmap)
17. [LinkedHashMap](#17-linkedhashmap)
18. [TreeMap](#18-treemap)
19. [Hashtable](#19-hashtable)
20. [HashMap vs LinkedHashMap vs TreeMap vs Hashtable](#20-hashmap-vs-linkedhashmap-vs-treemap-vs-hashtable)

### Part 6: Concurrent Collections
21. [ConcurrentHashMap](#21-concurrenthashmap)
22. [CopyOnWriteArrayList](#22-copyonwritearraylist)
23. [ConcurrentLinkedQueue](#23-concurrentlinkedqueue)

### Part 7: Legacy Collections
24. [Stack](#24-stack)
25. [Properties](#25-properties)

### Part 8: Utility Classes
26. [Collections Utility Class](#26-collections-utility-class)
27. [Arrays Utility Class](#27-arrays-utility-class)

### Part 9: Advanced Topics
28. [Comparable vs Comparator](#28-comparable-vs-comparator)
29. [Fail-Fast vs Fail-Safe Iterators](#29-fail-fast-vs-fail-safe-iterators)
30. [Time & Space Complexity Cheat Sheet](#30-time--space-complexity-cheat-sheet)

---

# 🎯 PART 1: COLLECTION FRAMEWORK OVERVIEW

---

## 1. What is Java Collections Framework?

### Definition

The **Java Collections Framework (JCF)** is a unified architecture for representing and manipulating collections of objects. It provides:

- **Interfaces**: Abstract data types (List, Set, Map, Queue)
- **Implementations**: Concrete classes (ArrayList, HashMap, HashSet)
- **Algorithms**: Utility methods (sorting, searching, shuffling)

---

### Benefits

✅ **Reduced Programming Effort** - Ready-to-use data structures  
✅ **Increased Performance** - Optimized implementations  
✅ **Interoperability** - Common interface across collections  
✅ **Reusability** - Standard algorithms work on all collections  
✅ **Type Safety** - Generics prevent ClassCastException

---

### Core Components

```
Collection Framework
├── Interfaces (Contract)
│   ├── Collection
│   ├── List
│   ├── Set
│   ├── Queue
│   └── Map
│
├── Implementations (Classes)
│   ├── ArrayList, LinkedList, Vector
│   ├── HashSet, TreeSet, LinkedHashSet
│   ├── HashMap, TreeMap, LinkedHashMap
│   └── PriorityQueue, ArrayDeque
│
└── Algorithms (Utilities)
    ├── Collections.sort()
    ├── Collections.shuffle()
    └── Collections.binarySearch()
```

---

## 2. Collection Hierarchy

### Complete Hierarchy Diagram

```
                    Iterable<E>
                        ↑
                        |
                   Collection<E>
                        ↑
        ┌───────────────┼───────────────┐
        |               |               |
     List<E>         Set<E>          Queue<E>
        |               |               |
   ┌────┼────┐     ┌────┼────┐     ┌────┼────┐
   |    |    |     |    |    |     |    |    |
ArrayList  Vector  HashSet TreeSet PriorityQueue
LinkedList Stack   LinkedHashSet   Deque<E>
                   SortedSet<E>       |
                       |          ArrayDeque
                   NavigableSet<E> LinkedList


                    Map<E>
                      |
        ┌─────────────┼─────────────┐
        |             |             |
    HashMap      TreeMap       Hashtable
        |          SortedMap        
LinkedHashMap    NavigableMap
ConcurrentHashMap
```

---

### Interface Descriptions

| Interface | Ordered | Sorted | Duplicates | Null |
|-----------|---------|--------|------------|------|
| **Collection** | - | - | - | - |
| **List** | ✅ Yes | ❌ No | ✅ Yes | ✅ Yes |
| **Set** | ❌ No | ❌ No | ❌ No | ✅ Yes (one null) |
| **SortedSet** | ✅ Yes | ✅ Yes | ❌ No | ❌ No |
| **Queue** | ✅ Yes | ❌ No | ✅ Yes | Depends |
| **Map** | ❌ No | ❌ No | Keys: No, Values: Yes | Depends |

---

## 3. Collection vs Collections

### Collection (Interface)

- Root interface for all collection classes
- Defines basic operations: `add()`, `remove()`, `size()`, `iterator()`

```java
Collection<String> list = new ArrayList<>();
list.add("Java");
list.size();
```

---

### Collections (Utility Class)

- Provides static utility methods for collections
- Sorting, searching, synchronization, etc.

```java
Collections.sort(list);
Collections.reverse(list);
Collections.shuffle(list);
Collections.binarySearch(list, "Java");
```

---

### Example

```java
import java.util.*;

class CollectionVsCollections {
    public static void main(String[] args) {
        // Collection - Interface
        Collection<Integer> numbers = new ArrayList<>();
        numbers.add(5);
        numbers.add(2);
        numbers.add(8);
        numbers.add(1);
        
        System.out.println("Original: " + numbers);
        
        // Collections - Utility Class
        List<Integer> list = new ArrayList<>(numbers);
        Collections.sort(list);
        System.out.println("Sorted: " + list);
        
        Collections.reverse(list);
        System.out.println("Reversed: " + list);
        
        Collections.shuffle(list);
        System.out.println("Shuffled: " + list);
        
        int max = Collections.max(list);
        int min = Collections.min(list);
        System.out.println("Max: " + max + ", Min: " + min);
    }
}
```

**Output:**
```
Original: [5, 2, 8, 1]
Sorted: [1, 2, 5, 8]
Reversed: [8, 5, 2, 1]
Shuffled: [2, 8, 1, 5]
Max: 8, Min: 1
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 📝 PART 2: LIST INTERFACE

---

## 4. ArrayList

### What is ArrayList?

**ArrayList** is a **resizable array** implementation of the `List` interface. It's the most commonly used List implementation.

**Key Characteristics:**
- ✅ Dynamic array (grows automatically)
- ✅ Maintains insertion order
- ✅ Allows duplicates
- ✅ Allows null elements
- ✅ Fast random access - O(1)
- ✅ Not synchronized (not thread-safe)
- ✅ Implements RandomAccess marker interface

---

### Internal Working

**Backed by Array:**
```java
// Internal structure (simplified)
public class ArrayList<E> {
    private Object[] elementData;  // Stores elements
    private int size;               // Number of elements
    
    private static final int DEFAULT_CAPACITY = 10;
}
```

**Growth Strategy:**
- Initial capacity: 10 (default)
- Growth: `newCapacity = oldCapacity + (oldCapacity >> 1)` → **1.5x growth**
- Example: 10 → 15 → 22 → 33 → 49...

---

### ArrayList Methods

```java
import java.util.*;

class ArrayListMethods {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        
        // Adding elements
        list.add("Java");
        list.add("Python");
        list.add("C++");
        list.add(1, "JavaScript"); // Add at index
        System.out.println("After add: " + list);
        
        // Accessing elements
        String first = list.get(0);
        System.out.println("First element: " + first);
        
        // Modifying elements
        list.set(2, "Go");
        System.out.println("After set: " + list);
        
        // Removing elements
        list.remove("C++");
        System.out.println("After remove by value: " + list);
        
        list.remove(1);
        System.out.println("After remove by index: " + list);
        
        // Size and contains
        System.out.println("Size: " + list.size());
        System.out.println("Contains Java: " + list.contains("Java"));
        
        // Iterating
        System.out.print("Iteration: ");
        for (String lang : list) {
            System.out.print(lang + " ");
        }
        System.out.println();
        
        // Clear
        list.clear();
        System.out.println("After clear: " + list + " (isEmpty: " + list.isEmpty() + ")");
    }
}
```

**Output:**
```
After add: [Java, JavaScript, Python, C++]
First element: Java
After set: [Java, JavaScript, Go, C++]
After remove by value: [Java, JavaScript, Go]
After remove by index: [Java, Go]
Size: 2
Contains Java: true
Iteration: Java Go 
After clear: [] (isEmpty: true)
```

---

### Real-World Example: Student Management

```java
import java.util.*;

class Student {
    int id;
    String name;
    double marks;
    
    Student(int id, String name, double marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }
    
    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', marks=" + marks + "}";
    }
}

class StudentManagement {
    public static void main(String[] args) {
        ArrayList<Student> students = new ArrayList<>();
        
        // Add students
        students.add(new Student(101, "Alice", 85.5));
        students.add(new Student(102, "Bob", 78.0));
        students.add(new Student(103, "Charlie", 92.5));
        students.add(new Student(104, "Diana", 88.0));
        
        System.out.println("All Students:");
        students.forEach(System.out::println);
        
        // Find student by ID
        int searchId = 102;
        Student found = students.stream()
                                .filter(s -> s.id == searchId)
                                .findFirst()
                                .orElse(null);
        System.out.println("\nStudent with ID " + searchId + ": " + found);
        
        // Students with marks > 80
        System.out.println("\nStudents with marks > 80:");
        students.stream()
                .filter(s -> s.marks > 80)
                .forEach(System.out::println);
        
        // Remove student
        students.removeIf(s -> s.id == 102);
        System.out.println("\nAfter removing ID 102:");
        students.forEach(System.out::println);
    }
}
```

**Output:**
```
All Students:
Student{id=101, name='Alice', marks=85.5}
Student{id=102, name='Bob', marks=78.0}
Student{id=103, name='Charlie', marks=92.5}
Student{id=104, name='Diana', marks=88.0}

Student with ID 102: Student{id=102, name='Bob', marks=78.0}

Students with marks > 80:
Student{id=101, name='Alice', marks=85.5}
Student{id=103, name='Charlie', marks=92.5}
Student{id=104, name='Diana', marks=88.0}

After removing ID 102:
Student{id=101, name='Alice', marks=85.5}
Student{id=103, name='Charlie', marks=92.5}
Student{id=104, name='Diana', marks=88.0}
```

---

## 5. LinkedList

### What is LinkedList?

**LinkedList** is a **doubly-linked list** implementation of the `List` and `Deque` interfaces.

**Key Characteristics:**
- ✅ Doubly-linked nodes
- ✅ Maintains insertion order
- ✅ Allows duplicates
- ✅ Allows null elements
- ✅ Fast insertion/deletion - O(1) at ends
- ✅ Slow random access - O(n)
- ✅ Not synchronized

---

### Internal Structure

```
Node Structure:
┌─────────────────────┐
│  LinkedList<E>      │
│  ├─ first: Node     │ ───┐
│  ├─ last: Node      │    │
│  └─ size: int       │    │
└─────────────────────┘    │
                           │
    ┌──────────────────────┘
    ↓
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  Node   │    │  Node   │    │  Node   │    │  Node   │
│ prev: ◄─┼────┼─ prev ◄─┼────┼─ prev ◄─┼────┼─ prev   │
│ item: A │    │ item: B │    │ item: C │    │ item: D │
│ next: ──┼───►│ next: ──┼───►│ next: ──┼───►│ next: ● │
└─────────┘    └─────────┘    └─────────┘    └─────────┘
```

---

### LinkedList Methods

```java
import java.util.*;

class LinkedListMethods {
    public static void main(String[] args) {
        LinkedList<String> list = new LinkedList<>();
        
        // Add elements
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        System.out.println("Initial list: " + list);
        
        // Add at first/last (Deque operations)
        list.addFirst("Mango");
        list.addLast("Orange");
        System.out.println("After addFirst/Last: " + list);
        
        // Get first/last
        System.out.println("First: " + list.getFirst());
        System.out.println("Last: " + list.getLast());
        
        // Remove first/last
        String removed = list.removeFirst();
        System.out.println("Removed first: " + removed);
        System.out.println("After removeFirst: " + list);
        
        // Peek (doesn't remove)
        System.out.println("Peek first: " + list.peekFirst());
        System.out.println("Peek last: " + list.peekLast());
        
        // Poll (removes and returns, null if empty)
        String polled = list.poll();
        System.out.println("Polled: " + polled);
        System.out.println("After poll: " + list);
        
        // Use as Stack
        list.push("Top");
        System.out.println("After push: " + list);
        String popped = list.pop();
        System.out.println("Popped: " + popped);
        System.out.println("After pop: " + list);
    }
}
```

**Output:**
```
Initial list: [Apple, Banana, Cherry]
After addFirst/Last: [Mango, Apple, Banana, Cherry, Orange]
First: Mango
Last: Orange
Removed first: Mango
After removeFirst: [Apple, Banana, Cherry, Orange]
Peek first: Apple
Peek last: Orange
Polled: Apple
After poll: [Banana, Cherry, Orange]
After push: [Top, Banana, Cherry, Orange]
Popped: Top
After pop: [Banana, Cherry, Orange]
```

---

## 6. Vector

### What is Vector?

**Vector** is a **synchronized** version of ArrayList. It's a legacy class from Java 1.0.

**Key Characteristics:**
- ✅ Synchronized (thread-safe)
- ✅ Dynamic array
- ✅ Legacy class (avoid in new code)
- ✅ Growth: 2x (doubles capacity)
- ⚠️ Slower than ArrayList due to synchronization overhead

---

### Vector Example

```java
import java.util.*;

class VectorDemo {
    public static void main(String[] args) {
        Vector<Integer> vector = new Vector<>();
        
        // Adding elements
        vector.add(10);
        vector.add(20);
        vector.add(30);
        System.out.println("Vector: " + vector);
        
        // Capacity
        System.out.println("Size: " + vector.size());
        System.out.println("Capacity: " + vector.capacity()); // Default 10
        
        // Add more elements
        for (int i = 40; i <= 110; i += 10) {
            vector.add(i);
        }
        System.out.println("After adding 8 more elements:");
        System.out.println("Size: " + vector.size());
        System.out.println("Capacity: " + vector.capacity()); // Doubled to 20
        
        // Vector-specific methods
        vector.addElement(120); // Legacy method
        System.out.println("After addElement: " + vector);
        
        System.out.println("First element: " + vector.firstElement());
        System.out.println("Last element: " + vector.lastElement());
    }
}
```

**Output:**
```
Vector: [10, 20, 30]
Size: 3
Capacity: 10
After adding 8 more elements:
Size: 11
Capacity: 20
After addElement: [10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120]
First element: 10
Last element: 120
```

---

## 7. ArrayList vs LinkedList vs Vector

### Comparison Table

| Feature | ArrayList | LinkedList | Vector |
|---------|-----------|------------|--------|
| **Structure** | Dynamic Array | Doubly Linked List | Dynamic Array |
| **Random Access** | ✅ O(1) | ❌ O(n) | ✅ O(1) |
| **Insert at End** | ✅ O(1) amortized | ✅ O(1) | ✅ O(1) amortized |
| **Insert at Start** | ❌ O(n) | ✅ O(1) | ❌ O(n) |
| **Insert at Middle** | ❌ O(n) | ✅ O(1)* | ❌ O(n) |
| **Delete** | ❌ O(n) | ✅ O(1)* | ❌ O(n) |
| **Memory** | Low overhead | High (node objects) | Low overhead |
| **Thread-Safe** | ❌ No | ❌ No | ✅ Yes |
| **Growth** | 1.5x (50%) | N/A | 2x (100%) |
| **Iterator** | Fail-fast | Fail-fast | Fail-fast |
| **Use Case** | Random access | Frequent insert/delete | Legacy (avoid) |

\* After finding the position (which is O(n))

---

### Performance Comparison Example

```java
import java.util.*;

class ListPerformanceComparison {
    public static void main(String[] args) {
        int size = 100000;
        
        // Test ArrayList
        ArrayList<Integer> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
        }
        long end = System.nanoTime();
        System.out.println("ArrayList add at end: " + (end - start) / 1000000 + "ms");
        
        // Test LinkedList
        LinkedList<Integer> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            linkedList.add(i);
        }
        end = System.nanoTime();
        System.out.println("LinkedList add at end: " + (end - start) / 1000000 + "ms");
        
        // Random access - ArrayList wins
        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            arrayList.get(50000);
        }
        end = System.nanoTime();
        System.out.println("\nArrayList random access (10k gets): " + (end - start) / 1000000 + "ms");
        
        start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            linkedList.get(50000);
        }
        end = System.nanoTime();
        System.out.println("LinkedList random access (10k gets): " + (end - start) / 1000000 + "ms");
        
        // Insert at beginning - LinkedList wins
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            arrayList.add(0, i);
        }
        end = System.nanoTime();
        System.out.println("\nArrayList insert at start (1k): " + (end - start) / 1000000 + "ms");
        
        start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            linkedList.addFirst(i);
        }
        end = System.nanoTime();
        System.out.println("LinkedList insert at start (1k): " + (end - start) / 1000000 + "ms");
    }
}
```

**Output:**
```
ArrayList add at end: 15ms
LinkedList add at end: 25ms

ArrayList random access (10k gets): 0ms
LinkedList random access (10k gets): 2450ms

ArrayList insert at start (1k): 45ms
LinkedList insert at start (1k): 1ms
```

**Conclusion:**
- ✅ ArrayList: Best for **random access** and **iteration**
- ✅ LinkedList: Best for **frequent insert/delete** at ends
- ✅ Vector: Avoid in new code (use ArrayList with Collections.synchronizedList() instead)

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 🎲 PART 3: SET INTERFACE

---

## 8. HashSet

### What is HashSet?

**HashSet** is a collection that uses a **HashMap** internally to store elements. It doesn't allow duplicate elements.

**Key Characteristics:**
- ✅ No duplicates allowed
- ✅ Allows one null element
- ✅ No ordering (unordered)
- ✅ Fast operations - O(1) average
- ✅ Uses hashCode() and equals()
- ✅ Not synchronized

---

### Internal Working

```java
// HashSet uses HashMap internally
public class HashSet<E> {
    private transient HashMap<E, Object> map;
    private static final Object PRESENT = new Object();
    
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }
    
    public boolean contains(Object o) {
        return map.containsKey(o);
    }
}
```

**Key Point:** Elements are stored as **keys** in HashMap, with dummy `PRESENT` object as value.

---

### HashSet Example

```java
import java.util.*;

class HashSetDemo {
    public static void main(String[] args) {
        HashSet<String> set = new HashSet<>();
        
        // Adding elements
        set.add("Java");
        set.add("Python");
        set.add("C++");
        set.add("Java"); // Duplicate - won't be added
        System.out.println("HashSet: " + set);
        
        // Size
        System.out.println("Size: " + set.size());
        
        // Contains
        System.out.println("Contains Python: " + set.contains("Python"));
        System.out.println("Contains Ruby: " + set.contains("Ruby"));
        
        // Remove
        set.remove("C++");
        System.out.println("After remove: " + set);
        
        // Iteration (order not guaranteed)
        System.out.print("Iteration: ");
        for (String lang : set) {
            System.out.print(lang + " ");
        }
        System.out.println();
        
        // Add null
        set.add(null);
        System.out.println("After adding null: " + set);
        
        // Clear
        set.clear();
        System.out.println("After clear: " + set + " (isEmpty: " + set.isEmpty() + ")");
    }
}
```

**Output:**
```
HashSet: [Java, C++, Python]
Size: 3
Contains Python: true
Contains Ruby: false
After remove: [Java, Python]
Iteration: Java Python 
After adding null: [null, Java, Python]
After clear: [] (isEmpty: true)
```

---

## 9. LinkedHashSet

### What is LinkedHashSet?

**LinkedHashSet** extends HashSet and maintains a **doubly-linked list** running through all entries, preserving **insertion order**.

**Key Characteristics:**
- ✅ Maintains insertion order
- ✅ No duplicates
- ✅ Allows one null
- ✅ Slightly slower than HashSet
- ✅ Uses more memory (for linked list)

---

### LinkedHashSet Example

```java
import java.util.*;

class LinkedHashSetDemo {
    public static void main(String[] args) {
        // HashSet - No order guaranteed
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("Zebra");
        hashSet.add("Apple");
        hashSet.add("Mango");
        hashSet.add("Banana");
        System.out.println("HashSet: " + hashSet);
        
        // LinkedHashSet - Maintains insertion order
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("Zebra");
        linkedHashSet.add("Apple");
        linkedHashSet.add("Mango");
        linkedHashSet.add("Banana");
        System.out.println("LinkedHashSet: " + linkedHashSet);
        
        // Duplicates not allowed
        linkedHashSet.add("Apple");
        System.out.println("After adding duplicate: " + linkedHashSet);
    }
}
```

**Output:**
```
HashSet: [Apple, Mango, Banana, Zebra]
LinkedHashSet: [Zebra, Apple, Mango, Banana]
After adding duplicate: [Zebra, Apple, Mango, Banana]
```

---

## 10. TreeSet

### What is TreeSet?

**TreeSet** is a **sorted set** implementation based on a **Red-Black Tree** (TreeMap internally).

**Key Characteristics:**
- ✅ Elements sorted in natural order or by Comparator
- ✅ No duplicates
- ✅ No null elements (throws NullPointerException)
- ✅ O(log n) for add, remove, contains
- ✅ Implements NavigableSet interface

---

### TreeSet Example

```java
import java.util.*;

class TreeSetDemo {
    public static void main(String[] args) {
        TreeSet<Integer> set = new TreeSet<>();
        
        // Add elements (automatically sorted)
        set.add(50);
        set.add(20);
        set.add(80);
        set.add(10);
        set.add(40);
        System.out.println("TreeSet (sorted): " + set);
        
        // NavigableSet operations
        System.out.println("First: " + set.first());
        System.out.println("Last: " + set.last());
        System.out.println("Higher than 40: " + set.higher(40));
        System.out.println("Lower than 40: " + set.lower(40));
        System.out.println("Ceiling of 45: " + set.ceiling(45)); // >= 45
        System.out.println("Floor of 45: " + set.floor(45));     // <= 45
        
        // Subset operations
        System.out.println("HeadSet (<40): " + set.headSet(40));
        System.out.println("TailSet (>=40): " + set.tailSet(40));
        System.out.println("SubSet [20,80): " + set.subSet(20, 80));
        
        // Poll operations
        System.out.println("PollFirst: " + set.pollFirst());
        System.out.println("After pollFirst: " + set);
    }
}
```

**Output:**
```
TreeSet (sorted): [10, 20, 40, 50, 80]
First: 10
Last: 80
Higher than 40: 50
Lower than 40: 20
Ceiling of 45: 50
Floor of 45: 40
HeadSet (<40): [10, 20]
TailSet (>=40): [40, 50, 80]
SubSet [20,80): [20, 40, 50]
PollFirst: 10
After pollFirst: [20, 40, 50, 80]
```

---

### TreeSet with Custom Comparator

```java
import java.util.*;

class TreeSetComparatorDemo {
    public static void main(String[] args) {
        // Natural ordering (ascending)
        TreeSet<String> naturalOrder = new TreeSet<>();
        naturalOrder.add("Banana");
        naturalOrder.add("Apple");
        naturalOrder.add("Mango");
        naturalOrder.add("Cherry");
        System.out.println("Natural order: " + naturalOrder);
        
        // Custom comparator (descending)
        TreeSet<String> reverseOrder = new TreeSet<>(Comparator.reverseOrder());
        reverseOrder.add("Banana");
        reverseOrder.add("Apple");
        reverseOrder.add("Mango");
        reverseOrder.add("Cherry");
        System.out.println("Reverse order: " + reverseOrder);
        
        // Custom comparator by length
        TreeSet<String> byLength = new TreeSet<>(Comparator.comparingInt(String::length));
        byLength.add("Java");
        byLength.add("Python");
        byLength.add("C");
        byLength.add("JavaScript");
        System.out.println("By length: " + byLength);
    }
}
```

**Output:**
```
Natural order: [Apple, Banana, Cherry, Mango]
Reverse order: [Mango, Cherry, Banana, Apple]
By length: [C, Java, Python, JavaScript]
```

---

## 11. HashSet vs LinkedHashSet vs TreeSet

### Comparison Table

| Feature | HashSet | LinkedHashSet | TreeSet |
|---------|---------|---------------|---------|
| **Ordering** | ❌ No order | ✅ Insertion order | ✅ Sorted order |
| **Performance** | O(1) | O(1) | O(log n) |
| **Null** | ✅ One null | ✅ One null | ❌ No null |
| **Memory** | Low | Medium (linked list) | Medium (tree) |
| **Use Case** | Fast lookup | Ordered uniqueness | Sorted data |
| **Internal** | HashMap | HashMap + LinkedList | TreeMap (Red-Black Tree) |

---

### Side-by-Side Example

```java
import java.util.*;

class SetComparison {
    public static void main(String[] args) {
        String[] elements = {"Dog", "Cat", "Elephant", "Ant", "Bear"};
        
        // HashSet - No order
        Set<String> hashSet = new HashSet<>();
        for (String e : elements) hashSet.add(e);
        System.out.println("HashSet:       " + hashSet);
        
        // LinkedHashSet - Insertion order
        Set<String> linkedHashSet = new LinkedHashSet<>();
        for (String e : elements) linkedHashSet.add(e);
        System.out.println("LinkedHashSet: " + linkedHashSet);
        
        // TreeSet - Sorted order
        Set<String> treeSet = new TreeSet<>();
        for (String e : elements) treeSet.add(e);
        System.out.println("TreeSet:       " + treeSet);
    }
}
```

**Output:**
```
HashSet:       [Ant, Cat, Dog, Bear, Elephant]
LinkedHashSet: [Dog, Cat, Elephant, Ant, Bear]
TreeSet:       [Ant, Bear, Cat, Dog, Elephant]
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 🚦 PART 4: QUEUE INTERFACE

---

## 12. Queue

### What is Queue?

**Queue** is an interface that represents a collection designed for holding elements prior to processing. It follows **FIFO** (First-In-First-Out) order.

**Queue Methods:**

| Operation | Throws Exception | Returns Special Value |
|-----------|-----------------|----------------------|
| **Insert** | `add(e)` | `offer(e)` |
| **Remove** | `remove()` | `poll()` |
| **Examine** | `element()` | `peek()` |

---

### Queue Example

```java
import java.util.*;

class QueueDemo {
    public static void main(String[] args) {
        Queue<String> queue = new LinkedList<>();
        
        // Add elements (enqueue)
        queue.offer("Customer-1");
        queue.offer("Customer-2");
        queue.offer("Customer-3");
        System.out.println("Queue: " + queue);
        
        // Peek (doesn't remove)
        System.out.println("Peek: " + queue.peek());
        System.out.println("After peek: " + queue);
        
        // Poll (removes and returns)
        String served = queue.poll();
        System.out.println("Served: " + served);
        System.out.println("After poll: " + queue);
        
        // Process all
        System.out.println("\nProcessing all customers:");
        while (!queue.isEmpty()) {
            System.out.println("Serving: " + queue.poll());
        }
        
        System.out.println("Queue empty: " + queue.isEmpty());
    }
}
```

**Output:**
```
Queue: [Customer-1, Customer-2, Customer-3]
Peek: Customer-1
After peek: [Customer-1, Customer-2, Customer-3]
Served: Customer-1
After poll: [Customer-2, Customer-3]

Processing all customers:
Serving: Customer-2
Serving: Customer-3
Queue empty: true
```

---

## 13. PriorityQueue

### What is PriorityQueue?

**PriorityQueue** is a queue where elements are ordered by their **priority** (natural ordering or custom Comparator), not FIFO.

**Key Characteristics:**
- ✅ Elements ordered by priority
- ✅ No null elements
- ✅ Unbounded (grows automatically)
- ✅ O(log n) for add and poll
- ✅ O(1) for peek
- ✅ Uses binary heap internally

---

### PriorityQueue Example

```java
import java.util.*;

class PriorityQueueDemo {
    public static void main(String[] args) {
        // Min-heap (default - smallest first)
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        
        minHeap.offer(50);
        minHeap.offer(20);
        minHeap.offer(80);
        minHeap.offer(10);
        minHeap.offer(40);
        
        System.out.println("PriorityQueue: " + minHeap); // Not sorted, but heap structure
        System.out.println("Peek (min): " + minHeap.peek());
        
        System.out.println("\nPolling in priority order:");
        while (!minHeap.isEmpty()) {
            System.out.println(minHeap.poll());
        }
        
        // Max-heap (largest first)
        System.out.println("\nMax-Heap:");
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        maxHeap.offer(50);
        maxHeap.offer(20);
        maxHeap.offer(80);
        maxHeap.offer(10);
        
        while (!maxHeap.isEmpty()) {
            System.out.println(maxHeap.poll());
        }
    }
}
```

**Output:**
```
PriorityQueue: [10, 20, 80, 50, 40]
Peek (min): 10

Polling in priority order:
10
20
40
50
80

Max-Heap:
80
50
20
10
```

---

## 14. Deque

### What is Deque?

**Deque** (Double Ended Queue) is an interface that allows insertion and removal at **both ends**.

**Key Operations:**

| Operation | First Element | Last Element |
|-----------|--------------|--------------|
| **Insert** | `addFirst()`, `offerFirst()` | `addLast()`, `offerLast()` |
| **Remove** | `removeFirst()`, `pollFirst()` | `removeLast()`, `pollLast()` |
| **Examine** | `getFirst()`, `peekFirst()` | `getLast()`, `peekLast()` |

---

## 15. ArrayDeque

### What is ArrayDeque?

**ArrayDeque** is a resizable array implementation of the Deque interface. It's faster than LinkedList for queue operations.

**Key Characteristics:**
- ✅ Resizable array (circular buffer)
- ✅ No capacity restrictions
- ✅ Null elements not allowed
- ✅ Faster than LinkedList
- ✅ Not thread-safe

---

### ArrayDeque Example

```java
import java.util.*;

class ArrayDequeDemo {
    public static void main(String[] args) {
        Deque<String> deque = new ArrayDeque<>();
        
        // Add at both ends
        deque.addFirst("A");
        deque.addLast("B");
        deque.addFirst("C");
        deque.addLast("D");
        System.out.println("Deque: " + deque);
        
        // Peek at both ends
        System.out.println("PeekFirst: " + deque.peekFirst());
        System.out.println("PeekLast: " + deque.peekLast());
        
        // Remove from both ends
        System.out.println("RemoveFirst: " + deque.removeFirst());
        System.out.println("RemoveLast: " + deque.removeLast());
        System.out.println("After removals: " + deque);
        
        // Use as Stack (LIFO)
        System.out.println("\nUsing as Stack:");
        deque.push("X");
        deque.push("Y");
        deque.push("Z");
        System.out.println("After pushes: " + deque);
        System.out.println("Pop: " + deque.pop());
        System.out.println("Pop: " + deque.pop());
        System.out.println("After pops: " + deque);
    }
}
```

**Output:**
```
Deque: [C, A, B, D]
PeekFirst: C
PeekLast: D
RemoveFirst: C
RemoveLast: D
After removals: [A, B]

Using as Stack:
After pushes: [Z, Y, X, A, B]
Pop: Z
Pop: Y
After pops: [X, A, B]
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 🗺️ PART 5: MAP INTERFACE

---

## 16. HashMap

### What is HashMap?

**HashMap** is a hash table implementation of the Map interface. It stores key-value pairs and uses **hashing** for fast access.

**Key Characteristics:**
- ✅ Key-value pairs
- ✅ No duplicate keys (values can be duplicate)
- ✅ One null key, multiple null values
- ✅ No ordering
- ✅ O(1) average for get/put
- ✅ Not synchronized

---

### Internal Structure (Java 8+)

```
HashMap Structure:
┌─────────────────────────────────────┐
│  HashMap<K,V>                       │
│  ├─ Node<K,V>[] table (array)      │
│  ├─ size (number of entries)       │
│  ├─ threshold (capacity * loadFactor)
│  └─ loadFactor (default 0.75)      │
└─────────────────────────────────────┘
           ↓
    Array of Buckets (default 16)
    ┌───┬───┬───┬───┬───┬───┐
    │ 0 │ 1 │ 2 │ 3 │ 4 │...│
    └─┬─┴───┴─┬─┴───┴───┴───┘
      │       │
      ↓       ↓
   LinkedList   Tree (if >8 nodes)
   Node → Node   Red-Black Tree
```

**Process:**
1. **hash(key)** → compute hash code
2. **index = hash & (n-1)** → find bucket
3. **equals()** → find exact match in bucket
4. If **load factor exceeded** → **rehash** (double capacity)

---

### HashMap Methods

```java
import java.util.*;

class HashMapMethods {
    public static void main(String[] args) {
        HashMap<String, Integer> map = new HashMap<>();
        
        // Put key-value pairs
        map.put("Alice", 85);
        map.put("Bob", 78);
        map.put("Charlie", 92);
        map.put("Diana", 88);
        System.out.println("HashMap: " + map);
        
        // Get value by key
        System.out.println("Alice's score: " + map.get("Alice"));
        System.out.println("Eve's score: " + map.get("Eve")); // null
        
        // Check if key/value exists
        System.out.println("Contains key Bob: " + map.containsKey("Bob"));
        System.out.println("Contains value 92: " + map.containsValue(92));
        
        // Replace value
        map.put("Alice", 90); // Overwrites existing
        System.out.println("After update: " + map);
        
        // putIfAbsent (Java 8)
        map.putIfAbsent("Alice", 95); // Won't update
        map.putIfAbsent("Eve", 87);   // Will add
        System.out.println("After putIfAbsent: " + map);
        
        // Remove
        map.remove("Bob");
        System.out.println("After remove: " + map);
        
        // Size
        System.out.println("Size: " + map.size());
        
        // Iterate
        System.out.println("\nIterating:");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        // Java 8 forEach
        System.out.println("\nJava 8 forEach:");
        map.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
```

**Output:**
```
HashMap: {Bob=78, Alice=85, Charlie=92, Diana=88}
Alice's score: 85
Eve's score: null
Contains key Bob: true
Contains value 92: true
After update: {Bob=78, Alice=90, Charlie=92, Diana=88}
After putIfAbsent: {Bob=78, Alice=90, Eve=87, Charlie=92, Diana=88}
After remove: {Alice=90, Eve=87, Charlie=92, Diana=88}
Size: 4

Iterating:
Alice = 90
Eve = 87
Charlie = 92
Diana = 88

Java 8 forEach:
Alice: 90
Eve: 87
Charlie: 92
Diana: 88
```

---

## 17. LinkedHashMap

### What is LinkedHashMap?

**LinkedHashMap** extends HashMap and maintains a **doubly-linked list** running through all entries, preserving **insertion order** or **access order**.

**Key Characteristics:**
- ✅ Maintains insertion order (or access order)
- ✅ Slightly slower than HashMap
- ✅ Useful for LRU cache implementation

---

### LinkedHashMap Example

```java
import java.util.*;

class LinkedHashMapDemo {
    public static void main(String[] args) {
        // Insertion order
        Map<String, Integer> insertionOrder = new LinkedHashMap<>();
        insertionOrder.put("Zebra", 1);
        insertionOrder.put("Apple", 2);
        insertionOrder.put("Mango", 3);
        insertionOrder.put("Banana", 4);
        System.out.println("Insertion order: " + insertionOrder);
        
        // Access order (LRU)
        Map<String, Integer> accessOrder = new LinkedHashMap<>(16, 0.75f, true);
        accessOrder.put("A", 1);
        accessOrder.put("B", 2);
        accessOrder.put("C", 3);
        accessOrder.put("D", 4);
        System.out.println("\nInitial: " + accessOrder);
        
        // Access some elements
        accessOrder.get("A");
        System.out.println("After accessing A: " + accessOrder);
        
        accessOrder.get("C");
        System.out.println("After accessing C: " + accessOrder);
    }
}
```

**Output:**
```
Insertion order: {Zebra=1, Apple=2, Mango=3, Banana=4}

Initial: {A=1, B=2, C=3, D=4}
After accessing A: {B=2, C=3, D=4, A=1}
After accessing C: {B=2, D=4, A=1, C=3}
```

---

### LRU Cache Implementation

```java
import java.util.*;

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // true = access-order
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
    
    public static void main(String[] args) {
        LRUCache<String, Integer> cache = new LRUCache<>(3);
        
        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        System.out.println("Cache: " + cache);
        
        cache.get("A"); // Access A
        System.out.println("After accessing A: " + cache);
        
        cache.put("D", 4); // Evicts B (least recently used)
        System.out.println("After adding D: " + cache);
        
        cache.put("E", 5); // Evicts C
        System.out.println("After adding E: " + cache);
    }
}
```

**Output:**
```
Cache: {A=1, B=2, C=3}
After accessing A: {B=2, C=3, A=1}
After adding D: {C=3, A=1, D=4}
After adding E: {A=1, D=4, E=5}
```

---

## 18. TreeMap

### What is TreeMap?

**TreeMap** is a **sorted map** implementation based on a **Red-Black Tree**.

**Key Characteristics:**
- ✅ Keys sorted in natural order or by Comparator
- ✅ O(log n) for get, put, remove
- ✅ No null keys (but null values allowed)
- ✅ Implements NavigableMap interface

---

### TreeMap Example

```java
import java.util.*;

class TreeMapDemo {
    public static void main(String[] args) {
        TreeMap<String, Integer> map = new TreeMap<>();
        
        // Add entries (automatically sorted by key)
        map.put("Zebra", 100);
        map.put("Apple", 50);
        map.put("Mango", 75);
        map.put("Banana", 60);
        System.out.println("TreeMap (sorted): " + map);
        
        // NavigableMap operations
        System.out.println("First key: " + map.firstKey());
        System.out.println("Last key: " + map.lastKey());
        System.out.println("Higher key than Mango: " + map.higherKey("Mango"));
        System.out.println("Lower key than Mango: " + map.lowerKey("Mango"));
        
        // Subset views
        System.out.println("HeadMap (<Mango): " + map.headMap("Mango"));
        System.out.println("TailMap (>=Mango): " + map.tailMap("Mango"));
        System.out.println("SubMap [Banana,Zebra): " + map.subMap("Banana", "Zebra"));
        
        // Poll operations
        System.out.println("PollFirstEntry: " + map.pollFirstEntry());
        System.out.println("After poll: " + map);
    }
}
```

**Output:**
```
TreeMap (sorted): {Apple=50, Banana=60, Mango=75, Zebra=100}
First key: Apple
Last key: Zebra
Higher key than Mango: Zebra
Lower key than Mango: Banana
HeadMap (<Mango): {Apple=50, Banana=60}
TailMap (>=Mango): {Mango=75, Zebra=100}
SubMap [Banana,Zebra): {Banana=60, Mango=75}
PollFirstEntry: Apple=50
After poll: {Banana=60, Mango=75, Zebra=100}
```

---

## 19. Hashtable

### What is Hashtable?

**Hashtable** is a **synchronized** implementation of Map. It's a legacy class from Java 1.0.

**Key Characteristics:**
- ✅ Synchronized (thread-safe)
- ✅ No null keys or values
- ✅ Legacy class (avoid in new code)
- ⚠️ Slower than HashMap due to synchronization

---

### Hashtable Example

```java
import java.util.*;

class HashtableDemo {
    public static void main(String[] args) {
        Hashtable<String, Integer> table = new Hashtable<>();
        
        table.put("One", 1);
        table.put("Two", 2);
        table.put("Three", 3);
        System.out.println("Hashtable: " + table);
        
        // No null key or value
        try {
            table.put(null, 4);
        } catch (NullPointerException e) {
            System.out.println("Cannot add null key: " + e.getClass().getSimpleName());
        }
        
        try {
            table.put("Four", null);
        } catch (NullPointerException e) {
            System.out.println("Cannot add null value: " + e.getClass().getSimpleName());
        }
        
        System.out.println("Final table: " + table);
    }
}
```

**Output:**
```
Hashtable: {Three=3, Two=2, One=1}
Cannot add null key: NullPointerException
Cannot add null value: NullPointerException
Final table: {Three=3, Two=2, One=1}
```

---

## 20. HashMap vs LinkedHashMap vs TreeMap vs Hashtable

### Comparison Table

| Feature | HashMap | LinkedHashMap | TreeMap | Hashtable |
|---------|---------|---------------|---------|-----------|
| **Ordering** | ❌ No order | ✅ Insertion order | ✅ Sorted order | ❌ No order |
| **Performance** | O(1) | O(1) | O(log n) | O(1) |
| **Null Key** | ✅ One null | ✅ One null | ❌ No null | ❌ No null |
| **Null Value** | ✅ Multiple | ✅ Multiple | ✅ Multiple | ❌ No null |
| **Thread-Safe** | ❌ No | ❌ No | ❌ No | ✅ Yes |
| **Internal** | Array + List/Tree | HashMap + LinkedList | Red-Black Tree | Array + List |
| **Use Case** | General purpose | Ordered map | Sorted keys | Legacy (avoid) |
| **Since** | Java 1.2 | Java 1.4 | Java 1.2 | Java 1.0 |

---

### Side-by-Side Example

```java
import java.util.*;

class MapComparison {
    public static void main(String[] args) {
        String[] keys = {"Dog", "Cat", "Elephant", "Ant", "Bear"};
        
        // HashMap - No order
        Map<String, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            hashMap.put(keys[i], i + 1);
        }
        System.out.println("HashMap:       " + hashMap);
        
        // LinkedHashMap - Insertion order
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) {
            linkedHashMap.put(keys[i], i + 1);
        }
        System.out.println("LinkedHashMap: " + linkedHashMap);
        
        // TreeMap - Sorted order
        Map<String, Integer> treeMap = new TreeMap<>();
        for (int i = 0; i < keys.length; i++) {
            treeMap.put(keys[i], i + 1);
        }
        System.out.println("TreeMap:       " + treeMap);
    }
}
```

**Output:**
```
HashMap:       {Ant=4, Cat=2, Dog=1, Bear=5, Elephant=3}
LinkedHashMap: {Dog=1, Cat=2, Elephant=3, Ant=4, Bear=5}
TreeMap:       {Ant=4, Bear=5, Cat=2, Dog=1, Elephant=3}
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 🔐 PART 6: CONCURRENT COLLECTIONS

---

## 21. ConcurrentHashMap

### What is ConcurrentHashMap?

**ConcurrentHashMap** is a **thread-safe** implementation of HashMap that allows concurrent reads and writes without locking the entire map.

**Key Characteristics:**
- ✅ Thread-safe without full synchronization
- ✅ Lock striping / CAS operations (Java 8+)
- ✅ No null keys or values
- ✅ Better performance than synchronized HashMap
- ✅ Fail-safe iterator

---

### ConcurrentHashMap vs HashMap vs Hashtable

| Feature | HashMap | ConcurrentHashMap | Hashtable |
|---------|---------|------------------|-----------|
| **Thread-Safe** | ❌ No | ✅ Yes | ✅ Yes |
| **Null Key/Value** | ✅ Yes | ❌ No | ❌ No |
| **Locking** | N/A | Segment/CAS | Full map |
| **Performance** | Fastest | Fast (concurrent) | Slowest |
| **Iterator** | Fail-fast | Fail-safe | Fail-fast |

---

### ConcurrentHashMap Example

```java
import java.util.concurrent.*;

class ConcurrentHashMapDemo {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        
        // Multiple threads updating same map
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                map.put("T1-" + i, i);
                System.out.println("Thread-1 added: T1-" + i);
            }
        });
        
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                map.put("T2-" + i, i);
                System.out.println("Thread-2 added: T2-" + i);
            }
        });
        
        t1.start();
        t2.start();
        
        t1.join();
        t2.join();
        
        System.out.println("\nFinal map size: " + map.size());
        System.out.println("Map: " + map);
    }
}
```

**Output:**
```
Thread-1 added: T1-1
Thread-2 added: T2-1
Thread-1 added: T1-2
Thread-2 added: T2-2
Thread-1 added: T1-3
Thread-2 added: T2-3
Thread-1 added: T1-4
Thread-2 added: T2-4
Thread-1 added: T1-5
Thread-2 added: T2-5

Final map size: 10
Map: {T2-1=1, T1-1=1, T2-2=2, T1-2=2, T2-3=3, T1-3=3, T2-4=4, T1-4=4, T2-5=5, T1-5=5}
```

---

### Java 8 Atomic Operations

```java
import java.util.concurrent.*;

class ConcurrentHashMapAtomicOps {
    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        
        map.put("count", 10);
        System.out.println("Initial: " + map);
        
        // compute - atomically compute new value
        map.compute("count", (key, value) -> value + 5);
        System.out.println("After compute: " + map);
        
        // computeIfAbsent
        map.computeIfAbsent("new", key -> 100);
        System.out.println("After computeIfAbsent: " + map);
        
        // computeIfPresent
        map.computeIfPresent("count", (key, value) -> value * 2);
        System.out.println("After computeIfPresent: " + map);
        
        // merge
        map.merge("count", 10, (oldVal, newVal) -> oldVal + newVal);
        System.out.println("After merge: " + map);
        
        // getOrDefault
        System.out.println("Get existing: " + map.getOrDefault("count", 0));
        System.out.println("Get missing: " + map.getOrDefault("missing", 999));
    }
}
```

**Output:**
```
Initial: {count=10}
After compute: {count=15}
After computeIfAbsent: {new=100, count=15}
After computeIfPresent: {new=100, count=30}
After merge: {new=100, count=40}
Get existing: 40
Get missing: 999
```

---

## 22. CopyOnWriteArrayList

### What is CopyOnWriteArrayList?

**CopyOnWriteArrayList** is a thread-safe variant of ArrayList where all mutative operations create a **fresh copy** of the underlying array.

**Key Characteristics:**
- ✅ Thread-safe without synchronization
- ✅ Best for **read-heavy** scenarios
- ✅ Fail-safe iterator (no ConcurrentModificationException)
- ⚠️ Expensive writes (copies entire array)
- ⚠️ Memory intensive

---

### CopyOnWriteArrayList Example

```java
import java.util.concurrent.*;
import java.util.*;

class CopyOnWriteArrayListDemo {
    public static void main(String[] args) throws InterruptedException {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        
        list.add("Java");
        list.add("Python");
        list.add("C++");
        
        // Writer thread
        Thread writer = new Thread(() -> {
            try {
                Thread.sleep(100);
                list.add("Go");
                System.out.println("Writer added: Go");
                
                Thread.sleep(100);
                list.add("Rust");
                System.out.println("Writer added: Rust");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        // Reader thread - can iterate while writes happen
        Thread reader = new Thread(() -> {
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                System.out.println("Reader reading: " + iterator.next());
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        
        writer.start();
        reader.start();
        
        writer.join();
        reader.join();
        
        System.out.println("\nFinal list: " + list);
    }
}
```

**Output:**
```
Reader reading: Java
Writer added: Go
Reader reading: Python
Writer added: Rust
Reader reading: C++

Final list: [Java, Python, C++, Go, Rust]
```

**Note:** Iterator sees snapshot at time of creation, doesn't see concurrent modifications.

---

## 23. ConcurrentLinkedQueue

### What is ConcurrentLinkedQueue?

**ConcurrentLinkedQueue** is an unbounded, thread-safe, **non-blocking** queue based on linked nodes.

**Key Characteristics:**
- ✅ Thread-safe (lock-free using CAS)
- ✅ Unbounded
- ✅ FIFO ordering
- ✅ No null elements
- ✅ Fail-safe iterator

---

### ConcurrentLinkedQueue Example

```java
import java.util.concurrent.*;

class ConcurrentLinkedQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        
        // Producer threads
        Thread producer1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                queue.offer("P1-" + i);
                System.out.println("Producer-1: " + "P1-" + i);
            }
        });
        
        Thread producer2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                queue.offer("P2-" + i);
                System.out.println("Producer-2: " + "P2-" + i);
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(500);
                while (!queue.isEmpty()) {
                    String item = queue.poll();
                    if (item != null) {
                        System.out.println("Consumer consumed: " + item);
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        
        producer1.start();
        producer2.start();
        consumer.start();
        
        producer1.join();
        producer2.join();
        consumer.join();
        
        System.out.println("\nQueue size: " + queue.size());
    }
}
```

**Output:**
```
Producer-1: P1-1
Producer-2: P2-1
Producer-1: P1-2
Producer-2: P2-2
Producer-1: P1-3
Producer-2: P2-3
Producer-1: P1-4
Producer-2: P2-4
Producer-1: P1-5
Producer-2: P2-5
Consumer consumed: P1-1
Consumer consumed: P2-1
Consumer consumed: P1-2
Consumer consumed: P2-2
...
Queue size: 0
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 📚 PART 7: LEGACY COLLECTIONS

---

## 24. Stack

### What is Stack?

**Stack** is a **LIFO** (Last-In-First-Out) data structure. It extends Vector.

**Key Characteristics:**
- ✅ LIFO order
- ✅ Synchronized (thread-safe)
- ✅ Legacy class
- ⚠️ Avoid in new code (use Deque instead)

---

### Stack Example

```java
import java.util.*;

class StackDemo {
    public static void main(String[] args) {
        Stack<String> stack = new Stack<>();
        
        // Push elements
        stack.push("First");
        stack.push("Second");
        stack.push("Third");
        System.out.println("Stack: " + stack);
        
        // Peek (doesn't remove)
        System.out.println("Peek: " + stack.peek());
        System.out.println("After peek: " + stack);
        
        // Pop (removes and returns)
        System.out.println("\nPopping elements:");
        while (!stack.isEmpty()) {
            System.out.println("Pop: " + stack.pop());
        }
        
        System.out.println("Stack empty: " + stack.isEmpty());
        
        // Modern alternative: ArrayDeque
        System.out.println("\n✅ Modern way (ArrayDeque):");
        Deque<String> deque = new ArrayDeque<>();
        deque.push("A");
        deque.push("B");
        deque.push("C");
        System.out.println("Deque as stack: " + deque);
        System.out.println("Pop: " + deque.pop());
    }
}
```

**Output:**
```
Stack: [First, Second, Third]
Peek: Third
After peek: [First, Second, Third]

Popping elements:
Pop: Third
Pop: Second
Pop: First
Stack empty: true

✅ Modern way (ArrayDeque):
Deque as stack: [C, B, A]
Pop: C
```

---

## 25. Properties

### What is Properties?

**Properties** is a subclass of Hashtable used for **key-value pairs** where both key and value are **Strings**. Commonly used for configuration files.

---

### Properties Example

```java
import java.util.*;
import java.io.*;

class PropertiesDemo {
    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        
        // Set properties
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "5432");
        props.setProperty("db.username", "admin");
        props.setProperty("db.password", "secret");
        
        System.out.println("Properties: " + props);
        
        // Get property
        String host = props.getProperty("db.host");
        System.out.println("Database host: " + host);
        
        // Get with default value
        String timeout = props.getProperty("db.timeout", "30");
        System.out.println("Database timeout: " + timeout);
        
        // Save to file
        try (FileOutputStream out = new FileOutputStream("config.properties")) {
            props.store(out, "Database Configuration");
            System.out.println("\n✅ Saved to config.properties");
        }
        
        // Load from file
        Properties loadedProps = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            loadedProps.load(in);
            System.out.println("✅ Loaded from file: " + loadedProps);
        }
    }
}
```

**Output:**
```
Properties: {db.password=secret, db.host=localhost, db.port=5432, db.username=admin}
Database host: localhost
Database timeout: 30

✅ Saved to config.properties
✅ Loaded from file: {db.password=secret, db.host=localhost, db.port=5432, db.username=admin}
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 🛠️ PART 8: UTILITY CLASSES

---

## 26. Collections Utility Class

### Common Methods

```java
import java.util.*;

class CollectionsUtilityDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3));
        System.out.println("Original: " + list);
        
        // 1. sort()
        Collections.sort(list);
        System.out.println("After sort: " + list);
        
        // 2. reverse()
        Collections.reverse(list);
        System.out.println("After reverse: " + list);
        
        // 3. shuffle()
        Collections.shuffle(list);
        System.out.println("After shuffle: " + list);
        
        // 4. binarySearch() (list must be sorted)
        Collections.sort(list);
        int index = Collections.binarySearch(list, 5);
        System.out.println("Binary search for 5: index = " + index);
        
        // 5. min() and max()
        System.out.println("Min: " + Collections.min(list));
        System.out.println("Max: " + Collections.max(list));
        
        // 6. frequency()
        list.add(5);
        list.add(5);
        System.out.println("Frequency of 5: " + Collections.frequency(list, 5));
        
        // 7. fill()
        Collections.fill(list, 0);
        System.out.println("After fill: " + list);
        
        // 8. copy()
        List<Integer> source = Arrays.asList(10, 20, 30);
        List<Integer> dest = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));
        Collections.copy(dest, source);
        System.out.println("After copy: " + dest);
        
        // 9. swap()
        Collections.swap(dest, 0, 2);
        System.out.println("After swap(0,2): " + dest);
        
        // 10. rotate()
        Collections.rotate(dest, 2);
        System.out.println("After rotate(2): " + dest);
        
        // 11. replaceAll()
        Collections.replaceAll(dest, 0, 99);
        System.out.println("After replaceAll(0->99): " + dest);
    }
}
```

**Output:**
```
Original: [5, 2, 8, 1, 9, 3]
After sort: [1, 2, 3, 5, 8, 9]
After reverse: [9, 8, 5, 3, 2, 1]
After shuffle: [3, 9, 1, 2, 8, 5]
Binary search for 5: index = 2
Min: 1
Max: 9
Frequency of 5: 3
After fill: [0, 0, 0, 0, 0, 0, 0, 0]
After copy: [10, 20, 30, 0, 0]
After swap(0,2): [30, 20, 10, 0, 0]
After rotate(2): [0, 0, 30, 20, 10]
After replaceAll(0->99): [99, 99, 30, 20, 10]
```

---

### Synchronization Wrappers

```java
import java.util.*;

class SynchronizedCollections {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        Map<String, Integer> map = new HashMap<>();
        
        // Create synchronized versions
        List<String> syncList = Collections.synchronizedList(list);
        Set<String> syncSet = Collections.synchronizedSet(set);
        Map<String, Integer> syncMap = Collections.synchronizedMap(map);
        
        // Add elements
        syncList.add("Java");
        syncSet.add("Python");
        syncMap.put("C++", 1);
        
        System.out.println("Synchronized List: " + syncList);
        System.out.println("Synchronized Set: " + syncSet);
        System.out.println("Synchronized Map: " + syncMap);
        
        // ⚠️ Must synchronize during iteration
        synchronized (syncList) {
            for (String s : syncList) {
                System.out.println(s);
            }
        }
    }
}
```

**Output:**
```
Synchronized List: [Java]
Synchronized Set: [Python]
Synchronized Map: {C++=1}
Java
```

---

### Unmodifiable Collections

```java
import java.util.*;

class UnmodifiableCollections {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
        
        // Create unmodifiable view
        List<String> unmodifiableList = Collections.unmodifiableList(list);
        System.out.println("Unmodifiable list: " + unmodifiableList);
        
        // Try to modify - throws UnsupportedOperationException
        try {
            unmodifiableList.add("D");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify: " + e.getClass().getSimpleName());
        }
        
        // But original list can be modified
        list.add("D");
        System.out.println("After modifying original: " + unmodifiableList);
        
        // Java 9+ immutable collections (truly immutable)
        List<String> immutable = List.of("X", "Y", "Z");
        System.out.println("Java 9 immutable: " + immutable);
        
        try {
            immutable.add("W");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify immutable: " + e.getClass().getSimpleName());
        }
    }
}
```

**Output:**
```
Unmodifiable list: [A, B, C]
Cannot modify: UnsupportedOperationException
After modifying original: [A, B, C, D]
Java 9 immutable: [X, Y, Z]
Cannot modify immutable: UnsupportedOperationException
```

---

## 27. Arrays Utility Class

### Common Methods

```java
import java.util.*;

class ArraysUtilityDemo {
    public static void main(String[] args) {
        // 1. toString()
        int[] arr = {5, 2, 8, 1, 9};
        System.out.println("Array: " + Arrays.toString(arr));
        
        // 2. sort()
        Arrays.sort(arr);
        System.out.println("After sort: " + Arrays.toString(arr));
        
        // 3. binarySearch()
        int index = Arrays.binarySearch(arr, 8);
        System.out.println("Binary search for 8: index = " + index);
        
        // 4. fill()
        int[] filled = new int[5];
        Arrays.fill(filled, 10);
        System.out.println("Filled array: " + Arrays.toString(filled));
        
        // 5. copyOf()
        int[] copy = Arrays.copyOf(arr, 7); // Extends with zeros
        System.out.println("Copy (length 7): " + Arrays.toString(copy));
        
        // 6. copyOfRange()
        int[] range = Arrays.copyOfRange(arr, 1, 4);
        System.out.println("Copy range [1,4): " + Arrays.toString(range));
        
        // 7. equals()
        int[] arr2 = {1, 2, 5, 8, 9};
        System.out.println("Arrays equal: " + Arrays.equals(arr, arr2));
        
        // 8. asList() - Array to List
        List<String> list = Arrays.asList("A", "B", "C");
        System.out.println("Array as List: " + list);
        
        // 9. stream() - Java 8
        int sum = Arrays.stream(arr).sum();
        System.out.println("Sum using stream: " + sum);
        
        // 10. parallelSort() - Java 8 (faster for large arrays)
        int[] large = {9, 3, 7, 1, 5};
        Arrays.parallelSort(large);
        System.out.println("Parallel sort: " + Arrays.toString(large));
    }
}
```

**Output:**
```
Array: [5, 2, 8, 1, 9]
After sort: [1, 2, 5, 8, 9]
Binary search for 8: index = 3
Filled array: [10, 10, 10, 10, 10]
Copy (length 7): [1, 2, 5, 8, 9, 0, 0]
Copy range [1,4): [2, 5, 8]
Arrays equal: true
Array as List: [A, B, C]
Sum using stream: 25
Parallel sort: [1, 3, 5, 7, 9]
```

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

---

# 🎓 PART 9: ADVANCED TOPICS

---

## 28. Comparable vs Comparator

### Comparable Interface

**Used for:** Natural ordering of objects (default sorting)

**Method:** `int compareTo(T o)`

**Location:** Inside the class being compared

---

### Comparable Example

```java
import java.util.*;

class Employee implements Comparable<Employee> {
    int id;
    String name;
    double salary;
    
    Employee(int id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }
    
    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id); // Sort by ID
    }
    
    @Override
    public String toString() {
        return "Emp{id=" + id + ", name='" + name + "', salary=" + salary + "}";
    }
    
    public static void main(String[] args) {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(103, "Alice", 75000));
        employees.add(new Employee(101, "Bob", 60000));
        employees.add(new Employee(102, "Charlie", 80000));
        
        System.out.println("Before sorting:");
        employees.forEach(System.out::println);
        
        Collections.sort(employees); // Uses compareTo()
        
        System.out.println("\nAfter sorting (by ID):");
        employees.forEach(System.out::println);
    }
}
```

**Output:**
```
Before sorting:
Emp{id=103, name='Alice', salary=75000.0}
Emp{id=101, name='Bob', salary=60000.0}
Emp{id=102, name='Charlie', salary=80000.0}

After sorting (by ID):
Emp{id=101, name='Bob', salary=60000.0}
Emp{id=102, name='Charlie', salary=80000.0}
Emp{id=103, name='Alice', salary=75000.0}
```

---

### Comparator Interface

**Used for:** Custom ordering (multiple sorting strategies)

**Method:** `int compare(T o1, T o2)`

**Location:** Separate class or lambda

---

### Comparator Example

```java
import java.util.*;

class Employee2 {
    int id;
    String name;
    double salary;
    
    Employee2(int id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }
    
    @Override
    public String toString() {
        return "Emp{id=" + id + ", name='" + name + "', salary=" + salary + "}";
    }
}

class ComparatorDemo {
    public static void main(String[] args) {
        List<Employee2> employees = new ArrayList<>();
        employees.add(new Employee2(103, "Alice", 75000));
        employees.add(new Employee2(101, "Bob", 60000));
        employees.add(new Employee2(102, "Charlie", 80000));
        
        // Sort by name
        Collections.sort(employees, Comparator.comparing(e -> e.name));
        System.out.println("Sorted by name:");
        employees.forEach(System.out::println);
        
        // Sort by salary (descending)
        Collections.sort(employees, Comparator.comparingDouble((Employee2 e) -> e.salary).reversed());
        System.out.println("\nSorted by salary (desc):");
        employees.forEach(System.out::println);
        
        // Sort by multiple fields
        Collections.sort(employees, 
            Comparator.comparingDouble((Employee2 e) -> e.salary)
                      .thenComparing(e -> e.name));
        System.out.println("\nSorted by salary, then name:");
        employees.forEach(System.out::println);
    }
}
```

**Output:**
```
Sorted by name:
Emp{id=103, name='Alice', salary=75000.0}
Emp{id=101, name='Bob', salary=60000.0}
Emp{id=102, name='Charlie', salary=80000.0}

Sorted by salary (desc):
Emp{id=102, name='Charlie', salary=80000.0}
Emp{id=103, name='Alice', salary=75000.0}
Emp{id=101, name='Bob', salary=60000.0}

Sorted by salary, then name:
Emp{id=101, name='Bob', salary=60000.0}
Emp{id=103, name='Alice', salary=75000.0}
Emp{id=102, name='Charlie', salary=80000.0}
```

---

### Comparable vs Comparator - Key Differences

| Aspect | Comparable | Comparator |
|--------|-----------|------------|
| **Package** | `java.lang` | `java.util` |
| **Method** | `compareTo(T o)` | `compare(T o1, T o2)` |
| **Location** | Inside class | Separate class/lambda |
| **Sorting** | Single natural order | Multiple custom orders |
| **Modification** | Requires class modification | No class modification |
| **Use Case** | Default sorting | Custom sorting strategies |

---

## 29. Fail-Fast vs Fail-Safe Iterators

### Fail-Fast Iterator

**Behavior:** Throws `ConcurrentModificationException` if collection is modified during iteration.

**Collections:** ArrayList, HashMap, HashSet, TreeMap, etc.

---

### Fail-Fast Example

```java
import java.util.*;

class FailFastDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        System.out.println("Original list: " + list);
        
        try {
            for (String item : list) {
                System.out.println("Processing: " + item);
                if (item.equals("B")) {
                    list.remove(item); // ❌ Modifying during iteration
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("\n❌ ConcurrentModificationException: " + e.getClass().getSimpleName());
        }
        
        // ✅ Correct way: Use Iterator.remove()
        System.out.println("\n✅ Correct way:");
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            System.out.println("Processing: " + item);
            if (item.equals("C")) {
                iterator.remove(); // Safe removal
            }
        }
        System.out.println("After safe removal: " + list);
    }
}
```

**Output:**
```
Original list: [A, B, C, D]
Processing: A
Processing: B

❌ ConcurrentModificationException: ConcurrentModificationException

✅ Correct way:
Processing: A
Processing: B
Processing: C
Processing: D
After safe removal: [A, B, D]
```

---

### Fail-Safe Iterator

**Behavior:** Works on a **clone/snapshot** of the collection. Doesn't throw exception.

**Collections:** CopyOnWriteArrayList, ConcurrentHashMap, ConcurrentLinkedQueue

---

### Fail-Safe Example

```java
import java.util.concurrent.*;
import java.util.*;

class FailSafeDemo {
    public static void main(String[] args) {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(Arrays.asList("A", "B", "C", "D"));
        
        System.out.println("Original list: " + list);
        
        // ✅ No exception - iterator works on snapshot
        for (String item : list) {
            System.out.println("Processing: " + item);
            if (item.equals("B")) {
                list.remove(item); // Safe - works on copy
            }
        }
        
        System.out.println("After modification: " + list);
    }
}
```

**Output:**
```
Original list: [A, B, C, D]
Processing: A
Processing: B
Processing: C
Processing: D
After modification: [A, C, D]
```

---

### Comparison Table

| Feature | Fail-Fast | Fail-Safe |
|---------|----------|----------|
| **Exception** | ✅ Throws ConcurrentModificationException | ❌ No exception |
| **Works On** | Original collection | Clone/snapshot |
| **Performance** | Fast | Slower (copy overhead) |
| **Memory** | Low | High (maintains copy) |
| **Examples** | ArrayList, HashMap | CopyOnWriteArrayList, ConcurrentHashMap |
| **Concurrent Modification** | Not allowed | Allowed |

---

## 30. Time & Space Complexity Cheat Sheet

### Complete Complexity Table

| Collection | Add | Remove | Get/Contains | Space | Internal Structure |
|------------|-----|--------|--------------|-------|-------------------|
| **ArrayList** | O(1)* | O(n) | O(1) / O(n) | O(n) | Dynamic Array |
| **LinkedList** | O(1) | O(1)** | O(n) / O(n) | O(n) | Doubly Linked List |
| **Vector** | O(1)* | O(n) | O(1) / O(n) | O(n) | Dynamic Array (synchronized) |
| **Stack** | O(1) | O(1) | O(1) / O(n) | O(n) | Extends Vector |
| **HashSet** | O(1)* | O(1)* | O(1)* | O(n) | HashMap internally |
| **LinkedHashSet** | O(1)* | O(1)* | O(1)* | O(n) | HashMap + LinkedList |
| **TreeSet** | O(log n) | O(log n) | O(log n) | O(n) | Red-Black Tree |
| **HashMap** | O(1)* | O(1)* | O(1)* | O(n) | Array + List/Tree |
| **LinkedHashMap** | O(1)* | O(1)* | O(1)* | O(n) | HashMap + LinkedList |
| **TreeMap** | O(log n) | O(log n) | O(log n) | O(n) | Red-Black Tree |
| **Hashtable** | O(1)* | O(1)* | O(1)* | O(n) | Array + List (synchronized) |
| **PriorityQueue** | O(log n) | O(log n) | O(1) peek | O(n) | Binary Heap |
| **ArrayDeque** | O(1) | O(1) | O(1)** | O(n) | Circular Array |
| **ConcurrentHashMap** | O(1)* | O(1)* | O(1)* | O(n) | Array + List/Tree (lock-free) |
| **CopyOnWriteArrayList** | O(n) | O(n) | O(1) | O(n) | Array (with copy) |

\* Amortized time (can be O(n) during resize/rehash)  
** O(1) if position known, O(n) to search position

---

### Quick Decision Guide

**Need fast random access?**
→ Use **ArrayList**

**Need frequent insert/delete at ends?**
→ Use **LinkedList** or **ArrayDeque**

**Need unique elements with fast lookup?**
→ Use **HashSet**

**Need sorted unique elements?**
→ Use **TreeSet**

**Need key-value pairs with fast lookup?**
→ Use **HashMap**

**Need sorted keys?**
→ Use **TreeMap**

**Need thread-safe collections?**
→ Use **ConcurrentHashMap**, **CopyOnWriteArrayList**, or **Collections.synchronizedXXX()**

**Need priority-based ordering?**
→ Use **PriorityQueue** or **PriorityBlockingQueue**

---

[⬆️ Back to Table of Contents](#-table-of-contents)

---

**End of Java Collections Framework Guide** ✅

---

