package io.github.alecredmond.internal.method.inference.indirect.samplepicker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.inference.indirect.SamplePickerFactoryData;

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
