package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

public abstract class AbstractSamplePicker implements SamplePicker {
  protected final int[] conditionIndexesInSampleArray;
  protected final int eventNodeIndexInSampleArray;
  protected final int[] eventCptSteps;
  protected final NodeState[] eventStates;
  protected final int[] strideLengths;
  protected final double[] cptProbabilities;
  protected final double[] sampleWeighting;

  protected AbstractSamplePicker(SamplePickerFactoryData factoryData) {
    this.conditionIndexesInSampleArray = factoryData.getConditionIndexesInSampleArray();
    this.eventCptSteps = factoryData.getEventCptSteps();
    this.eventStates = factoryData.getEventStates();
    this.eventNodeIndexInSampleArray = factoryData.getEventNodeIndexInSampleArray();
    this.strideLengths = factoryData.getStrideLengths();
    this.cptProbabilities = factoryData.getCptProbabilities();
    this.sampleWeighting = factoryData.getSampleWeighting();
  }

  protected int randomIndex(double[] weights, double totalWeight) {
    double randomValue = RANDOM.nextDouble() * totalWeight;
    int i;
    for (i = 0; i < weights.length; i++) {
      randomValue -= weights[i];
      if (randomValue <= 0.0) break;
    }
    return i;
  }

  protected int getInitialCptIndex(NodeState[] sampleArray) {
    int cptPosition = 0;
    for (int i = 0; i < conditionIndexesInSampleArray.length; i++) {
      int conditionIndex = conditionIndexesInSampleArray[i];
      int statePosition = sampleArray[conditionIndex].getPosition();
      cptPosition += strideLengths[i] * statePosition;
    }
    return cptPosition;
  }
}
