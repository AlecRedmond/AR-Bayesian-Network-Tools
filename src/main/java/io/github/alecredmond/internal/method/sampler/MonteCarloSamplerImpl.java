package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class MonteCarloSamplerImpl implements MonteCarloSampler {
  protected final BayesianNetwork network;

  protected MonteCarloSamplerImpl(BayesianNetwork network) {
    this.network = network;
  }

  @Override
  public SampleCollectionImpl generateSamples(
      Map<Node, NodeObservation> currentObservations, int numberOfSamples) {
    return generateSamplesInternal(new HashMap<>(), numberOfSamples);
  }

  public SampleCollectionImpl generateSamples(InferenceEngine engine, int numberOfSamples) {
    return generateSamplesInternal(engine.getCurrentObservations(), numberOfSamples);
  }

  protected abstract SampleCollectionImpl generateSamplesInternal(
      Map<Node, NodeObservation> observations, int numberOfSamples);

  @Override
  public SampleCollectionImpl generateSamples(
      Collection<NodeState> observedStates, int numberOfSamples) {
    Map<Node, NodeObservation> observationMap = NodeObservation.createMap(network);
    observationMap = NodeObservation.observe(observationMap, observedStates);
    try {
      return generateSamplesInternal(observationMap, numberOfSamples);
    } catch (NodeStateConflictException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  @Override
  public <T extends Serializable> SampleCollectionImpl generateSamplesById(
      Collection<T> observedStateIds, int numberOfSamples) {
    return generateSamples(network.getNodeStates(observedStateIds), numberOfSamples);
  }
}
