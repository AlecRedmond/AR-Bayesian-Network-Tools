package io.github.alecredmond.internal.method.solver.constraint.types.sumconstraint;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.SumProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ValidatedConstraint;
import java.util.Collection;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ValidatedSumConstraint extends SumProbabilityConstraint
    implements ValidatedConstraint<SumProbabilityConstraint> {
  /**
   * Constructs a {@code SumProbabilityConstraint} representing a marginalised probability.
   *
   * @param eventStates The collection of {@link NodeState} values to be summed over.
   * @param conditionStates The collection of conditioning {@link NodeState} values.
   * @param probability The summed probability of the specified state combinations.
   */
  public ValidatedSumConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    super(eventStates, conditionStates, probability);
  }

  @Override
  public SumProbabilityConstraint getConstraint() {
    return this;
  }
}
