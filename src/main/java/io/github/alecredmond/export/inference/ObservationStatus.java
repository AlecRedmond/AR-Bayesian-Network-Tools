package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * The Observation status of a single {@link Node} within an {@link Observable}. There are 4
 * {@code ObservationStatus} elements:
 *
 * <ul>
 *   <li>{@code UNOBSERVED}: All {@link NodeState}s associated with the {@link Node} are active
 *       within the {@link Observable}.
 *   <li>{@code PARTIALLY_OBSERVED}: Some {@link NodeState}s associated with the {@link Node} have
 *       been eliminated, but more than one active state remains.
 *   <li>{@code OBSERVED}: A single {@link NodeState} associated with the {@link Node} remains
 *       active, with all other states eliminated.
 *   <li>{@code NEGATED}: All {@link NodeState}s associated with the {@link Node} have been
 *       eliminated. This will set the joint probability of the entire {@link Observable} to 0.
 * </ul>
 */
public enum ObservationStatus {
  /**
   * All {@link NodeState}s associated with the {@link Node} are active within the {@link
   * Observable}.
   */
  UNOBSERVED,
  /**
   * Some {@link NodeState}s associated with the {@link Node} have been eliminated, but more than
   * one active state remains.
   */
  PARTIALLY_OBSERVED,
  /**
   * A single {@link NodeState} associated with the {@link Node} remains active, with all other
   * states eliminated.
   */
  OBSERVED,
  /**
   * All {@link NodeState}s associated with the {@link Node} have been eliminated. This will set the
   * joint probability of the entire {@link Observable} to 0.
   */
  NEGATED
}
