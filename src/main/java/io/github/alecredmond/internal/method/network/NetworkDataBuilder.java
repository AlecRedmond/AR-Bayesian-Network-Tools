package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.method.network.validator.ValidatorType;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.NetworkTableBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.Data;

@Data
public class NetworkDataBuilder {
  private final BayesianNetworkData networkData;
  private final NetworkTableBuilder tableBuilder = new NetworkTableBuilder();

  public void build() {
    validateData();
    Map<Node, Integer> layerMap = orderNodes();
    rebuildIdMaps(networkData.getNodes());
    buildNetworkTablesMap(layerMap);
  }

  public Map<Node, Integer> orderNodes() {
    Map<Node, Integer> layerMap = createLayerMap();
    List<Node> nodes = networkData.getNodes();
    nodes.clear();
    layerMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .forEach(nodes::add);
    return layerMap;
  }

  public void rebuildIdMaps(Collection<Node> nodes) {
    Map<Serializable, Node> nodeIdMap = networkData.getNodeIDsMap();
    Map<Serializable, NodeState> stateIdMap = networkData.getNodeStateIDsMap();
    nodeIdMap.clear();
    stateIdMap.clear();

    nodes.forEach(n -> nodeIdMap.put(n.getId(), n));
    nodes.stream()
        .map(Node::getNodeStates)
        .flatMap(Collection::stream)
        .forEach(nodeState -> stateIdMap.put(nodeState.getId(), nodeState));
  }

  public void buildNetworkTablesMap(Map<Node, Integer> layerMap) {
    networkData.getNetworkTablesMap().clear();
    networkData
        .getNodes()
        .forEach(
            node -> {
              List<Node> events = List.of(node);
              List<Node> conditions = orderConditions(node.getParents(), layerMap);
              NetworkTable table = tableBuilder.buildTable(events, conditions);
              table.normalizeTable();
              networkData.getNetworkTablesMap().put(node, table);
              node.setCpt(table);
            });
  }

  public List<Node> orderConditions(List<Node> parents, Map<Node, Integer> layerMap) {
    return parents.stream().sorted(Comparator.comparingInt(layerMap::get)).toList();
  }

  public static int calculateNodeLayer(Node node, Map<Node, Integer> layerMap) {
    if (layerMap.containsKey(node)) return layerMap.get(node);

    int layer =
        node.getParents().stream()
            .mapToInt(parent -> calculateNodeLayer(parent, layerMap) + 1)
            .max()
            .orElse(0);

    layerMap.put(node, layer);
    return layer;
  }

  private void validateData() {
    Arrays.stream(ValidatorType.values())
        .map(ValidatorType::getValidatorSupplier)
        .map(Supplier::get)
        .forEach(networkValidator -> networkValidator.validateData(networkData));
  }

  private Map<Node, Integer> createLayerMap() {
    Map<Node, Integer> layerMap = new HashMap<>();
    networkData.getNodeIDsMap().values().forEach(node -> calculateNodeLayer(node, layerMap));
    return layerMap;
  }
}
