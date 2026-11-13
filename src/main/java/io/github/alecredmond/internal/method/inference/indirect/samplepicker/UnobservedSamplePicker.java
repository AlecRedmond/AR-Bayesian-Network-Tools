package io.github.alecredmond.internal.method.inference.indirect.samplepicker;

import io.github.alecredmond.internal.application.inference.indirect.SamplePickerFactoryData;

public class UnobservedSamplePicker extends AbstractSamplePicker {

  public UnobservedSamplePicker(SamplePickerFactoryData factoryData) {
    super(factoryData);
  }

  @Override
  public double selectStateAndReturnWeight(int[] sampleStatePositions) {
    int cptIndex = super.getInitialCptIndex(sampleStatePositions);
    for (int i = 0; i < sampleWeighting.length; i++) {
      sampleWeighting[i] = cptProbabilities[cptIndex++];
    }
    int eventStateIndex = randomIndex(sampleWeighting, 1.0);
    sampleStatePositions[eventNodeIndexInSampleArray] = eventStateIndex;
    return sampleWeighting[eventStateIndex] == 0.0 ? 0.0 : 1.0;
  }
}
