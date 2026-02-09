package analytics;

import java.util.Collection;

/**
 * A utility class to calculate statistics.
 *
 * @author Aadarsh Pandey
 * @since 10th Feb 2026
 */
public class StatisticsCalculator {

    /**
     * Calculates the standard deviation of a collection of values.
     *
     * @param values The collection of values.
     * @return The standard deviation of the values.
     */
    public static double stdDeviation(Collection<Integer> values) {
        double mean = values.stream().mapToInt(i -> i).average().orElse(0);
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }
}
