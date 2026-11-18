package io.github.alecredmond.export.node;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.*;

/**
 * A default implementation of {@link BinaryNode}.
 *
 * <p>The states of this node are named {@link #TRUE_STATE_NAME} and {@link #FALSE_STATE_NAME}.
 * Every constructor that takes a {@link BinaryNode.StateOrder} creates both states immediately, in
 * the requested order, and gives each state a randomly generated {@link UUID} as its identifier.
 *
 * <p>The no-argument constructor creates an uninitialized node. It is intended for deserializers,
 * which populate the node through its setters. Public setters exist for the true state, the false
 * state, and the state order. These setters only assign the corresponding field and do not modify
 * the list of states, so calling them directly can leave the node inconsistent.
 *
 * <p>In addition to the fields used by {@link AbstractBayesNode}, the {@code equals}, {@code
 * hashCode}, and {@code toString} methods of this class use the true state, the false state, and
 * the state order.
 *
 * @author Alec Redmond
 * @see BinaryNode
 * @see DefaultNode
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Setter
public class DefaultBinaryNode extends AbstractBayesNode implements BinaryNode {
  /** The name of the state that represents {@code true}. */
  public static final String TRUE_STATE_NAME = "TRUE";

  /** The name of the state that represents {@code false}. */
  public static final String FALSE_STATE_NAME = "FALSE";

  /** The state that represents {@code false}. */
  private NodeState falseState;

  /** The state that represents {@code true}. */
  private NodeState trueState;

  /**
   * Constructs a binary node with a randomly generated identifier, the specified name and parents,
   * and the specified state order.
   *
   * @param name the name of the node
   * @param parents the parents of the node
   * @param stateOrder the order in which the {@code "TRUE"} and {@code "FALSE"} states are listed
   * @throws NullPointerException if {@code parents} or {@code stateOrder} is {@code null}
   */
  public DefaultBinaryNode(String name, List<Node> parents, StateOrder stateOrder) {
    this(UUID.randomUUID(), name, parents, stateOrder);
  }

  /**
   * Constructs a binary node with the specified identifier, name, parents, and state order.
   *
   * @param id the unique identifier of the node
   * @param name the name of the node
   * @param parents the parents of the node
   * @param stateOrder the order in which the {@code "TRUE"} and {@code "FALSE"} states are listed
   * @throws NullPointerException if {@code parents} or {@code stateOrder} is {@code null}
   */
  public DefaultBinaryNode(
      Serializable id, String name, List<Node> parents, StateOrder stateOrder) {
    setId(id);
    setName(name);
    setParents(parents);
    setStatesByOrder(stateOrder);
  }

  private void setStatesByOrder(StateOrder stateOrder) {
    if (Objects.requireNonNull(stateOrder) == StateOrder.FALSE_FIRST) {
      setStates(NodeState.createStates(this, FALSE_STATE_NAME, TRUE_STATE_NAME));
      this.falseState = getStates().getFirst();
      this.trueState = getStates().getLast();
    } else if (stateOrder == StateOrder.TRUE_FIRST) {
      setStates(NodeState.createStates(this, TRUE_STATE_NAME, FALSE_STATE_NAME));
      this.falseState = getStates().getLast();
      this.trueState = getStates().getFirst();
    }
  }

  /**
   * Constructs a binary node with a randomly generated identifier, the specified name, no parents,
   * and the specified state order.
   *
   * @param name the name of the node
   * @param stateOrder the order in which the {@code "TRUE"} and {@code "FALSE"} states are listed
   * @throws NullPointerException if {@code stateOrder} is {@code null}
   */
  public DefaultBinaryNode(String name, StateOrder stateOrder) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(List.of());
    setStatesByOrder(stateOrder);
  }

  @Override
  public NodeState getStateById(Serializable stateId) {
    return stateIdMap.get(stateId);
  }

  @Override
  public NodeState trueState() {
    return trueState;
  }

  @Override
  public NodeState falseState() {
    return falseState;
  }
}
