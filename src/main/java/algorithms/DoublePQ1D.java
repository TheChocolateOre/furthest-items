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
        @NotNull Collection<T> furthest;
        @Nullable Queue<T> minQ = new LinkedList<>();
        @Nullable Queue<T> maxQ = new LinkedList<>();

        public Bucket(@NotNull T query, final int k) {
            this.QUERY_VALUE = DoublePQ1D.this.toDouble.applyAsDouble(query);
            this.furthest = new ArrayList<>(k);
        }

        void feed(@NotNull T min, @NotNull T max) {
            this.minQ.add(min);
            this.maxQ.add(max);
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

    private class TransitiveMap implements Map<T, Collection<T>> {

        @NotNull Map<T, Bucket> wrappedItems;
        @Nullable Collection<Collection<T>> values;
        @Nullable Set<Entry<T, Collection<T>>> entries;

        TransitiveMap(@NotNull Map<T, Bucket> wrappedItems) {
            this.wrappedItems = wrappedItems;
        }

        @Override
        public int size() {
            return this.wrappedItems.size();
        }

        @Override
        public boolean isEmpty() {
            return this.wrappedItems.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.wrappedItems.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.wrappedItems.entrySet()
                       .stream()
                       .anyMatch(e -> e.getValue().furthest.equals(value));
        }

        @Override
        public Collection<T> get(Object key) {
            return this.wrappedItems.get(key).furthest;
        }

        @Override
        public Collection<T> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Set<T> keySet() {
            return this.wrappedItems.keySet();
        }

        @Override
        public @NotNull Collection<Collection<T>> values() {
            if (null == this.values) {
                this.values = this.wrappedItems.values()
                        .stream()
                        .map(b -> b.furthest)
                        .collect(Collectors.toUnmodifiableList());
            }//end if

            return this.values;
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

        @Override
        public void putAll(@NotNull Map<? extends T, ? extends Collection<T>>
                m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable Collection<T> put(T key, Collection<T> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return this.wrappedItems.toString();
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
                                      .feed(MIN.ITEM, MAX.ITEM));
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
            bucket.feed(MIN.ITEM, MAX.ITEM);
        }//end for

        this.minPQ.addAll(minBuffer);
        this.maxPQ.addAll(maxBuffer);

        return bucket.furthest;
    }

}//end class DoublePQ1D