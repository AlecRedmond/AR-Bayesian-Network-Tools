package io.github.alecredmond.export.nodedef;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.nodedef.attachedcpt.AttachedCpt;
import io.github.alecredmond.internal.method.nodedef.NodeDefUpdateUtility;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface NodeDefinition {
  Serializable getId();

  String getName();

  List<NodeDefinition> getParentDefs();

  Node getNode();

  void setNode(Node node);

  default void updateNode() {
    NodeDefUpdateUtility.updateNode(this);
  }

  List<StateDef> getStateDefs();

  Optional<AttachedCpt> getAttachedCpt();
}
