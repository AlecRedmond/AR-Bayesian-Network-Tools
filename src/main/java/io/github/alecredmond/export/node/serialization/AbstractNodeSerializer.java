package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.DefaultNodeState;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;
import lombok.NonNull;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Provides the parts of a {@link NodeSerializer} that do not depend on the type of node.
 *
 * <p>This class implements {@link #serializeGeneric(Node, double[])} by copying the identifier,
 * name, states, and parents of a node into a {@link SerializedNode}. It also offers {@link
 * #deserializeStates(SerializedNode, Node)}, which subclasses use to rebuild the states of a node.
 * Subclasses supply the type-specific parts: casting, the node class, the serializer identifier,
 * and deserialization.
 *
 * @author Alec Redmond
 * @param <T> the type of {@link Node} handled by this serializer
 * @see NodeSerializer
 * @see SerializedNode
 */
public abstract class AbstractNodeSerializer<T extends Node> implements NodeSerializer<T> {

  /**
   * {@inheritDoc}
   *
   * <p>The identifiers and names of the states are recorded in state order. The parents are
   * recorded by identifier only. The result uses {@link #serializerId()} as its serializer
   * identifier, and stores {@code cptProbabilities} as supplied, without copying it.
   *
   * @throws NullPointerException if {@code node} is {@code null}
   */
  @Override
  public SerializedNode serializeGeneric(T node, double[] cptProbabilities) {
    Serializable id = SerializationUtils.clone(node.getId());
    String name = node.getName();
    List<NodeState> states = node.getStates();
    final int numOfStates = states.size();
    Serializable[] stateIds = new Serializable[numOfStates];
    String[] stateNames = new String[numOfStates];
    for (int i = 0; i < numOfStates; i++) {
      NodeState state = states.get(i);
      stateIds[i] = SerializationUtils.clone(state.getId());
      stateNames[i] = state.getName();
    }
    Serializable[] parentIds =
        node.getParents().stream().map(Node::getId).toArray(Serializable[]::new);
    return new SerializedNode(
        id, name, stateIds, stateNames, parentIds, cptProbabilities, serializerId());
  }

  /**
   * Rebuilds the states recorded in a serialized node.
   *
   * <p>A {@link NodeState} is created for each recorded state. Its position is its index in the
   * recorded arrays, and its identifier and name are the recorded values.
   *
   * @param serializedNode the serialized node that records the state identifiers and names
   * @param defaultNode the node to which the rebuilt states belong, which should already have its
   *     identifier and name set
   * @return an unmodifiable list of the rebuilt states, in the recorded order
   * @throws NullPointerException if {@code serializedNode} or {@code defaultNode} are {@code null}
   * @throws SerializationException if the state name and id array lengths differ
   */
  protected List<NodeState> deserializeStates(
      @NonNull SerializedNode serializedNode, @NonNull Node defaultNode) {
    Serializable[] stateIds = serializedNode.stateIds();
    String[] stateNames = serializedNode.stateNames();
    if (stateNames.length != stateIds.length) {
      throw new SerializationException("State name and id lengths differ in " + serializedNode);
    }
    return IntStream.range(0, stateNames.length)
        .mapToObj(i -> reconstructState(defaultNode, i, stateNames[i], stateIds[i]))
        .toList();
  }

  /**
   * Rebuilds a {@link NodeState} from a {@link SerializedNode}, called in {@link
   * #deserializeStates(SerializedNode, Node)}. The default implementation returns a {@link
   * DefaultNodeState}
   *
   * @param node the node to which the rebuilt state belongs
   * @param position the index of the rebuilt state in the list
   * @param name the name of the rebuilt state
   * @param id the unique identifier of the rebuilt state
   * @return the rebuilt {@link NodeState}
   */
  protected NodeState reconstructState(Node node, int position, String name, Serializable id) {
    return new DefaultNodeState(node, position, name, SerializationUtils.clone(id));
  }
}
