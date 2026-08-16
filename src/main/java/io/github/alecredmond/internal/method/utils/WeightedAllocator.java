package io.github.alecredmond.internal.method.utils;

import java.util.*;
import java.util.stream.Collectors;

public class WeightedAllocator {

  private WeightedAllocator() {}

  public static <T> Map<T, Integer> allocate(Map<T, ? extends Number> weights, int toAllocate) {
    double totalWeight = weights.values().stream().mapToDouble(Number::doubleValue).sum();
    if (totalWeight == 0.0) return new HashMap<>();
    Map<T, Integer> firstPass = runFirstPass(weights, totalWeight, toAllocate);
    int sumInMap = firstPass.values().stream().mapToInt(Integer::intValue).sum();
    int shortfall = toAllocate - sumInMap;
    allocateShortFall(firstPass, shortfall);
    return firstPass;
  }

  private static <T> Map<T, Integer> runFirstPass(
      Map<T, ? extends Number> weights, double totalWeight, int toAllocate) {
    Comparator<Map.Entry<T, Double>> remainderSizeReversed = sortByRemainderSizeReversed();
    return weights.entrySet().stream()
        .map(entry -> weightEntry(totalWeight, toAllocate, entry))
        .sorted(remainderSizeReversed)
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().intValue()))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
  }

  private static <T> void allocateShortFall(Map<T, Integer> fraction, int shortfall) {
    List<T> toAdd = fraction.keySet().stream().limit(shortfall).toList();
    toAdd.forEach(t -> fraction.put(t, fraction.get(t) + 1));
  }

  private static <T> Comparator<Map.Entry<T, Double>> sortByRemainderSizeReversed() {
    return Comparator.comparingDouble((Map.Entry<T, Double> entry) -> entry.getValue() % 1)
        .reversed();
  }

  private static <T> Map.Entry<T, Double> weightEntry(
      double totalWeight, long toAllocate, Map.Entry<T, ? extends Number> entry) {
    return Map.entry(entry.getKey(), (entry.getValue().doubleValue() * toAllocate) / totalWeight);
  }
}
