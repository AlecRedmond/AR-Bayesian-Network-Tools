package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.network.newbuilder.NetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.method.network.validator.ValidatorType;
import io.github.alecredmond.internal.method.node.NodeComparator;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.NetworkTableFactory;
import io.github.alecredmond.internal.method.solver.BayesSolverImpl;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.Data;

@Data
public abstract class BaseNetworkFactory implements NetworkBuilder {
  protected final BayesianNetworkData networkData;
  protected final NetworkTableFactory tableBuilder;

  protected BaseNetworkFactory(BayesianNetworkData networkData, NetworkTableFactory tableBuilder) {
    this.networkData = networkData;
    this.tableBuilder = tableBuilder;
  }

  public BayesianNetwork build() {
    if (!networkData.isSolved()) buildSolver().solve();
    return new BayesianNetworkImpl(networkData);
  }

  @Override
  public BayesSolver buildSolver() {
    setChildren();
    validateData();
    rebuildIdMaps();
    buildNetworkTablesMap(orderNodes());
    return new BayesSolverImpl(networkData);
  }

  private void setChildren() {
    for (Node node : networkData.getNodes()) {
      for (Node parent : node.getParents()) {
        parent.getChildren().add(node);
      }
    }
  }

  private void validateData() {
    Arrays.stream(ValidatorType.values())
        .map(ValidatorType::getValidatorSupplier)
        .map(Supplier::get)
        .forEach(networkValidator -> networkValidator.validateData(networkData));
  }

  private void rebuildIdMaps() {
    List<Node> nodes = networkData.getNodes();
    Map<Serializable, Node> nodeIdMap = networkData.getNodeIDsMap();
    Map<Serializable, NodeState> stateIdMap = networkData.getNodeStateIDsMap();
    nodeIdMap.clear();
    stateIdMap.clear();

    nodes.forEach(n -> nodeIdMap.put(n.getId(), n));
    nodes.stream()
        .map(Node::getStates)
        .flatMap(Collection::stream)
        .forEach(nodeState -> stateIdMap.put(nodeState.getId(), nodeState));
  }

  private void buildNetworkTablesMap(NodeComparator comparator) {
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

  private NodeComparator orderNodes() {
    NodeComparator comparator = new NodeComparator(networkData);
    List<Node> nodes = networkData.getNodes();
    nodes.clear();
    nodes.addAll(comparator.getOrderedNodes());
    return comparator;
  }
}
