package algorithms;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.ToDoubleBiFunction;

/**
 * An exact algorithm, that solves the k-furthest items problem using brute
 * force.
 * @param <T> The type of the items.
 */
public class BruteForce<T> implements FurthestItems<T> {

    /**
     * A {@link Collection} with the reference items this {@link BruteForce}
     * runs on.
     */
    private @NotNull Collection<T> universe;

    /**
     * A {@link ToDoubleBiFunction} with the properties of a premetric, to
     * compute the distance between 2 items.
     */
    private @NotNull ToDoubleBiFunction<T, T> distFunction;

    /**
     * Creates a {@link BruteForce}, ready to accept queries.
     * @param universe A {@link Collection} with the reference items.
     * @param distFunction A {@link ToDoubleBiFunction} with the properties
     * of a premetric, to compute the distance between 2 items.
     * @throws IllegalArgumentException If universe has no items.
     */
    public BruteForce(@NotNull Collection<T> universe, @NotNull
            ToDoubleBiFunction<T, T> distFunction) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection universe " +
                    "can't be empty.");
        }//end if

        this.universe = universe;
        this.distFunction = distFunction;
    }

    /**
     * Solves the k-furthest problem with a single query item. Given an item,
     * the query and an integer k, computes a subset {@link Collection} of the
     * universe, such that every item in that {@link Collection} is inside the
     * k-furthest, from the query item.
     * @param query The query item.
     * @param k The number of furthest items to find.
     * @return A {@link Collection} with all the k-furthest items, from the
     * query item.
     */
    @Override
    public @NotNull Collection<T> find(@NotNull T query, final int k) {
        return FurthestItems.maxK(this.universe, Comparator.comparingDouble(v ->
                this.distFunction.applyAsDouble(v, query)), k);
    }

    /**
     * Sets the reference items of this {@link BruteForce}.
     * @param universe A {@link Collection} with the new reference items.
     */
    public void setUniverse(@NotNull Collection<T> universe) {
        this.universe = universe;
    }

    /**
     * Sets the distance function (premetric) of this {@link BruteForce}.
     * @param distFunction A {@link ToDoubleBiFunction} with the properties of a
     * premetric, to compute the distance between 2 items.
     */
    public void setDistFunction(@NotNull ToDoubleBiFunction<T, T>
            distFunction) {
        this.distFunction = distFunction;
    }

    @Override
    public String toString() {
        return "Brute Force";
    }

}//end class BruteForce