package io.github.alecredmond.export.sampler;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.sampler.AbstractMonteCarloSampler;
import io.github.alecredmond.internal.method.sampler.picker.SamplePicker;
import io.github.alecredmond.internal.method.sampler.picker.SamplePickerFactory;
import io.github.alecredmond.internal.method.sampler.samplestore.SampleStore;
import io.github.alecredmond.internal.method.utils.WeightedAllocator;
import java.util.*;

public final class LikelihoodWeightingSampler extends AbstractMonteCarloSampler {
  private final SampleStore sampleStore;

  public LikelihoodWeightingSampler(BayesianNetwork network) {
    super(network);
    this.sampleStore = SampleStore.create(network);
  }

  @Override
  protected Map<NodeState[], Integer> generateSampledStates(int numberOfSamples) {
    sampleStore.clear();
    SamplePicker[] samplePickers = new SamplePickerFactory().createArray(network, observations);
    int[] selectedStatePositions = new int[samplePickers.length];
    for (int s = 0; s < numberOfSamples; s++) {
      performWeightedRandomWalk(samplePickers, selectedStatePositions);
    }
    return distributeStates(numberOfSamples);
  }

  private void performWeightedRandomWalk(
      SamplePicker[] samplePickers, int[] selectedStatePositions) {
    double weight = 1.0;
    for (SamplePicker samplePicker : samplePickers) {
      weight *= samplePicker.selectStateAndReturnWeight(selectedStatePositions);
      if (weight <= 0) return;
    }
    sampleStore.storeSample(selectedStatePositions, weight);
  }

  private Map<NodeState[], Integer> distributeStates(int numberOfSamples) {
    return WeightedAllocator.allocate(sampleStore.returnWeightsAndClear(), numberOfSamples);
  }
}
