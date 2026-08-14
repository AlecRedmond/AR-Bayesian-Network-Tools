package io.github.alecredmond.export.node;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

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
@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeState {

  /** The unique identifier for this state. */
  @EqualsAndHashCode.Include private final Serializable id;

  /** The parent {@link Node} that exhibits this state. */
  private final Node node;

  private int position;

  /**
   * Constructs a new {@code NodeState} with a unique identifier and its associated {@link Node}.
   *
   * @param id the {@link Serializable} unique identifier for this state
   * @param node the {@link Node} that this state belongs to
   * @param <T> the type of the state identifier
   * @throws NullPointerException if either the id or the node is {@code null}
   */
  public <T extends Serializable> NodeState(@NonNull T id, @NonNull Node node) {
    this.id = id;
    this.node = node;
  }

  /**
   * Returns a string representation of this state, which corresponds to its identifier.
   *
   * @return the string representation of the state identifier
   */
  @Override
  public String toString() {
    return id.toString();
  }

  /**
   * Returns the unique identifier for this {@code NodeState}.
   *
   * @return the {@link Serializable} identifier of this state
   */
  public Serializable getId() {
    return this.id;
  }

  /**
   * Returns the parent {@link Node} associated with this state.
   *
   * @return the {@link Node} instance that exhibits this state
   */
  public Node getNode() {
    return this.node;
  }

  // TODO = JAVADOC
  public int getPosition() {
    return this.position;
  }

  // TODO = JAVADOC
  public void setPosition(int position) {
    this.position = position;
  }
}
