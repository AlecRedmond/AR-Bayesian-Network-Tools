package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.inference.BayesianInference;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.io.Serializable;
import java.util.*;

public abstract class AbstractBayesianInference<T extends BayesianInference<T>>
    implements BayesianInference<T> {
  protected final BayesianNetwork network;
  protected final Map<Node, NodeObservation> observations;
  private final NodeObservationFactory factory;

  protected AbstractBayesianInference(BayesianNetwork network) {
    this.network = network;
    this.factory = new NodeObservationFactory();
    this.observations = factory.initializeFromNetwork(network);
  }

  @Override
  public final BayesianNetwork getNetwork() {
    return network;
  }

  @Override
  public final Map<Node, NodeObservation> getCurrentObservations() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(observations));
  }

  @Override
  public final T resetObservations() {
    observations.replaceAll((n, o) -> factory.buildUnobserved(n));
    return observationsUpdatedLogic();
  }

  protected abstract T observationsUpdatedLogic();

  @Override
  public final T setObserved(Map<Node, NodeObservation> newObservations) {
    boolean allEqual = true;
    for (Map.Entry<Node, NodeObservation> entry : observations.entrySet()) {
      Node node = entry.getKey();
      if (!newObservations.containsKey(node)) {
        throw new IllegalArgumentException(
            "Request did not contain a NodeObservation for " + node.getName());
      }
      NodeObservation next = newObservations.get(node);
      NodeObservation previous = entry.getValue();
      boolean isEqual = next.equals(previous);
      if (isEqual) continue;
      allEqual = false;
      validateAllStatesPresent(node, next);
      entry.setValue(next);
    }
    if (allEqual) return returnThis();
    return observationsUpdatedLogic();
  }

  private void validateAllStatesPresent(Node node, NodeObservation next) {
    Set<NodeState> states = new HashSet<>(node.getStates());
    Set<NodeState> statesInNext = new HashSet<>(next.observedStates());
    statesInNext.addAll(next.eliminatedStates());
    if (states.equals(statesInNext)) return;
    throw new IllegalArgumentException(
        "States in NodeObservation "
            + next
            + " do not match expected states "
            + CollectionToString.apply(node.getStates()));
  }

  protected abstract T returnThis();

  @Override
  public final T eliminateStates(Collection<NodeState> toEliminate) {
    if (toEliminate.isEmpty()) return returnThis();
    NodeUtils.generateMultiRequest(toEliminate)
        .forEach(
            (node, states) ->
                observations.computeIfPresent(node, (n, o) -> factory.eliminateStates(o, states)));
    return observationsUpdatedLogic();
  }

  @Override
  public final <S extends Serializable> T eliminateStatesById(Collection<S> toEliminateIDs) {
    Set<NodeState> states = network.getNodeStatesById(toEliminateIDs);
    return eliminateStates(states);
  }

  @Override
  public final T setObserved(Collection<NodeState> observedStates) {
    Map<Node, Set<NodeState>> request = NodeUtils.generateMultiRequest(observedStates);
    for (Map.Entry<Node, NodeObservation> entry : observations.entrySet()) {
      Node node = entry.getKey();
      Set<NodeState> toObserve = request.getOrDefault(node, Set.of());
      entry.setValue(factory.observeStates(node, toObserve));
    }
    return observationsUpdatedLogic();
  }

  @Override
  public final <S extends Serializable> T setObservedById(Collection<S> observedStateIDs) {
    Set<NodeState> states = network.getNodeStatesById(observedStateIDs);
    return setObserved(states);
  }
}
