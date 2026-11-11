package io.github.alecredmond.internal.method.sampler.samplestore;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.NodeState;
import java.util.Map;

public interface SampleStore {
  void clear();

  void storeSample(int[] statePositions, double weight);

  Map<NodeState[], Double> getWeightedSamples();

  static SampleStore create(BayesianNetwork bayesianNetwork) {
    return new SampleStoreFactory().buildSampleStore(bayesianNetwork);
  }
}
