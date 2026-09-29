package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintFactoryOutput;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.function.Predicate;
import lombok.NonNull;

public class NetworkConstraintHandler {
  private final BayesianNetworkData networkData;
  private final ConstraintFactory factory;

  public NetworkConstraintHandler(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.factory = new ConstraintFactory(networkData);
  }

  public List<ConstraintValidationException> addConstraints(
      Collection<ProbabilityConstraint> constraints) {
    return constraints.stream()
        .map(this::addConstraint)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  public Optional<ConstraintValidationException> addConstraint(ProbabilityConstraint constraint) {
    return verifyFromData(factory.verifyConstraint(constraint));
  }

  private Optional<ConstraintValidationException> verifyFromData(ConstraintFactoryOutput output) {
    Optional<ConstraintValidationException> e = Optional.ofNullable(output.optException());
    if (e.isPresent()) return e;
    networkData.getConstraints().add(output.validated().getConstraint());
    return Optional.empty();
  }

  public void removeCptConstraints(Node node) {
    Set<Node> eventNode = Set.of(node);
    Set<Node> conditionNodes = new HashSet<>(node.getParents());
    removeConstraints(
        constraint ->
            constraint.getEventNodes().equals(eventNode)
                && constraint.getConditionNodes().equals(conditionNodes),
        networkData);
  }

  public static boolean removeConstraints(
      Predicate<ProbabilityConstraint> predicate, BayesianNetworkData networkData) {
    return networkData.getConstraints().removeIf(predicate);
  }

  public Optional<ConstraintValidationException> addConstraints(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    List<Set<NodeState>> splitConditions = NodeUtils.splitStatesSharingNodes(conditionStates);
    List<Optional<ConstraintValidationException>> list = new ArrayList<>();
    for (Set<NodeState> condition : splitConditions) {
      list.add(addConstraint(eventStates, condition, probability));
    }
    return list.stream().findFirst().orElse(Optional.empty());
  }

  public Optional<ConstraintValidationException> addConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    return verifyFromData(factory.buildConstraint(eventStates, conditionStates, probability));
  }

  public boolean removeAllConstraints() {
    Set<ProbabilityConstraint> probabilityConstraints = networkData.getConstraints();
    if (probabilityConstraints.isEmpty()) {
      return false;
    }
    networkData.getConstraints().clear();
    return true;
  }

  public boolean removeConstraint(Set<NodeState> eventStates, Set<NodeState> conditionStates) {
    return removeConstraints(exactMatch(eventStates, conditionStates), networkData);
  }

  private Predicate<ProbabilityConstraint> exactMatch(
      Set<NodeState> eventStates, Set<NodeState> conditionStates) {
    return constraint ->
        constraint.getEventStates().equals(eventStates)
            && constraint.getConditionStates().equals(conditionStates);
  }

  public boolean removeConstraint(@NonNull ProbabilityConstraint probabilityConstraint) {
    return networkData.getConstraints().remove(probabilityConstraint);
  }

  public ProbabilityConstraint getConstraint(
      Set<NodeState> eventStates, Set<NodeState> conditionStates) {
    return networkData.getConstraints().parallelStream()
        .filter(exactMatch(eventStates, conditionStates))
        .findFirst()
        .orElse(null);
  }
}
