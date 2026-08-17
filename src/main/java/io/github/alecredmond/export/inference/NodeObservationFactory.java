package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.Collectors;

/** Package-private helper class for the static factory methods in {@link NodeObservation}. */
class NodeObservationFactory {
  Map<Node, NodeObservation> buildFromObservedStates(
      Map<Node, NodeObservation> existingObservations, Map<Node, Set<NodeState>> observedStateMap) {
    Map<Node, NodeObservation> newObservations = new LinkedHashMap<>(existingObservations);
    observedStateMap.forEach(
        (node, observed) -> newObservations.put(node, buildNodeObservation(node, observed)));
    return Collections.unmodifiableMap(newObservations);
  }

  private NodeObservation buildNodeObservation(Node node, Set<NodeState> observed) {
    Set<NodeState> nodeStates = new LinkedHashSet<>(node.getNodeStates());
    if (observed.isEmpty()) {
      return new NodeObservation(
          node, Collections.unmodifiableSet(nodeStates), Set.of(), ObservationStatus.UNOBSERVED);
    }
    Set<NodeState> eliminated = new LinkedHashSet<>(nodeStates);
    eliminated.removeAll(observed);
    nodeStates.removeAll(eliminated);
    return new NodeObservation(
        node,
        Collections.unmodifiableSet(nodeStates),
        Collections.unmodifiableSet(eliminated),
        getObservationStatus(nodeStates, node));
  }

  private ObservationStatus getObservationStatus(Set<NodeState> observed, Node node) {
    int totalStates = node.getNodeStates().size();
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
            .formatted(observed.size(), node, NodeUtils.formatStatesToString(observed)));
  }

  Map<Node, NodeObservation> buildUnobservedNetwork(List<Node> orderedNodes) {
    Map<Node, NodeObservation> map = new LinkedHashMap<>();
    orderedNodes.forEach(node -> map.put(node, buildNodeObservation(node, Set.of())));
    return Collections.unmodifiableMap(map);
  }

  Map<Node, NodeObservation> buildFromEliminatedStates(
      Map<Node, NodeObservation> existingObservations,
      Map<Node, Set<NodeState>> eliminatedStateMap) {
    if (eliminatedStateMap.isEmpty()) return existingObservations;
    Map<Node, NodeObservation> newObservations = new LinkedHashMap<>(existingObservations);
    eliminatedStateMap.forEach(
        (node, eliminated) -> {
          Set<NodeState> newObserved = getNewObserved(existingObservations, node, eliminated);
          newObservations.put(node, buildNodeObservation(node, newObserved));
        });
    return Collections.unmodifiableMap(newObservations);
  }

  private static Set<NodeState> getNewObserved(
      Map<Node, NodeObservation> existingObservations, Node node, Set<NodeState> eliminated) {
    return existingObservations.get(node).observedStates().stream()
        .filter(ns -> !eliminated.contains(ns))
        .collect(Collectors.toSet());
  }
}
