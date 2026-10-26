package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.cptdefinition.CptDefinition;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import io.github.alecredmond.internal.method.utils.DoublePrecision;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class ConstraintBuilderIterator extends StateUpdateBase<Node, NodeState>
    implements ProbabilityIteratorTemplate {
  private final StandardCartesianIterator iterator;
  private final Node event;
  private final double[] probabilities;
  private final int eventNodeIdx;
  private List<ProbabilityConstraint> built;

  public ConstraintBuilderIterator(Node event, ProbabilityVector vector) {
    super(vector, NodeState[]::new);
    this.event = event;
    this.iterator = StandardCartesianIterator.create(this, vector);
    this.probabilities = vector.getProbabilities();
    this.eventNodeIdx = calculateEventNodeIndex(event, vector.getOrderedNodes());
  }

  private int calculateEventNodeIndex(Node event, Node[] orderedNodes) {
    return IntStream.range(0, orderedNodes.length)
        .filter(i -> event.equals(orderedNodes[i]))
        .findFirst()
        .orElseThrow();
  }

  public ConstraintBuilderIterator(CptDefinition cptDefinition) {
    super(cptDefinition.getVector(), NodeState[]::new);
    ProbabilityVector vector = cptDefinition.getVector();
    this.event = cptDefinition.measuredNode();
    this.iterator = StandardCartesianIterator.create(this, vector);
    this.probabilities = vector.getProbabilities();
    this.eventNodeIdx = calculateEventNodeIndex(event, vector.getOrderedNodes());
  }

  public List<ProbabilityConstraint> buildConstraints() {
    built = new ArrayList<>();
    if (states.length == 1) {
      createMarginals(probabilities, states);
    } else {
      createConditionals(probabilities, states);
    }
    return built;
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node -> node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return event::equals;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> !event.equals(node);
  }

  public Function<Node, boolean[]> updateEvidenceArrays() {
    return node -> null;
  }

  private void createMarginals(double[] probabilities, NodeState[] states) {
    List<MarginalConstraint> constraints = new ArrayList<>();
    int[] count = {0};
    iterator.iterateOuter(
        () -> {
          constraints.clear();
          count[0] = 0;
          iterator.iterateInner(
              i -> {
                count[0]++;
                constraints.add(new MarginalConstraint(states[0], probabilities[i]));
              });
          validateConstraintSums(constraints, count[0]);
          built.addAll(constraints);
        });
  }

  private void createConditionals(double[] probabilities, NodeState[] states) {
    List<ConditionalConstraint> constraints = new ArrayList<>();
    int[] count = {0};
    iterator.iterateOuter(
        () -> {
          constraints.clear();
          count[0] = 0;
          Set<NodeState> conditionStates = getConditionStates(states);
          iterator.iterateInner(
              i -> {
                count[0]++;
                double prob = probabilities[i];
                NodeState eventState = states[eventNodeIdx];
                constraints.add(new ConditionalConstraint(eventState, conditionStates, prob));
              });
          validateConstraintSums(constraints, count[0]);
          built.addAll(constraints);
        });
  }

  private void validateConstraintSums(
      List<? extends ProbabilityConstraint> constraints, int numEventStates) {
    double sum = constraints.stream().mapToDouble(ProbabilityConstraint::getProbability).sum();
    if (numEventStates == constraints.size()) {
      assertSumEqualsOne(constraints, sum);
    } else {
      assertSumWouldNotExceedOne(constraints, sum);
    }
  }

  private void assertSumEqualsOne(List<? extends ProbabilityConstraint> constraints, double sum) {
    if (DoublePrecision.fuzzyEquals(sum, 1.0)) return;
    Set<NodeState> condition = constraints.getFirst().getConditionStates();
    throw new ConstraintValidationException(
        "Constraints on [%s] the condition [%s] do not sum to 1"
            .formatted(event.getId(), CollectionToString.apply(condition)));
  }

  private void assertSumWouldNotExceedOne(
      List<? extends ProbabilityConstraint> constraints, double sum) {
    if (sum < 1.0) return;
    Set<NodeState> condition = constraints.getFirst().getConditionStates();
    throw new ConstraintValidationException(
        "Constraints on [%s] the condition [%s] would sum to > 1"
            .formatted(event.getId(), CollectionToString.apply(condition)));
  }

  private Set<NodeState> getConditionStates(NodeState[] states) {
    return IntStream.range(0, states.length)
        .filter(i -> i != eventNodeIdx)
        .mapToObj(i -> states[i])
        .collect(Collectors.toSet());
  }
}
