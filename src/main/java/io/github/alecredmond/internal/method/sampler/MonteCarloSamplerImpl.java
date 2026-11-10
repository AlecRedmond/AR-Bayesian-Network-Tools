package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.sampler.SampleCollection;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.observable.ObservableUtils;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class MonteCarloSamplerImpl implements MonteCarloSampler {
  protected final BayesianNetwork network;
  protected Map<Node, NodeObservation> observations;

  protected MonteCarloSamplerImpl(BayesianNetwork network) {
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
  public Map<Node, NodeObservation> getCurrentObservations() {
    return observations;
  }

  @Override
  public SampleCollection generateSamples(int numberOfSamples) {
    try {
      return generateSamplesInternal(observations, numberOfSamples);
    } catch (NodeStateConflictException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  protected abstract SampleCollection generateSamplesInternal(
      Map<Node, NodeObservation> observations, int numberOfSamples);

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
