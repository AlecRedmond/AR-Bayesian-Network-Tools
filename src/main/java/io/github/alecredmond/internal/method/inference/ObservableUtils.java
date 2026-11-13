package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObservableUtils {
  private ObservableUtils() {}

  public static List<NodeState> convertToEliminations(
      BayesianNetwork network, Map<Node, NodeObservation> newObservations) {
    Set<Node> networkNodes = network.getNodes(LinkedHashSet::new);
    return newObservations.entrySet().stream()
        .filter(e -> networkNodes.contains(e.getKey()))
        .map(Map.Entry::getValue)
        .map(NodeObservation::eliminatedStates)
        .flatMap(Collection::stream)
        .toList();
  }

  public static Map<Node, NodeObservation> buildFromObservedStates(
      Map<Node, NodeObservation> existingObservations, Map<Node, Set<NodeState>> observedStateMap) {
    NodeObservationFactory factory = new NodeObservationFactory();
    Map<Node, NodeObservation> newObservations = new LinkedHashMap<>(existingObservations);
    observedStateMap.forEach((node, obs) -> newObservations.put(node, factory.build(node, obs)));
    return Collections.unmodifiableMap(newObservations);
  }

  public static Map<Node, NodeObservation> buildUnobservedNetwork(BayesianNetwork bayesianNetwork) {
    NodeObservationFactory factory = new NodeObservationFactory();
    Map<Node, NodeObservation> map = new LinkedHashMap<>();
    bayesianNetwork
        .getNodes(ArrayList::new)
        .forEach(node -> map.put(node, factory.build(node, Set.of())));
    return Collections.unmodifiableMap(map);
  }

  public static Map<Node, NodeObservation> buildFromEliminatedStates(
      Map<Node, NodeObservation> existingObservations,
      Map<Node, Set<NodeState>> eliminatedStateMap) {
    if (eliminatedStateMap.isEmpty()) return existingObservations;
    Map<Node, NodeObservation> newObservations = new LinkedHashMap<>(existingObservations);
    NodeObservationFactory factory = new NodeObservationFactory();
    eliminatedStateMap.forEach(
        (node, eliminated) -> {
          Set<NodeState> newObserved = getNewObserved(existingObservations, node, eliminated);
          newObservations.put(node, factory.build(node, newObserved));
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
