package io.github.alecredmond.internal.method.inference.indirect;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.inference.indirect.Sample;
import io.github.alecredmond.export.inference.indirect.SampleCollection;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.inference.AbstractObservable;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMonteCarloSampler extends AbstractObservable<MonteCarloSampler>
    implements MonteCarloSampler {

  protected AbstractMonteCarloSampler(BayesianNetwork network) {
    super(network);
    resetObservations();
  }

  @Override
  public SampleCollection generateSamples(int numberOfSamples) {
    if (numberOfSamples < 0) {
      log.error("Attempted to generate less than zero samples!");
      return null;
    }
    if (anyNegatedObservations(observations)) {
      return null;
    }
    Map<NodeState[], Integer> sampledStates = generateSampledStates(numberOfSamples);
    List<Sample> samples = convertArraysToSamples(sampledStates);
    return new SampleCollectionBuilder().build(numberOfSamples, samples, observations, network);
  }

  private boolean anyNegatedObservations(Map<Node, NodeObservation> observations) {
    List<Node> negated =
        observations.values().stream()
            .filter(obs -> obs.status().equals(ObservationStatus.NEGATED))
            .map(NodeObservation::node)
            .toList();
    if (negated.isEmpty()) return false;
    log.error(
        "Cannot run sampler, Nodes were negated: {}",
        CollectionToString.apply(negated, Node::getName));
    return true;
  }

  protected abstract Map<NodeState[], Integer> generateSampledStates(int numberOfSamples);

  private List<Sample> convertArraysToSamples(Map<NodeState[], Integer> sampledStates) {
    List<Sample> samples = new ArrayList<>();
    sampledStates.forEach((states, count) -> samples.add(new SampleImpl(states, count)));
    return samples;
  }

  @Override
  protected final MonteCarloSampler observationsUpdatedLogic() {
    return this;
  }

  @Override
  protected final MonteCarloSampler returnThis() {
    return this;
  }
}
