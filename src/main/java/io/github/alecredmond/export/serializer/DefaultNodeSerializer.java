package io.github.alecredmond.export.serializer;

import io.github.alecredmond.export.node.DefaultNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import java.io.Serializable;
import java.util.*;

public class DefaultNodeSerializer extends AbstractNodeSerializer<DefaultNode> {

  @Override
  public boolean canSerialize(Node node) {
    return node instanceof DefaultNode;
  }

  @Override
  public boolean canDeSerialize(SerializedNode serializedNode) {
    return serializedNode.serializerId().equals(serializerId());
  }

  @Override
  public Serializable serializerId() {
    return this.getClass().getName();
  }

  @Override
  public DefaultNode safeCastNode(Node node) {
    return node instanceof DefaultNode defaultNode ? defaultNode : null;
  }

  @Override
  public DefaultNode deSerialize(SerializedNode serializedNode, List<Node> parents) {
    DefaultNode defaultNode = new DefaultNode();
    defaultNode.setId(serializedNode.id());
    defaultNode.setName(serializedNode.name());
    defaultNode.setStates(deserializeStates(serializedNode, defaultNode));
    defaultNode.setParents(parents);
    return defaultNode;
  }
}
