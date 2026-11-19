package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.DefaultNode;
import io.github.alecredmond.export.node.Node;
import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang3.SerializationUtils;

/**
 * A {@link NodeSerializer} for {@link DefaultNode} instances.
 *
 * @author Alec Redmond
 * @see DefaultBinaryNodeSerializer
 * @see AbstractNodeSerializer
 */
public class DefaultNodeSerializer extends AbstractNodeSerializer<DefaultNode> {

  /** Constructs a new {@code DefaultNodeSerializer} */
  public DefaultNodeSerializer() {
    /* Default constructor */
  }

  @Override
  public DefaultNode safeCastNode(Node node) {
    return node instanceof DefaultNode defaultNode ? defaultNode : null;
  }

  @Override
  public Class<DefaultNode> nodeClass() {
    return DefaultNode.class;
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

  @Override
  public DefaultNode deSerialize(SerializedNode serializedNode, List<Node> parents) {
    DefaultNode defaultNode = new DefaultNode();
    defaultNode.setId(SerializationUtils.clone(serializedNode.id()));
    defaultNode.setName(serializedNode.name());
    defaultNode.setStates(deserializeStates(serializedNode, defaultNode));
    defaultNode.setParents(parents);
    return defaultNode;
  }
}
