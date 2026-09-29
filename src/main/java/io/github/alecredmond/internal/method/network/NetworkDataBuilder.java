package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.method.network.prevalidationlogic.PreValidationLogicType;
import io.github.alecredmond.internal.method.network.validator.ValidatorType;
import io.github.alecredmond.internal.method.node.NodeComparator;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.NetworkTableBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.Data;

@Data
public class NetworkDataBuilder {
  private final BayesianNetworkImpl bayesianNetwork;
  private final BayesianNetworkData networkData;
  private final NetworkTableBuilder tableBuilder = new NetworkTableBuilder();

  public NetworkDataBuilder(BayesianNetworkImpl bayesianNetwork) {
    this.bayesianNetwork = bayesianNetwork;
    this.networkData = bayesianNetwork.getNetworkData();
  }

  public void build() {
    runPreValidationLogic();
    validateData();
    NodeComparator comparator = orderNodes();
    rebuildIdMaps(networkData.getNodes());
    buildNetworkTablesMap(comparator);
  }

  private void runPreValidationLogic() {
    Arrays.stream(PreValidationLogicType.values())
        .forEach(value -> value.runLogic(bayesianNetwork));
  }

  private void validateData() {
    Arrays.stream(ValidatorType.values())
        .map(ValidatorType::getValidatorSupplier)
        .map(Supplier::get)
        .forEach(networkValidator -> networkValidator.validateData(networkData));
  }

  public NodeComparator orderNodes() {
    NodeComparator comparator = new NodeComparator(networkData);
    List<Node> nodes = networkData.getNodes();
    nodes.clear();
    nodes.addAll(comparator.getOrderedNodes());
    return comparator;
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

  public void buildNetworkTablesMap(NodeComparator comparator) {
    networkData.getNetworkTablesMap().clear();
    networkData
        .getNodes()
        .forEach(
            node -> {
              List<Node> events = List.of(node);
              List<Node> conditions = node.getParents().stream().sorted(comparator).toList();
              NetworkTable table = tableBuilder.buildTable(events, conditions);
              table.normalizeTable();
              networkData.getNetworkTablesMap().put(node, table);
              node.setCpt(table);
            });
  }
}
