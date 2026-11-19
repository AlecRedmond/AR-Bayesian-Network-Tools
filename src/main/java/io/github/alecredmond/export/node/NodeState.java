package io.github.alecredmond.export.node;

import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a single state that a {@link Node} variable can exhibit.
 *
 * <p>A node in a Bayesian network usually has several states, for example {@code "TRUE"} and {@code
 * "FALSE"}. Together, the states of a node should cover every outcome that the node can have.
 *
 * <p>Each {@code NodeState} must declare its index in the node's state list {@link
 * Node#getStates()}, as well as its unique identifier and name. If a randomly assigned identifier
 * is acceptable, the static method {@link NodeState#createStates(Node, String...)} may be used to
 * build a list of states from the node and the state names.
 *
 * @author Alec Redmond
 * @see Node
 * @see DefaultNodeState
 */
public interface NodeState extends CartesianState {

  /**
   * Returns the unique identifier of this state.
   *
   * @return the {@link Serializable} identifier of this state
   */
  Serializable getId();

  /**
   * Returns the node to which this state belongs.
   *
   * @return the {@link Node} that can exhibit this state
   */
  Node getNode();

  /**
   * Returns the index of this state in the list of states of its parent {@link Node}.
   *
   * @return the position of this state
   */
  int getPosition();

  /**
   * Returns the name of this state.
   *
   * @return the name of this state
   */
  String getName();

  /**
   * Creates a state for each of the specified names, for the specified node.
   *
   * <p>The state at index {@code i} of the returned list is a {@link DefaultNodeState} with
   * position {@code i}, the name at index {@code i} of {@code stateNames}, and a randomly generated
   * {@link UUID} as its identifier.
   *
   * <p>The hash code of each state is computed immediately from the identifier and name of {@code
   * node}, so both should be set on {@code node} before this method is called. The created states
   * are not added to {@code node}; the caller must assign them.
   *
   * @param node the node to which the created states belong
   * @param stateNames the names of the states, in the order of their positions
   * @return an unmodifiable list of the new states, in the order of {@code stateNames}
   * @throws NullPointerException if {@code node} or {@code stateNames} is {@code null}
   */
  static List<NodeState> createStates(Node node, String... stateNames) {
    return createStates(node, Arrays.stream(stateNames).toList());
  }

  /**
   * Creates a state for each of the specified names, for the specified node.
   *
   * <p>The state at index {@code i} of the returned list is a {@link DefaultNodeState} with
   * position {@code i}, the name at index {@code i} of {@code stateNames}, and a randomly generated
   * {@link UUID} as its identifier.
   *
   * <p>The hash code of each state is computed immediately from the identifier and name of {@code
   * node}, so both should be set on {@code node} before this method is called. The created states
   * are not added to {@code node}; the caller must assign them.
   *
   * @param node the node to which the created states belong
   * @param stateNames the names of the states, in the order of their positions
   * @return an unmodifiable list of the new states, in the order of {@code stateNames}
   * @throws NullPointerException if {@code node} or {@code stateNames} is {@code null}
   */
  static List<NodeState> createStates(Node node, List<String> stateNames) {
    List<NodeState> states = new ArrayList<>();
    int position = 0;
    for (String name : stateNames) {
      states.add(new DefaultNodeState(node, position++, name, UUID.randomUUID()));
    }
    return Collections.unmodifiableList(states);
  }
}
