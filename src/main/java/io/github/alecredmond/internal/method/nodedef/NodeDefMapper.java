package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.nodedef.NodeDef;

public interface NodeDefMapper<T extends NodeDef> {
  Node createNode(BayesianNetwork network, T nodeDef);

  void addParents(BayesianNetwork network, T nodeDef);
}
