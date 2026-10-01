package io.github.alecredmond.internal.method.constraints.base;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintBuilderData;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.github.alecredmond.internal.method.utils.CollectionToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ConstraintValidatorBase<
    P extends ProbabilityConstraint, V extends ValidatedConstraint<P>> {

  protected ConstraintValidatorBase() {}

  public V validateConstraint(ProbabilityConstraint constraint, BayesianNetworkData data) {
    ConstraintBuilderData cbd = new ConstraintBuilderData(data, constraint);
    return validateConstraint(cbd);
  }

  public V validateConstraint(ConstraintBuilderData data) {
    validateInputs(data);
    instanceSpecificValidation(data);
    validateForNetwork(data);
    return buildValidatedConstraint(data);
  }

  protected abstract void validateInputs(ConstraintBuilderData data)
      throws ConstraintValidationException;

  protected void instanceSpecificValidation(ConstraintBuilderData data) {
    notConditionalOnSelf(data);
    noSharedConditionNodes(data);
    probabilityWithinBounds(data);
  }

  protected void validateForNetwork(ConstraintBuilderData data) {
    statesExistInNetwork(data);
    noIdenticalConstraintsInNetwork(data);
  }

  protected V buildValidatedConstraint(ConstraintBuilderData data) {
    Optional<P> optP = Optional.ofNullable(safeCastConstraint(data.getConstraint()));
    if (optP.isPresent()) {
      V validated = validatedConstraintConstructor(optP.get());
      data.setValidatedConstraint(validated);
      return validated;
    }
    throw new ConstraintValidationException(
        "Constraint %s could not be safely cast to class %s"
            .formatted(data.getConstraint(), getConstraintClass()));
  }

  protected void notConditionalOnSelf(ConstraintBuilderData data) {
    Set<Node> eventNodes = data.getEventNodes();
    Set<Node> conditionNodes = data.getConditionNodes();
    boolean noCrossover = eventNodes.stream().noneMatch(conditionNodes::contains);
    if (noCrossover) {
      return;
    }
    throw new ConstraintValidationException(
        "%s found events conditional upon themselves!".formatted(formatOutput(data)));
  }

  protected void noSharedConditionNodes(ConstraintBuilderData data) {
    Set<NodeState> conditionStates = data.getConditionStates();
    Set<Node> conditionNodes = data.getConditionNodes();
    if (conditionStates.size() == conditionNodes.size()) {
      return;
    }
    throw new ConstraintValidationException("%s Found condition states sharing a node!");
  }

  protected void probabilityWithinBounds(ConstraintBuilderData data) {
    double probability = data.getProbability();
    if (probability < 0) {
      throw new ConstraintValidationException(
          "%s has probability < 0".formatted(formatOutput(data)));
    } else if (probability > 1) {
      throw new ConstraintValidationException(
          "%s has probability > 1".formatted(formatOutput(data)));
    }
  }

  private void statesExistInNetwork(ConstraintBuilderData data) {
    ProbabilityConstraint constraint = data.getConstraint();
    BayesianNetworkData networkData = data.getNetworkData();
    Set<NodeState> states = new HashSet<>(constraint.getAllStates());
    states.removeAll(networkData.getNodeStateIDsMap().values());
    if (states.isEmpty()) {
      return;
    }
      throw new ConstraintValidationException(
        "NodeStates %s in %s are not defined in the network!"
            .formatted(CollectionToString.apply(states), constraint));
  }

  protected void noIdenticalConstraintsInNetwork(ConstraintBuilderData data) {
    ProbabilityConstraint constraint = data.getConstraint();
    BayesianNetworkData networkData = data.getNetworkData();
    Set<NodeState> eventStates = constraint.getEventStates();
    Set<NodeState> conditionStates = constraint.getConditionStates();
    boolean parametersAreUnique =
        networkData.getConstraints().parallelStream()
            .filter(c -> eventStates.equals(c.getEventStates()))
            .noneMatch(c -> conditionStates.equals(c.getConditionStates()));
    if (parametersAreUnique) {
      return;
    }
      throw new ConstraintValidationException(
        String.format(
            "Attempted to add a probabilityConstraint C(%s | %s), which already exists!",
                CollectionToString.apply(eventStates),
                CollectionToString.apply(conditionStates)));
  }

  protected abstract P safeCastConstraint(ProbabilityConstraint constraint);

  protected abstract V validatedConstraintConstructor(P constraint);

  public abstract Class<P> getConstraintClass();

  protected static String formatOutput(ConstraintBuilderData data) {
    Set<NodeState> events = data.getEventStates();
    Set<NodeState> conditions = data.getConditionStates();
    double prob = data.getProbability();
    if (conditions.isEmpty()) {
        return "P(%s) == %.3f".formatted(CollectionToString.apply(events), prob);
    }
      return "P(%s|%s) == %.3f"
        .formatted(
                CollectionToString.apply(events),
                CollectionToString.apply(conditions),
            prob);
  }

  public boolean checkInputsValid(ConstraintBuilderData data) {
    try {
      validateInputs(data);
      return true;
    } catch (ConstraintValidationException e) {
      return false;
    }
  }

  public V buildFromInputs(ConstraintBuilderData data) {
    instanceSpecificValidation(data);
    data.setConstraint(constructConstraint(data));
    validateForNetwork(data);
    return buildValidatedConstraint(data);
  }

  protected abstract P constructConstraint(ConstraintBuilderData data);
}
