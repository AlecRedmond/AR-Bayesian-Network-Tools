package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
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
    this.observations = NodeObservation.createUnobservedMap(network);
    return this;
  }

  @Override
  public MonteCarloSampler setObserved(Map<Node, NodeObservation> observations) {
    return NetworkDataUtils.setObservations(this, network, observations);
  }

  @Override
  public Map<Node, NodeObservation> getCurrentObservations() {
    return observations;
  }

  @Override
  public SampleCollectionImpl generateSamples(int numberOfSamples) {
    try {
      return generateSamplesInternal(observations, numberOfSamples);
    } catch (NodeStateConflictException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  protected abstract SampleCollectionImpl generateSamplesInternal(
      Map<Node, NodeObservation> observations, int numberOfSamples);

  @Override
  public MonteCarloSampler eliminateStates(Collection<NodeState> toEliminate) {
    this.observations = NodeObservation.eliminate(observations, toEliminate);
    return this;
  }

  @Override
  public MonteCarloSampler eliminateStates(NodeState toEliminate) {
    return Optional.ofNullable(toEliminate).map(List::of).map(this::eliminateStates).orElse(this);
  }

  @Override
  public <T extends Serializable> MonteCarloSampler eliminateStatesById(
      Collection<T> toEliminateIDs) {
    return eliminateStates(network.getNodeStates(toEliminateIDs));
  }

  @Override
  public <T extends Serializable> MonteCarloSampler eliminateStatesById(T toEliminateId) {
    return Optional.ofNullable(toEliminateId)
        .map(List::of)
        .map(this::eliminateStatesById)
        .orElse(this);
  }

  @Override
  public MonteCarloSampler setObserved(Collection<NodeState> observedStates) {
    resetObservations();
    if (!observedStates.isEmpty()) {
      this.observations = NodeObservation.observe(this.observations, observedStates);
    }
    return this;
  }

  @Override
  public MonteCarloSampler setObserved(NodeState observedState) {
    return Optional.ofNullable(observedState).map(List::of).map(this::setObserved).orElse(this);
  }

  @Override
  public <T extends Serializable> MonteCarloSampler setObservedById(T observedStateId) {
    return Optional.ofNullable(observedStateId)
        .map(List::of)
        .map(this::setObservedById)
        .orElse(this);
  }

  @Override
  public <T extends Serializable> MonteCarloSampler setObservedById(
      Collection<T> observedStateIDs) {
    return this.setObserved(network.getNodeStates(observedStateIDs));
  }
}
