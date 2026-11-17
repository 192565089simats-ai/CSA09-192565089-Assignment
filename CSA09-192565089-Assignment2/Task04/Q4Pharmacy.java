import java.util.*;

/** Q4 - Hospital pharmacy: HashMap vs TreeMap vs LinkedHashMap (+ Hashtable and sorted-array/binarySearch). */
public class Q4Pharmacy {
    static long sink = 0;
    static class Medicine {
        final String code; final String name; int stock;
        Medicine(String c, String n, int s) { code = c; name = n; stock = s; }
        @Override public String toString() { return code + ":" + stock; }
    }
    static final Comparator<Medicine> BY_CODE = Comparator.comparing(m -> m.code);

    /** ------- alternative: sorted array + Arrays.binarySearch ------- */
    static class SortedArrayInventory {
        Medicine[] a = new Medicine[16]; int size = 0;
        void add(Medicine m) {
            int i = Arrays.binarySearch(a, 0, size, m, BY_CODE);
            if (i >= 0) { a[i] = m; return; }
            i = -i - 1;
            if (size == a.length) a = Arrays.copyOf(a, size * 2);
            System.arraycopy(a, i, a, i + 1, size - i); a[i] = m; size++;
        }
        Medicine get(String code) { int i = Arrays.binarySearch(a, 0, size, new Medicine(code, "", 0), BY_CODE); return i >= 0 ? a[i] : null; }
    }

    static String[] makeCodes(int n, long seed) {      // distinct, shuffled (insertion order != code order)
        List<Integer> v = new ArrayList<>(); for (int i = 0; i < n; i++) v.add(i * 7 + 13);
        Collections.shuffle(v, new Random(seed));
        String[] c = new String[n]; for (int i = 0; i < n; i++) c[i] = String.format("MED%07d", v.get(i));
        return c;
    }

    static double ms(long ns) { return ns / 1e6; }

    public static void main(String[] args) {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));
        int[] sizes = {1_000, 10_000, 100_000};
        final int LOOKUPS = 100_000, WARM = 3, RUNS = 7;
        System.out.println("Each figure = average of " + RUNS + " runs after " + WARM + " warm-up runs. Lookups = " + LOOKUPS + " random existing codes.\n");

        for (int n : sizes) {
            String[] codes = makeCodes(n, 42);
            String[] sortedCodes = codes.clone(); Arrays.sort(sortedCodes);
            String[] probe = new String[LOOKUPS]; Random r = new Random(5); for (int i = 0; i < LOOKUPS; i++) probe[i] = codes[r.nextInt(n)];
            Medicine[] meds = new Medicine[n]; for (int i = 0; i < n; i++) meds[i] = new Medicine(codes[i], "Med" + i, 10 + i % 90);

            String[] names = {"HashMap", "TreeMap", "LinkedHashMap", "Hashtable", "SortedArray+binSearch"};
            double[] put = new double[5], look = new double[5], repCode = new double[5], repIns = new double[5], repInsExtra = new double[5];
            boolean[] codeOK = new boolean[5], insOK = new boolean[5];

            for (int run = -WARM; run < RUNS; run++) {
                // ---- build + lookup + reports ----
                @SuppressWarnings("unchecked") Map<String, Medicine>[] maps = new Map[]{new HashMap<>(), new TreeMap<>(), new LinkedHashMap<>(), new Hashtable<>()};
                for (int k = 0; k < 4; k++) {
                    Map<String, Medicine> m = maps[k];
                    long t0 = System.nanoTime(); for (Medicine x : meds) m.put(x.code, x); long t1 = System.nanoTime();
                    long s = 0; for (String p : probe) s += m.get(p).stock; long t2 = System.nanoTime(); sink += s;

                    // report 1: all medicines in CODE order
                    long t3 = System.nanoTime(); List<String> rep1 = new ArrayList<>(m.size());
                    if (k == 1) { for (String key : m.keySet()) rep1.add(key); }               // TreeMap: already sorted
                    else { for (String key : m.keySet()) rep1.add(key); Collections.sort(rep1); }   // others: extra sort needed
                    long t4 = System.nanoTime();
                    // report 2: in the order ADDED
                    List<String> rep2 = new ArrayList<>(m.size()); for (String key : m.keySet()) rep2.add(key); long t5 = System.nanoTime();
                    // the "extra work" for the wrong map: keep a side list of codes in insertion order and look each one up
                    List<String> side = Arrays.asList(codes); List<Medicine> rep2b = new ArrayList<>(n); for (String c : side) rep2b.add(m.get(c)); long t6 = System.nanoTime();

                    if (run >= 0) { put[k] += ms(t1 - t0); look[k] += ms(t2 - t1); repCode[k] += ms(t4 - t3); repIns[k] += ms(t5 - t4); repInsExtra[k] += ms(t6 - t5); }
                    codeOK[k] = rep1.equals(Arrays.asList(sortedCodes));
                    insOK[k] = rep2.equals(Arrays.asList(codes));
                    sink += rep1.size() + rep2.size() + rep2b.size();
                }
                // ---- sorted array alternative ----
                SortedArrayInventory sa = new SortedArrayInventory();
                long t0 = System.nanoTime(); for (Medicine x : meds) sa.add(x); long t1 = System.nanoTime();
                long s = 0; for (String p : probe) s += sa.get(p).stock; long t2 = System.nanoTime(); sink += s;
                long t3 = System.nanoTime(); List<String> rep1 = new ArrayList<>(n); for (int i = 0; i < sa.size; i++) rep1.add(sa.a[i].code); long t4 = System.nanoTime();
                if (run >= 0) { put[4] += ms(t1 - t0); look[4] += ms(t2 - t1); repCode[4] += ms(t4 - t3); }
                codeOK[4] = rep1.equals(Arrays.asList(sortedCodes)); insOK[4] = false;
            }

            System.out.println("=== n = " + n + " medicines ===");
            System.out.printf("%-22s %10s %12s %10s %14s %12s %12s %14s%n", "Structure", "put all ms", "100k get ms", "ns/get", "code-rpt ms", "code order?", "added-rpt ms", "added order?");
            for (int k = 0; k < 5; k++) {
                String ins = k == 4 ? "-" : String.format("%.3f", repIns[k] / RUNS);
                System.out.printf("%-22s %10.2f %12.2f %10.0f %14.3f %12s %12s %14s%n", names[k], put[k] / RUNS, look[k] / RUNS, look[k] / RUNS * 1e6 / LOOKUPS,
                        repCode[k] / RUNS, codeOK[k] ? "YES" : "NO", ins, k == 4 ? "-" : (insOK[k] ? "YES" : "NO"));
            }
            System.out.printf("Extra work for 'added order' report with side-list + lookups: HashMap %.3f ms, TreeMap %.3f ms (LinkedHashMap needs none: %.3f ms)%n%n",
                    repInsExtra[0] / RUNS, repInsExtra[1] / RUNS, repIns[2] / RUNS);
        }
        System.out.println("Note: the code-order report time for HashMap, LinkedHashMap and Hashtable INCLUDES the extra Collections.sort() step; TreeMap and the sorted array iterate directly.");
        System.out.println("(sink=" + (sink != 0) + ")");
    }
}
