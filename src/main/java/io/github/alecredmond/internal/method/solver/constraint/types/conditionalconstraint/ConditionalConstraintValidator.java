package io.github.alecredmond.internal.method.solver.constraint.types.conditionalconstraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.solver.constraint.ConditionalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.solver.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.solver.constraint.base.ConstraintValidatorBase;
import io.github.alecredmond.internal.method.solver.constraint.strategy.CPTConstraintValidator;

public class ConditionalConstraintValidator
    extends ConstraintValidatorBase<ConditionalConstraint, ValidatedConditionalConstraint>
    implements CPTConstraintValidator<ConditionalConstraint, ValidatedConditionalConstraint> {

  public ConditionalConstraintValidator() {
    super();
  }

  @Override
  public ValidatedConditionalConstraint validateCPTConstraint(ConditionalConstraint constraint) {
    ConstraintBuilderData data = new ConstraintBuilderData(constraint);
    validateInputs(data);
    instanceSpecificValidation(data);
    return buildValidatedConstraint(data);
  }

  @Override
  public void validateInputs(ConstraintBuilderData data) {
    if (data.getEventStates().size() != 1) {
      throw new ConstraintValidationException("Event State Size Must Equal 1!");
    }
    if (data.getConditionStates().isEmpty()) {
      throw new ConstraintValidationException("Conditional Constraints must have conditions!");
    }
  }

  @Override
  protected ConditionalConstraint safeCastConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof ConditionalConstraint cc) return cc;
    return null;
  }

  @Override
  protected ValidatedConditionalConstraint validatedConstraintConstructor(
      ConditionalConstraint constraint) {
    return new ValidatedConditionalConstraint(
        constraint.getEventState(), constraint.getConditionStates(), constraint.getProbability());
  }

  @Override
  public Class<ConditionalConstraint> getConstraintClass() {
    return ConditionalConstraint.class;
  }

  @Override
  protected ConditionalConstraint constructConstraint(ConstraintBuilderData data) {
    return new ConditionalConstraint(
        data.getEventStates().stream().findAny().orElseThrow(),
        data.getConditionStates(),
        data.getProbability());
  }
}
