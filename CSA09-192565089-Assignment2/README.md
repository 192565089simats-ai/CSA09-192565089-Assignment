# CSA09-192565089-Assignment2

| | |
|---|---|
| **Name** | RITHISH KUMAR D |
| **Register No.** | 192565089 |
| **Course** | Programming in Java (CSA09) |
| **Assignment** | Assignment 2 – CO2 (Strings, Collections, hashCode–equals, Generics, Comparable/Comparator) |
| **Java version used** | OpenJDK 21.0.12.1 (the programs need **Java 19 or later**; Java 21 recommended – `Thread.threadId()` is Java 19+, `record` is Java 16+) |
| **GitHub repository** | `https://github.com/<your-username>/CSA09-192565089-Assignment2`  ← *replace with your real link* |

## Repository layout
```
CSA09-192565089-Assignment2/
├── README.md                 (this file)
├── REPORT.md                 consolidated assignment report (Q1–Q7)
├── run_all.sh                compiles and runs every program, saving output.txt
├── git_commits.sh            one commit per task (see "Commit history")
├── Task01/  Q1.md  Q1Ticket.java    output.txt
├── Task02/  Q2.md  Q2Playlist.java  output.txt
├── Task03/  Q3.md  Q3_CompleteCode.md  Q3Experiments.java  output.txt
│            before/Attendance.java output.txt     (TODO 1 not completed)
│            after/Attendance.java  output.txt     (TODO 1 completed)
├── Task04/  Q4.md  Q4Pharmacy.java   output.txt
├── Task05/  Q5.md  Q5_CompleteCode.md
│            design/Q5Design.java  output.txt      (my generic ranking design)
│            complete/LeaderboardDemo.java output.txt   (Complete the Code)
├── Task06/  Q6.md  Q6_CompleteCode.md
│            design/Q6Triage.java  output.txt      (my triage design)
│            complete/Triage.java  output.txt      (Complete the Code)
└── Task07/  Q7.md  Q7Redesign.java   output.txt
```
The 12 Markdown files are: `README.md`, `REPORT.md`, `Q1.md`, `Q2.md`, `Q3.md`, `Q3_CompleteCode.md`, `Q4.md`, `Q5.md`, `Q5_CompleteCode.md`, `Q6.md`, `Q6_CompleteCode.md`, `Q7.md`. Each task file contains the problem, prediction, full source code, the program output, analysis and the alternative-concept/complexity analysis.

## How to compile and run
Requirements: a JDK, Java 19 or later (Java 21 recommended). Check with `javac -version`.

**All programs at once** (creates/overwrites every `output.txt`):
```bash
bash run_all.sh
```
**Individually** (example for Q1; run from the repository root):
```bash
mkdir -p out && javac -d out Task01/Q1Ticket.java && java -cp out Q1Ticket
```
| Task | Compile | Run |
|---|---|---|
| Q1 | `javac -d out Task01/Q1Ticket.java` | `java -cp out Q1Ticket` |
| Q2 | `javac -d out Task02/Q2Playlist.java` | `java -cp out Q2Playlist` |
| Q3 experiments | `javac -d out Task03/Q3Experiments.java` | `java -cp out Q3Experiments` |
| Q3 Complete the Code (before) | `javac -d out_b Task03/before/Attendance.java` | `java -cp out_b Attendance` |
| Q3 Complete the Code (after) | `javac -d out_a Task03/after/Attendance.java` | `java -cp out_a Attendance` |
| Q4 | `javac -d out Task04/Q4Pharmacy.java` | `java -cp out Q4Pharmacy` |
| Q5 design | `javac -d out Task05/design/Q5Design.java` | `java -cp out Q5Design` |
| Q5 Complete the Code | `javac -d out Task05/complete/LeaderboardDemo.java` | `java -cp out LeaderboardDemo` |
| Q6 design | `javac -d out Task06/design/Q6Triage.java` | `java -cp out Q6Triage` |
| Q6 Complete the Code | `javac -d out Task06/complete/Triage.java` | `java -cp out Triage` |
| Q7 | `javac -d out Task07/Q7Redesign.java` | `java -cp out Q7Redesign` |

Use a separate output folder for the two `Attendance` programs because both define the classes `Student` and `Attendance`. Approximate run times in my environment: Q1 7 s, Q2 16 s, Q4 12 s, Q6 design 38 s, Q7 22 s; the others finish in a few seconds.

## Result of each task (short note)
| Task | Result |
|---|---|
| **Q1** | Repeated `+` is quadratic: 2.0× slower than `StringBuilder` for 10 fields, 2.9× for 50 fields, ≈ 1,850× for a 20,000-field ticket (and ≈ 3,000× more memory allocated). `StringBuffer` ≈ `StringBuilder` for ticket building. Per-ticket text: `StringBuilder`; shared log: `StringBuffer` (a shared `StringBuilder` lost characters in some runs). |
| **Q2** | `ArrayList` wins `get(k)` (≈ 96 ns vs ≈ 60 µs for `LinkedList` at 100,000) and iteration; front inserts cost ≈ 7.5 µs at 100,000 vs ≈ 35 ns for `LinkedList`/`ArrayDeque`. For the "Up Next" queue `ArrayDeque` is the right structure (≈ 4 ns/op vs ≈ 5.9 µs for `ArrayList`). |
| **Q3** | Without `hashCode()` the program prints `Unique students: 3`; with `hashCode()` it prints `2` (expected). Mutating a hashed key field makes the element unfindable. A `TreeSet` ordered by name dropped a student that `equals()` considered distinct. |
| **Q4** | `HashMap`/`LinkedHashMap` lookups ≈ 34–43 ns vs `TreeMap` ≈ 559 ns at 100,000 medicines; `TreeMap` gives the code-order report directly (4.3 ms) and `LinkedHashMap` the insertion-order report (2.2 ms). Deployed choice: `LinkedHashMap`. |
| **Q5** | Comparator chain output equals the expected `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`. Faulty comparators (subtraction overflow, missing tie-breaker, fuzzy, whole-chain `reversed()`) were exposed by the second data set. |
| **Q6** | `PriorityQueue` with `(severity, arrival counter)` gives the expected `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`. 5,000 patients/day cost ≈ 8 ms in total. Strict priority starves an S5 patient; an aging key bounds the wait (120 min in the experiment). |
| **Q7** | Redesign of the Q4 code-order report (HashMap + incrementally merged sorted index): 12–14× faster when few changes occur, 74× faster when nothing changed, only 1.8× for a bulk change; lookup stays at ≈ 31 ns; the simpler original is still preferable for a once-per-evening report. |

## Important notes on the experimental evidence
* All `output.txt` files and the numbers quoted in the `.md` files come from **one run on a 1-CPU Linux sandbox with OpenJDK 21**. Absolute timings (especially at n = 1,000 and the thread-race counts in Q1) are machine- and run-dependent; the *ratios and growth trends* are the evidence. **Re-run `bash run_all.sh` on your own computer, compare with the numbers in the reports and update the tables if they differ**, because every student must execute every task individually and may be asked about the figures in the viva.
* Predictions in the reports are derived from the complexity analysis of each class (Big-O, immutability, heap/hash/tree structure); be ready to explain each of them.
* **Output screenshots:** take a screenshot of each program's console output on your computer and save it in the matching task folder (for example `Task01/output_Q1.png`); the `output.txt` files are the text version of the same output.

## Commit history
Commit progressively (at least one commit per task). `git_commits.sh` lists the commands, one block per task – run each block when you finish and re-check that task, so the history reflects your real progress:
```bash
git init && git branch -M main
git remote add origin https://github.com/<your-username>/CSA09-192565089-Assignment2.git
```
Then share the repository with the course faculty (public, or private with the faculty added as collaborator).
