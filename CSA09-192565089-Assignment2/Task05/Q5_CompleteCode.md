# Q5 – Complete the Code (generic Leaderboard and comparator)

**Name:** RITHISH KUMAR D  |  **Register No.:** 192565089  |  **Course:** Programming in Java (CSA09)  |  **Assignment 2 – CO2**

**Files:** `complete/LeaderboardDemo.java`, `complete/output.txt`

---

## Completed program
Only the two `TODO` markers were replaced; the rest of the program is unchanged.

* **TODO 1** – `ranking()` returns a new sorted copy (`items` is not modified).
* **TODO 2** – comparator: points (high first), then goal difference (high first), then name (A–Z).

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
class Team {
    final String name; final int points; final int goalDiff;
    Team(String name, int points, int goalDiff) {
        this.name = name; this.points = points; this.goalDiff = goalDiff;
    }
    @Override public String toString() {
        return name + "(" + points + "," + goalDiff + ")";
    }
}
class Leaderboard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;
    Leaderboard(Comparator<? super T> order) { this.order = order; }
    void add(T item) { items.add(item); }
    List<T> ranking() {
        // TODO 1 (completed): return a new sorted copy of items using order, without modifying items
        List<T> copy = new ArrayList<>(items);
        copy.sort(order);
        return copy;
    }
}
public class LeaderboardDemo {
    public static void main(String[] args) {
        // TODO 2 (completed): points (high first), then goal difference (high first), then name (A-Z)
        Comparator<Team> byRank = Comparator.comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                .thenComparing(t -> t.name);
        Leaderboard<Team> board = new Leaderboard<>(byRank);
        board.add(new Team("Panthers", 9, 4));
        board.add(new Team("Eagles", 9, 6));
        board.add(new Team("Cobras", 6, 1));
        board.add(new Team("Bisons", 9, 4));
        System.out.println(board.ranking());
    }
}
```

## Output
```text
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
```

Expected output from the assignment: `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]` ✔ (matches exactly).

## Explanation
* **Eagles first:** 9 points like Panthers and Bisons, but a better goal difference (6 > 4).
* **Bisons above Panthers:** both have 9 points and goal difference 4, so the first two comparators return 0 and the third key decides – the names compare alphabetically and `"Bisons".compareTo("Panthers") < 0`.
* **Cobras last:** only 6 points.
* `ranking()` copies the list (`new ArrayList<>(items)`) before `sort(order)`, so adding teams later or calling `ranking()` twice never reorders the stored list.
* `reversed()` is applied to each numeric key separately; applying it to the whole chain would also reverse the name order.
