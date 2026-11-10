package io.github.alecredmond.internal.method.observable;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.observable.ObservationStatus;
import io.github.alecredmond.internal.application.observable.NodeObservationImpl;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;

public class NodeObservationFactory {

  public NodeObservation build(Node node, Set<NodeState> observed) {
    Set<NodeState> nodeStates = new LinkedHashSet<>(node.getStates());
    if (observed.isEmpty()) {
      return new NodeObservationImpl(
          node, Collections.unmodifiableSet(nodeStates), Set.of(), ObservationStatus.UNOBSERVED);
    }
    Set<NodeState> eliminated = new LinkedHashSet<>(nodeStates);
    eliminated.removeAll(observed);
    nodeStates.removeAll(eliminated);
    return new NodeObservationImpl(
        node,
        Collections.unmodifiableSet(nodeStates),
        Collections.unmodifiableSet(eliminated),
        getObservationStatus(nodeStates, node));
  }

  private ObservationStatus getObservationStatus(Set<NodeState> observed, Node node) {
    int totalStates = node.getStates().size();
    int observedSize = observed.size();
    if (observedSize == totalStates) return ObservationStatus.UNOBSERVED;
    if (observedSize > totalStates) throwTooManyStatesError(observed, node);
    return switch (observedSize) {
      case 0 -> ObservationStatus.NEGATED;
      case 1 -> ObservationStatus.OBSERVED;
      default -> ObservationStatus.PARTIALLY_OBSERVED;
    };
  }

  private void throwTooManyStatesError(Set<NodeState> observed, Node node) {
    throw new IllegalStateException(
        "Attempted to observe %d states, which is more than exists in Node %s. States Observed = %s."
            .formatted(observed.size(), node, CollectionToString.apply(observed)));
  }
}
