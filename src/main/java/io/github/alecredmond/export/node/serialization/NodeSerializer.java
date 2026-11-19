package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.serialization.NodeSerializers;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.SerializationException;

/**
 * Converts {@link Node}s of one type to and from their {@link SerializedNode} form.
 *
 * <p>Each serializer handles a single {@code Node} implementation, identified by {@link
 * #nodeClass()}. Custom {@code Node} implementations are supported by providing a serializer for
 * them and passing it to {@link #register(NodeSerializer[])}. Most serializers can extend {@link
 * AbstractNodeSerializer}, which implements the serialization half of this interface.
 *
 * @author Alec Redmond
 * @param <T> the type of {@code Node} handled by this serializer
 * @see SerializedNode
 * @see AbstractNodeSerializer
 */
public interface NodeSerializer<T extends Node> {

  /**
   * Registers the specified serializers so that the library can use them to serialize and
   * deserialize nodes.
   *
   * @param serializers the serializers to register
   */
  static void register(NodeSerializer<?>... serializers) {
    NodeSerializers.getInstance().registerSerializers(serializers);
  }

  /**
   * Converts the specified node to its serialized form.
   *
   * <p>The node is first converted to the type handled by this serializer with {@link
   * #safeCastNode(Node)}, and the result is passed to {@link #serializeGeneric(Node, double[])}. If
   * the node is not of the handled type, {@code safeCastNode} returns {@code null} and an exception
   * is thrown. Callers should therefore only pass nodes that are instances of {@link #nodeClass()}.
   *
   * @param node the node to serialize, which should be an instance of {@link #nodeClass()}
   * @param cptProbabilities the probability values from the conditional probability table of the
   *     node
   * @return the serialized form of the node
   * @throws SerializationException if the node was not of the handled type
   */
  default SerializedNode serialize(Node node, double[] cptProbabilities) {
    T t = safeCastNode(node);
    if (t != null) return serializeGeneric(t, cptProbabilities);
    throw new SerializationException(
        "Node " + node + " was not of expected type " + nodeClass().getName());
  }

  /**
   * Converts the specified node to the type handled by this serializer.
   *
   * @param node the node to convert
   * @return the node as the type handled by this serializer, or {@code null} if the node is not an
   *     instance of that type
   */
  T safeCastNode(Node node);

  /**
   * Converts a node of the type handled by this serializer to its serialized form.
   *
   * @param node the node to serialize
   * @param cptProbabilities the probability values from the conditional probability table of the
   *     node
   * @return the serialized form of the node
   */
  SerializedNode serializeGeneric(T node, double[] cptProbabilities);

  /**
   * Returns the type of node handled by this serializer.
   *
   * @return the {@link Class} of the nodes that this serializer can serialize and deserialize
   */
  Class<T> nodeClass();

  /**
   * Returns the unique identifier of this serializer.
   *
   * <p>The identifier is recorded in every {@link SerializedNode} that this serializer produces, so
   * that the matching serializer can be found when the node is deserialized.
   *
   * @return the unique identifier of this serializer
   */
  Serializable serializerId();

  /**
   * Reconstructs a node from its serialized form.
   *
   * @param serializedNode the serialized form of the node
   * @param parents the parents of the node, which must already have been reconstructed
   * @return the reconstructed node
   */
  T deSerialize(SerializedNode serializedNode, List<Node> parents);
}
