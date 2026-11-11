package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.observable.ObservationStatus;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.sampler.Sample;
import io.github.alecredmond.export.sampler.SampleCollection;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.observable.ObservableUtils;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class AbstractMonteCarloSampler implements MonteCarloSampler {
  protected final BayesianNetwork network;
  protected Map<Node, NodeObservation> observations;

  protected AbstractMonteCarloSampler(BayesianNetwork network) {
    this.network = network;
    resetObservations();
  }

  @Override
  public MonteCarloSampler resetObservations() {
    this.observations = ObservableUtils.buildUnobservedNetwork(network);
    return this;
  }

  @Override
  public MonteCarloSampler setObserved(Map<Node, NodeObservation> observations) {
    resetObservations();
    return eliminateStates(ObservableUtils.convertToEliminations(network, observations));
  }

  @Override
  public MonteCarloSampler eliminateStates(Collection<NodeState> toEliminate) {
    Map<Node, Set<NodeState>> toEliminateRequest = NodeUtils.generateMultiRequest(toEliminate);
    this.observations = ObservableUtils.buildFromEliminatedStates(observations, toEliminateRequest);
    return this;
  }

  @Override
  public <T extends Serializable> MonteCarloSampler eliminateStatesById(
      Collection<T> toEliminateIDs) {
    return eliminateStates(network.getNodeStatesById(toEliminateIDs));
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
  public Map<Node, NodeObservation> getCurrentObservations() {
    return observations;
  }

  @Override
  public MonteCarloSampler setObserved(Collection<NodeState> observedStates) {
    resetObservations();
    if (!observedStates.isEmpty()) {
      this.observations =
          ObservableUtils.buildFromObservedStates(
              this.observations, NodeUtils.generateMultiRequest(observedStates));
    }
    return this;
  }

  @Override
  public <T extends Serializable> MonteCarloSampler setObservedById(
      Collection<T> observedStateIDs) {
    return this.setObserved(network.getNodeStatesById(observedStateIDs));
  }
}
