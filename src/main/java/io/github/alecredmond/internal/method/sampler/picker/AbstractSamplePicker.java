package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;
import java.util.Arrays;

public abstract class AbstractSamplePicker implements SamplePicker {
  protected final Node node;
  protected final int[] conditionIndexesInSampleArray;
  protected final int eventNodeIndexInSampleArray;
  protected final int[] eventCptSteps;
  protected final NodeState[] eventStates;
  protected final int[] strideLengths;
  protected final double[] cptProbabilities;
  protected final double[] sampleWeighting;

  protected AbstractSamplePicker(SamplePickerFactoryData factoryData) {
    this.node = factoryData.getNode();
    this.conditionIndexesInSampleArray = factoryData.getConditionIndexesInSampleArray();
    this.eventCptSteps = factoryData.getEventCptSteps();
    this.eventStates = factoryData.getEventStates();
    this.eventNodeIndexInSampleArray = factoryData.getEventNodeIndexInSampleArray();
    this.strideLengths = factoryData.getStrideLengths();
    this.cptProbabilities = factoryData.getCptProbabilities();
    this.sampleWeighting = factoryData.getSampleWeighting();
  }

  @Override
  public double pick(NodeState[] sampleArray, double currentWeight) {
    if (currentWeight == 0.0) return 0.0;
    return pickNextState(sampleArray, currentWeight);
  }

  protected abstract double pickNextState(NodeState[] sampleArray, double currentWeight);

  protected static int randomIndex(double[] weights) {
    double totalWeight = Arrays.stream(weights).sum();
    double randomValue = RANDOM.nextDouble() * totalWeight;
    for (int i = 0; i < weights.length; i++) {
      randomValue -= weights[i];
      if (randomValue <= 0.0) return i;
    }
    return -1;
  }

  protected int getInitialCptIndex(NodeState[] sampleArray) {
    int cptPosition = 0;
    for (int i = 0; i < conditionIndexesInSampleArray.length; i++) {
      int conditionIndex = conditionIndexesInSampleArray[i];
      int statePosition = sampleArray[conditionIndex].getPosition();
      cptPosition += i * statePosition;
    }
    return cptPosition;
  }
}
