package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public class ObservedSamplePicker extends AbstractSamplePicker {
  private final NodeState observedState;
  private final int cptIndexDelta;

  public ObservedSamplePicker(SamplePickerFactoryData factoryData, NodeState observedState) {
    super(factoryData);
    this.observedState = observedState;
    this.cptIndexDelta = observedState.getPosition();
  }

  @Override
  protected double pickNextState(NodeState[] sampleArray, double currentWeight) {
    sampleArray[eventNodeIndexInSampleArray] = observedState;
    int cptIndex = super.getInitialCptIndex(sampleArray) + cptIndexDelta;
    return currentWeight * cptProbabilities[cptIndex];
  }
}
