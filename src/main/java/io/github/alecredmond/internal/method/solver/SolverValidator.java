package io.github.alecredmond.internal.method.solver;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SolverValidator {
  private final BayesianNetwork network;

  public SolverValidator(BayesianNetwork network) {
    this.network = network;
  }

  public void validateDataBuilt() {
    if (checkNetworkBuilt()) return;
    network.buildNetworkData();
  }

  private boolean checkNetworkBuilt() {
    return tableDimensionsAreCorrect() && constraintsAreValidated();
  }

  private boolean tableDimensionsAreCorrect() {
    BayesianNetworkData data = network.getNetworkData();
    Map<Node, NetworkTable> tableMap = data.getNetworkTablesMap();
    Set<Node> nodes = new HashSet<>(data.getNodeIDsMap().values());
    for (Node node : nodes) {
      NetworkTable networkTable = tableMap.get(node);
      boolean allCorrect =
          networkTable != null
              && tableContainsAllNodes(networkTable, nodes, node)
              && tableSizeIsCorrect(networkTable);
      if (!allCorrect) return false;
    }
    return true;
  }

  private boolean constraintsAreValidated() {
    return network.getNetworkData().getConstraints().stream()
        .allMatch(ValidatedConstraint.class::isInstance);
  }

  private boolean tableContainsAllNodes(NetworkTable table, Set<Node> allNodes, Node node) {
    Set<Node> nodeParents = new HashSet<>(node.getParents());
    boolean tableNodesInNetwork = allNodes.containsAll(table.getNodes());
    boolean eventNodeCorrect = table.getNetworkNode().equals(node);
    boolean parentRelationshipsValid = table.getConditions().equals(nodeParents);
    return tableNodesInNetwork && eventNodeCorrect && parentRelationshipsValid;
  }

  private boolean tableSizeIsCorrect(NetworkTable table) {
    ProbabilityVector vector = table.getVector();
    Node[] nodes = vector.getOrderedNodes();
    NodeState[][] states = vector.getStateArrays();
    for (int i = 0; i < nodes.length; i++) {
      if (states[i].length != nodes[i].getStates().size()) return false;
    }
    return true;
  }

  public void resetNetworkTables() {
    network
        .getNetworkTables()
        .values()
        .forEach(
            table -> {
              Arrays.fill(table.getProbabilities(), 1.0);
              table.normalizeTable();
            });
  }
}
