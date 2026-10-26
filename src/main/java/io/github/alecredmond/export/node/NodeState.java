package io.github.alecredmond.export.node;

import io.github.alecredmond.export.node.newnode.DefaultNodeState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a single state that a {@link Node} variable can exhibit.
 *
 * <p>Nodes within a Bayesian Network will typically have multiple states (for example "TRUE" or
 * "FALSE"), and these should comprehensively cover all possible outcomes a Node can achieve.
 *
 * <p>While a {@code NodeState}'s identifier can be anything both unique and serializable, when
 * defining an identifier using a descriptive string, it is highly advisable to prepend the state
 * name with the name of its parent {@code Node}:
 *
 * <ul>
 *   <li><b>Recommended:</b> {@code NodeState rainTrue = new NodeState("RAIN:TRUE", rain)}
 *   <li><b>Avoid:</b> {@code NodeState rainTrue = new NodeState("TRUE", rain)}
 * </ul>
 *
 * <p>This avoids any ID conflicts with other nodes that share the same state names ("TRUE" and
 * "FALSE" being the most obvious) while also making the origin of the state immediately clear.
 *
 * @author Alec Redmond
 * @see Node
 */
public interface NodeState extends CartesianState {

  /**
   * Returns the unique identifier for this {@code NodeState}.
   *
   * @return the {@link Serializable} identifier of this state
   */
  Serializable getId();

  /**
   * Returns the parent {@link Node} associated with this state.
   *
   * @return the {@link Node} instance that exhibits this state
   */
  Node getNode();

  /**
   * Returns the index of this {@code NodeState} in its parent {@link Node}'s state list.
   *
   * @return the position of this {@code NodeState}
   */
  int getPosition();

  String getName();

  static List<NodeState> createStates(Node node, String... stateNames) {
    return createStates(node, Arrays.stream(stateNames).toList());
  }

  static List<NodeState> createStates(Node node, List<String> stateNames) {
    List<NodeState> states = new ArrayList<>();
    int position = 0;
    for (String name : stateNames) {
      states.add(new DefaultNodeState(node, position++, name, UUID.randomUUID()));
    }
    return Collections.unmodifiableList(states);
  }
}
