package io.github.alecredmond.export.node;

/**
 * Represents a {@link Node} with exactly two states, named {@code "TRUE"} and {@code "FALSE"}.
 *
 * <p>In addition to the operations of {@code Node}, a {@code BinaryNode} returns its states
 * directly from a {@code boolean} value through {@link #getState(boolean)}, or through {@link
 * #trueState()} and {@link #falseState()}.
 *
 * <p>Every {@code BinaryNode} declares its {@link BinaryStateOrder}, which specifies whether the
 * {@code "TRUE"} state or the {@code "FALSE"} state comes first in the list returned by {@link
 * #getStates()}.
 *
 * @author Alec Redmond
 * @see DefaultBinaryNode
 * @see Node
 */
public interface BinaryNode extends Node {

  /**
   * Returns the state that corresponds to the specified {@code boolean} value.
   *
   * @param binaryState {@code true} to obtain the {@code "TRUE"} state, or {@code false} to obtain
   *     the {@code "FALSE"} state
   * @return the {@code "TRUE"} state if {@code binaryState} is {@code true}; otherwise, the {@code
   *     "FALSE"} state
   */
  default NodeState getState(boolean binaryState) {
    return binaryState ? trueState() : falseState();
  }

  /**
   * Returns the state that represents {@code true}.
   *
   * @return the {@code "TRUE"} state of this node
   */
  NodeState trueState();

  /**
   * Returns the state that represents {@code false}.
   *
   * @return the {@code "FALSE"} state of this node
   */
  NodeState falseState();

}
