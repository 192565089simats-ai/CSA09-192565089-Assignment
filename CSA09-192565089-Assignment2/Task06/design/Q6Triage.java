import java.util.*;

/** Q6 - Emergency triage: PriorityQueue design, correctness, growth, starvation and fairness. */
public class Q6Triage {
    static long sink = 0;

    static final class Patient {
        final String name; final int severity; final long seq; final long minute;
        Patient(String n, int s, long seq, long minute) { name = n; severity = s; this.seq = seq; this.minute = minute; }
        @Override public String toString() { return name + "(S" + severity + ")"; }
    }

    /** Strict rule: severity first, then arrival sequence (the sequence number is the FIFO tie-breaker). */
    static final Comparator<Patient> STRICT = Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.seq);
    /** Fair rule: every severity level is worth W minutes of waiting. Static key => heap stays valid, no re-heapify needed. */
    static Comparator<Patient> fair(int W) { return Comparator.comparingLong((Patient p) -> p.minute + (long) W * (p.severity - 1)).thenComparingLong(p -> p.seq); }

    /** the main design */
    static class TriageQueue {
        private long seq = 0; private final PriorityQueue<Patient> q;
        TriageQueue(Comparator<Patient> c) { q = new PriorityQueue<>(c); }
        Patient admit(String n, int sev, long minute) { Patient p = new Patient(n, sev, seq++, minute); q.add(p); return p; }
        Patient next() { return q.poll(); }
        int size() { return q.size(); }
        List<Patient> waitingList() { List<Patient> l = new ArrayList<>(q); l.sort(q.comparator()); return l; }   // O(n log n) snapshot, queue untouched
        Collection<Patient> raw() { return q; }
    }

    /** alternative 1: LinkedList kept sorted - O(n) insert, O(1) removal */
    static class SortedLinkedTriage {
        long seq = 0; final LinkedList<Patient> l = new LinkedList<>();
        void admit(String n, int sev) {
            Patient p = new Patient(n, sev, seq++, 0);
            ListIterator<Patient> it = l.listIterator();
            while (it.hasNext()) { if (it.next().severity > sev) { it.previous(); break; } }
            it.add(p);
        }
        Patient next() { return l.pollFirst(); }
    }
    /** alternative 2: one FIFO queue per severity level - O(1) admit, O(5)=O(1) next */
    static class LevelQueuesTriage {
        long seq = 0; @SuppressWarnings("unchecked") final ArrayDeque<Patient>[] lv = new ArrayDeque[5];
        LevelQueuesTriage() { for (int i = 0; i < 5; i++) lv[i] = new ArrayDeque<>(); }
        void admit(String n, int sev) { lv[sev - 1].addLast(new Patient(n, sev, seq++, 0)); }
        Patient next() { for (ArrayDeque<Patient> d : lv) if (!d.isEmpty()) return d.pollFirst(); return null; }
    }

    static int[] severities(int n, long seed) { Random r = new Random(seed); int[] s = new int[n]; for (int i = 0; i < n; i++) s[i] = 1 + r.nextInt(5); return s; }

    public static void main(String[] args) {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));

        // ---------------- 1. why a plain queue is not enough ----------------
        System.out.println("\n--- 1. Plain FIFO queue vs PriorityQueue (8 patients) ---");
        String[] names = {"Asha", "Ravi", "John", "Divya", "Lata", "Sunil", "Meena", "Kumar"};
        int[] sev = {5, 3, 3, 4, 2, 2, 1, 1};
        Queue<String> fifo = new ArrayDeque<>(); TriageQueue tq = new TriageQueue(STRICT);
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < names.length; i++) { fifo.add(names[i] + "(S" + sev[i] + ")"); tq.admit(names[i], sev[i], i); in.append(names[i]).append("(S").append(sev[i]).append(") "); }
        System.out.println("Arrival order        : " + in.toString().trim());
        System.out.println("Plain Queue serves   : " + fifo + "   <- Asha (S5) would be seen before Kumar (S1)!");

        // ---------------- 2. hand-verified correctness ----------------
        System.out.println("\n--- 2. Hand-worked correctness check ---");
        String[] expected = {"Meena(S1)", "Kumar(S1)", "Lata(S2)", "Sunil(S2)", "Ravi(S3)", "John(S3)", "Divya(S4)", "Asha(S5)"};
        System.out.println("Hand-derived order   : " + String.join(" ", expected));
        System.out.println("PriorityQueue printed directly (heap array order): " + tq.raw());
        System.out.println("Waiting list (sorted snapshot, queue untouched)  : " + tq.waitingList() + "  size still " + tq.size());
        List<String> got = new ArrayList<>(); Patient p; while ((p = tq.next()) != null) got.add(p.toString());
        System.out.println("Treatment order (poll): " + String.join(" ", got));
        System.out.println("Matches hand-derived order: " + got.equals(Arrays.asList(expected)));

        // ---------------- 3. growth investigation ----------------
        System.out.println("\n--- 3. Growth investigation (average of 5 runs after 2 warm-ups) ---");
        System.out.println("Prediction: add = O(log n) worst case (O(1) average), poll = O(log n). Expect ns/op to grow slowly (about +1 level per doubling).");
        int[] sizes = {1_000, 10_000, 100_000, 1_000_000};
        System.out.printf("%10s %16s %16s %24s%n", "n", "ns/add (bulk)", "ns/poll (drain)", "ns per (poll+add) at size n");
        for (int n : sizes) {
            int[] sv = severities(n, 9); double addT = 0, pollT = 0, steady = 0;
            for (int run = -2; run < 5; run++) {
                TriageQueue q = new TriageQueue(STRICT);
                long t0 = System.nanoTime(); for (int i = 0; i < n; i++) q.admit("P", sv[i], i); long t1 = System.nanoTime();
                int ops = 200_000; Random r = new Random(1);
                long t2 = System.nanoTime(); for (int i = 0; i < ops; i++) { sink += q.next().severity; q.admit("P", 1 + r.nextInt(5), n + i); } long t3 = System.nanoTime();
                long t4 = System.nanoTime(); while (q.next() != null) { } long t5 = System.nanoTime();
                if (run >= 0) { addT += (t1 - t0) / (double) n; steady += (t3 - t2) / (double) ops; pollT += (t5 - t4) / (double) n; }
            }
            System.out.printf("%10d %16.1f %16.1f %24.1f%n", n, addT / 5, pollT / 5, steady / 5);
        }

        // ---------------- 4. alternative designs ----------------
        System.out.println("\n--- 4. Alternatives: sorted LinkedList vs 5 FIFO queues vs PriorityQueue (ns per operation) ---");
        System.out.printf("%10s | %12s %12s | %12s %12s | %12s %12s%n", "n", "PQ admit", "PQ next", "SortedLL adm", "SortedLL nxt", "5xFIFO adm", "5xFIFO nxt");
        for (int n : new int[]{1_000, 10_000, 50_000}) {
            int[] sv = severities(n, 21); double[] a = new double[6];
            for (int run = -1; run < 3; run++) {
                TriageQueue q = new TriageQueue(STRICT); SortedLinkedTriage s = new SortedLinkedTriage(); LevelQueuesTriage f = new LevelQueuesTriage();
                long t0 = System.nanoTime(); for (int i = 0; i < n; i++) q.admit("P", sv[i], i);
                long t1 = System.nanoTime(); for (int i = 0; i < n; i++) sink += q.next().severity;
                long t2 = System.nanoTime(); for (int i = 0; i < n; i++) s.admit("P", sv[i]);
                long t3 = System.nanoTime(); for (int i = 0; i < n; i++) sink += s.next().severity;
                long t4 = System.nanoTime(); for (int i = 0; i < n; i++) f.admit("P", sv[i]);
                long t5 = System.nanoTime(); for (int i = 0; i < n; i++) sink += f.next().severity;
                long t6 = System.nanoTime();
                if (run >= 0) { a[0] += t1 - t0; a[1] += t2 - t1; a[2] += t3 - t2; a[3] += t4 - t3; a[4] += t5 - t4; a[5] += t6 - t5; }
            }
            System.out.printf("%10d | %12.1f %12.1f | %12.1f %12.1f | %12.1f %12.1f%n", n, a[0] / 3 / n, a[1] / 3 / n, a[2] / 3 / n, a[3] / 3 / n, a[4] / 3 / n, a[5] / 3 / n);
        }

        // ---------------- 5. starvation and fairness ----------------
        System.out.println("\n--- 5. Starvation: one S5 patient 'Old' arrives at minute 0; a new S1 patient arrives EVERY minute; one patient served per minute ---");
        for (String label : new String[]{"STRICT (severity, then arrival)", "FAIR (arrival + 30 min per severity level)"}) {
            TriageQueue q = new TriageQueue(label.startsWith("STRICT") ? STRICT : fair(30));
            q.admit("Old", 5, 0); long servedAt = -1;
            for (long t = 0; t < 5000 && servedAt < 0; t++) { q.admit("Crit" + t, 1, t); Patient s = q.next(); if (s.name.equals("Old")) servedAt = t; }
            System.out.println(label + ": Old (S5) " + (servedAt < 0 ? "STILL WAITING after 5000 minutes (starvation)" : "treated at minute " + servedAt + " (waited " + servedAt + " min)"));
        }
        System.out.println("\n--- 5b. Realistic stream: 20,000 minutes, mean 1.02 arrivals/min (surge), 1 patient served/min, severity mix 10/20/30/25/15 % ---");
        System.out.printf("%-34s %s%n", "Rule", "mean wait / max wait in minutes, per severity S1..S5");
        for (String label : new String[]{"STRICT", "FAIR W=30", "FAIR W=10"}) {
            Random r = new Random(77); TriageQueue q = new TriageQueue(label.equals("STRICT") ? STRICT : fair(label.equals("FAIR W=30") ? 30 : 10));
            double[] sum = new double[6]; long[] cnt = new long[6], max = new long[6]; long id = 0;
            for (long t = 0; t < 20_000; t++) {
                int arrivals = (r.nextDouble() < 0.62 ? 1 : 0) + (r.nextDouble() < 0.40 ? 1 : 0);   // mean 1.02 arrivals per minute (surge)
                for (int a = 0; a < arrivals; a++) { double x = r.nextDouble(); int s = x < .10 ? 1 : x < .30 ? 2 : x < .60 ? 3 : x < .85 ? 4 : 5; q.admit("P" + id++, s, t); }
                Patient s = q.next(); if (s != null) { long w = t - s.minute; sum[s.severity] += w; cnt[s.severity]++; max[s.severity] = Math.max(max[s.severity], w); }
            }
            StringBuilder sb = new StringBuilder();
            for (int s = 1; s <= 5; s++) sb.append(String.format("S%d: %.0f/%d  ", s, sum[s] / Math.max(1, cnt[s]), max[s]));
            System.out.printf("%-34s %s | left waiting at end: %d%n", label, sb.toString().trim(), q.size());
        }

        // ---------------- 6. deployment ----------------
        System.out.println("\n--- 6. Deployment check: 5,000 patients/day ---");
        TriageQueue day = new TriageQueue(STRICT); Random r = new Random(5);
        long t0 = System.nanoTime(); for (int i = 0; i < 5000; i++) day.admit("P" + i, 1 + r.nextInt(5), i); while (day.next() != null) { } long t1 = System.nanoTime();
        System.out.printf("Admit + treat 5,000 patients in one day: %.3f ms total (%.2f us per patient). A day has 86,400,000 ms.%n", (t1 - t0) / 1e6, (t1 - t0) / 1e3 / 5000);
        System.out.println("(sink=" + (sink != 0) + ")");
    }
}
