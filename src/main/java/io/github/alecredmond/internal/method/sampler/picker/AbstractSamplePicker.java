package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public abstract class AbstractSamplePicker implements SamplePicker {
  protected final int[] conditionIndexesInSampleArray;
  protected final int eventNodeIndexInSampleArray;
  protected final int[] observedStatePositions;
  protected final int[] strideLengths;
  protected final double[] cptProbabilities;
  protected final double[] sampleWeighting;

  protected AbstractSamplePicker(SamplePickerFactoryData factoryData) {
    this.conditionIndexesInSampleArray = factoryData.getConditionIndexesInSampleArray();
    this.observedStatePositions = factoryData.getEventStatePositions();
    this.eventNodeIndexInSampleArray = factoryData.getEventNodeIndexInSampleArray();
    this.strideLengths = factoryData.getStrideLengths();
    this.cptProbabilities = factoryData.getCptProbabilities();
    this.sampleWeighting = factoryData.getSampleWeighting();
  }

  protected final int randomIndex(double[] weights, double totalWeight) {
    double randomValue = RANDOM.nextDouble() * totalWeight;
    final int bound = weights.length;
    int i;
    for (i = 0; i < bound; i++) {
      randomValue -= weights[i];
      if (randomValue <= 0.0) break;
    }
    return i;
  }

  protected final int getInitialCptIndex(int[] sampleStatePositions) {
    int cptPosition = 0;
    for (int i = 0; i < conditionIndexesInSampleArray.length; i++) {
      int conditionIndex = conditionIndexesInSampleArray[i];
      int statePosition = sampleStatePositions[conditionIndex];
      cptPosition += strideLengths[i] * statePosition;
    }
    return cptPosition;
  }
}
