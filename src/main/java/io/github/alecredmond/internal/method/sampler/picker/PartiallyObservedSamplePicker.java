package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class PartiallyObservedSamplePicker extends AbstractSamplePicker {

  public PartiallyObservedSamplePicker(SamplePickerFactoryData factoryData) {
    super(factoryData);
  }

  @Override
  public double selectStateAndReturnWeight(int[] sampleStatePositions) {
    int cptIndex = super.getInitialCptIndex(sampleStatePositions);
    int index = 0;
    double totalWeight = 0.0;
    for (int statePosition : eventStatePositions) {
      double weight = cptProbabilities[cptIndex + statePosition];
      totalWeight += weight;
      sampleWeighting[index++] = weight;
    }
    int pickedIndex = randomIndex(sampleWeighting, totalWeight);
    sampleStatePositions[eventNodeIndexInSampleArray] = eventStatePositions[pickedIndex];
    return sampleWeighting[pickedIndex] == 0.0 ? 0.0 : totalWeight;
  }
}
