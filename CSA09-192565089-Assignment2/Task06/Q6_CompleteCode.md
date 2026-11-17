# Q6 – Complete the Code (Triage priority queue)

**Name:** RITHISH KUMAR D  |  **Register No.:** 192565089  |  **Course:** Programming in Java (CSA09)  |  **Assignment 2 – CO2**

**Files:** `complete/Triage.java`, `complete/output.txt`

---

## Completed program
Only **TODO 1** was replaced – the `PriorityQueue` declaration:
```java
private final PriorityQueue<Patient> queue = new PriorityQueue<>(
        Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival));
```
```java
import java.util.Comparator;
import java.util.PriorityQueue;
class Patient {
    final String name; final int severity; final long arrival;
    Patient(String name, int severity, long arrival) {
        this.name = name; this.severity = severity; this.arrival = arrival;
    }
    @Override public String toString() { return name + "(S" + severity + ")"; }
}
public class Triage {
    private long counter = 0;
    // TODO 1 (completed): lowest severity number first; for equal severity, earliest arrival first
    private final PriorityQueue<Patient> queue = new PriorityQueue<>(
            Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival));
    void admit(String name, int severity) {
        queue.add(new Patient(name, severity, counter++));
    }
    Patient next() { return queue.poll(); }
    public static void main(String[] args) {
        Triage er = new Triage();
        er.admit("Ravi", 3); er.admit("Meena", 1); er.admit("John", 3);
        er.admit("Asha", 5); er.admit("Kumar", 1); er.admit("Lata", 2);
        StringBuilder order = new StringBuilder();
        Patient p;
        while ((p = er.next()) != null) order.append(p).append(' ');
        System.out.println(order.toString().trim());
    }
}
```

## Output
```text
Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)
```

Expected output from the assignment: `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)` ✔ (matches exactly).

## Explanation – how the arrival counter keeps equal severities in arrival order
`PriorityQueue` is a heap and is **not stable**: when two patients have the same severity the comparator alone returns 0 and the queue may serve either first. `admit()` gives each patient `counter++` as `arrival`, a number that increases with every admission and is therefore unique. The comparator's second key, `thenComparingLong(p -> p.arrival)`, compares these numbers, so among equal severities the smaller (earlier) arrival is "smaller" and is served first: Meena (arrival 1) before Kumar (arrival 4) for S1, and Ravi (0) before John (2) for S3. Because every pair of patients now differs in at least one key, the heap order is a strict total order and the result is deterministic.
