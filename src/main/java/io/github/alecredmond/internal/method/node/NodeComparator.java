package io.github.alecredmond.internal.method.node;

import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import java.util.*;
import lombok.Getter;

public class NodeComparator implements Comparator<Node> {
  private final Comparator<Node> comparator;
  @Getter private final List<Node> orderedNodes;

  public NodeComparator(BayesianNetworkData networkData) {
    Map<Node, Integer> layerMap = new HashMap<>();
    Map<Node, Integer> orderMap = new HashMap<>();
    fillLayerAndOrderMap(networkData, layerMap, orderMap);
    comparator = Comparator.<Node, Integer>comparing(layerMap::get).thenComparing(orderMap::get);
    orderedNodes = buildOrderedNodes(networkData.getNodeIDsMap().values());
  }

  private void fillLayerAndOrderMap(
      BayesianNetworkData networkData, Map<Node, Integer> layerMap, Map<Node, Integer> orderMap) {
    String networkName = networkData.getNetworkName();
    Collection<Node> nodes = networkData.getNodeIDsMap().values();
    Map<Node, Integer> degreeMap = new HashMap<>();
    Queue<Node> nodeQueue = buildQueueAndDegreeMap(nodes, degreeMap, networkName);
    int order = 0;
    while (!nodeQueue.isEmpty()) {
      Node node = nodeQueue.poll();
      addToLayerMap(node, layerMap, networkName);
      decrementConnectedAndAppend(node.getChildren(), degreeMap, nodeQueue, networkName);
      orderMap.put(node, order++);
    }
  }

  private List<Node> buildOrderedNodes(Collection<Node> nodes) {
    return nodes.stream().sorted(comparator).toList();
  }

  private Queue<Node> buildQueueAndDegreeMap(
      Collection<Node> nodes, Map<Node, Integer> degreeMap, String networkName) {
    Queue<Node> nodeQueue = new ArrayDeque<>();
    nodes.forEach(
        node -> {
          int degree = node.getParents().size();
          if (degree == 0) nodeQueue.add(node);
          degreeMap.put(node, degree);
        });
    if (!nodeQueue.isEmpty()) return nodeQueue;
    throw new NetworkStructureException(
        "No nodes without parents exist in network %s".formatted(networkName));
  }

  private void addToLayerMap(Node node, Map<Node, Integer> layerMap, String networkName) {
    if (layerMap.containsKey(node)) {
      throwCycleError(node, networkName);
    }

    int layer =
        node.getParents().stream()
            .mapToInt(layerMap::get)
            .map(parentLayer -> parentLayer + 1)
            .max()
            .orElse(0);

    layerMap.put(node, layer);
  }

  private void decrementConnectedAndAppend(
      List<Node> children,
      Map<Node, Integer> degreeMap,
      Queue<Node> nodeQueue,
      String networkName) {
    children.forEach(
        node -> {
          int newDegree = degreeMap.merge(node, -1, Integer::sum);
          if (newDegree == 0) nodeQueue.add(node);
          if (newDegree < 0) throwCycleError(node, networkName);
        });
  }

  private void throwCycleError(Node node, String networkName) {
    throw new NetworkStructureException(
        "Network '%s': a cycle was found in the graph involving Node %s"
            .formatted(networkName, node));
  }

  @Override
  public int compare(Node o1, Node o2) {
    return comparator.compare(o1, o2);
  }
}
