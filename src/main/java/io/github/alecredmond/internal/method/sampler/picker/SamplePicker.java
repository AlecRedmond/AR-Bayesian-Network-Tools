package io.github.alecredmond.internal.method.sampler.picker;

import java.util.random.RandomGenerator;

public interface SamplePicker {
  RandomGenerator RANDOM = RandomGenerator.of("Xoroshiro128PlusPlus");

  double selectStateAndReturnWeight(int[] statePositions);
}
