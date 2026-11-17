import java.util.*;

/** Q3 - Investigating the equals()/hashCode() contract with HashSet and TreeSet. */
public class Q3Experiments {

    /** The junior developer's class: equals() on regNo, hashCode() NOT overridden. */
    static class BadStudent {
        String regNo; String name;
        BadStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { if (!(o instanceof BadStudent)) return false; return regNo.equals(((BadStudent) o).regNo); }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Corrected class: immutable key, hashCode consistent with equals. */
    static final class GoodStudent {
        private final String regNo; private final String name;
        GoodStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GoodStudent)) return false; return regNo.equals(((GoodStudent) o).regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        String regNo() { return regNo; }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Mutable key: hashCode uses regNo, but regNo is NOT final (dangerous). */
    static class MutableStudent {
        String regNo; String name;
        MutableStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof MutableStudent && regNo.equals(((MutableStudent) o).regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** natural ordering by NAME while equals() uses regNo -> inconsistent with equals */
    static class NameOrdered implements Comparable<NameOrdered> {
        final String regNo, name;
        NameOrdered(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof NameOrdered && regNo.equals(((NameOrdered) o).regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public int compareTo(NameOrdered o) { return name.compareTo(o.name); }
        @Override public String toString() { return regNo + " " + name; }
    }

    static void heading(String s) { System.out.println("\n----- " + s + " -----"); }

    public static void main(String[] args) {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));

        heading("TEST A: HashSet<BadStudent> that APPEARS to work");
        BadStudent a1 = new BadStudent("192311001", "Arun"), a2 = new BadStudent("192311002", "Divya");
        Set<BadStudent> A = new HashSet<>(); A.add(a1); A.add(a2); A.add(a1);   // a1 scanned twice - the SAME object
        System.out.println("PREDICTION: size 2. Same object -> identical identity hashCode -> same bucket, and equals() is true.");
        System.out.println("OBSERVED  : size = " + A.size());
        System.out.println("hashCode(a1) twice: " + a1.hashCode() + " and " + a1.hashCode() + " (same object, same value)");

        heading("TEST B: HashSet<BadStudent> with duplicate scans made by NEW objects");
        Set<BadStudent> B = new HashSet<>();
        B.add(new BadStudent("192311001", "Arun")); B.add(new BadStudent("192311002", "Divya")); B.add(new BadStudent("192311001", "Arun"));
        System.out.println("PREDICTION: size 3 (wrong; should be 2). equals() says the two Arun objects are equal, but their identity-based hashCodes differ, so they sit in different buckets and equals() is never called.");
        System.out.println("OBSERVED  : size = " + B.size() + "  -> " + B);
        BadStudent x = new BadStudent("192311001", "Arun"), y = new BadStudent("192311001", "Arun");
        System.out.println("x.equals(y) = " + x.equals(y) + ", x.hashCode()==y.hashCode() ? " + (x.hashCode() == y.hashCode()));
        // bulk version
        Set<BadStudent> bulkBad = new HashSet<>(); Set<GoodStudent> bulkGood = new HashSet<>();
        for (int round = 0; round < 3; round++) for (int i = 0; i < 1000; i++) { String r = String.valueOf(192310000 + i); bulkBad.add(new BadStudent(r, "S" + i)); bulkGood.add(new GoodStudent(r, "S" + i)); }
        System.out.println("PREDICTION (bulk): 1000 students scanned 3 times each -> bad class ~3000, fixed class 1000.");
        System.out.println("OBSERVED  (bulk): bad class = " + bulkBad.size() + ", fixed class = " + bulkGood.size());

        heading("TEST C: field used in hashCode() changed AFTER insertion (even with a correct hashCode)");
        MutableStudent m = new MutableStudent("192311001", "Arun");
        Set<MutableStudent> C = new HashSet<>(); C.add(m);
        System.out.println("Before change: contains(m) = " + C.contains(m) + ", size = " + C.size());
        m.regNo = "192311999";   // key field mutated while stored in the set
        System.out.println("PREDICTION: size stays 1, but contains(m) = false and remove(m) = false, because m is still stored in the bucket of the OLD hash while lookups now use the NEW hash.");
        System.out.println("OBSERVED  : size = " + C.size() + ", contains(m) = " + C.contains(m)
                + ", contains(new equal key) = " + C.contains(new MutableStudent("192311999", "Arun"))
                + ", remove(m) = " + C.remove(m));
        int seen = 0; for (MutableStudent s : C) seen++;
        System.out.println("Iteration still finds " + seen + " element (a 'ghost' entry that cannot be looked up or removed)");
        C.add(new MutableStudent("192311999", "Arun"));
        System.out.println("Adding an equal student now gives size = " + C.size() + " (duplicate admitted)");

        heading("TEST D: TreeSet whose compareTo() (by name) is inconsistent with equals() (by regNo)");
        NameOrdered d1 = new NameOrdered("192311010", "Kiran"), d2 = new NameOrdered("192311020", "Kiran"), d3 = new NameOrdered("192311030", "Meena");
        Set<NameOrdered> hs = new HashSet<>(List.of(d1, d2, d3));
        Set<NameOrdered> ts = new TreeSet<>(List.of(d1, d2, d3));
        System.out.println("PREDICTION: HashSet size 3 (uses equals/hashCode: three different regNo). TreeSet size 2 (uses only compareTo: the two 'Kiran' entries compare as 0, so the second is dropped).");
        System.out.println("OBSERVED  : HashSet size = " + hs.size() + ", TreeSet size = " + ts.size() + "  -> TreeSet = " + ts);
        System.out.println("d1.equals(d2) = " + d1.equals(d2) + " but d1.compareTo(d2) = " + d1.compareTo(d2));

        heading("VERIFICATION of the corrected class (GoodStudent)");
        GoodStudent g1 = new GoodStudent("192311001", "Arun"), g2 = new GoodStudent("192311001", "Arun (Gate 3)");
        System.out.println("equals symmetric: " + (g1.equals(g2) && g2.equals(g1)) + " | equal => same hashCode: " + (g1.hashCode() == g2.hashCode()));
        Set<GoodStudent> G = new HashSet<>(List.of(g1, new GoodStudent("192311002", "Divya"))); G.add(g2);
        System.out.println("Set size after the Gate-3 re-scan = " + G.size() + " (expected 2)");

        heading("ALTERNATIVES: HashSet vs TreeSet(Comparator on regNo) vs HashMap<String,Student>");
        int unique = 100_000, scans = 400_000;
        String[] ids = new String[scans]; Random rnd = new Random(7);
        for (int i = 0; i < scans; i++) ids[i] = String.valueOf(192300000 + rnd.nextInt(unique));
        System.out.printf("%d scans over up to %d unique students (averages of 7 runs after 3 warm-ups)%n", scans, unique);
        double[] t = new double[3]; int[] sz = new int[3];
        for (int run = -3; run < 7; run++) {
            long t0 = System.nanoTime(); Set<GoodStudent> hset = new HashSet<>(); for (String id : ids) hset.add(new GoodStudent(id, "n")); long t1 = System.nanoTime();
            Set<GoodStudent> tset = new TreeSet<>(Comparator.comparing(GoodStudent::regNo)); for (String id : ids) tset.add(new GoodStudent(id, "n")); long t2 = System.nanoTime();
            Map<String, GoodStudent> hmap = new HashMap<>(); for (String id : ids) hmap.putIfAbsent(id, new GoodStudent(id, "n")); long t3 = System.nanoTime();
            if (run >= 0) { t[0] += (t1 - t0) / 1e6; t[1] += (t2 - t1) / 1e6; t[2] += (t3 - t2) / 1e6; }
            sz[0] = hset.size(); sz[1] = tset.size(); sz[2] = hmap.size();
        }
        System.out.printf("HashSet<Student>              : %7.2f ms for %d scans (%.0f ns/scan), unique = %d%n", t[0] / 7, scans, t[0] / 7 * 1e6 / scans, sz[0]);
        System.out.printf("TreeSet<Student>(regNo cmp)   : %7.2f ms for %d scans (%.0f ns/scan), unique = %d%n", t[1] / 7, scans, t[1] / 7 * 1e6 / scans, sz[1]);
        System.out.printf("HashMap<String,Student>       : %7.2f ms for %d scans (%.0f ns/scan), unique = %d%n", t[2] / 7, scans, t[2] / 7 * 1e6 / scans, sz[2]);
    }
}
