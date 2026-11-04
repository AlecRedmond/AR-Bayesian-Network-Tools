package io.github.alecredmond.internal.method.sampler.picker;

import java.util.Random;

public interface SamplePicker {
  Random RANDOM = new Random();

  double selectStateAndReturnWeight(int[] statePositions);
}
