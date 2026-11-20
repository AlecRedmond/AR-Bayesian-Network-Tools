package io.github.alecredmond.internal.method.inference.indirect.samplestore;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.NodeState;
import java.util.Map;

public interface SampleStore {
  void clear();

  void storeSample(int[] statePositions, double weight);

  Map<NodeState[], Double> returnWeightsAndClear();

  static SampleStore create(BayesianNetwork bayesianNetwork) {
    return new SampleStoreFactory().buildSampleStore(bayesianNetwork);
  }
}
