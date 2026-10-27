package io.github.alecredmond.internal.method.node;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeUtils {

  private NodeUtils() {}

  public static Map<Node, NodeState> generateRequest(Collection<NodeState> states) {
    try {
      return states.stream()
          .map((state -> Map.entry(state.getNode(), state)))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IllegalStateException e) {
      throw new NodeStateConflictException(
          "Error generating request : Multiple values of NodeState shared the same Node", e);
    }
  }

  public static Map<Node, Set<NodeState>> generateMultiRequest(Collection<NodeState> states) {
    return states.stream()
        .collect(Collectors.groupingBy(NodeState::getNode, HashMap::new, Collectors.toSet()));
  }

  public static Set<NodeState> combineStates(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates) {
    return Stream.concat(eventStates.stream(), conditionStates.stream())
        .collect(Collectors.toSet());
  }

  public static List<Set<NodeState>> splitStatesSharingNodes(Collection<NodeState> states) {
    return states.stream()
        .collect(Collectors.groupingBy(NodeState::getNode, LinkedHashMap::new, Collectors.toList()))
        .values()
        .stream()
        .reduce(List.of(new HashSet<>()), NodeUtils::accumulatePerturbations, (x, y) -> y);
  }

  private static List<Set<NodeState>> accumulatePerturbations(
      List<Set<NodeState>> previousAccumulation, List<NodeState> statesWithSharedNode) {
    return previousAccumulation.stream()
        .flatMap(
            currentSet ->
                statesWithSharedNode.stream()
                    .map(
                        state -> {
                          Set<NodeState> newPerturbation = new LinkedHashSet<>(currentSet);
                          newPerturbation.add(state);
                          return newPerturbation;
                        }))
        .toList();
  }

  public static void buildStateNameAndIdMap(
      List<NodeState> states,
      Map<String, NodeState> stateNameMap,
      Map<Serializable, NodeState> stateIdMap) {
    Set<String> visited = new HashSet<>();
    for (NodeState state : states) {
      String name = state.getName();
      if (!visited.add(name)) {
        throw new NodeStateConflictException("Duplicated state name %s".formatted(name));
      }
      stateNameMap.put(name, state);
      stateIdMap.put(state.getId(), state);
    }
  }

  public static Set<Node> getNodes(Collection<NodeState> states) {
    return states.stream().map(NodeState::getNode).collect(Collectors.toSet());
  }

  public static <N extends CartesianVariable> Map<N, Integer> buildNodeIndexMap(N[] nodes) {
    return IntStream.range(0, nodes.length)
        .mapToObj(i -> Map.entry(nodes[i], i))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static List<Serializable> getNodeIds(Collection<Node> nodes) {
    return nodes.stream().map(Node::getId).toList();
  }

  public static List<NodeState> getAllStates(Collection<Node> nodes) {
    return nodes.stream().map(Node::getStates).flatMap(Collection::stream).toList();
  }

  public static List<Serializable> getNodeStateIds(Collection<NodeState> nodeStates) {
    return nodeStates.stream().map(NodeState::getId).toList();
  }

  public static Set<Node> getOverlap(Set<Node> nodesA, Set<Node> nodesB) {
    return nodesA.stream()
        .filter(nodesB::contains)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
