package io.github.alecredmond.export.sampler;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.networkscenario.ObservationPackage;
import java.util.Set;
import java.util.function.Function;
import lombok.Data;

@Data
public class SamplePackage {
  private final SampleCollection test;
  private final BayesianNetwork network;
  private final InferenceEngine engine;
  private final int numberOfSamples;
  private final Set<Node> exportNodes;
  private final Set<NodeState> observedStates;
  private final Set<NodeState> measuredStates;
  private final boolean printMarginals;
  private final SampleCollection jointTest;
  private final InferenceEngine priorEngine;

  public SamplePackage(
      BayesianNetwork network,
      int numberOfSamples,
      ObservationPackage observationPackage,
      boolean printMarginals,
      Function<BayesianNetwork, InferenceEngine> constructor) {
    this.printMarginals = printMarginals;
    this.network = network;
    this.engine = constructor.apply(network).setObserved(observationPackage.observedStates());
    this.priorEngine = constructor.apply(network);
    this.numberOfSamples = numberOfSamples;
    this.observedStates = observationPackage.observedStates();
    this.exportNodes = observationPackage.exportNodes();
    this.measuredStates = observationPackage.measuredStates();
    this.test =
        MonteCarloSampler.create(network)
            .setObserved(engine.getCurrentObservations())
            .generateSamples(numberOfSamples);
    this.jointTest = MonteCarloSampler.create(network).generateSamples(numberOfSamples);
  }
}
