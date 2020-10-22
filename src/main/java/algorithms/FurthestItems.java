package algorithms;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An interface that represents a k-furthest items algorithm. This interface is
 * elastic, in the sense that implementors are free to implement approximate
 * solvers of the problem.
 * @param <T> The type of the items.
 */
public interface FurthestItems<T> {

    /**
     * Finds the k smallest items of a given {@link Collection}.
     * @param collection A {@link Collection} with all the items, to find its k
     * smallest.
     * @param comp A {@link Comparator} to compare the items.
     * @param k The number of smallest items that will be retrieved.
     * @param <T> The type of the items.
     * @return A {@link Collection} with the k smallest items of the given
     * {@link Collection}.
     */
    static <T> @NotNull Collection<T> minK(@NotNull Collection<T> collection,
            @NotNull Comparator<T> comp, final int k) {
        return maxK(collection, comp.reversed(), k);
    }

    /**
     * Finds the k largest items of a given {@link Collection}.
     * @param collection A {@link Collection} with all the items, to find its k
     * largest.
     * @param comp A {@link Comparator} to compare the items.
     * @param k The number of largest items that will be retrieved.
     * @param <T> The type of the items.
     * @return A {@link Collection} with the k largest items of the given
     * {@link Collection}.
     * @throws IllegalArgumentException If {@code k > collection.size() || k <
     * 1}.
     */
    static <T> @NotNull Collection<T> maxK(@NotNull Collection<T> collection,
            @NotNull Comparator<T> comp, final int k) {
        if (k < 1 || k > collection.size()) {
            throw new IllegalArgumentException("Argument k must be in " +
                    "[1, collection.size()].");
        }//end if

        PriorityQueue<T> minQueue = new PriorityQueue<>(collection.size(),
                comp);
        Iterator<T> itr = collection.iterator();

        while (minQueue.size() < k) {
            minQueue.add(itr.next());
        }//end while

        while (itr.hasNext()) {
            T v = itr.next();
            if (comp.compare(v, minQueue.element()) > 0) {
                minQueue.remove();
                minQueue.add(v);
            }//end if
        }//end while

        return minQueue;
    }

    @Deprecated(forRemoval = true)
    static <T> long bench(@NotNull FurthestItems<T> algorithm, @NotNull
            Collection<T> query, final int k) {
        final long START = System.nanoTime();
        algorithm.find(query, k);
        return System.nanoTime() - START;
    }

    /**
     * Solves the k-furthest items problem
     * @param query A {@link Collection} with the query items.
     * @param k The number of furthest items to compute.
     * @return A {@link Map} that accepts an item of the query and returns a
     * subset {@link Collection} of the universe, such that every item in that
     * {@link Collection} is inside the k-furthest, from that query item.
     * @throws IllegalArgumentException If {@code query.isEmpty()}.
     * @throws IllegalArgumentException If {@code k < 1 || k > universe.size()}.
     */
    default @NotNull Map<T, Collection<T>> find(@NotNull Collection<T> query,
            final int k) {
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection query " +
                    "can't be empty.");
        }//end if

        return query.parallelStream()
                    .collect(Collectors.toConcurrentMap(Function.identity(),
                            q -> this.find(q, k)));
    }

    /**
     * Solves the k-furthest items problem, with a single query item.
     * @param query The query item.
     * @param k The number of furthest items to compute.
     * @return A subset {@link Collection} of the universe, such that every item
     * in that {@link Collection} is inside the k-furthest, from the query item.
     * @throws IllegalArgumentException If {@code k < 1 || k > universe.size()}.
     */
    @NotNull Collection<T> find(@NotNull T query, final int k);

}//end interface FurthestItems