package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class PartiallyObservedSamplePicker extends AbstractSamplePicker {

  public PartiallyObservedSamplePicker(SamplePickerFactoryData factoryData) {
    super(factoryData);
  }

  @Override
  protected double pickNextState(NodeState[] sampleArray, double currentWeight) {
    int cptIndex = super.getInitialCptIndex(sampleArray);
    int i = 0;
    for (int delta : eventCptSteps) {
      sampleWeighting[i++] = cptProbabilities[cptIndex + delta];
    }
    int eventStateIndex = randomIndex(sampleWeighting);
    sampleArray[eventNodeIndexInSampleArray] = eventStates[eventStateIndex];
    return sampleWeighting[eventStateIndex] == 0.0 ? 0.0 : currentWeight;
  }
}
