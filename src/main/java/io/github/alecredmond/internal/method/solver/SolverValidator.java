package io.github.alecredmond.internal.method.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SolverValidator {

  private final BayesianNetwork network;

  public SolverValidator(BayesianNetwork network) {
    this.network = network;
  }

  public void validateDataBuilt() {
    resetNetworkTables();
    if (checkNetworkBuilt()) return;
    throw new IllegalStateException("Network data not correctly built!");
  }

  private void resetNetworkTables() {
    for (NetworkTable table : network.getAllNetworkTables()) {
      Arrays.fill(table.getProbabilities(), 1.0);
      table.normalizeTable();
    }
  }

  private boolean checkNetworkBuilt() {
    return tableDimensionsAreCorrect() && constraintsAreValidated();
  }

  private boolean tableDimensionsAreCorrect() {
    NetworkTable[] tables = network.getAllNetworkTables();
    Node[] nodes = network.getNodes();
    Set<Node> visited = new HashSet<>();
    for (int i = 0; i < nodes.length; i++) {
      Node node = nodes[i];
      NetworkTable networkTable = tables[i];
      boolean allCorrect =
          networkTable != null
              && tableContainsAllNodes(networkTable, visited, node)
              && tableSizeIsCorrect(networkTable);
      if (!allCorrect) return false;
    }
    return true;
  }

  private boolean constraintsAreValidated() {
    return network.getConstraints().stream().allMatch(ValidatedConstraint.class::isInstance);
  }

  private boolean tableContainsAllNodes(NetworkTable table, Set<Node> visited, Node node) {
    Set<Node> nodeParents = new HashSet<>(node.getParents());
    boolean eventNodeCorrect = table.getNetworkNode().equals(node);
    boolean nodeNotYetVisited = visited.add(node);
    boolean parentsAlreadyVisited = visited.containsAll(nodeParents);
    boolean parentRelationshipsValid = table.getConditions().equals(nodeParents);
    return eventNodeCorrect
        && nodeNotYetVisited
        && parentsAlreadyVisited
        && parentRelationshipsValid;
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
}
