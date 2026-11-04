package io.github.alecredmond.networkscenario;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.BayesSolver;
import java.util.List;
import java.util.Set;

public interface NetworkScenario {
  BayesianNetworkBuilder getBuilder();

  BayesianNetwork buildNetwork();

  BayesSolver getDefaultSolver();

  Node getNodeByName(String name);

  NodeState getStateByName(String nodeName, String stateName);

  Set<ConstraintData> getConstraintData();

  Node[] getOrderedNodes();

  List<ObservationPackage> buildObservationPackages();
}
