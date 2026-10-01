package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.application.constraint.ConstraintFactoryOutput;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintValidator;
import io.github.alecredmond.internal.method.utils.CollectionToString;

import java.util.*;

public class ConstraintFactory {
  private final BayesianNetworkData networkData;
  private final ConstraintRegistry registry;

  public ConstraintFactory(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.registry = new ConstraintRegistry();
  }

  public ConstraintFactoryOutput buildConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    ConstraintBuilderData cbd =
        new ConstraintBuilderData(networkData, eventStates, conditionStates, probability);
    selectValidatorAndBuild(cbd);
    return new ConstraintFactoryOutput(cbd.getValidatedConstraint(), cbd.getException());
  }

  private void selectValidatorAndBuild(ConstraintBuilderData cbd) {
    registry
        .streamValidators()
        .filter(v -> v.checkInputsValid(cbd))
        .findFirst()
        .ifPresentOrElse(v -> v.buildFromInputs(cbd), () -> addValidatorRefusedException(cbd));
  }

  private void addValidatorRefusedException(ConstraintBuilderData cbd) {
      cbd.setException(
        new ConstraintValidationException(
            "Could not match inputs P(%s|%s) to any ProbabilityConstraint type!"
                .formatted(
                        CollectionToString.apply(cbd.getEventStates()),
                        CollectionToString.apply(cbd.getConditionStates()))));
  }

  public <T extends ProbabilityConstraint> ConstraintFactoryOutput verifyConstraint(T constraint) {
    ConstraintBuilderData cbd = new ConstraintBuilderData(networkData, constraint);
    try {
      getValidator(constraint).validateConstraint(cbd);
    } catch (ConstraintValidationException e) {
      cbd.setException(e);
    }
    return new ConstraintFactoryOutput(cbd.getValidatedConstraint(), cbd.getException());
  }

  private <T extends ProbabilityConstraint> ConstraintValidator<?, ?> getValidator(T constraint) {
    return Optional.ofNullable(registry.getValidator(constraint))
        .orElseThrow(
            () ->
                new ConstraintValidationException(
                    "NO VALIDATOR FOR CONSTRAINT %s".formatted(constraint)));
  }
}
