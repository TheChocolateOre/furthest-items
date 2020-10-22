package util;

import algorithms.FurthestItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Benchmarks the performance of {@link FurthestItems} algorithms.
 * @param <T> The type of the items of the {@link FurthestItems}
 * algorithms.
 */
public class Benchmark<T> {

    private class Instance {

        final long INIT_DURATION;
        final long RUN_DURATION;
        final @NotNull Map<T, Collection<T>> SOLUTION;
        final @NotNull String ALGORITHM_NAME;

        Instance(@NotNull Function<Collection<T>, FurthestItems<T>>
                initFunction, @NotNull Collection<T> query, final int k) {
            final long INIT_START = System.nanoTime();
            FurthestItems<T> furthestItems = initFunction.apply(
                    Benchmark.this.universe);
            this.INIT_DURATION = System.nanoTime() - INIT_START;

            final long RUN_START = System.nanoTime();
            this.SOLUTION = furthestItems.find(query, k);
            this.RUN_DURATION = System.nanoTime() - RUN_START;
            this.ALGORITHM_NAME = furthestItems.toString();
        }

    }//end inner class Instance

    private class InstanceGroup {

        final @NotNull List<Instance> instances;

        InstanceGroup(final int size, @NotNull Function<Collection<T>,
                FurthestItems<T>> initFunction, @NotNull Collection<T> query,
                final int k) {
            List<Instance> instances = new ArrayList<>(size);
            for (int i = 1; i <= size; ++i) {
                instances.add(new Instance(initFunction, query, k));
            }//end for

            this.instances = instances;
        }

        /**
         * Gets the average initialization duration of the algorithm in seconds.
         * @return The average initialization duration of the algorithm in
         * seconds.
         */
        double avgInitDuration() {
            return this.instances.stream()
                                 .mapToLong(i -> i.INIT_DURATION)
                                 .average()
                                 .orElseThrow() / 1_000_000_000.0;
        }

        /**
         * Gets the average running duration of the algorithm in seconds.
         * @return The average running duration of the algorithm in seconds.
         */
        double avgRunDuration() {
            return this.instances.stream()
                                 .mapToLong(i -> i.RUN_DURATION)
                                 .average()
                                 .orElseThrow() / 1_000_000_000.0;
        }

        /**
         * Gets the average initialization + running duration of the algorithm
         * in seconds.
         * @return The average initialization + running duration of the
         * algorithm in seconds.
         */
        double avgTotalDuration() {
            return this.instances.stream()
                                 .mapToLong(i -> i.INIT_DURATION +
                                         i.RUN_DURATION)
                                 .average()
                                 .orElseThrow() / 1_000_000_000.0;
        }

        double avgQualityRatio(double optQuality) {
            return this.instances.stream()
                    .mapToDouble(i -> optQuality /
                            Benchmark.this.qualityEstimator.estimate(
                            i.SOLUTION))
                    .average()
                    .orElseThrow();
        }

        @NotNull String algorithmName() {
            return this.instances.get(0).ALGORITHM_NAME;
        }

    }//end inner class InstanceGroup

    private @NotNull Collection<T> universe;
    private @NotNull String datasetName;
    private @NotNull Collection<Function<Collection<T>, FurthestItems<T>>>
            initFunctions;
    private int runs;
    private @NotNull List<InstanceGroup> instanceGroups;
    private @NotNull QualityEstimator<T> qualityEstimator;

    /**
     * Creates a {@link Benchmark}. It will not be initialized, the creation is
     * lightweight.
     * @param universe A {@link Collection} with the items the algorithms will
     * run on.
     * @param datasetName The name of the dataset, i.e. the name of the data in
     * universe {@link Collection}.
     * @param initFunctions A {@link Collection} of {@link Function}s that
     * accept the dataset and return a {@link FurthestItems} instance that will
     * run on that dataset. That dataset will be the universe {@link Collection}
     * from above.
     * @param runs How many times the benchmark will run. The average of the end
     * result will be taken.
     * @param qualityEstimator A {@link QualityEstimator} to estimate the
     * quality of the results during the benchmark.
     * @see #perform(Collection, int)
     */
    public Benchmark(@NotNull Collection<T> universe, @NotNull String
            datasetName, @NotNull Collection<Function<Collection<T>,
            FurthestItems<T>>> initFunctions, int runs, @NotNull
            QualityEstimator<T> qualityEstimator) {
        this.universe = universe;
        this.datasetName = datasetName;
        this.initFunctions = initFunctions;
        this.runs = runs;
        this.instanceGroups = new ArrayList<>(initFunctions.size());
        this.qualityEstimator = qualityEstimator;
    }

    /**
     * Performs the benchmark. The results are stored internally.
     * @param query A {@link Collection} with the query items.
     * @param k The number of furthest items to compute.
     * @see #print()
     */
    public void perform(@NotNull Collection<T> query, final int k) {
        if (!this.instanceGroups.isEmpty()) {
            this.instanceGroups.clear();
        }//end if

        for (var f : this.initFunctions) {
            this.instanceGroups.add(new InstanceGroup(this.runs, f, query, k));
        }//end for
    }

    /**
     * Prints the result of this {@link Benchmark}.
     * @throws IllegalStateException If {@link #perform(Collection, int)} is not
     * called.
     */
    public void print() {
        if (this.instanceGroups.isEmpty()) {
            throw new IllegalStateException("Call perform() first.");
        }//end if

        final double OPT_QUALITY = this.qualityEstimator.estimate(
                this.instanceGroups.get(0).instances.get(0).SOLUTION);
        System.out.println("-------------------------------------------------");
        System.out.println(this.datasetName);
        this.instanceGroups.forEach(g -> this.print(g, OPT_QUALITY));
    }

    private void print(@NotNull InstanceGroup instanceGroup, final double
            optQuality) {
        System.out.println("--------------------------------");
        System.out.println(instanceGroup.algorithmName());
        System.out.println();
        System.out.printf("%20s %7.2fs%n", "avg. init duration:",
                instanceGroup.avgInitDuration());
        System.out.printf("%20s %7.2fs%n", "avg. run duration:",
                instanceGroup.avgRunDuration());
        System.out.printf("%20s %7.2fs%n", "avg. total duration:",
                instanceGroup.avgTotalDuration());
        System.out.printf("%20s %f%n", "avg. quality ratio:",
                instanceGroup.avgQualityRatio(optQuality));
    }

}//end class Benchmark