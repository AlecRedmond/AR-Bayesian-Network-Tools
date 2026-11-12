package io.github.alecredmond.export.node;

/**
 * A {@link Node} with only two states: "TRUE" and "FALSE". This interface allows additional actions
 * to obtain the state from its boolean value via {@link #getState(boolean)}, or directly using
 * {@link #trueState()} or {@link #falseState()}. {@code BinaryNode} instances must declare their
 * {@link StateOrder}, whether "TRUE" or "FALSE" is the first ordinal in the state list.
 *
 * @see DefaultBinaryNode
 * @see Node
 * @author Alec Redmond
 */
public interface BinaryNode extends Node {
  default NodeState getState(boolean binaryState) {
    return binaryState ? trueState() : falseState();
  }

  NodeState trueState();

  NodeState falseState();

  StateOrder getStateOrder();

  enum StateOrder {
    FALSE_FIRST,
    TRUE_FIRST
  }
}
