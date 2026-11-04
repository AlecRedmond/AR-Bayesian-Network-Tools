package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class ObservedSamplePicker extends AbstractSamplePicker {
  private final int observedStatePosition;

  public ObservedSamplePicker(SamplePickerFactoryData factoryData, NodeState observedState) {
    super(factoryData);
    this.observedStatePosition = observedState.getPosition();
  }

  @Override
  public double selectStateAndReturnWeight(int[] statePositions) {
    statePositions[eventNodeIndexInSampleArray] = observedStatePosition;
    int cptIndex = super.getInitialCptIndex(statePositions) + observedStatePosition;
    return cptProbabilities[cptIndex];
  }
}
