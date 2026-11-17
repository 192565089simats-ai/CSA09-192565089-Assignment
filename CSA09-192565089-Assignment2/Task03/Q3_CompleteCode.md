# Q3 – Complete the Code (Attendance program)

**Name:** RITHISH KUMAR D  |  **Register No.:** 192565089  |  **Course:** Programming in Java (CSA09)  |  **Assignment 2 – CO2**

**Files:** `before/Attendance.java` (TODO 1 not completed), `after/Attendance.java` (TODO 1 completed)

---

## Step 1 – run WITHOUT completing TODO 1
```java
import java.util.HashSet;
import java.util.Set;
class Student {
    private final String regNo;
    private final String name;
    Student(String regNo, String name) { this.regNo = regNo; this.name = name; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        return regNo.equals(((Student) o).regNo);
    }
    // TODO 1: Override hashCode() so that it is consistent with equals()

    @Override public String toString() { return regNo + " " + name; }
}
public class Attendance {
    public static void main(String[] args) {
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun"));   // scanned at Gate 1
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun"));   // scanned again at Gate 3
        System.out.println("Unique students: " + present.size());
    }
}
```

Output recorded (`before/output.txt`):
```text
Unique students: 3
```

## Step 2 – complete TODO 1 and run again
The only change is the replacement of the TODO comment by a `hashCode()` that uses the same field as `equals()`:
```java
@Override public int hashCode() { return regNo.hashCode(); }
```
```java
import java.util.HashSet;
import java.util.Set;
class Student {
    private final String regNo;
    private final String name;
    Student(String regNo, String name) { this.regNo = regNo; this.name = name; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        return regNo.equals(((Student) o).regNo);
    }
    // TODO 1 (completed): hashCode() uses exactly the field that equals() uses (regNo)
    @Override public int hashCode() { return regNo.hashCode(); }

    @Override public String toString() { return regNo + " " + name; }
}
public class Attendance {
    public static void main(String[] args) {
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun"));   // scanned at Gate 1
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun"));   // scanned again at Gate 3
        System.out.println("Unique students: " + present.size());
    }
}
```

Output (`after/output.txt`):
```text
Unique students: 2
```

Expected output from the assignment: `Unique students: 2` ✔ (matches).

## Explanation of the difference
| Run | `hashCode()` | Output |
|---|---|---|
| Before TODO 1 | inherited `Object.hashCode()` (identity based) | `Unique students: 3` |
| After TODO 1 | `regNo.hashCode()` | `Unique students: 2` |

Before the fix, the two scans of `192311001` are different objects with different identity hash codes, so `HashSet` places them in different buckets and never calls `equals()`; all three scans are kept. After the fix, equal students (same `regNo`) have the same hash code, so they land in the same bucket, `equals()` is called, returns `true`, and the second scan is rejected as a duplicate: 2 unique students.
