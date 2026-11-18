package io.github.alecredmond.export.inference.indirect;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.inference.indirect.AbstractMonteCarloSampler;
import io.github.alecredmond.internal.method.inference.indirect.samplepicker.SamplePicker;
import io.github.alecredmond.internal.method.inference.indirect.samplepicker.SamplePickerFactory;
import io.github.alecredmond.internal.method.inference.indirect.samplestore.SampleStore;
import io.github.alecredmond.internal.method.utils.WeightedAllocator;
import java.util.*;

/**
 * Likelihood Weighting Sampler (LWS) for indirect inference. LWS performs a random walk down each
 * CPT in the {@link BayesianNetwork}, randomly picking each new {@link NodeState} according to its
 * conditional probability {@code P(X=x|Pa(X)=sampled))}. When the algorithm reaches a {@link Node}
 * constrained to an observed subset of its {@link NodeState}s, the weight of the sample is
 * multiplied by the sum of all conditional probabilities in that subset. At the end of the run, the
 * weighted samples are normalized and proportionally correct frequencies are assigned.
 *
 * @see MonteCarloSampler
 * @author Alec Redmond
 */
public final class LikelihoodWeightingSampler extends AbstractMonteCarloSampler {
  private final SampleStore sampleStore;

  /**
   * Constructs a new {@code LikelihoodWeightingSampler} for sampling.
   *
   * @param bayesianNetwork the {@link BayesianNetwork} to sample.
   */
  public LikelihoodWeightingSampler(BayesianNetwork bayesianNetwork) {
    super(bayesianNetwork);
    this.sampleStore = SampleStore.create(bayesianNetwork);
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
