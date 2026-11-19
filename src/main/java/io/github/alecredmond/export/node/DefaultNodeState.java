package io.github.alecredmond.export.node;

import java.io.Serializable;
import java.util.Objects;

/**
 * A default implementation of {@link NodeState}.
 *
 * <p>A state records the {@link Node} to which it belongs, its position in that node's list of
 * states, its name, and its identifier. All of these fields are final.
 *
 * <p>For faster hashmap performance, the hash code is computed once, when the state is constructed,
 * from the identifier and name of the parent node together with the position, name, and identifier
 * of the state. It is not updated if the identifier or name of the parent node changes later.
 * Equality compares only the position, name, and identifier of the states; the parent node is not
 * compared.
 *
 * @author Alec Redmond
 * @see NodeState
 * @see Node
 */
public final class DefaultNodeState implements NodeState {
  private final Node node;
  private final int position;
  private final String name;
  private final Serializable id;
  private final int hashCode;

  /**
   * Constructs a state with the specified parent node, position, name, and identifier.
   *
   * <p>The hash code of the new state is computed immediately from the identifier and name of
   * {@code node}, so both should be set on {@code node} before this constructor is called.
   *
   * @param node the node to which this state belongs
   * @param position the index of this state in the list of states of {@code node}
   * @param name the name of this state
   * @param id the unique identifier of this state
   * @throws NullPointerException if {@code node} is {@code null}
   */
  public DefaultNodeState(Node node, int position, String name, Serializable id) {
    this.node = node;
    this.position = position;
    this.name = name;
    this.id = id;
    this.hashCode = Objects.hash(node.getId(), node.getName(), id, name, position);
  }

  /**
   * Returns the hash code of this state.
   *
   * <p>The value is computed once, during construction, and does not change.
   *
   * @return the hash code of this state
   */
  @Override
  public int hashCode() {
    return hashCode;
  }

  /**
   * Compares the specified object with this state for equality.
   *
   * <p>The specified object is equal to this state if it is also a {@code DefaultNodeState} with
   * the same position, name, and identifier. The parent node is not compared.
   *
   * @param object the object to compare with this state
   * @return {@code true} if the specified object is equal to this state; {@code false} otherwise
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof DefaultNodeState that)) return false;
    return position == that.position
        && Objects.equals(name, that.name)
        && Objects.equals(id, that.id);
  }

  @Override
  public String toString() {
    return "DefaultNodeState{"
        + "position="
        + position
        + ", node = '"
        + node.getName()
        + "', name='"
        + name
        + '\''
        + ", id="
        + id
        + '}';
  }

  @Override
  public Serializable getId() {
    return id;
  }

  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public String getName() {
    return name;
  }
}
