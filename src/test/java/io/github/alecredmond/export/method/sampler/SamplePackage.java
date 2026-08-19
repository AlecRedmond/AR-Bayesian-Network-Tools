package io.github.alecredmond.export.method.sampler;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.sampler.SampleCollection;
import java.util.Set;
import lombok.Data;

@Data
public class SamplePackage {
  private final SampleCollection test;
  private final BayesianNetwork network;
  private final InferenceEngine engine;
  private final int numberOfSamples;
  private final Set<String> observedStateIds;
  private final Set<NodeState> observedStates;
  private final Set<String> exportNodeIds;
  private final Set<Node> exportNodes;
  private final Set<String> measuredStateIds;
  private final Set<NodeState> measuredStates;
  private final boolean printMarginals;

  public SamplePackage(
      BayesianNetwork network,
      int numberOfSamples,
      Set<String> observedStateIds,
      Set<String> exportNodeIds,
      Set<String> measuredStateIds,
      boolean printMarginals) {
    this.printMarginals = printMarginals;
    this.network = network.solveNetwork();
    this.engine = InferenceEngine.create(network).setObservedById(observedStateIds);
    this.numberOfSamples = numberOfSamples;
    this.observedStateIds = observedStateIds;
    this.observedStates = network.getNodeStates(observedStateIds);
    this.exportNodeIds = exportNodeIds;
    this.exportNodes = network.getNodes(exportNodeIds);
    this.measuredStateIds = measuredStateIds;
    this.measuredStates = network.getNodeStates(measuredStateIds);
    this.test =
        MonteCarloSampler.create(network)
            .setObserved(engine.getCurrentObservations())
            .generateSamples(numberOfSamples);
  }
}
