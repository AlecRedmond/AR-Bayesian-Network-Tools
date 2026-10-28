package io.github.alecredmond.internal.application.constraint;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Set;
import lombok.Data;

@Data
public class ConstraintBuilderData {
  private final Set<ProbabilityConstraint> allDeclaredConstraints;
  private final Set<Node> allDeclaredNodes;
  private final Set<NodeState> eventStates;
  private final Set<Node> eventNodes;
  private final Set<NodeState> conditionStates;
  private final Set<Node> conditionNodes;
  private final double probability;
  private ProbabilityConstraint constraint = null;
  private ValidatedConstraint<?> validatedConstraint = null;
  private ConstraintValidationException exception = null;

  public ConstraintBuilderData(
      Set<Node> allDeclaredNodes,
      Set<ProbabilityConstraint> allDeclaredConstraints,
      Set<NodeState> eventStates,
      Set<NodeState> conditionStates,
      double probability) {
    this.eventStates = eventStates;
    this.conditionStates = conditionStates;
    this.probability = probability;
    this.eventNodes = NodeUtils.getNodes(eventStates);
    this.conditionNodes = NodeUtils.getNodes(conditionStates);
    this.allDeclaredConstraints = allDeclaredConstraints;
    this.allDeclaredNodes = allDeclaredNodes;
  }

  public ConstraintBuilderData(
      Set<Node> allDeclaredNodes,
      Set<ProbabilityConstraint> allDeclaredConstraints,
      ProbabilityConstraint constraint) {
    this.allDeclaredConstraints = allDeclaredConstraints;
    this.allDeclaredNodes = allDeclaredNodes;
    this.constraint = constraint;
    this.eventStates = constraint.getEventStates();
    this.conditionStates = constraint.getConditionStates();
    this.probability = constraint.getProbability();
    this.eventNodes = constraint.getEventNodes();
    this.conditionNodes = constraint.getConditionNodes();
  }

  public ConstraintBuilderData(ProbabilityConstraint cptInputConstraint) {
    this.allDeclaredConstraints = null;
    this.allDeclaredNodes = null;
    this.constraint = cptInputConstraint;
    this.eventStates = constraint.getEventStates();
    this.conditionStates = constraint.getConditionStates();
    this.probability = constraint.getProbability();
    this.eventNodes = cptInputConstraint.getEventNodes();
    this.conditionNodes = cptInputConstraint.getConditionNodes();
  }
}
