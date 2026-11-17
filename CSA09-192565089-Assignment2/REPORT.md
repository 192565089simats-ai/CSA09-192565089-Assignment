# Programming in Java – Assignment 2 (CO2) – Report

| | |
|---|---|
| **Student** | RITHISH KUMAR D |
| **Register No.** | 192565089 |
| **Course** | Programming in Java (CSA09) |
| **Nature** | Individual, 50 marks |
| **GitHub repository** | `https://github.com/<your-username>/CSA09-192565089-Assignment2` ← *replace with your link (also required on the cover page)* |
| **Environment** | OpenJDK 21.0.12.1, Linux, 1 CPU core (results are from this run; ratios and growth trends are the evidence) |

## 1. Method
Every task follows the same cycle required by the assignment: **Predict → Design → Implement → Measure → Analyse → Evaluate**. Predictions come from complexity analysis (immutability, heap / hash table / red-black tree / array behaviour). Timings use `System.nanoTime()`, repeated runs after warm-up and averaged; operation counts (comparisons) and allocated bytes (`ThreadMXBean`) are used as primary evidence where times are noisy. Full code, raw console output, tables and the alternative-concept analyses are in the per-task files:

| Task | Marks | Report | Code and raw output |
|---|---:|---|---|
| Q1 String / StringBuilder / StringBuffer | 6 | `Task01/Q1.md` | `Task01/Q1Ticket.java`, `output.txt` |
| Q2 ArrayList / LinkedList / ArrayDeque | 4 | `Task02/Q2.md` | `Task02/Q2Playlist.java`, `output.txt` |
| Q3 hashCode–equals contract | 6 | `Task03/Q3.md`, `Task03/Q3_CompleteCode.md` | `Task03/Q3Experiments.java`, `before/`, `after/` |
| Q4 HashMap / TreeMap / LinkedHashMap | 6 | `Task04/Q4.md` | `Task04/Q4Pharmacy.java`, `output.txt` |
| Q5 Generic ranking (Comparable / Comparator) | 7 | `Task05/Q5.md`, `Task05/Q5_CompleteCode.md` | `design/Q5Design.java`, `complete/LeaderboardDemo.java` |
| Q6 Queue and PriorityQueue | 8 | `Task06/Q6.md`, `Task06/Q6_CompleteCode.md` | `design/Q6Triage.java`, `complete/Triage.java` |
| Q7 Diagnose, redesign, validate | 13 | `Task07/Q7.md` | `Task07/Q7Redesign.java`, `output.txt` |

## 2. Summary of findings

### Q1 – Building text
Repeated `+` copies all previous characters on every step (O(n²)): relative to `StringBuilder` it was 2.0× slower for a 10-field ticket, 2.9× for 50 fields and ≈ 1,850× for a 20,000-field ticket, allocating up to ≈ 3,000× more memory (7.8 GB vs 2.6 MB). `StringBuffer` was within a few percent of `StringBuilder` for ticket building but ≈ 7× slower in a tight single-character-append loop. A shared `StringBuilder` lost up to 39 % of the appended characters in racing threads (timing-dependent); `StringBuffer` was always exact. **Decision:** `StringBuilder` for per-ticket text (thread-confined), `StringBuffer` (or a queue/logging framework) for the shared log.

### Q2 – Playlist
At 100,000 songs: `get(k)` 96 ns (`ArrayList`) vs 59,884 ns (`LinkedList`); front insert/remove 7,492 ns (`ArrayList`) vs 37 ns (`LinkedList`) and 32 ns (`ArrayDeque`); middle insert/remove 3,611 ns vs 93,898 ns (the `LinkedList` "O(1) insert" is hidden behind an O(n) walk). **Boundary:** `ArrayList` is right for index-based, append-heavy playlists of up to thousands of songs; stop when large lists need frequent front/middle edits. For "Up Next" use `ArrayDeque` (4.3 ns/op vs 5,877 ns/op for `ArrayList`, 4 B vs 24 B per element for `LinkedList`).

### Q3 – hashCode–equals
Test A (same object twice) looked correct (size 2); Test B (new equal objects) gave 3 instead of 2; Test C (key field changed after insertion) left an unfindable "ghost" element and later admitted a duplicate; Test D showed a `TreeSet` ordered by name holding 2 elements while a `HashSet` held 3. The `Attendance` program prints `Unique students: 3` before TODO 1 and `Unique students: 2` after overriding `hashCode()` with `regNo.hashCode()`. Alternatives: `TreeSet` O(log n) (695 ns/scan, ≈ 9× slower) vs `HashSet`/`HashMap` O(1) (77–168 ns/scan).

### Q4 – Pharmacy maps
At 100,000 medicines: lookups 34 ns (`HashMap`), 43 ns (`LinkedHashMap`), 559 ns (`TreeMap`); code-order report 4.3 ms (`TreeMap`) vs ≈ 28–37 ms (sort needed); insertion-order report free only with `LinkedHashMap` (2.2 ms; the wrong maps need a side list, up to 60.7 ms for `TreeMap`). Acceptability criterion: ≤ 1 ms per lookup, ≤ 1 s per report – all options pass, so the decision follows the requirement mix: **`LinkedHashMap`**. `Hashtable` is not preferred (legacy, every method synchronized, no nulls, no ordering). Sorted array + binary search: O(log n) lookup but O(n) insertion (260 ms to insert 100,000 items).

### Q5 – Generic ranking
Design: separate `Comparator`s (`comparingInt(...).reversed().thenComparing(...)`), generic `Leaderboard<T>` taking `Comparator<? super T>`, `addAll(Collection<? extends T>)`, bounded `natural()` factory. Output matches `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`; Bisons precedes Panthers because points and goal difference tie and `"Bisons"` < `"Panthers"`. Faulty comparators were exposed: subtraction overflow (2,000,000,000 ranked last), ignored tie-breaker, a non-transitive fuzzy comparator (TimSort raised *Comparison method violates its general contract!*) and a whole-chain `reversed()`. Correctness proved by lexicographic composition of total orders. Three stable sorts need 2.1× the comparisons of one comparator chain.

### Q6 – Emergency triage
`PriorityQueue` with `(severity, arrival counter)` reproduces the hand-derived order and the expected `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`; printing the queue shows heap-array order, not treatment order. Cost grows logarithmically (poll 113 ns at 1,000 → 333 ns at 1,000,000). A sorted `LinkedList` is O(n) per admit (154.9 µs at 50,000), five FIFO queues are O(1). Strict priority starved an S5 patient (> 5,000 min); an aging key `arrival + W·(severity − 1)` bounded the wait (120 min), at the price of longer S1 waits in an overloaded department. 5,000 patients/day ≈ 8 ms in total: **the design can serve a city hospital**; the limiting resource is staff, not the algorithm.

### Q7 – Redesign of the pharmacy code-order report
Diagnosis: `HashMap` forgets order, so each report copies and re-sorts all keys (≈ 712,000 comparisons and ≈ 0.86 MB per request at n = 100,000, even when nothing changed). Redesign: `HashMap` + cached sorted index updated by sorting only the *k* new codes and merging (O(k log k + n)), with a dirty flag. Result: 12.1× (n = 10,000) and 13.7× (n = 100,000) faster with 50 new medicines per report, 74.2× when nothing changed (0 comparisons), allocation down up to 62×, lookups unchanged at 31 ns; only 1.8× for a bulk change. For a once-per-evening report the original remains preferable (simpler; 28–37 ms ≪ 1 s).

## 3. Cross-cutting conclusions
1. **Complexity matters only at scale:** the `+` operator, `ArrayList` front inserts and sorted-list insertion are harmless at n = 10–1,000 and unacceptable at 20,000–100,000.
2. **Constants matter as well as Big-O:** `ArrayList` beats `LinkedList` for middle insertion (both O(n)) because shifting contiguous memory is cheaper than walking nodes; `StringBuffer`'s lock is cheap per ticket but costly per tiny append.
3. **Correctness comes before speed:** the equals/hashCode contract, the comparator's transitivity and a stable tie-breaker in the priority queue determine whether the result is right at all.
4. **Choose by requirement:** the right map, list or queue is the one whose natural order and cost model match the dominant operation, and redesign is justified only when the diagnosed repeated work really occurs in the usage pattern.
5. **Measure carefully:** warm-ups, repeated runs and operation counts were used because single timings on a shared single-core machine are noisy (visible at n = 1,000 in Q2 and Q4).

## 4. Raw data and reproduction
Raw experimental data = the `output.txt` files in each task folder (and the tables embedded in each task report). To reproduce, run `bash run_all.sh` (Java 19+). Predictions, tables and conclusions should be re-checked against the figures from your own run.
