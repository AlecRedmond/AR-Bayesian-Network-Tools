package io.github.alecredmond.internal.method.solver.constraint.types.marginalconstraint;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ValidatedConstraint;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class ValidatedMarginalConstraint extends MarginalConstraint
    implements ValidatedConstraint<MarginalConstraint> {

  /**
   * Constructs a {@code MarginalConstraint} for a single, unconditional event.
   *
   * @param eventState The single measured {@link NodeState} {@code E}.
   * @param probability The unconditional probability of the event {@code p}.
   */
  public ValidatedMarginalConstraint(@NonNull NodeState eventState, double probability) {
    super(eventState, probability);
  }

  @Override
  public MarginalConstraint getConstraint() {
    return this;
  }
}
