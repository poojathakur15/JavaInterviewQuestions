import java.util.*;
import java.util.stream.Collectors;

/**
 * Program to Find Second Highest Salary
 * Multiple Approaches with Complete Working Code
 *
 * @author Java Interview Preparation
 * @date February 23, 2026
 */

// Employee Class
class Employee {
    private String name;
    private int salary;

    public Employee(String name, int salary) {
        this.name = name;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public int getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return "Employee{name='" + name + "', salary=" + salary + "}";
    }
}

public class SecondHighestSalary {

    public static void main(String[] args) {
        // Test data with duplicate salaries
        List<Employee> employees = List.of(
            new Employee("Hari", 10000),
            new Employee("Harish", 10000),
            new Employee("Harika", 10001),
            new Employee("Harry", 10002),
            new Employee("Hara", 10000)
        );

        System.out.println("All Employees:");
        employees.forEach(System.out::println);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 1: Using Streams with distinct() and skip()
        System.out.println("APPROACH 1: Using Streams with distinct() and skip()");
        Integer result1 = findSecondHighestSalary_Approach1(employees);
        System.out.println("Second Highest Salary: " + result1);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 2: Using TreeSet (Natural Descending Order)
        System.out.println("APPROACH 2: Using TreeSet with Descending Order");
        Integer result2 = findSecondHighestSalary_Approach2(employees);
        System.out.println("Second Highest Salary: " + result2);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 3: Using Sorting
        System.out.println("APPROACH 3: Using Sorted List");
        Integer result3 = findSecondHighestSalary_Approach3(employees);
        System.out.println("Second Highest Salary: " + result3);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 4: Using Stream sorted() with limit()
        System.out.println("APPROACH 4: Using Stream sorted() with limit()");
        Integer result4 = findSecondHighestSalary_Approach4(employees);
        System.out.println("Second Highest Salary: " + result4);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 5: Using Two Variables (Optimal - Single Pass)
        System.out.println("APPROACH 5: Using Two Variables (Optimal - O(n))");
        Integer result5 = findSecondHighestSalary_Approach5(employees);
        System.out.println("Second Highest Salary: " + result5);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 6: Using Priority Queue
        System.out.println("APPROACH 6: Using Priority Queue");
        Integer result6 = findSecondHighestSalary_Approach6(employees);
        System.out.println("Second Highest Salary: " + result6);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Approach 7: Get Second Highest Salary Employee
        System.out.println("APPROACH 7: Get Employee with Second Highest Salary");
        Employee result7 = findEmployeeWithSecondHighestSalary(employees);
        System.out.println("Employee with Second Highest Salary: " + result7);
        System.out.println("\n" + "=".repeat(60) + "\n");

        // Edge Cases
        testEdgeCases();
    }

    /**
     * APPROACH 1: Using Streams with distinct() and skip()
     * Time Complexity: O(n log n) - due to sorting in distinct()
     * Space Complexity: O(n)
     */
    public static Integer findSecondHighestSalary_Approach1(List<Employee> employees) {
        return employees.stream()
            .map(Employee::getSalary)           // Extract salaries
            .distinct()                         // Remove duplicates
            .sorted(Comparator.reverseOrder())  // Sort in descending order
            .skip(1)                            // Skip the highest
            .findFirst()                        // Get second highest
            .orElse(null);                      // Return null if not found
    }

    /**
     * APPROACH 2: Using TreeSet with Descending Order
     * TreeSet automatically removes duplicates and maintains sorted order
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static Integer findSecondHighestSalary_Approach2(List<Employee> employees) {
        // Create TreeSet with reverse order comparator
        TreeSet<Integer> salaries = new TreeSet<>(Collections.reverseOrder());

        // Add all salaries to TreeSet (duplicates automatically removed)
        for (Employee emp : employees) {
            salaries.add(emp.getSalary());
        }

        // Check if we have at least 2 distinct salaries
        if (salaries.size() < 2) {
            return null;
        }

        // Skip first (highest) and get second
        Iterator<Integer> iterator = salaries.iterator();
        iterator.next(); // Skip highest
        return iterator.next(); // Return second highest
    }

    /**
     * APPROACH 3: Using Sorted List
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static Integer findSecondHighestSalary_Approach3(List<Employee> employees) {
        List<Integer> distinctSalaries = employees.stream()
            .map(Employee::getSalary)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        if (distinctSalaries.size() < 2) {
            return null;
        }

        return distinctSalaries.get(1);
    }

    /**
     * APPROACH 4: Using Stream sorted() with limit()
     * Time Complexity: O(n log n)
     * Space Complexity: O(n)
     */
    public static Integer findSecondHighestSalary_Approach4(List<Employee> employees) {
        return employees.stream()
            .map(Employee::getSalary)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .limit(2)                           // Take only top 2
            .skip(1)                            // Skip first (highest)
            .findFirst()
            .orElse(null);
    }

    /**
     * APPROACH 5: Using Two Variables (Most Optimal - Single Pass)
     * Time Complexity: O(n) - Single pass through list
     * Space Complexity: O(1) - Only two variables
     * BEST APPROACH for interviews!
     */
    public static Integer findSecondHighestSalary_Approach5(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return null;
        }

        Integer highest = null;
        Integer secondHighest = null;

        for (Employee emp : employees) {
            int salary = emp.getSalary();

            if (highest == null || salary > highest) {
                // Found new highest
                secondHighest = highest;
                highest = salary;
            } else if (salary < highest && (secondHighest == null || salary > secondHighest)) {
                // Found new second highest
                secondHighest = salary;
            }
        }

        return secondHighest;
    }

    /**
     * APPROACH 6: Using Priority Queue (Max Heap)
     * Time Complexity: O(n log k) where k = 2
     * Space Complexity: O(n)
     */
    public static Integer findSecondHighestSalary_Approach6(List<Employee> employees) {
        // Create max heap
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        // Add all distinct salaries
        Set<Integer> distinctSalaries = employees.stream()
            .map(Employee::getSalary)
            .collect(Collectors.toSet());

        maxHeap.addAll(distinctSalaries);

        if (maxHeap.size() < 2) {
            return null;
        }

        maxHeap.poll(); // Remove highest
        return maxHeap.poll(); // Return second highest
    }

    /**
     * APPROACH 7: Get Employee with Second Highest Salary
     * Returns the employee object, not just the salary
     */
    public static Employee findEmployeeWithSecondHighestSalary(List<Employee> employees) {
        // First find the second highest salary
        Integer secondHighestSalary = findSecondHighestSalary_Approach5(employees);

        if (secondHighestSalary == null) {
            return null;
        }

        // Find first employee with that salary
        return employees.stream()
            .filter(emp -> emp.getSalary() == secondHighestSalary)
            .findFirst()
            .orElse(null);
    }

    /**
     * BONUS: Get all employees with second highest salary
     */
    public static List<Employee> findAllEmployeesWithSecondHighestSalary(List<Employee> employees) {
        Integer secondHighestSalary = findSecondHighestSalary_Approach5(employees);

        if (secondHighestSalary == null) {
            return Collections.emptyList();
        }

        return employees.stream()
            .filter(emp -> emp.getSalary() == secondHighestSalary)
            .collect(Collectors.toList());
    }

    /**
     * BONUS: Get Nth highest salary
     */
    public static Integer findNthHighestSalary(List<Employee> employees, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }

        return employees.stream()
            .map(Employee::getSalary)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .skip(n - 1)
            .findFirst()
            .orElse(null);
    }

    /**
     * Test Edge Cases
     */
    public static void testEdgeCases() {
        System.out.println("EDGE CASES:");
        System.out.println("-".repeat(60));

        // Edge Case 1: Empty list
        System.out.println("1. Empty List:");
        List<Employee> emptyList = Collections.emptyList();
        System.out.println("Result: " + findSecondHighestSalary_Approach1(emptyList));
        System.out.println();

        // Edge Case 2: Single employee
        System.out.println("2. Single Employee:");
        List<Employee> singleEmp = List.of(new Employee("John", 5000));
        System.out.println("Result: " + findSecondHighestSalary_Approach1(singleEmp));
        System.out.println();

        // Edge Case 3: All employees with same salary
        System.out.println("3. All Same Salary:");
        List<Employee> sameSalary = List.of(
            new Employee("John", 5000),
            new Employee("Jane", 5000),
            new Employee("Jack", 5000)
        );
        System.out.println("Result: " + findSecondHighestSalary_Approach1(sameSalary));
        System.out.println();

        // Edge Case 4: Two distinct salaries
        System.out.println("4. Two Distinct Salaries:");
        List<Employee> twoSalaries = List.of(
            new Employee("John", 5000),
            new Employee("Jane", 6000),
            new Employee("Jack", 5000)
        );
        System.out.println("Result: " + findSecondHighestSalary_Approach1(twoSalaries));
        System.out.println();

        // Edge Case 5: Get 3rd highest salary
        System.out.println("5. Find 3rd Highest Salary:");
        List<Employee> multiSalaries = List.of(
            new Employee("A", 1000),
            new Employee("B", 2000),
            new Employee("C", 3000),
            new Employee("D", 4000),
            new Employee("E", 5000)
        );
        System.out.println("3rd Highest: " + findNthHighestSalary(multiSalaries, 3));
        System.out.println("4th Highest: " + findNthHighestSalary(multiSalaries, 4));
        System.out.println();

        // Edge Case 6: All employees with second highest salary
        System.out.println("6. All Employees with Second Highest Salary:");
        List<Employee> multipleSecond = List.of(
            new Employee("A", 5000),
            new Employee("B", 4000),
            new Employee("C", 4000),
            new Employee("D", 3000)
        );
        List<Employee> result = findAllEmployeesWithSecondHighestSalary(multipleSecond);
        result.forEach(System.out::println);
    }
}

