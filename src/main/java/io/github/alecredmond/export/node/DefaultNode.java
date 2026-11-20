package io.github.alecredmond.export.node;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A default implementation of {@link Node} that can have any number of states.
 *
 * <p>The no-argument constructor creates an uninitialized node. It is intended for deserializers,
 * which populate the node through its setters.
 *
 * <p>The {@code equals}, {@code hashCode}, and {@code toString} methods of this class use the same
 * fields as {@link AbstractBayesNode}: the identifier, the name, and the states.
 *
 * @author Alec Redmond
 * @see Node
 * @see DefaultBinaryNode
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public final class DefaultNode extends AbstractBayesNode {

  /**
   * Constructs a node with a randomly generated identifier and a new state for each specified name.
   *
   * <p>The states are created by {@link NodeState#createStates(Node, List)}, so each receives a
   * randomly generated identifier.
   *
   * @param name the name of the node
   * @param stateNames the names of the states, in the order in which they are listed
   * @param parents the parents of the node
   * @throws NullPointerException if {@code stateNames} or {@code parents} is {@code null}
   */
  public DefaultNode(String name, List<String> stateNames, List<Node> parents) {
    this(UUID.randomUUID(), name, stateNames, parents);
  }

  /**
   * Constructs a node with a new state for each specified name.
   *
   * <p>The states are created by {@link NodeState#createStates(Node, List)}, so each receives a
   * randomly generated identifier.
   *
   * @param id the unique identifier of the node
   * @param name the name of the node
   * @param stateNames the names of the states, in the order in which they are listed
   * @param parents the parents of the node
   * @throws NullPointerException if {@code stateNames} or {@code parents} is {@code null}
   */
  public DefaultNode(Serializable id, String name, List<String> stateNames, List<Node> parents) {
    setId(id);
    setName(name);
    setParents(parents);
    setStates(NodeState.createStates(this, stateNames));
  }

  @Override
  public NodeState getStateById(Serializable stateId) {
    return stateIdMap.get(stateId);
  }
}
