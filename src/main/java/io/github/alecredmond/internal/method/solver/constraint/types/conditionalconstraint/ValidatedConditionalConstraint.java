package io.github.alecredmond.internal.method.solver.constraint.types.conditionalconstraint;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.ConditionalConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ValidatedConstraint;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class ValidatedConditionalConstraint extends ConditionalConstraint
    implements ValidatedConstraint<ConditionalConstraint> {

  /**
   * Constructs a {@code ConditionalConstraint} for a single event state given a set of conditions.
   *
   * @param eventState The single measured {@link NodeState} {@code E}.
   * @param conditionStates The collection of conditioning {@link NodeState} values {@code C}.
   * @param probability The probability of the event given the conditions {@code p}.
   */
  public ValidatedConditionalConstraint(
      @NonNull NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    super(eventState, conditionStates, probability);
  }

  @Override
  public ConditionalConstraint getConstraint() {
    return this;
  }
}
