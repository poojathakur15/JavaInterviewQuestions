import java.util.*;

class Employee {
    private String firstName;
    private String lastName;
    private int age;
    private String department;
    private int salary;

    public Employee(String firstName, String lastName, int age, String department, int salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.department = department;
        this.salary = salary;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public int getSalary() { return salary; }

    @Override
    public String toString() {
        return firstName + " " + lastName + ", Age: " + age + ", Dept: " + department + ", Salary: " + salary;
    }
}

public class EmployeeMultiLevelSorting {
    public static void main(String[] args) {
        List<Employee> empList = List.of(
                new Employee("Ajay","Rathore",30, "Computer",10000),
                new Employee("Mina","Rathore",28, "Computer",12000),
                new Employee("Mina","Bakshi",30, "Science",10000),
                new Employee("Teku","Meena",21, "Geography",10000),
                new Employee("Ajay","Rathore",28, "Geography",10000),
                new Employee("Ajay","Rathore",28, "Computer",15000),
                new Employee("Ajay","Rathore",28, "Computer",17000)
        );

        List<Employee> sortedList = empList.stream()
                .sorted(Comparator.comparing(Employee::getFirstName)
                        .thenComparing(Employee::getLastName)
                        .thenComparing(Employee::getAge)
                        .thenComparing(Employee::getDepartment)
                        .thenComparing(Employee::getSalary))
                .toList();

        sortedList.forEach(System.out::println);
    }
}

