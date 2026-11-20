package io.github.alecredmond.export.solver.constraint;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * A Constraint holding only one non-conditional event {@code P(E) = p}. This represents the
 * unconditional probability of the event in the network. If defining the network in graph order
 * {@code P(X|Pa(X))}, this is the constraint used for the unconditional probabilities of root
 * nodes.
 *
 * <p>Valid examples:
 *
 * <ul>
 *   <li>{@code P(RAIN:TRUE) = 0.8}<br>
 *       Graph-Ordered: The unconditional probability for P(RAIN)
 *   <li>{@code P(SPRINKLER:TRUE) = 0.6}<br>
 *       Non-Graph-Ordered: A marginal constraint on P(SPRINKLER), which will be projected back to
 *       P(SPRINKLER|RAIN) after solving.
 * </ul>
 *
 * @see ProbabilityConstraint
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@EqualsAndHashCode(callSuper = true)
public class MarginalConstraint extends ProbabilityConstraint {
  private final NodeState eventState;
  private final Node eventNode;

  /**
   * Constructs a {@code MarginalConstraint} for a single, unconditional event.
   *
   * @param eventState The single measured {@link NodeState} {@code E}.
   * @param probability The unconditional probability of the event {@code p}.
   */
  public MarginalConstraint(@NonNull NodeState eventState, double probability) {
    super(Set.of(eventState), new HashSet<>(), probability);
    this.eventState = eventState;
    this.eventNode = eventState.getNode();
  }

  /**
   * Retrieves the single event state targeted by this marginal constraint.
   *
   * @return The measured {@link NodeState}.
   */
  public NodeState getEventState() {
    return this.eventState;
  }

  /**
   * Retrieves the node associated with the targeted event state.
   *
   * @return The {@link Node} belonging to the event state.
   */
  public Node getEventNode() {
    return this.eventNode;
  }
}
