package analytics;

import java.util.Collection;

public class StatisticsCalculator {

    public static double stdDeviation(Collection<Integer> values) {
        double mean = values.stream().mapToInt(i -> i).average().orElse(0);
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }
}
