package io.github.alecredmond.export.nodedef;

import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;

public interface StateDef {
  Serializable getId();

  String getName();

  NodeState getNodeState();

  void setNodeState(NodeState nodeState);

  NodeDefinition getNodeDef();
}
