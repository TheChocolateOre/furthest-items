package algorithms;

import org.jetbrains.annotations.NotNull;
import util.Vector;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An approximate algorithm that solves the k-furthest items problem. It can
 * guarantee the quality of its solutions.
 * @param <T> The type of the items.
 */
public class GuaranteedDrusilla<T> implements FurthestItems<T> {

    /**
     * A {@link Collection} with the reference items this {@link
     * GuaranteedDrusilla} runs on.
     */
    private @NotNull Collection<T> universe;

    /**
     * A {@link Function} that accepts an item and returns its {@link Vector}
     * representation.
     */
    private @NotNull Function<T, Vector> toVector;

    /**
     * The set size.
     */
    private int m;

    /**
     * The approximation level.
     */
    private double e;

    /**
     * The center {@link Vector} of universe {@link Collection}.
     */
    private @NotNull Vector center;

    /**
     * A {@link FurthestItems} instance to compute the furthest items on the
     * processed set.
     */
    private @NotNull FurthestItems<T> algorithm;

    /**
     * Creates a {@link GuaranteedDrusilla}, ready to accept queries.
     * @param universe A {@link Collection} with the reference items.
     * @param toVector A {@link Function} that accepts an item and returns its
     * {@link Vector} representation.
     * @param e The approximation level.
     * @param m The set size.
     * @throws IllegalArgumentException If universe has no items.
     * @throws IllegalArgumentException If {@code e <= 0.0 || e >= 1.0}.
     * @throws IllegalArgumentException If {@code m < 1}.
     */
    public GuaranteedDrusilla(@NotNull Collection<T> universe, @NotNull
            Function<T, Vector> toVector, final double e, final int m) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection universe " +
                    "can't be empty.");
        }//end if

        if (e <= 0.0 || e >= 1.0) {
            throw new IllegalArgumentException("Argument e must be in (0, 1).");
        }//end if

        if (m < 1) {
            throw new IllegalArgumentException("Argument m must be >= 1.");
        }//end if

        Vector center = Vector.center(universe.stream()
                                              .map(toVector)
                                              .iterator());

        class Item {
            @NotNull T data;
            @NotNull Vector v;
            final double NORM;
            double s;

            Item(@NotNull T data) {
                this.data = data;
                this.v = Vector.subtract(toVector.apply(data), center);
                this.NORM = this.v.norm2();
            }
        }//end local class Item

        Set<Item> items = universe.parallelStream()
                                  .map(Item::new)
                                  .filter(i -> i.NORM != 0.0)
                                  .sorted(Comparator.comparingDouble((Item i) ->
                                          i.NORM).reversed())
                                  .collect(Collectors.toCollection(
                                          LinkedHashSet::new));

        Collection<T> r = new ArrayList<>();
        final double THRESHOLD = items.iterator().next().NORM * e / (6 + 3 * e);

        Item min;
        Iterator<Item> itr = items.iterator();
        do {
            min = itr.next();
        } while (itr.hasNext());

        //Checks if all the items will eventually be collected, for performance
        //reasons
        if (min.NORM > THRESHOLD) {
            items.forEach(i -> r.add(i.data));

            this.universe = universe;
            this.toVector = toVector;
            this.e = e;
            this.m = m;

            this.center = center;
            this.algorithm = new BruteForce<>(r, (i1, i2) -> Vector.sqrDistance(
                    toVector.apply(i1), toVector.apply(i2)));
            return;
        }//end if

        //If true items set is virtually empty. If false items set is or is not
        //empty. Used for performance reasons
        boolean isItemsEmpty = false;
        Item max;
        while ((max = items.iterator().next()).NORM > THRESHOLD) {
            if (items.size() <= m) {
                items.forEach(i -> r.add(i.data));
                isItemsEmpty = true;
                break;
            }//end if

            Vector u = new Vector(max.v).divide(max.NORM);
            items.parallelStream()
                 .forEach(i -> {
                     final double o = Vector.dotProduct(i.v, u);
                     final double d = Vector.subtract(i.v,
                             Vector.multiply(u, o)).norm2();
                     i.s = Math.abs(o) - d;
                 });

            FurthestItems.maxK(items, Comparator.comparingDouble(i -> i.s), m)
                         .forEach(i -> {r.add(i.data); items.remove(i);});
        }//end while

        if (!isItemsEmpty) {
            items.stream()
                 .map(i -> i.data)
                 .findAny()
                 .ifPresent(r::add);
        }//end if

        this.universe = universe;
        this.toVector = toVector;
        this.e = e;
        this.m = m;

        this.center = center;
        this.algorithm = new BruteForce<>(r, (i1, i2) -> Vector.sqrDistance(
                toVector.apply(i1), toVector.apply(i2)));
    }

    /**
     * Solves approximately the k-furthest items problem, with a single query
     * item.
     * @param query The query item.
     * @param k The number of furthest items to compute.
     * @return A subset {@link Collection} of the universe, such that every item
     * in that {@link Collection} is approximately inside the k-furthest, from
     * the query item.
     * @throws IllegalArgumentException If {@code k < 1 || k > universe.size()}.
     */
    @Override
    public @NotNull Collection<T> find(@NotNull T query, int k) {
        //The query must not be centered at the origin, as the reference items
        //of this.algorithm are the original, before the centering took place
        return this.algorithm.find(query, k);
    }

    @Override
    public String toString() {
        return String.format("Guaranteed Drusilla - m: %d - e: %f", this.m,
                this.e);
    }

}//end class GuaranteedDrusilla