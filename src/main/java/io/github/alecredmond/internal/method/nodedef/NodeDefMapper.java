package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.nodedef.NodeDefinition;

public interface NodeDefMapper {
  Node createNode(BayesianNetwork network, NodeDefinition nodeDef);

  void addParents(BayesianNetwork network, NodeDefinition nodeDef);
}
