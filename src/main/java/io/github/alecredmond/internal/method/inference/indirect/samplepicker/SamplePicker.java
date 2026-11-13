package io.github.alecredmond.internal.method.inference.indirect.samplepicker;

import java.util.random.RandomGenerator;

public interface SamplePicker {
  RandomGenerator RANDOM = RandomGenerator.of("Xoroshiro128PlusPlus");

  double selectStateAndReturnWeight(int[] statePositions);
}
