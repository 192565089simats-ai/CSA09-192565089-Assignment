import java.lang.management.ManagementFactory;
import java.util.StringJoiner;

/** Q1 - Railway e-ticket generation: String (+) vs StringBuilder vs StringBuffer (+ alternatives). */
public class Q1Ticket {
    static long sink = 0; // prevents dead-code elimination

    static String[] makeFields(int n) {
        String[] f = new String[n];
        for (int j = 0; j < n; j++) f[j] = "F" + j + "=VAL" + (j * 7919 % 1000) + "|";
        return f;
    }

    // ---------- the three required approaches ----------
    static String viaConcat(String[] f) { String s = ""; for (String x : f) s = s + x; return s; }
    static String viaBuilder(String[] f) { StringBuilder sb = new StringBuilder(); for (String x : f) sb.append(x); return sb.toString(); }
    static String viaBuffer(String[] f) { StringBuffer sb = new StringBuffer(); for (String x : f) sb.append(x); return sb.toString(); }
    // ---------- alternatives ----------
    static String viaJoin(String[] f) { return String.join("", f); }
    static String viaJoiner(String[] f) { StringJoiner j = new StringJoiner(""); for (String x : f) j.add(x); return j.toString(); }
    static String viaFormat(String[] f) { String s = ""; for (String x : f) s = String.format("%s%s", s, x); return s; }

    interface Builder { String build(String[] f); }

    static long allocated() {
        com.sun.management.ThreadMXBean b = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return b.getThreadAllocatedBytes(Thread.currentThread().threadId());
    }

    /** returns {avg ms per full workload run, avg bytes allocated per run} */
    static double[] measure(Builder b, String[] f, int builds, int warm, int runs) {
        for (int w = 0; w < warm; w++) for (int i = 0; i < builds; i++) sink += b.build(f).length();
        double totalMs = 0; long totalAlloc = 0;
        for (int r = 0; r < runs; r++) {
            long a0 = allocated(); long t0 = System.nanoTime();
            for (int i = 0; i < builds; i++) sink += b.build(f).length();
            totalMs += (System.nanoTime() - t0) / 1e6; totalAlloc += allocated() - a0;
        }
        return new double[]{totalMs / runs, (double) totalAlloc / runs};
    }

    static long estObjectsConcat(int n) { return 2L * n; }          // String + byte[] per '+'
    static long estObjectsBuilder(int totalChars) {                   // SB + byte[] + resizes + String + byte[]
        int cap = 16, resizes = 0; while (cap < totalChars) { cap = cap * 2 + 2; resizes++; }
        return 2 + resizes + 2;
    }

    static void workload(String name, int builds, int nFields, boolean withFormat, int warm, int runs) {
        String[] f = makeFields(nFields);
        int chars = viaBuilder(f).length();
        System.out.println("\n=== " + name + ": " + builds + " ticket(s) x " + nFields + " fields (ticket length = " + chars + " chars) ===");
        // correctness: all approaches must give the same text
        boolean same = viaConcat(f).equals(viaBuilder(f)) && viaBuilder(f).equals(viaBuffer(f))
                && viaBuffer(f).equals(viaJoin(f)) && viaJoin(f).equals(viaJoiner(f));
        System.out.println("All approaches produce identical text: " + same);
        System.out.printf("%-14s %14s %16s %18s %22s%n", "Approach", "total ms/run", "us per ticket", "MB allocated/run", "est. objects/ticket");
        String[] names = {"String (+)", "StringBuilder", "StringBuffer", "String.join", "StringJoiner", "String.format"};
        Builder[] bs = {Q1Ticket::viaConcat, Q1Ticket::viaBuilder, Q1Ticket::viaBuffer, Q1Ticket::viaJoin, Q1Ticket::viaJoiner, Q1Ticket::viaFormat};
        for (int k = 0; k < bs.length; k++) {
            if (k == 5 && !withFormat) { System.out.printf("%-14s %14s %16s %18s %22s%n", names[k], "skipped", "-", "-", "-"); continue; }
            double[] m = measure(bs[k], f, builds, warm, runs);
            String est = k == 0 || k == 5 ? String.valueOf(estObjectsConcat(nFields)) : (k <= 2 ? String.valueOf(estObjectsBuilder(chars)) : "~" + estObjectsBuilder(chars));
            System.out.printf("%-14s %14.3f %16.3f %18.2f %22s%n", names[k], m[0], m[0] * 1000 / builds, m[1] / 1e6, est);
        }
    }

    // ---------- thread-safety demonstration ----------
    static void threadTest() throws Exception {
        final int T = 8, PER = 200_000;
        System.out.println("\n=== Shared log: " + T + " threads x " + PER + " appends of \"ab\" (expected length " + (T * PER * 2) + ") ===");
        for (int trial = 1; trial <= 5; trial++) {
            StringBuilder unsafe = new StringBuilder(); StringBuffer safe = new StringBuffer();
            final int[] errors = {0};
            Thread[] ts = new Thread[T];
            for (int i = 0; i < T; i++) {
                ts[i] = new Thread(() -> { for (int k = 0; k < PER; k++) { try { unsafe.append("ab"); } catch (RuntimeException e) { synchronized (errors) { errors[0]++; } } safe.append("ab"); } });
            }
            for (Thread t : ts) t.start(); for (Thread t : ts) t.join();
            System.out.printf("Trial %d: StringBuilder length=%d (lost %d, exceptions %d) | StringBuffer length=%d%n",
                    trial, unsafe.length(), T * PER * 2 - unsafe.length(), errors[0], safe.length());
        }
        // uncontended cost of synchronization
        long t0 = System.nanoTime(); StringBuilder a = new StringBuilder(); for (int i = 0; i < 20_000_000; i++) a.append('x'); double sbMs = (System.nanoTime() - t0) / 1e6;
        t0 = System.nanoTime(); StringBuffer b = new StringBuffer(); for (int i = 0; i < 20_000_000; i++) b.append('x'); double sfMs = (System.nanoTime() - t0) / 1e6;
        System.out.printf("Single thread, 20M appends: StringBuilder %.1f ms, StringBuffer %.1f ms%n", sbMs, sfMs);
        sink += a.length() + b.length();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));
        Runtime rt = Runtime.getRuntime();
        System.gc(); long before = rt.totalMemory() - rt.freeMemory();
        workload("W1 short ticket", 200_000, 10, true, 3, 5);
        workload("W2 many tickets", 10_000, 50, true, 3, 5);
        workload("W3 one very long ticket", 3, 20_000, false, 1, 3);
        long afterNoGc = rt.totalMemory() - rt.freeMemory();
        System.gc(); long after = rt.totalMemory() - rt.freeMemory();
        System.out.printf("%nHeap used before: %.2f MB | after workloads (before GC): %.2f MB | after GC: %.2f MB%n", before / 1e6, afterNoGc / 1e6, after / 1e6);
        threadTest();
        System.out.println("(sink=" + (sink != 0) + ")");
    }
}
