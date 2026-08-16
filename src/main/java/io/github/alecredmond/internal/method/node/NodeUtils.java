package io.github.alecredmond.internal.method.node;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
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

  public static void addParent(Node child, Node parent) {
    if (!child.getParents().contains(parent)) {
      List<Node> newParents = addToList(child.getParents(), parent);
      child.setParents(newParents);
    }
    if (!parent.getChildren().contains(child)) {
      List<Node> newChildren = addToList(parent.getChildren(), child);
      parent.setChildren(newChildren);
    }
  }

  public static <T> List<T> addToList(List<T> list, T elementToAdd) {
    return addAllToList(list, List.of(elementToAdd));
  }

  public static <T> List<T> addAllToList(List<T> list, List<T> elementsToAdd) {
    return Stream.concat(list.stream(), elementsToAdd.stream()).toList();
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

  public static Set<Node> getNodes(Collection<NodeState> states) {
    return states.stream().map(NodeState::getNode).collect(Collectors.toSet());
  }

  public static Map<Node, Integer> buildNodeIndexMap(Node[] nodes) {
    return IntStream.range(0, nodes.length)
        .mapToObj(i -> Map.entry(nodes[i], i))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public static String formatStatesToString(Collection<NodeState> stateCollection) {
    return formatCollectionToString(stateCollection);
  }

  private static <T> String formatCollectionToString(Collection<T> collection) {
    if (collection.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      sb.append(iterator.next().toString());
      if (iterator.hasNext()) sb.append(", ");
    }
    return sb.toString();
  }

  public static <T extends Serializable> String formatIDsToString(Collection<T> ids) {
    return formatCollectionToString(ids);
  }

  public static String formatNodesToString(Collection<Node> nodeCollection) {
    return formatCollectionToString(nodeCollection);
  }

  public static List<Serializable> getNodeIds(Collection<Node> nodes) {
    return nodes.stream().map(Node::getId).toList();
  }

  public static List<Serializable> getNodeStateIds(Collection<NodeState> nodeStates) {
    return nodeStates.stream().map(NodeState::getId).toList();
  }

  public static List<Serializable> getAllNodeStateIds(Collection<Node> nodes) {
    return nodes.stream().flatMap(n -> n.getNodeStates().stream()).map(NodeState::getId).toList();
  }

  public static <E extends Serializable> List<NodeState> statesWithoutId(
      List<NodeState> nodeStates, E removalId) {
    return nodeStates.stream().filter(t -> !t.getId().equals(removalId)).toList();
  }

  public static void removeParent(Node node, Node parent) {
    if (node.getParents().contains(parent)) {
      node.setParents(removeFromList(node.getParents(), parent::equals));
    }
    if (parent.getChildren().contains(node)) {
      parent.setChildren(removeFromList(parent.getChildren(), node::equals));
    }
  }

  private static <E> List<E> removeFromList(List<E> list, Predicate<E> predicate) {
    return list.stream().filter(predicate.negate()).toList();
  }

  public static Set<Node> getOverlap(Set<Node> nodesA, Set<Node> nodesB) {
    return nodesA.stream()
        .filter(nodesB::contains)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
