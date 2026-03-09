# HashMap Keys and Values to List - Complete Guide

## Quick Answer

To retrieve all **keys** and **values** from a HashMap into a List:

```java
Map<String, Integer> map = new HashMap<>();
map.put("Apple", 10);
map.put("Banana", 20);

// Get all KEYS as a List
List<String> keys = new ArrayList<>(map.keySet());

// Get all VALUES as a List  
List<Integer> values = new ArrayList<>(map.values());
```

---

## Method 1: Using Constructor (Recommended) ✅

### For Keys

```java
List<String> keyList = new ArrayList<>(map.keySet());
```

**How it works:**
- `map.keySet()` returns a `Set<K>` view of all keys
- `new ArrayList<>(Set)` creates a new ArrayList from the Set
- Creates a **modifiable** copy of keys

### For Values

```java
List<Integer> valueList = new ArrayList<>(map.values());
```

**How it works:**
- `map.values()` returns a `Collection<V>` view of all values
- `new ArrayList<>(Collection)` creates a new ArrayList from the Collection
- Creates a **modifiable** copy of values

---

## Method 2: Using Streams (Java 8+) 🔥

### For Keys

```java
List<String> keyList = map.keySet().stream()
                          .collect(Collectors.toList());
```

### For Values

```java
List<Integer> valueList = map.values().stream()
                             .collect(Collectors.toList());
```

**Advantages:**
- More functional programming style
- Can easily add filters, sorting, or transformations

**Example with filtering:**
```java
// Get keys where value > 15
List<String> filteredKeys = map.entrySet().stream()
    .filter(entry -> entry.getValue() > 15)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
```

---

## Method 3: Manual Iteration (Old Way)

### For Keys

```java
List<String> keyList = new ArrayList<>();
for (String key : map.keySet()) {
    keyList.add(key);
}
```

### For Values

```java
List<Integer> valueList = new ArrayList<>();
for (Integer value : map.values()) {
    valueList.add(value);
}
```

---

## Complete Working Example

```java
import java.util.*;
import java.util.stream.Collectors;

public class HashMapToListDemo {
    public static void main(String[] args) {
        // Create HashMap
        Map<String, Integer> fruitPrices = new HashMap<>();
        fruitPrices.put("Apple", 10);
        fruitPrices.put("Banana", 20);
        fruitPrices.put("Cherry", 30);
        fruitPrices.put("Date", 40);
        
        System.out.println("Original Map: " + fruitPrices);
        
        // METHOD 1: Using Constructor (Best for simple conversion)
        List<String> keys1 = new ArrayList<>(fruitPrices.keySet());
        List<Integer> values1 = new ArrayList<>(fruitPrices.values());
        System.out.println("\nMethod 1 - Constructor:");
        System.out.println("Keys: " + keys1);
        System.out.println("Values: " + values1);
        
        // METHOD 2: Using Streams (Best for transformations)
        List<String> keys2 = fruitPrices.keySet().stream()
            .collect(Collectors.toList());
        List<Integer> values2 = fruitPrices.values().stream()
            .collect(Collectors.toList());
        System.out.println("\nMethod 2 - Streams:");
        System.out.println("Keys: " + keys2);
        System.out.println("Values: " + values2);
        
        // Bonus: Get sorted keys
        List<String> sortedKeys = new ArrayList<>(fruitPrices.keySet());
        Collections.sort(sortedKeys);
        System.out.println("\nSorted Keys: " + sortedKeys);
        
        // Bonus: Get sorted values
        List<Integer> sortedValues = new ArrayList<>(fruitPrices.values());
        Collections.sort(sortedValues);
        System.out.println("Sorted Values: " + sortedValues);
    }
}
```

**Output:**
```
Original Map: {Apple=10, Cherry=30, Date=40, Banana=20}

Method 1 - Constructor:
Keys: [Apple, Cherry, Date, Banana]
Values: [10, 30, 40, 20]

Method 2 - Streams:
Keys: [Apple, Cherry, Date, Banana]
Values: [10, 30, 40, 20]

Sorted Keys: [Apple, Banana, Cherry, Date]
Sorted Values: [10, 20, 30, 40]
```

---

## Important Points to Remember ⚠️

### 1. HashMap Order
- **HashMap** has **NO guaranteed order**
- Keys/values in the List will be in an **unpredictable order**
- Order may change when HashMap is modified

### 2. Modifiable vs Unmodifiable

```java
// ✅ MODIFIABLE - Can add/remove elements
List<String> keys = new ArrayList<>(map.keySet());
keys.add("NewKey"); // Works fine

// ⚠️ UNMODIFIABLE - Using Arrays.asList() on Set
List<String> keys2 = new ArrayList<>(Arrays.asList(map.keySet().toArray(new String[0])));
// This is unnecessarily complex
```

### 3. Null Values
```java
map.put("Key1", null);
List<Integer> values = new ArrayList<>(map.values());
// values will contain null
```

### 4. Duplicates in Values
```java
map.put("A", 10);
map.put("B", 10); // Duplicate value
map.put("C", 20);

List<Integer> values = new ArrayList<>(map.values());
// values = [10, 10, 20] - duplicates preserved
```

---

## Use Cases & Interview Questions

### Q1: "How do you get all keys from a HashMap?"

**Answer:**
```java
List<String> keys = new ArrayList<>(map.keySet());
```
or
```java
Set<String> keySet = map.keySet();
```

### Q2: "How do you get all values from a HashMap?"

**Answer:**
```java
List<Integer> values = new ArrayList<>(map.values());
```
or
```java
Collection<Integer> valueCollection = map.values();
```

### Q3: "What's the difference between keySet() and values()?"

**Answer:**
- `keySet()` returns a **Set** (no duplicates, keys are unique)
- `values()` returns a **Collection** (can have duplicates)

### Q4: "Are the collections returned by keySet() and values() backed by the original map?"

**Answer:** 
**YES!** They are **backed views**:
```java
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);

Set<String> keySet = map.keySet(); // Backed view
map.put("B", 2);

System.out.println(keySet); // [A, B] - reflects change!

// However, when you create ArrayList:
List<String> keyList = new ArrayList<>(map.keySet()); // Snapshot copy
map.put("C", 3);
System.out.println(keyList); // [A, B] - doesn't reflect change
```

### Q5: "How do you get both keys and values together?"

**Answer:**
```java
// Using entrySet()
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    String key = entry.getKey();
    Integer value = entry.getValue();
}

// Or convert to Lists of entries
List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
```

---

## Performance Comparison

| Method | Time Complexity | Space Complexity | Best For |
|--------|----------------|------------------|----------|
| `new ArrayList<>(map.keySet())` | O(n) | O(n) | Simple conversion |
| `map.keySet().stream().collect()` | O(n) | O(n) | With transformations |
| Manual iteration | O(n) | O(n) | Custom logic |

**Where n = number of entries in HashMap**

---

## Common Mistakes to Avoid ❌

### Mistake 1: Trying to modify the backed view incorrectly
```java
Set<String> keys = map.keySet();
keys.add("NewKey"); // ❌ UnsupportedOperationException
```

### Mistake 2: Expecting order in HashMap
```java
map.put("C", 3);
map.put("A", 1);
map.put("B", 2);
List<String> keys = new ArrayList<>(map.keySet());
// Don't expect [C, A, B] - order is NOT guaranteed
```

### Mistake 3: Using wrong collection type
```java
// ❌ Don't do this
String[] keysArray = map.keySet().toArray(new String[0]);
List<String> keys = Arrays.asList(keysArray);
// This creates a fixed-size list!

// ✅ Do this instead
List<String> keys = new ArrayList<>(map.keySet());
```

---

## Advanced Examples

### Example 1: Get Keys Sorted by Values

```java
List<String> keysSortedByValue = map.entrySet().stream()
    .sorted(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
```

### Example 2: Get Values for Specific Keys

```java
List<String> specificKeys = Arrays.asList("Apple", "Cherry");
List<Integer> specificValues = specificKeys.stream()
    .map(map::get)
    .filter(Objects::nonNull)
    .collect(Collectors.toList());
```

### Example 3: Get Filtered Keys/Values

```java
// Get keys where value > 25
List<String> expensiveFruits = map.entrySet().stream()
    .filter(entry -> entry.getValue() > 25)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());

// Get values that are even
List<Integer> evenPrices = map.values().stream()
    .filter(price -> price % 2 == 0)
    .collect(Collectors.toList());
```

---

## Related HashMap Operations

### Get Keys and Values Separately but Maintain Association

```java
// Using parallel lists (not recommended for large data)
List<String> keys = new ArrayList<>(map.keySet());
List<Integer> values = keys.stream()
    .map(map::get)
    .collect(Collectors.toList());

// Better: Use Entry list
List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
```

### Convert Back to HashMap

```java
List<String> keys = List.of("A", "B", "C");
List<Integer> values = List.of(1, 2, 3);

Map<String, Integer> recreatedMap = new HashMap<>();
for (int i = 0; i < keys.size(); i++) {
    recreatedMap.put(keys.get(i), values.get(i));
}
```

---

## Summary

✅ **Best Practice for Keys:**
```java
List<String> keys = new ArrayList<>(map.keySet());
```

✅ **Best Practice for Values:**
```java
List<Integer> values = new ArrayList<>(map.values());
```

✅ **With Java 8+ Streams (for transformations):**
```java
List<String> keys = map.keySet().stream().collect(Collectors.toList());
List<Integer> values = map.values().stream().collect(Collectors.toList());
```

**Key Takeaways:**
1. `keySet()` returns a Set, `values()` returns a Collection
2. Both are backed by the original map
3. Creating `new ArrayList<>()` makes an independent copy
4. HashMap has NO guaranteed order
5. Use streams for filtering/transformation operations

