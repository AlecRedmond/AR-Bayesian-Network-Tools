package io.github.alecredmond.internal.method.node;

import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;

public class NodeComparator implements Comparator<Node> {
  private final Comparator<Node> comparator;
  private final Node[] nodes;

  public NodeComparator(BayesianNetworkData networkData) {
    this.nodes = networkData.getNodes();
    Map<Node, Integer> layerMap = new HashMap<>();
    Map<Node, Integer> orderMap = new HashMap<>();
    fillLayerAndOrderMap(networkData, layerMap, orderMap);
    comparator = Comparator.<Node, Integer>comparing(layerMap::get).thenComparing(orderMap::get);
  }

  private void fillLayerAndOrderMap(
      BayesianNetworkData networkData, Map<Node, Integer> layerMap, Map<Node, Integer> orderMap) {
    String networkName = networkData.getNetworkName();
    Map<Node, Integer> degreeMap = new HashMap<>();
    Queue<Node> nodeQueue = buildQueueAndDegreeMap(degreeMap, networkName);
    int order = 0;
    Node node = null;
    while (!nodeQueue.isEmpty()) {
      node = nodeQueue.poll();
      addToLayerMap(node, layerMap, networkName);
      decrementConnectedAndAppend(node.getChildren(), degreeMap, nodeQueue, networkName);
      orderMap.put(node, order++);
    }
    int expectedLen = networkData.getNodes().length;
    if (order != expectedLen) {
      if (order < expectedLen) throwCycleInRemaining(node, networkName);
      else throwUnexpectedCycleException(networkName);
    }
  }

  private Queue<Node> buildQueueAndDegreeMap(Map<Node, Integer> degreeMap, String networkName) {
    Queue<Node> nodeQueue = new ArrayDeque<>();
    for (Node node : nodes) {
      int degree = node.getParents().size();
      if (degree == 0) nodeQueue.add(node);
      degreeMap.put(node, degree);
    }
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

  private void throwCycleInRemaining(Node node, String networkName) {
    String nodeName;
    String children;
    if (node == null) {
      nodeName = "NULL";
      children = "NULL";
    } else {
      nodeName = node.getName();
      children = CollectionToString.apply(node.getChildren());
    }
    throw new NetworkStructureException(
        "The children of node "
            + nodeName
            + "(Network : "
            + networkName
            + ")"
            + "could not be processed. This implies one or more children create a cycle"
            + "\n Affected Nodes = "
            + children);
  }

  private void throwUnexpectedCycleException(String networkName) {
    throw new NetworkStructureException(
        "Found a loopy network graph: "
            + networkName
            + "\nThis error implies an fault with the Network Builder, which should have caught it upstream.");
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
