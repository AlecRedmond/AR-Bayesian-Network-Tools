package io.github.alecredmond.internal.method.solver.constraint.types.jointconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.solver.constraint.JointProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.solver.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.solver.constraint.base.ConstraintValidatorBase;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintValidator;

public class JointConstraintValidator
    extends ConstraintValidatorBase<JointProbabilityConstraint, ValidatedJointConstraint>
    implements ConstraintValidator<JointProbabilityConstraint, ValidatedJointConstraint> {

  public JointConstraintValidator() {
    super();
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    boolean hasMultipleEvents = data.getEventStates().size() > 1;
    if (!hasMultipleEvents) {
      throw new ConstraintValidationException("A joint constraint must have multiple events!");
    }
    boolean overDifferentNodes = data.getEventNodes().size() == data.getEventStates().size();
    if (!overDifferentNodes) {
      throw new ConstraintValidationException(
          "A joint constraint cannot have event states sharing the same node!");
    }
  }

  @Override
  protected JointProbabilityConstraint safeCastConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof JointProbabilityConstraint jpc) return jpc;
    return null;
  }

  @Override
  protected ValidatedJointConstraint validatedConstraintConstructor(
      JointProbabilityConstraint constraint) {
    return new ValidatedJointConstraint(
        constraint.getEventStates(), constraint.getConditionStates(), constraint.getProbability());
  }

  @Override
  public Class<JointProbabilityConstraint> getConstraintClass() {
    return JointProbabilityConstraint.class;
  }

  @Override
  protected JointProbabilityConstraint constructConstraint(ConstraintBuilderData data) {
    return new JointProbabilityConstraint(
        data.getEventStates(), data.getConditionStates(), data.getProbability());
  }
}
