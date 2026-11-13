package io.github.alecredmond.internal.method.solver.constraint.types.marginalconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.solver.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.solver.constraint.base.ConstraintValidatorBase;
import io.github.alecredmond.internal.method.solver.constraint.strategy.CPTConstraintValidator;

public class MarginalConstraintValidator
    extends ConstraintValidatorBase<MarginalConstraint, ValidatedMarginalConstraint>
    implements CPTConstraintValidator<MarginalConstraint, ValidatedMarginalConstraint> {

  public MarginalConstraintValidator() {
    super();
  }

  @Override
  public ValidatedMarginalConstraint validateCPTConstraint(MarginalConstraint constraint) {
    ConstraintBuilderData data = new ConstraintBuilderData(constraint);
    validateInputs(data);
    instanceSpecificValidation(data);
    return buildValidatedConstraint(data);
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    boolean noConditions = data.getConditionStates().isEmpty();
    if (!noConditions) {
      throw new ConstraintValidationException("Marginal Constraints must not have conditions!");
    }
    boolean oneEventState = data.getEventStates().size() == 1;
    if (!oneEventState) {
      throw new ConstraintValidationException(
          "Marginal Constraints must have only one event state!");
    }
  }

  @Override
  protected MarginalConstraint safeCastConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof MarginalConstraint mc) return mc;
    return null;
  }

  @Override
  protected ValidatedMarginalConstraint validatedConstraintConstructor(
      MarginalConstraint constraint) {
    return new ValidatedMarginalConstraint(constraint.getEventState(), constraint.getProbability());
  }

  @Override
  public Class<MarginalConstraint> getConstraintClass() {
    return MarginalConstraint.class;
  }

  @Override
  protected MarginalConstraint constructConstraint(ConstraintBuilderData data) {
    return new MarginalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(), data.getProbability());
  }
}
