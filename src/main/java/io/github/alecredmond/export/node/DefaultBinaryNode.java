package io.github.alecredmond.export.node;

import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 * A default implementation of {@link BinaryNode}.
 *
 * <p>The states of this node are named {@link #TRUE_STATE_NAME} and {@link #FALSE_STATE_NAME}.
 * Every constructor that takes a {@link StateOrder} creates both states immediately, in the
 * requested order, and gives each state a randomly generated {@link UUID} as its identifier.
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
public final class DefaultBinaryNode extends AbstractBayesNode implements BinaryNode {
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

  /**
   * Creates and sets the {@link NodeState} values from the given order. This method also
   * automatically sets the {@code "TRUE"} and {@code "FALSE"} states of this {@code
   * DefaultBinaryNode}.
   *
   * @param stateOrder the order in which the {@code "TRUE"} and {@code "FALSE"} states are listed
   * @throws NullPointerException if {@code stateOrder} is {@code null}
   */
  public void setStatesByOrder(StateOrder stateOrder) {
    if (Objects.requireNonNull(stateOrder) == StateOrder.FALSE_FIRST) {
      super.setStates(NodeState.createStates(this, FALSE_STATE_NAME, TRUE_STATE_NAME));
      this.falseState = getStates().getFirst();
      this.trueState = getStates().getLast();
    } else if (stateOrder == StateOrder.TRUE_FIRST) {
      super.setStates(NodeState.createStates(this, TRUE_STATE_NAME, FALSE_STATE_NAME));
      this.falseState = getStates().getLast();
      this.trueState = getStates().getFirst();
    }
  }

  /**
   * Constructs a binary node with the specified identifier, name and parents. Called during the
   * de-serialization process.
   *
   * @param id the unique identifier of the node
   * @param name the name of the node
   * @param parents the parents of the node
   * @throws NullPointerException if {@code parents} or {@code stateOrder} is {@code null}
   */
  public DefaultBinaryNode(Serializable id, String name, List<Node> parents) {
    setId(id);
    setName(name);
    setParents(parents);
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

  /**
   * {@inheritDoc}
   *
   * <p>The input for {@code DefaultBinaryNode} must hold exactly two states, one with the name
   * {@link #TRUE_STATE_NAME} and the other with {@link #FALSE_STATE_NAME}.
   *
   * @throws NullPointerException if {@code states} is {@code null}
   * @throws IllegalArgumentException if {@code states} does not have a size of 2, or if there is
   *     not one state with name {@link #TRUE_STATE_NAME} and another with name {@link
   *     #FALSE_STATE_NAME}
   */
  @Override
  public void setStates(@NonNull List<NodeState> states) {
    if (states.size() != 2) {
      throw new IllegalArgumentException("Binary Node Must have exactly 2 states!");
    }
    StateOrder order = findStateOrder(states);
    super.setStates(states);
    switch (order) {
      case TRUE_FIRST -> {
        trueState = states.getFirst();
        falseState = states.getLast();
      }
      case FALSE_FIRST -> {
        falseState = states.getFirst();
        trueState = states.getLast();
      }
      default -> throw new IllegalStateException("Unexpected value: " + order);
    }
  }

  private static StateOrder findStateOrder(List<NodeState> states) {
    String first = states.getFirst().getName();
    StateOrder order;
    switch (first) {
      case TRUE_STATE_NAME -> order = StateOrder.TRUE_FIRST;
      case FALSE_STATE_NAME -> order = StateOrder.FALSE_FIRST;
      default ->
          throw new IllegalArgumentException(
              "Binary states must have name matching either \"%s\" or \"%s\". Found name \"%s\" "
                  .formatted(TRUE_STATE_NAME, FALSE_STATE_NAME, first));
    }
    String lastExpected = order.equals(StateOrder.TRUE_FIRST) ? FALSE_STATE_NAME : TRUE_STATE_NAME;
    String last = states.getLast().getName();
    if (!last.equals(lastExpected)) {
      throw new IllegalArgumentException(
          "Expected state name \"%s\", but found \"%s\"".formatted(lastExpected, last));
    }
    return order;
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
