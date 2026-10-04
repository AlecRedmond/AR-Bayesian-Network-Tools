package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import io.github.alecredmond.internal.method.utils.DoublePrecision;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetOnlyOnBuild;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ConstraintBuilderIterator implements OdometerResetOnlyOnBuild {
  private final VectorIterator iterator;
  private final Node event;
  private final VectorOdometer odometer;
  private List<ProbabilityConstraint> built;

  public ConstraintBuilderIterator(Node event, ProbabilityVector vector) {
    this.event = event;
    this.iterator = StandardIteratorFactory.create(this, vector);
    this.odometer = iterator.getOdometer();
  }

  public List<ProbabilityConstraint> buildConstraints() {
    built = new ArrayList<>();
    double[] probabilities = odometer.getProbabilities();
    NodeState[] states = odometer.getStates();
    if (states.length == 1) {
      createMarginals(probabilities, states);
    } else {
      createConditionals(probabilities, states);
    }
    return built;
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return ResetLogicUtils.initializeToFirstNodeStates();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return event::equals;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> !event.equals(node);
  }

  @Override
  public UpdateStateArrayLogic updateConsumerType() {
    return UpdateStateArrayLogic.WRITE_STATES_TO_ARRAY;
  }

  private void createMarginals(double[] probabilities, NodeState[] states) {
    List<MarginalConstraint> constraints = new ArrayList<>();
    int[] count = {0};
    iterator.iterateOuter(
        () -> {
          constraints.clear();
          count[0] = 0;
          iterator.iterateInner(
              (o, i) -> {
                count[0]++;
                double prob = probabilities[i];
                if (prob > 1.0 || prob < 0.0) return;
                NodeState eventState = states[0];
                constraints.add(new MarginalConstraint(eventState, prob));
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
              (o, i) -> {
                count[0]++;
                double prob = probabilities[i];
                if (prob > 1.0 || prob < 0.0) return;
                NodeState eventState = getEventState(states);
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
    return Arrays.stream(states)
        .filter(state -> !event.equals(state.getNode()))
        .collect(Collectors.toSet());
  }

  private NodeState getEventState(NodeState[] states) {
    return Arrays.stream(states)
        .filter(state -> event.equals(state.getNode()))
        .findFirst()
        .orElseThrow();
  }
}
