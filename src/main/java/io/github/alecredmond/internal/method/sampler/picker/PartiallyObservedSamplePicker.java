package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class PartiallyObservedSamplePicker extends AbstractSamplePicker {

  public PartiallyObservedSamplePicker(SamplePickerFactoryData factoryData) {
    super(factoryData);
  }

  @Override
  public double selectStateAndReturnWeight(int[] sampleStatePositions) {
    int conditionIndex = super.getInitialCptIndex(sampleStatePositions);
    int nthObserved = 0;
    double totalWeight = 0.0;
    // Traverse all N observed states in the node, by their position P
    for (int position : observedStatePositions) {
      double weight = cptProbabilities[conditionIndex + position];
      totalWeight += weight;
      sampleWeighting[nthObserved++] = weight;
    }
    // Randomly select the Nth Observed state.
    nthObserved = randomIndex(sampleWeighting, totalWeight);
    // Convert Nth observed state to Pth actual state
    sampleStatePositions[eventNodeIndexInSampleArray] = observedStatePositions[nthObserved];
    return sampleWeighting[nthObserved] == 0.0 ? 0.0 : totalWeight;
  }
}
