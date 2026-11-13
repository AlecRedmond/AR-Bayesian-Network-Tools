package io.github.alecredmond.internal.method.solver.constraint.types.jointconstraint;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.JointProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ValidatedConstraint;
import java.util.Collection;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ValidatedJointConstraint extends JointProbabilityConstraint
    implements ValidatedConstraint<JointProbabilityConstraint> {
  /**
   * Constructs a {@code JointProbabilityConstraint} representing the intersection of multiple
   * events.
   *
   * @param eventStates A collection of concurrent {@link NodeState} values across different nodes.
   * @param conditionStates A collection of conditioning {@link NodeState} values.
   * @param probability The joint probability of the events.
   */
  public ValidatedJointConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    super(eventStates, conditionStates, probability);
  }

  @Override
  public JointProbabilityConstraint getConstraint() {
    return this;
  }
}
