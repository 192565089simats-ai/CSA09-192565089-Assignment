import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Q7 - Diagnose, Redesign, Validate.
 * Chosen solution: Q4 pharmacy inventory kept in a HashMap; the "all medicines in code order" report
 * re-collects and re-sorts every key on EVERY request  (O(n log n) each time, mostly repeated work).
 * Redesign: HashMap (O(1) lookup) + a cached sorted index that is updated incrementally
 *           (sort only the k NEW codes, then merge: O(k log k + n)) and not touched at all when nothing changed.
 */
public class Q7Redesign {
    static long sink = 0;
    static long cmpCount = 0;                                           // comparison counter (operation count)
    static final Comparator<String> COUNTING = (a, b) -> { cmpCount++; return a.compareTo(b); };

    static class Medicine { final String code; int stock; Medicine(String c, int s) { code = c; stock = s; } }

    // ===================== ORIGINAL (Q4 design) =====================
    static class OriginalInventory {
        final Map<String, Medicine> map = new HashMap<>();
        void add(Medicine m) { map.put(m.code, m); }
        Medicine get(String c) { return map.get(c); }
        long reportByCode() {                                           // checksum stands for "print the report"
            String[] keys = map.keySet().toArray(new String[0]);        // hash order = effectively random order
            Arrays.sort(keys, COUNTING);                                // re-sorted from scratch on every request
            long h = 0; for (String k : keys) h = h * 31 + k.hashCode(); return h;
        }
        List<String> orderedCodes() { String[] k = map.keySet().toArray(new String[0]); Arrays.sort(k); return Arrays.asList(k); }
    }

    // ===================== REDESIGN =====================
    static class IndexedInventory {
        final Map<String, Medicine> map = new HashMap<>();              // O(1) lookups preserved
        String[] sorted = new String[16]; int size = 0;                 // cached, always-sorted prefix
        final ArrayList<String> pending = new ArrayList<>();            // codes added since the last report
        boolean dirty = false;

        void add(Medicine m) { if (map.put(m.code, m) == null) { pending.add(m.code); dirty = true; } }   // stock updates leave the index untouched
        Medicine get(String c) { return map.get(c); }

        private void refresh() {
            if (!dirty) return;                                         // nothing changed: zero comparisons
            String[] add = pending.toArray(new String[0]);
            Arrays.sort(add, COUNTING);                                 // k log k
            if (size + add.length > sorted.length) sorted = Arrays.copyOf(sorted, Math.max(sorted.length * 3 / 2, size + add.length));
            int i = size - 1, j = add.length - 1, w = size + add.length - 1;   // in-place merge from the back: O(n + k)
            while (j >= 0) { cmpCount++; if (i >= 0 && sorted[i].compareTo(add[j]) > 0) sorted[w--] = sorted[i--]; else sorted[w--] = add[j--]; }
            size += add.length; pending.clear(); dirty = false;
        }
        long reportByCode() { refresh(); long h = 0; for (int i = 0; i < size; i++) h = h * 31 + sorted[i].hashCode(); return h; }
        List<String> orderedCodes() { refresh(); return new ArrayList<>(Arrays.asList(sorted).subList(0, size)); }
    }

    // ===================== reference: TreeMap =====================
    static class TreeInventory {
        final TreeMap<String, Medicine> map = new TreeMap<>();
        void add(Medicine m) { map.put(m.code, m); }
        Medicine get(String c) { return map.get(c); }
        long reportByCode() { long h = 0; for (String k : map.keySet()) h = h * 31 + k.hashCode(); return h; }
    }

    static long allocated() { return ((com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean()).getThreadAllocatedBytes(Thread.currentThread().threadId()); }

    static String code(int v) { return String.format("MED%07d", v); }

    /** verify that original and redesign produce identical ordered output after every batch */
    static boolean verify(int n, int k, int R) {
        Random r = new Random(1); List<Integer> pool = new ArrayList<>(); for (int i = 0; i < n + k * R; i++) pool.add(i * 3 + 1); Collections.shuffle(pool, r);
        OriginalInventory o = new OriginalInventory(); IndexedInventory x = new IndexedInventory(); int p = 0;
        for (int i = 0; i < n; i++) { Medicine m = new Medicine(code(pool.get(p)), 5); p++; o.add(m); x.add(m); }
        if (!o.orderedCodes().equals(x.orderedCodes())) return false;
        for (int rep = 0; rep < R; rep++) {
            for (int i = 0; i < k; i++) { Medicine m = new Medicine(code(pool.get(p)), 5); p++; o.add(m); x.add(m); }
            if (rep % 2 == 0 && n > 0) { Medicine upd = new Medicine(code(pool.get(rep)), 99); o.add(upd); x.add(upd); }   // stock update of an existing code
            if (!o.orderedCodes().equals(x.orderedCodes())) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));
        System.out.println("\nCorrectness first: original and redesign give identical code-ordered output after every batch (incl. stock updates): "
                + (verify(2000, 25, 40) && verify(5000, 0, 10) && verify(3000, 1500, 3)));

        // name, n (initial medicines), k (new medicines between reports), R (report requests)
        Object[][] scenarios = {
                {"S1  few changes   ", 10_000, 50, 50}, {"S2  few changes   ", 100_000, 50, 50},
                {"S3  no changes     ", 100_000, 0, 50}, {"S4  bulk change    ", 100_000, 50_000, 5}};
        System.out.println("\nEach figure = average of 3 runs after 1 warm-up. ms covers: k adds + 1 report, repeated R times (report = ordered traversal of all codes).");
        System.out.printf("%-20s %8s %6s %4s | %10s %14s %10s | %10s %14s %10s | %10s %10s%n", "Scenario", "n", "k", "R",
                "ORIG ms", "ORIG compares", "ORIG MB", "NEW ms", "NEW compares", "NEW MB", "TreeMap ms", "speed-up");
        for (Object[] sc : scenarios) {
            String name = (String) sc[0]; int n = (Integer) sc[1], k = (Integer) sc[2], R = (Integer) sc[3];
            Random rr = new Random(9); List<Integer> pool = new ArrayList<>(); for (int i = 0; i < n + k * R + 10; i++) pool.add(i * 3 + 1); Collections.shuffle(pool, rr);
            Medicine[] all = new Medicine[pool.size()]; for (int i = 0; i < all.length; i++) all[i] = new Medicine(code(pool.get(i)), 10);
            double[] ms = new double[3]; long[] cmp = new long[2]; double[] mb = new double[2];
            for (int run = -1; run < 3; run++) {
                // ORIGINAL
                OriginalInventory o = new OriginalInventory(); for (int i = 0; i < n; i++) o.add(all[i]);
                cmpCount = 0; long a0 = allocated(); long t0 = System.nanoTime(); int p = n;
                for (int rep = 0; rep < R; rep++) { for (int i = 0; i < k; i++) o.add(all[p++]); sink += o.reportByCode(); }
                long t1 = System.nanoTime(); long a1 = allocated(); long cO = cmpCount;
                // REDESIGN
                IndexedInventory x = new IndexedInventory(); for (int i = 0; i < n; i++) x.add(all[i]); x.reportByCode();   // initial index built once
                cmpCount = 0; long b0 = allocated(); long t2 = System.nanoTime(); p = n;
                for (int rep = 0; rep < R; rep++) { for (int i = 0; i < k; i++) x.add(all[p++]); sink += x.reportByCode(); }
                long t3 = System.nanoTime(); long b1 = allocated(); long cN = cmpCount;
                // TreeMap reference
                TreeInventory tr = new TreeInventory(); for (int i = 0; i < n; i++) tr.add(all[i]);
                long t4 = System.nanoTime(); p = n;
                for (int rep = 0; rep < R; rep++) { for (int i = 0; i < k; i++) tr.add(all[p++]); sink += tr.reportByCode(); }
                long t5 = System.nanoTime();
                if (run >= 0) { ms[0] += (t1 - t0) / 1e6; ms[1] += (t3 - t2) / 1e6; ms[2] += (t5 - t4) / 1e6; mb[0] += (a1 - a0) / 1e6; mb[1] += (b1 - b0) / 1e6; cmp[0] = cO; cmp[1] = cN; }
            }
            System.out.printf("%-20s %8d %6d %4d | %10.1f %14d %10.1f | %10.1f %14d %10.1f | %10.1f %9.1fx%n", name, n, k, R,
                    ms[0] / 3, cmp[0], mb[0] / 3, ms[1] / 3, cmp[1], mb[1] / 3, ms[2] / 3, ms[0] / ms[1]);
        }

        // lookup cost and memory of the structures (the trade-off side)
        int n = 100_000; Random rr = new Random(4); String[] codes = new String[n]; Medicine[] meds = new Medicine[n];
        for (int i = 0; i < n; i++) { codes[i] = code(i * 3 + 1); meds[i] = new Medicine(codes[i], 10); }
        OriginalInventory o = new OriginalInventory(); IndexedInventory x = new IndexedInventory(); TreeInventory tr = new TreeInventory();
        for (Medicine m : meds) { o.add(m); x.add(m); tr.add(m); } x.reportByCode();
        String[] probe = new String[100_000]; for (int i = 0; i < probe.length; i++) probe[i] = codes[rr.nextInt(n)];
        double[] look = new double[3];
        for (int run = -2; run < 5; run++) {
            long t0 = System.nanoTime(); long s = 0; for (String q : probe) s += o.get(q).stock; long t1 = System.nanoTime();
            for (String q : probe) s += x.get(q).stock; long t2 = System.nanoTime();
            for (String q : probe) s += tr.get(q).stock; long t3 = System.nanoTime(); sink += s;
            if (run >= 0) { look[0] += (t1 - t0); look[1] += (t2 - t1); look[2] += (t3 - t2); }
        }
        System.out.printf("%nLookup cost (n=100,000, avg of 5 runs): Original HashMap %.0f ns/get | Redesign (HashMap + index) %.0f ns/get | TreeMap %.0f ns/get%n", look[0] / 5 / probe.length, look[1] / 5 / probe.length, look[2] / 5 / probe.length);
        System.out.printf("Extra retained memory of the redesign's cached index at n=100,000: about %.2f MB (one 4-byte reference per code with compressed oops; the String objects are shared with the HashMap)%n", n * 4 / 1e6);
        System.out.println("(sink=" + (sink != 0) + ")");
    }
}
