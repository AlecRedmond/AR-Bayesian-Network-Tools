package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.DefaultBinaryNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;

/**
 * A {@link NodeSerializer} for {@link DefaultBinaryNode} instances.
 *
 * <p>When a node is deserialized, its recorded states must be exactly one state named {@code
 * "TRUE"} and one state named {@code "FALSE"}.
 *
 * @author Alec Redmond
 * @see DefaultNodeSerializer
 * @see AbstractNodeSerializer
 */
public class DefaultBinaryNodeSerializer extends AbstractNodeSerializer<DefaultBinaryNode> {

  @Override
  public DefaultBinaryNode safeCastNode(Node node) {
    return node instanceof DefaultBinaryNode binaryNode ? binaryNode : null;
  }

  @Override
  public Class<DefaultBinaryNode> nodeClass() {
    return DefaultBinaryNode.class;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the fully qualified name of the runtime class of this
   * serializer.
   */
  @Override
  public Serializable serializerId() {
    return this.getClass().getName();
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if the recorded states are not exactly one {@code "TRUE"} state
   *     and one {@code "FALSE"} state
   */
  @Override
  public DefaultBinaryNode deSerialize(SerializedNode serializedNode, List<Node> parents) {
    DefaultBinaryNode node = new DefaultBinaryNode();
    node.setId(SerializationUtils.clone(serializedNode.id()));
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
