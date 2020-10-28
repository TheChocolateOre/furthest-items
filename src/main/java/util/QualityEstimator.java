package util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.ToDoubleBiFunction;

/**
 * Estimates the quality of solutions to the k-furthest items problem.
 * @param <T> The type of the items.
 */
@FunctionalInterface
public interface QualityEstimator<T> {

    /**
     * A {@link QualityEstimator} that flat sums the distances of query items to
     * their mapped reference items.
     * @param <T> The type of the items.
     */
    class FlatSum<T> implements QualityEstimator<T> {

        /**
         * A {@link ToDoubleBiFunction} to compute the distance between 2 items.
         */
        private @NotNull ToDoubleBiFunction<T, T> distFunction;

        /**
         * Creates a {@link FlatSum}.
         * @param distFunction A {@link ToDoubleBiFunction} to compute the
         * distance between 2 items.
         */
        public FlatSum(@NotNull ToDoubleBiFunction<T, T> distFunction) {
            this.distFunction = distFunction;
        }

        /**
         * Computes the quality of a given solution.
         * @param solution A solution to the k-furthest items problem.
         * @return The quality of the solution. High values describe higher
         * quality than low values.
         */
        @Override
        public double estimate(@NotNull Map<T, ? extends Collection<T>>
                solution) {
            return solution.entrySet()
                           .parallelStream()
                           .mapToDouble(e -> e.getValue()
                                              .parallelStream()
                                              .mapToDouble(p ->
                                                  distFunction.applyAsDouble(p,
                                                  e.getKey()))
                                              .sum())
                           .sum();
        }

    }//end inner class FlatSum

    /**
     * Computes the quality of a given solution.
     * @param solution A solution to the k-furthest items problem.
     * @return The quality of the solution. High values describe higher
     * quality than low values.
     */
    double estimate(@NotNull Map<T, ? extends Collection<T>> solution);

}//end interface QualityEstimator