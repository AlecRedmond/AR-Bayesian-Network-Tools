package io.github.alecredmond.export.nodedef;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.nodedef.NodeDefUpdateUtility;
import java.io.Serializable;
import java.util.List;

public interface NodeDef {
  Serializable getId();

  String getName();

  List<NodeDef> getParentDefs();

  Node getNode();

  void setNode(Node node);

  default void updateNode() {
    NodeDefUpdateUtility.updateNode(this);
  }
}
