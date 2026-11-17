import java.util.*;

/** Q2 - ArrayList vs LinkedList vs ArrayDeque (and a manual array) for a playlist editor. */
public class Q2Playlist {
    static long sink = 0;
    interface Op { void run(); }

    /** average nanoseconds per operation, best of several repetitions' average */
    static double perOpNs(Op op, int ops, int warm, int runs) {
        for (int i = 0; i < warm; i++) op.run();
        double tot = 0;
        for (int i = 0; i < runs; i++) { long t = System.nanoTime(); op.run(); tot += System.nanoTime() - t; }
        return tot / runs / ops;
    }

    static List<Integer> fill(List<Integer> l, int n) { for (int i = 0; i < n; i++) l.add(i); return l; }

    /** plain array with manual shifting (the "alternative concept") */
    static class ManualArray {
        int[] a = new int[16]; int size = 0;
        void add(int v) { if (size == a.length) a = Arrays.copyOf(a, size * 2); a[size++] = v; }
        void insert(int idx, int v) {
            if (size == a.length) a = Arrays.copyOf(a, size * 2);
            for (int i = size; i > idx; i--) a[i] = a[i - 1];   // manual shifting
            a[idx] = v; size++;
        }
        void remove(int idx) { for (int i = idx; i < size - 1; i++) a[i] = a[i + 1]; size--; }
        int get(int i) { return a[i]; }
    }

    public static void main(String[] args) {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));
        int[] sizes = {1_000, 10_000, 100_000};
        final int OPS = 1000;     // operations per measurement for the position-based tests
        System.out.println("\nAll times are average nanoseconds PER OPERATION (lower is better).");
        System.out.println("Position tests use " + OPS + " operations on a list that already holds n songs.\n");
        System.out.printf("%-34s %8s %12s %12s %12s %12s%n", "Operation", "n", "ArrayList", "LinkedList", "ArrayDeque", "int[] manual");

        for (int n : sizes) {
            // --- add at end (build whole list: n adds) ---
            double al = perOpNs(() -> fill(new ArrayList<>(), n), n, 20, 10);
            double ll = perOpNs(() -> fill(new LinkedList<>(), n), n, 20, 10);
            double ad = perOpNs(() -> { ArrayDeque<Integer> d = new ArrayDeque<>(); for (int i = 0; i < n; i++) d.addLast(i); }, n, 20, 10);
            double ma = perOpNs(() -> { ManualArray m = new ManualArray(); for (int i = 0; i < n; i++) m.add(i); }, n, 20, 10);
            row("Add at end", n, al, ll, ad, ma);

            List<Integer> A = fill(new ArrayList<>(), n), L = fill(new LinkedList<>(), n);
            ArrayDeque<Integer> D = new ArrayDeque<>(A);
            ManualArray M = new ManualArray(); for (int i = 0; i < n; i++) M.add(i);

            // --- insert + remove at front (size stays n) ---
            al = perOpNs(() -> { for (int i = 0; i < OPS; i++) { A.add(0, i); A.remove(0); } }, OPS * 2, 8, 7);
            ll = perOpNs(() -> { for (int i = 0; i < OPS; i++) { L.add(0, i); L.remove(0); } }, OPS * 2, 8, 7);
            ad = perOpNs(() -> { for (int i = 0; i < OPS; i++) { D.addFirst(i); D.removeFirst(); } }, OPS * 2, 8, 7);
            ma = perOpNs(() -> { for (int i = 0; i < OPS; i++) { M.insert(0, i); M.remove(0); } }, OPS * 2, 8, 7);
            row("Insert+remove at FRONT", n, al, ll, ad, ma);

            // --- insert + remove in the middle ---
            al = perOpNs(() -> { for (int i = 0; i < OPS; i++) { A.add(n / 2, i); A.remove(n / 2); } }, OPS * 2, 8, 7);
            ll = perOpNs(() -> { for (int i = 0; i < OPS; i++) { L.add(n / 2, i); L.remove(n / 2); } }, OPS * 2, 8, 7);
            ma = perOpNs(() -> { for (int i = 0; i < OPS; i++) { M.insert(n / 2, i); M.remove(n / 2); } }, OPS * 2, 8, 7);
            row("Insert+remove in MIDDLE", n, al, ll, Double.NaN, ma);

            // --- get k-th (pseudo-random k) ---
            al = perOpNs(() -> { long s = 0; Random r = new Random(1); for (int i = 0; i < OPS; i++) s += A.get(r.nextInt(n)); sink += s; }, OPS, 8, 7);
            ll = perOpNs(() -> { long s = 0; Random r = new Random(1); for (int i = 0; i < OPS; i++) s += L.get(r.nextInt(n)); sink += s; }, OPS, 8, 7);
            ma = perOpNs(() -> { long s = 0; Random r = new Random(1); for (int i = 0; i < OPS; i++) s += M.get(r.nextInt(n)); sink += s; }, OPS, 8, 7);
            row("Get k-th element", n, al, ll, Double.NaN, ma);

            // --- iterate whole list (per element) ---
            al = perOpNs(() -> { long s = 0; for (int x : A) s += x; sink += s; }, n, 20, 10);
            ll = perOpNs(() -> { long s = 0; for (int x : L) s += x; sink += s; }, n, 20, 10);
            ad = perOpNs(() -> { long s = 0; for (int x : D) s += x; sink += s; }, n, 20, 10);
            ma = perOpNs(() -> { long s = 0; for (int i = 0; i < M.size; i++) s += M.get(i); sink += s; }, n, 20, 10);
            row("Iterate whole list (per element)", n, al, ll, ad, ma);
            System.out.println();
        }

        // ------------- Changed condition: "Up Next" queue -------------
        System.out.println("=== Changed condition: 'Up Next' queue (addLast, addFirst, removeFirst), 200,000 mixed operations ===");
        System.out.printf("%-34s %8s %12s %12s %12s%n", "Structure", "n", "ArrayList", "LinkedList", "ArrayDeque");
        for (int n : sizes) {
            final int OPS2 = 200_000;
            double al = perOpNs(() -> { List<Integer> q = fill(new ArrayList<>(), n); for (int i = 0; i < OPS2 / 4; i++) { q.add(i); q.add(0, i); q.remove(0); q.remove(0); } }, OPS2, 3, 5);
            double ll = perOpNs(() -> { LinkedList<Integer> q = (LinkedList<Integer>) fill(new LinkedList<>(), n); for (int i = 0; i < OPS2 / 4; i++) { q.addLast(i); q.addFirst(i); q.removeFirst(); q.removeFirst(); } }, OPS2, 3, 5);
            double ad = perOpNs(() -> { ArrayDeque<Integer> q = new ArrayDeque<>(); for (int i = 0; i < n; i++) q.addLast(i); for (int i = 0; i < OPS2 / 4; i++) { q.addLast(i); q.addFirst(i); q.removeFirst(); q.removeFirst(); } }, OPS2, 3, 5);
            System.out.printf("%-34s %8d %12s %12s %12s%n", "ns per op (incl. initial fill)", n, f(al), f(ll), f(ad));
        }

        // ------------- memory per element (analytical + measured) -------------
        System.out.println("\n=== Memory per element (measured by heap difference for 1,000,000 Integers, boxed values shared) ===");
        memory();
        System.out.println("(sink=" + (sink != 0) + ")");
    }

    static void memory() {
        Runtime rt = Runtime.getRuntime(); int n = 1_000_000;
        Integer[] shared = new Integer[n]; for (int i = 0; i < n; i++) shared[i] = i;
        System.gc(); long b0 = used(rt); ArrayList<Integer> a = new ArrayList<>(n); for (Integer x : shared) a.add(x); System.gc(); long b1 = used(rt);
        LinkedList<Integer> l = new LinkedList<>(); for (Integer x : shared) l.add(x); System.gc(); long b2 = used(rt);
        ArrayDeque<Integer> d = new ArrayDeque<>(n + 1); for (Integer x : shared) d.add(x); System.gc(); long b3 = used(rt);
        System.out.printf("ArrayList : %.1f bytes/element%nLinkedList: %.1f bytes/element%nArrayDeque: %.1f bytes/element%n",
                (b1 - b0) / (double) n, (b2 - b1) / (double) n, (b3 - b2) / (double) n);
        sink += a.size() + l.size() + d.size() + shared.length;
    }
    static long used(Runtime rt) { return rt.totalMemory() - rt.freeMemory(); }
    static String f(double v) { return Double.isNaN(v) ? "n/a" : String.format("%.1f", v); }
    static void row(String op, int n, double a, double l, double d, double m) {
        System.out.printf("%-34s %8d %12s %12s %12s %12s%n", op, n, f(a), f(l), f(d), f(m));
    }
}
