package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.DefaultBinaryNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;

public class DefaultBinaryNodeSerializer extends AbstractNodeSerializer<DefaultBinaryNode> {

  @Override
  public boolean canSerialize(Node node) {
    return node instanceof DefaultBinaryNode;
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
  public DefaultBinaryNode safeCastNode(Node node) {
    return node instanceof DefaultBinaryNode binaryNode ? binaryNode : null;
  }

  @Override
  public DefaultBinaryNode deSerialize(SerializedNode serializedNode, List<Node> parents) {
    DefaultBinaryNode node = new DefaultBinaryNode();
    node.setId(serializedNode.id());
    node.setName(serializedNode.name());
    node.setStates(deserializeStates(serializedNode, node));
    node.setParents(parents);
    setTrueAndFalseStates(node);
    return node;
  }

  private void setTrueAndFalseStates(DefaultBinaryNode node) {
    boolean trueIsSet = false;
    boolean falseIsSet = false;
    for (NodeState state : node.getStates()) {
      switch (state.getName()) {
        case DefaultBinaryNode.FALSE_STATE_NAME -> {
          if (falseIsSet) {
            throw new IllegalStateException("Multiple FALSE states in Node : " + node);
          }
          node.setFalseState(state);
          falseIsSet = true;
        }
        case DefaultBinaryNode.TRUE_STATE_NAME -> {
          if (trueIsSet) {
            throw new IllegalStateException("Multiple TRUE states in Node : " + node);
          }
          node.setTrueState(state);
          trueIsSet = true;
        }
        default -> throw new IllegalStateException("Unexpected value: " + state.getName());
      }
    }
    if (trueIsSet && falseIsSet) return;
    throw new IllegalStateException("Could not set both TRUE and FALSE states in Node : " + node);
  }
}
