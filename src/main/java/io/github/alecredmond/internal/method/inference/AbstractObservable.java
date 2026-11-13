package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.Observable;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AbstractObservable<T extends Observable<T>> implements Observable<T> {
  protected final BayesianNetwork network;
  protected Map<Node, NodeObservation> observations;

  protected AbstractObservable(BayesianNetwork network) {
    this.network = network;
  }

  @Override
  public final BayesianNetwork getNetwork() {
    return network;
  }

  @Override
  public final Map<Node, NodeObservation> getCurrentObservations() {
    return Collections.unmodifiableMap(observations);
  }

  @Override
  public final T resetObservations() {
    observations = ObservableUtils.buildUnobservedNetwork(network);
    return observationsUpdatedLogic();
  }

  protected abstract T observationsUpdatedLogic();

  @Override
  public final T setObserved(Map<Node, NodeObservation> newObservations) {
    boolean allEqual = true;
    for (Node node : network.getNodes()) {
      if (!newObservations.containsKey(node)) {
        throw new IllegalArgumentException(
            "Request did not contain a NodeObservation for " + node.getName());
      }
      if (allEqual) {
        NodeObservation next = newObservations.get(node);
        NodeObservation previous = observations.get(node);
        allEqual = next.equals(previous);
      }
    }
    if (allEqual) return returnThis();
    observations = newObservations;
    return observationsUpdatedLogic();
  }

  protected abstract T returnThis();

  @Override
  public final T eliminateStates(Collection<NodeState> toEliminate) {
    if (toEliminate.isEmpty()) return returnThis();
    observations =
        ObservableUtils.buildFromEliminatedStates(
            observations, NodeUtils.generateMultiRequest(toEliminate));
    return observationsUpdatedLogic();
  }

  @Override
  public final <S extends Serializable> T eliminateStatesById(Collection<S> toEliminateIDs) {
    Set<NodeState> states = network.getNodeStatesById(toEliminateIDs);
    return eliminateStates(states);
  }

  @Override
  public final T setObserved(Collection<NodeState> observedStates) {
    observations =
        ObservableUtils.buildFromObservedStates(
            ObservableUtils.buildUnobservedNetwork(network),
            NodeUtils.generateMultiRequest(observedStates));
    return observationsUpdatedLogic();
  }

  @Override
  public final <S extends Serializable> T setObservedById(Collection<S> observedStateIDs) {
    Set<NodeState> states = network.getNodeStatesById(observedStateIDs);
    return setObserved(states);
  }
}
