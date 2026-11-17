import java.util.*;

/** Q5 - Reusable generic ranking component derived from the "consistent comparison" principle. */
public class Q5Design {

    // ---------- domain classes (NOT Comparable: ranking rules are policies, not identity) ----------
    static class Team {
        final String name; final int points; final int goalDiff;
        Team(String n, int p, int g) { name = n; points = p; goalDiff = g; }
        @Override public String toString() { return name + "(" + points + "," + goalDiff + ")"; }
    }
    static class EliteTeam extends Team { EliteTeam(String n, int p, int g) { super(n, p, g); } }
    record Player(String name, int runs, int catches) { }

    /** Alternative: Team that bakes ONE ordering into the class via Comparable. */
    static class ComparableTeam extends Team implements Comparable<ComparableTeam> {
        ComparableTeam(String n, int p, int g) { super(n, p, g); }
        @Override public int compareTo(ComparableTeam o) {
            int c = Integer.compare(o.points, points); if (c != 0) return c;
            c = Integer.compare(o.goalDiff, goalDiff); if (c != 0) return c;
            return name.compareTo(o.name);
        }
    }

    // ---------- the correct comparator: Integer.compare / thenComparing, each key reversed individually ----------
    static final Comparator<Team> BY_RANK = Comparator.comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
            .thenComparing(t -> t.name);

    // ---------- faulty comparators (deliberately broken) ----------
    static final Comparator<Team> F1_SUBTRACTION = (a, b) -> b.points - a.points;            // overflow
    static final Comparator<Team> F2_NO_TIEBREAK = (a, b) -> Integer.compare(b.points, a.points); // ignores goalDiff and name
    static final Comparator<Team> F3_FUZZY = (a, b) -> Math.abs(a.points - b.points) <= 2 ? 0 : Integer.compare(b.points, a.points); // not transitive
    static final Comparator<Team> F4_REVERSED_ALL = BY_RANK.reversed();                          // misuse: reverses the name rule too

    // ---------- generic reusable component ----------
    static class Leaderboard<T> {
        private final List<T> items = new ArrayList<>();
        private final Comparator<? super T> order;       // ? super T : a Comparator<Object>/<Team> can rank Elite teams
        Leaderboard(Comparator<? super T> order) { this.order = Objects.requireNonNull(order); }
        void add(T item) { items.add(item); }
        void addAll(Collection<? extends T> more) { items.addAll(more); }   // ? extends T : producer
        List<T> ranking() { List<T> copy = new ArrayList<>(items); copy.sort(order); return copy; }
        /** bounded type: only types that know their own natural order may use this factory */
        static <E extends Comparable<? super E>> Leaderboard<E> natural() { return new Leaderboard<>(Comparator.naturalOrder()); }
    }

    /** Wildcard in a stand-alone method: reads from any list of subtypes of T. */
    static <T> List<T> top(List<? extends T> source, int n, Comparator<? super T> order) {
        List<T> copy = new ArrayList<>(source); copy.sort(order);
        return new ArrayList<>(copy.subList(0, Math.min(n, copy.size())));
    }

    // ---------- contract checker: sgn symmetry, transitivity, consistency of equality ----------
    static String check(String label, Comparator<Team> c, List<Team> data) {
        int sym = 0, trans = 0, n = data.size();
        for (Team a : data) for (Team b : data) if (Integer.signum(c.compare(a, b)) != -Integer.signum(c.compare(b, a))) sym++;
        for (Team a : data) for (Team b : data) for (Team d : data)
            if ((c.compare(a, b) < 0 && c.compare(b, d) < 0 && c.compare(a, d) >= 0)          // a<b, b<d but not a<d
                    || (c.compare(a, b) == 0 && c.compare(b, d) == 0 && c.compare(a, d) != 0)) trans++;   // a==b, b==d but a!=d
        return String.format("%-22s antisymmetry violations=%d, transitivity violations=%d", label, sym, trans);
    }

    static <T> String sorted(List<T> in, Comparator<? super T> c) { try { List<T> l = new ArrayList<>(in); l.sort(c); return l.toString(); } catch (RuntimeException e) { return "EXCEPTION: " + e.getMessage(); } }

    static class Counting<T> implements Comparator<T> { final Comparator<T> in; long count; Counting(Comparator<T> in) { this.in = in; } public int compare(T a, T b) { count++; return in.compare(a, b); } }

    public static void main(String[] args) {
        System.out.println("Name: RITHISH KUMAR D | Reg.No: 192565089 | Java " + System.getProperty("java.version"));

        // ---------- Data set 1: ties at every level ----------
        List<Team> ties = List.of(new Team("Panthers", 9, 4), new Team("Eagles", 9, 6), new Team("Bisons", 9, 4), new Team("Cobras", 6, 1),
                new Team("Eagles2", 6, 3), new Team("Aces", 6, 1), new Team("Hawks", 3, 0), new Team("Falcons", 3, 0), new Team("Zebras", 9, 6));
        System.out.println("\n--- Data set 1 (ties at all three levels) ---");
        System.out.println("Input     : " + ties);
        Leaderboard<Team> board = new Leaderboard<>(BY_RANK); board.addAll(ties);
        System.out.println("Ranking   : " + board.ranking());
        System.out.println("Hand check: points desc; 9s -> goalDiff 6 (Eagles, Zebras by name) then 4 (Bisons, Panthers by name); 6s -> 3 (Eagles2) then 1 (Aces, Cobras); 3s -> Falcons, Hawks");
        System.out.println(check("BY_RANK (correct)", BY_RANK, ties));

        // ---------- Data set 2: designed to break faulty comparators ----------
        List<Team> trap = List.of(new Team("Big", 2_000_000_000, 0), new Team("Neg", -2_000_000_000, 0), new Team("Mid", 5, 0),
                new Team("Eight", 8, 2), new Team("Nine", 9, 7), new Team("Seven", 7, 1), new Team("Nine2", 9, 1), new Team("Aaa", 9, 1));
        System.out.println("\n--- Data set 2 (designed to break faulty comparators) ---");
        System.out.println("Input             : " + trap);
        System.out.println("Correct           : " + sorted(trap, BY_RANK));
        System.out.println("F1 subtraction    : " + sorted(trap, F1_SUBTRACTION));
        System.out.println("F2 no tie-breaker : " + sorted(trap, F2_NO_TIEBREAK));
        System.out.println("F3 fuzzy (|d|<=2) : " + sorted(trap, F3_FUZZY));
        System.out.println("F4 reversed chain : " + sorted(trap, F4_REVERSED_ALL));
        System.out.println(check("F1 subtraction", F1_SUBTRACTION, trap));
        System.out.println(check("F2 no tie-breaker", F2_NO_TIEBREAK, trap));
        System.out.println(check("F3 fuzzy", F3_FUZZY, trap));
        System.out.println(check("F4 reversed chain", F4_REVERSED_ALL, trap));
        System.out.println(check("BY_RANK (correct)", BY_RANK, trap));

        // larger random data: TimSort may detect a broken contract
        List<Team> random = new ArrayList<>(); Random r = new Random(3);
        for (int i = 0; i < 2000; i++) random.add(new Team("T" + i, r.nextInt(20), r.nextInt(10) - 5));
        System.out.println("\nSorting 2000 random teams with F3 (non-transitive): " + (sorted(random, F3_FUZZY).startsWith("EXCEPTION") ? sorted(random, F3_FUZZY) : "no exception raised, but the order is not a valid ranking"));

        // ---------- Reuse + wildcards ----------
        System.out.println("\n--- Reuse and wildcards ---");
        List<EliteTeam> elites = List.of(new EliteTeam("Lions", 12, 9), new EliteTeam("Tigers", 12, 9));
        Leaderboard<Team> mixed = new Leaderboard<>(BY_RANK); mixed.addAll(ties); mixed.addAll(elites);   // addAll(Collection<? extends T>)
        System.out.println("Teams + EliteTeams : " + mixed.ranking().subList(0, 4));
        System.out.println("top(elites,1) via List<? extends Team>: " + top(elites, 1, BY_RANK));
        Leaderboard<Player> players = new Leaderboard<>(Comparator.comparingInt(Player::runs).reversed().thenComparing(Player::name));
        players.add(new Player("Kohli", 82, 1)); players.add(new Player("Rohit", 82, 0)); players.add(new Player("Gill", 64, 2));
        System.out.println("Leaderboard<Player> (same class reused): " + players.ranking());
        Leaderboard<String> words = Leaderboard.natural(); words.add("pear"); words.add("apple"); words.add("fig");
        System.out.println("Leaderboard.natural() (bounded type)    : " + words.ranking());
        Leaderboard<ComparableTeam> ct = Leaderboard.natural();
        for (Team t : ties) ct.add(new ComparableTeam(t.name, t.points, t.goalDiff));
        System.out.println("Comparable<Team> alternative gives the same order: " + ct.ranking().toString().equals(board.ranking().toString()));

        // ---------- Alternative: three stable sorts ----------
        System.out.println("\n--- Alternative: three stable sorts (name, goalDiff, points) vs one comparator chain ---");
        int n = 200_000; List<Team> big = new ArrayList<>(); Random rr = new Random(11);
        for (int i = 0; i < n; i++) big.add(new Team("T" + rr.nextInt(50_000), rr.nextInt(40), rr.nextInt(30) - 15));
        Counting<Team> cc = new Counting<>(BY_RANK); List<Team> one = new ArrayList<>(big); one.sort(cc);
        Counting<Team> c1 = new Counting<>(Comparator.comparing((Team t) -> t.name)), c2 = new Counting<>(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()), c3 = new Counting<>(Comparator.comparingInt((Team t) -> t.points).reversed());
        List<Team> three = new ArrayList<>(big); three.sort(c1); three.sort(c2); three.sort(c3);
        System.out.println("Same final order: " + one.equals(three));
        System.out.println("Comparisons, one chain  : " + cc.count + "  (1 sort)");
        System.out.println("Comparisons, three sorts: " + (c1.count + c2.count + c3.count) + "  (3 sorts: " + c1.count + " + " + c2.count + " + " + c3.count + ")");
        double t1 = 0, t3 = 0;
        for (int run = -2; run < 5; run++) {
            List<Team> a = new ArrayList<>(big); long s = System.nanoTime(); a.sort(BY_RANK); long m = System.nanoTime();
            List<Team> b = new ArrayList<>(big); b.sort(Comparator.comparing((Team t) -> t.name)); b.sort(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()); b.sort(Comparator.comparingInt((Team t) -> t.points).reversed()); long e = System.nanoTime();
            if (run >= 0) { t1 += (m - s) / 1e6; t3 += (e - m) / 1e6; }
        }
        System.out.printf("Time for n=%d (avg of 5 runs): one chain %.1f ms, three sorts %.1f ms%n", n, t1 / 5, t3 / 5);
    }
}
