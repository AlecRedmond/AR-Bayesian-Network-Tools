package io.github.alecredmond.networkscenario;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.newbuilder.NetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Set;

public interface NetworkScenario {
  NetworkBuilder getBuilder();

  BayesianNetwork buildNetwork();

  Node getNodeByName(String name);

  NodeState getStateByName(String nodeName, String stateName);

  Set<ConstraintData> getConstraintData();
}
