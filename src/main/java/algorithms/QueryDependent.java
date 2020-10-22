package algorithms;

import org.jetbrains.annotations.NotNull;
import util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An approximate algorithm that solves the k-furthest items problem.
 * @param <T> The type of the items.
 */
public class QueryDependent<T> implements FurthestItems<T> {

    /**
     * A {@link Collection} with the reference items this {@link QueryDependent}
     * runs on.
     */
    private @NotNull Collection<T> universe;

    /**
     * A {@link Function} that accepts an item and returns its {@link Vector}
     * representation.
     */
    private @NotNull Function<T, Vector> toVector;

    /**
     * The number of random lines.
     */
    private int l;

    /**
     * The number of candidates to be examined at query time.
     */
    private int m;

    private @NotNull List<Vector> a;
    private @NotNull List<List<T>> s;

    private @NotNull Map<T, Double>[] caches;

    private static <T> @NotNull List<List<T>> computeS(@NotNull Collection<T>
            universe, @NotNull Function<T, Vector> toVector, final int l, final
            int m, @NotNull List<Vector> a, @NotNull Map<T, Double>[] caches) {
        return IntStream.range(0, l)
                .parallel()
                .mapToObj(i -> {
                    Map<T, Double> xCache = universe.parallelStream()
                            .collect(Collectors.toConcurrentMap(
                                    Function.identity(),
                                    x -> Vector.dotProduct(a.get(i),
                                    toVector.apply(x))));
                    //Critical line as it is a side effect and we write
                    //concurrently
                    caches[i] = xCache;
                    List<T> result = new ArrayList<>(FurthestItems.maxK(
                            universe,
                            Comparator.comparingDouble(xCache::get),
                            m));
                    result.sort(Comparator.comparingDouble(xCache::get)
                                          .reversed());
                    return result;
                })
                .collect(Collectors.toList());
    }

    private static <T> int dimensions(@NotNull Collection<T> universe, @NotNull
            Function<T, Vector> toVector) {
        return toVector.apply(universe.stream()
                                      .findAny()
                                      .orElseThrow())
                       .size();
    }

    private static List<Vector> randomVectors(final int num, final int
            dimensions) {
        if (num < 1) {
            throw new IllegalArgumentException("Argument num must be >= 1.");
        }//end if

        return Stream.generate(() -> new Vector(dimensions, i ->
                            ThreadLocalRandom.current().nextGaussian()))
                     .parallel()
                     .limit(num)
                     .collect(Collectors.toList());
    }

    /**
     * Creates a {@link QueryDependent}, ready to accept queries.
     * @param universe A {@link Collection} with the reference items.
     * @param toVector A {@link Function} that accepts an item and returns its
     * {@link Vector} representation.
     * @param l The number of random lines.
     * @param m The number of candidates to be examined at query time.
     * @throws IllegalArgumentException If universe has no items.
     * @throws IllegalArgumentException If {@code l < 1}.
     * @throws IllegalArgumentException If {@code m < 1}.
     */
    public QueryDependent(@NotNull Collection<T> universe, @NotNull Function<T,
            Vector> toVector, final int l, final int m) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection universe " +
                    "can't be empty.");
        }//end if

        if (l < 1) {
            throw new IllegalArgumentException("Argument l must be >= 1.");
        }//end if

        if (m < 1) {
            throw new IllegalArgumentException("Argument m must be >= 1.");
        }//end if

        @SuppressWarnings("unchecked")
        Map<T, Double>[] caches = (Map<T, Double>[]) new Map[l];

        this.universe = universe;
        this.toVector = toVector;
        this.l = l;
        this.m = m;
        this.a = QueryDependent.randomVectors(l, QueryDependent.dimensions(
                universe, toVector));
        this.s = QueryDependent.computeS(universe, toVector, l, m, this.a,
                caches);
        this.caches = caches;
    }

    /**
     * Solves approximately, the k-furthest items problem, with a single query
     * item.
     * @param query The query item.
     * @param k The number of furthest items to compute.
     * @return A subset {@link Collection} of the universe that is immutable and
     * contains the furthest approximate item, from the query item.
     * @throws IllegalArgumentException If {@code k != 1}.
     */
    @Override
    public @NotNull Collection<T> find(@NotNull T query, int k) {
        if (k != 1) {
            throw new IllegalArgumentException("Argument k must be 1.");
        }//end if

        class Pair implements Comparable<Pair> {
            @NotNull T x;
            int i;
            double value;

            public Pair(@NotNull T x, int i, double value) {
                this.x = x;
                this.i = i;
                this.value = value;
            }

            @Override
            public int compareTo(@NotNull Pair o) {
                return Double.compare(this.value, o.value);
            }
        }//end local class Pair

        final Vector q = this.toVector.apply(query);
        PriorityQueue<Pair> maxQ = new PriorityQueue<>(
                Comparator.reverseOrder());
        List<Double> values = this.a.parallelStream()
                                    .map(a -> Vector.dotProduct(a, q))
                                    .collect(Collectors.toList());
        List<Iterator<T>> iterators = this.s.stream()
                                            .map(List::iterator)
                                            .collect(Collectors.toList());

        for (int i = 0; i < this.l; ++i) {
            T x = iterators.get(i).next();
            maxQ.add(new Pair(x, i, this.caches[i].get(x) - values.get(i)));
        }//end for

        T rual = null;
        for (int j = 0; j < this.m; ++j) {
            if (maxQ.isEmpty()) break;
            Pair maxPair = maxQ.remove();
            if (null == rual || Vector.sqrDistance(this.toVector.apply(
                    maxPair.x), q) > Vector.sqrDistance(this.toVector.apply(
                    rual), q)) {
                rual = maxPair.x;
            }//end if
            if (!iterators.get(maxPair.i).hasNext()) continue;
            T x2 = iterators.get(maxPair.i).next();
            maxQ.add(new Pair(x2, maxPair.i, this.caches[maxPair.i].get(x2)
                    - values.get(maxPair.i)));
        }//end for

        return Collections.singleton(rual);
    }

    /**
     * Sets the reference data.
     * @param universe A {@link Collection} with the new reference data.
     * @throws IllegalArgumentException If the given {@link Collection} is
     * empty.
     */
    public void setUniverse(@NotNull Collection<T> universe) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection universe " +
                    "can't be empty.");
        }//end if

        final int d = QueryDependent.dimensions(universe, this.toVector);
        if (d != this.a.get(0).size()) {
            this.a = QueryDependent.randomVectors(l, d);
        }//end if

        this.universe = universe;
        this.s = QueryDependent.computeS(universe, this.toVector, this.l,
                this.m, this.a, this.caches);
    }

    /**
     * Sets the {@link Function} that extracts a {@link Vector} from an item.
     * @param toVector A {@link Function} that extracts a {@link Vector} from an
     * item.
     */
    public void setToVectorFunction(@NotNull Function<T, Vector> toVector) {
        this.toVector = toVector;
        this.s = QueryDependent.computeS(this.universe, toVector, this.l,
                this.m, this.a, this.caches);
    }

    /**
     * Sets the number of random lines.
     * @param l The number of random lines.
     * @throws IllegalArgumentException If {@code l < 1}.
     */
    public void setL(final int l) {
        if (l < 1) {
            throw new IllegalArgumentException("Argument l must be >= 1.");
        }//end if

        if (l == this.l) {
            return;
        }//end if

        if (l > this.caches.length) {
            @SuppressWarnings("unchecked")
            Map<T, Double>[] caches = (Map<T, Double>[]) new Map[l];
            this.caches = caches;
        }//end if

        this.a = QueryDependent.randomVectors(l, this.a.get(0).size());
        this.s = QueryDependent.computeS(this.universe, this.toVector, this.l,
                this.m, this.a, this.caches);
    }

    /**
     * Sets the number of candidates to be examined at query time.
     * @param m The number of candidates to be examined at query time.
     * @throws IllegalArgumentException If {@code m < 1}.
     */
    public void setM(final int m) {
        if (m < 1) {
            throw new IllegalArgumentException("Argument m must be >= 1.");
        }//end if

        if (m == this.m) {
            return;
        }//end if

        this.m = m;
        this.s = QueryDependent.computeS(this.universe, this.toVector, this.l,
                m, this.a, this.caches);
    }

    @Override
    public String toString() {
        return String.format("Query Dependent - l: %d - m: %d", this.l, this.m);
    }

}//end class QueryDependent