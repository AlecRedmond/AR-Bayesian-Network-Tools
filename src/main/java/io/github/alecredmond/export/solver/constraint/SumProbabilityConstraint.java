package io.github.alecredmond.export.solver.constraint;

import io.github.alecredmond.export.node.NodeState;
import java.util.Collection;
import lombok.EqualsAndHashCode;

/**
 * A constraint representing the marginalised probability obtained by summing over a subset of
 * states belonging to one or more nodes, in the form:<br>
 * {@code P(E|C) = Σ P(Ei|C) = p}<br>
 * where the summation is over all specified state combinations within the event set.
 *
 * <p>Consider states in the events (E) belonging to the same Node (Nx), such that for<br>
 * {@code X ∈ Nx = {Xi,Xj,...,Xn}, A = E \ X}:<br>
 * {@code P(X,A|C) = P(Xi,A|C) + P(Xj,A|C) ... + P(Xn,A|C) = p} <br>
 * When multiple nodes each contribute multiple states, the sum ranges over the full Cartesian
 * product of those state combinations. e.g:<br>
 * {@code Y ∈ Ny = {Yi,Yj,...,Yn}, B = E \ {X,Y}} <br>
 * {@code P(X,Y,B|C) = P(Xi,Yi,B|C) + P(Xi,Yj,B|C) ... + P(Xn,Yn,B|C) = p}
 *
 * <p>Valid examples:
 *
 * <ul>
 *   <li>{@code P(CLOUD:NONE,CLOUD:LIGHT) = 0.5}<br>
 *       A constraint on the sum of two states from the same node
 *   <li>{@code P(CLOUD:MEDIUM,CLOUD:HEAVY|RAIN:TRUE) = 0.9}<br>
 *       As above, but conditional on a different node's state
 *   <li>{@code P(CLOUD:NONE,CLOUD:LIGHT,SPRINKLER:TRUE|RAIN:FALSE) = 0.5}<br>
 *       As above, but with states from another node added to the evidence.
 * </ul>
 *
 * @see ProbabilityConstraint
 */
@EqualsAndHashCode(callSuper = true)
public class SumProbabilityConstraint extends ProbabilityConstraint {
  /**
   * Constructs a {@code SumProbabilityConstraint} representing a marginalised probability.
   *
   * @param eventStates The collection of {@link NodeState} values to be summed over.
   * @param conditionStates The collection of conditioning {@link NodeState} values.
   * @param probability The summed probability of the specified state combinations.
   */
  public SumProbabilityConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    super(eventStates, conditionStates, probability);
  }
}
