package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class PartiallyObservedSamplePicker extends AbstractSamplePicker {

  public PartiallyObservedSamplePicker(SamplePickerFactoryData factoryData) {
    super(factoryData);
  }

  @Override
  public double selectStateAndReturnWeight(NodeState[] sampleArray) {
    int cptIndex = super.getInitialCptIndex(sampleArray);
    int statePosition = 0;
    double totalWeight = 0.0;
    for (int delta : eventCptSteps) {
      double weight = cptProbabilities[cptIndex + delta];
      totalWeight += weight;
      sampleWeighting[statePosition++] = weight;
    }
    int pickedStatePosition = randomIndex(sampleWeighting, totalWeight);
    sampleArray[eventNodeIndexInSampleArray] = eventStates[pickedStatePosition];
    return sampleWeighting[pickedStatePosition] == 0.0 ? 0.0 : totalWeight;
  }
}
