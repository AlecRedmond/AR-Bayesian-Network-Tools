package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.DefaultBinaryNode;
import io.github.alecredmond.export.node.Node;
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
public final class DefaultBinaryNodeSerializer extends AbstractNodeSerializer<DefaultBinaryNode> {

  /** Constructs a new {@code DefaultBinaryNodeSerializer} */
  public DefaultBinaryNodeSerializer() {
    /* Default constructor */
  }

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
    Serializable id = SerializationUtils.clone(serializedNode.id());
    String name = serializedNode.name();
    DefaultBinaryNode node = new DefaultBinaryNode(id, name, parents);
    node.setStates(deserializeStates(serializedNode, node));
    return node;
  }
}
