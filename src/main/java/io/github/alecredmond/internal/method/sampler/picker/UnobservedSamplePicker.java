package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class UnobservedSamplePicker extends AbstractSamplePicker {

  public UnobservedSamplePicker(SamplePickerFactoryData factoryData) {
    super(factoryData);
  }

  @Override
  public double selectStateAndReturnWeight(NodeState[] sampleArray) {
    int cptIndex = super.getInitialCptIndex(sampleArray);
    for (int i = 0; i < sampleWeighting.length; i++) {
      sampleWeighting[i] = cptProbabilities[cptIndex++];
    }
    int eventStateIndex = randomIndex(sampleWeighting, 1.0);
    sampleArray[eventNodeIndexInSampleArray] = eventStates[eventStateIndex];
    return sampleWeighting[eventStateIndex] == 0.0 ? 0.0 : 1.0;
  }
}
