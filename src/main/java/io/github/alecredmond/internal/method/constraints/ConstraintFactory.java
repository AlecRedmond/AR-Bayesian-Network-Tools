package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintValidator;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;
import java.util.stream.Collectors;

public class ConstraintFactory {
  private final Set<ProbabilityConstraint> declaredConstraints;
  private final Set<Node> allNodes;
  private final ConstraintRegistry registry;

  public ConstraintFactory(Node[] nodeArray, Set<ProbabilityConstraint> declaredConstraints) {
    this.allNodes = Arrays.stream(nodeArray).collect(Collectors.toCollection(LinkedHashSet::new));
    this.declaredConstraints = declaredConstraints;
    this.registry = new ConstraintRegistry();
  }

  public ProbabilityConstraint buildConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    var cbd =
        new ConstraintBuilderData(
            allNodes, declaredConstraints, eventStates, conditionStates, probability);
    return registry
        .streamValidators()
        .filter(v -> v.checkInputsValid(cbd))
        .findFirst()
        .map(v -> v.buildFromInputs(cbd))
        .map(ValidatedConstraint::getConstraint)
        .orElseThrow(() -> validatorRefusedException(cbd));
  }

  private ConstraintValidationException validatorRefusedException(ConstraintBuilderData cbd) {
    return new ConstraintValidationException(
        "Could not match inputs P(%s|%s) to any ProbabilityConstraint type!"
            .formatted(
                CollectionToString.apply(cbd.getEventStates()),
                CollectionToString.apply(cbd.getConditionStates())));
  }

  public <T extends ProbabilityConstraint> T validateConstraint(T constraint) {
    return getValidator(constraint)
        .validateConstraint(allNodes, declaredConstraints, constraint)
        .getConstraint();
  }

  private <T extends ProbabilityConstraint> ConstraintValidator<T, ?> getValidator(T constraint) {
    return Optional.ofNullable(registry.getValidator(constraint))
        .orElseThrow(
            () ->
                new ConstraintValidationException(
                    "NO VALIDATOR FOR CONSTRAINT %s".formatted(constraint)));
  }
}
