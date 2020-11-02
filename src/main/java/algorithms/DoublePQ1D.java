package algorithms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class DoublePQ1D<T> implements FurthestItems<T> {

    private class Wrapper implements Comparable<Wrapper> {

        final @NotNull T ITEM;
        final @NotNull Comparator<T> COMP;

        Wrapper(@NotNull T item, @NotNull Comparator<T> comp) {
            this.ITEM = item;
            this.COMP = comp;
        }

        @Override
        public int compareTo(@NotNull Wrapper o) {
            return this.COMP.compare(this.ITEM, o.ITEM);
        }

    }//end inner class Wrapper

    private class Bucket implements AutoCloseable {

        final double QUERY_VALUE;
        final int K;
        @NotNull Collection<T> furthest;
        @Nullable Queue<T> minQ = new LinkedList<>();
        @Nullable Queue<T> maxQ = new LinkedList<>();

        public Bucket(@NotNull T query, final int k) {
            this.QUERY_VALUE = DoublePQ1D.this.toDouble.applyAsDouble(query);
            this.K = k;
            this.furthest = new ArrayList<>(k);
        }

        void add(@NotNull T min, @NotNull T max) {
            if (this.furthest.size() + this.minQ.size() < this.K) {
                this.minQ.add(min);
            }//end if
            if (this.furthest.size() + this.maxQ.size() < this.K) {
                this.maxQ.add(max);
            }//end if
            this.next();
        }

        void next() {
            final double D_MIN = Math.abs(this.QUERY_VALUE -
                    DoublePQ1D.this.toDouble.applyAsDouble(
                    this.minQ.element()));
            final double D_MAX = Math.abs(this.QUERY_VALUE -
                    DoublePQ1D.this.toDouble.applyAsDouble(
                    this.maxQ.element()));

            if (D_MIN > D_MAX) {
                this.furthest.add(this.minQ.remove());
            } else {
                this.furthest.add(this.maxQ.remove());
            }//end if
        }

        @Override
        public void close() {
            //There is no need to carry the queues. We avoid calling clear(),
            //for immediate performance reasons, although makes the GC work
            //harder
            this.minQ = null;
            this.maxQ = null;
        }

        @Override
        public String toString() {
            return this.furthest.toString();
        }

    }//end inner class Bucket

    private class TransitiveMap extends AbstractMap<T, Collection<T>> {

        @NotNull Map<T, Bucket> wrappedItems;
        @Nullable Set<Entry<T, Collection<T>>> entries;

        TransitiveMap(@NotNull Map<T, Bucket> wrappedItems) {
            this.wrappedItems = wrappedItems;
        }

        @Override
        public @NotNull Set<Entry<T, Collection<T>>> entrySet() {
            if (null == this.entries) {
                this.entries = this.wrappedItems.entrySet()
                        .stream()
                        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),
                                e.getValue().furthest))
                        .collect(Collectors.toUnmodifiableSet());
            }//end if

            return this.entries;
        }

    }//end inner class TransitiveMap

    private @NotNull ToDoubleFunction<T> toDouble;
    private @NotNull PriorityQueue<Wrapper> minPQ;
    private @NotNull PriorityQueue<Wrapper> maxPQ;

    public DoublePQ1D(@NotNull Collection<T> universe, @NotNull
            ToDoubleFunction<T> toDouble) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection universe " +
                    "can't be empty.");
        }//end if

        this.toDouble = toDouble;
        this.minPQ = new PriorityQueue<>(universe.stream()
                .map(i -> new Wrapper(i, Comparator.comparingDouble(toDouble)))
                .collect(Collectors.toList()));
        this.maxPQ = new PriorityQueue<>(universe.stream()
                .map(i -> new Wrapper(i, Comparator.comparingDouble(toDouble)
                                                   .reversed()))
                .collect(Collectors.toList()));
    }

    @Override
    public @NotNull Map<T, ? extends Collection<T>> find(@NotNull Collection<T>
            query, final int k) {
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection query " +
                    "can't be empty.");
        }//end if

        if (k < 1 || k > this.minPQ.size()) {
            throw new IllegalArgumentException("Argument k must be in range " +
                    "[1, universe.size()].");
        }//end if

        Map<T, Bucket> buckets = new ConcurrentHashMap<>(query.size());
        List<Wrapper> minBuffer = new ArrayList<>(k);
        List<Wrapper> maxBuffer = new ArrayList<>(k);
        for (int i = 1; i <= k; ++i) {
            final Wrapper MIN = this.minPQ.remove();
            final Wrapper MAX = this.maxPQ.remove();
            minBuffer.add(MIN);
            maxBuffer.add(MAX);
            query.parallelStream()
                 .forEach(q -> buckets.computeIfAbsent(q, key -> new Bucket(key,
                            k))
                                      .add(MIN.ITEM, MAX.ITEM));
        }//end for

        buckets.forEach((item, bucket) -> bucket.close());
        this.minPQ.addAll(minBuffer);
        this.maxPQ.addAll(maxBuffer);

        return new TransitiveMap(buckets);
    }

    @Override
    public @NotNull Collection<T> find(@NotNull T query, final int k) {
        if (k < 1 || k > this.minPQ.size()) {
            throw new IllegalArgumentException("Argument k must be in range " +
                    "[1, universe.size()].");
        }//end if

        Bucket bucket = new Bucket(query, k);
        List<Wrapper> minBuffer = new ArrayList<>(k);
        List<Wrapper> maxBuffer = new ArrayList<>(k);
        for (int i = 1; i <= k; ++i) {
            final Wrapper MIN = this.minPQ.remove();
            final Wrapper MAX = this.maxPQ.remove();
            minBuffer.add(MIN);
            maxBuffer.add(MAX);
            bucket.add(MIN.ITEM, MAX.ITEM);
        }//end for

        this.minPQ.addAll(minBuffer);
        this.maxPQ.addAll(maxBuffer);

        return bucket.furthest;
    }

    @Override
    public String toString() {
        return "DoublePQ1D";
    }
}//end class DoublePQ1D