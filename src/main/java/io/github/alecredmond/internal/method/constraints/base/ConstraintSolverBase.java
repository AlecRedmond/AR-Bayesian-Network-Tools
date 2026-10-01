package io.github.alecredmond.internal.method.constraints.base;

import static io.github.alecredmond.internal.method.utils.DoublePrecision.*;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetOnlyOnBuild;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.OdometerUpdateBlank;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstraintSolverBase
    implements OdometerResetOnlyOnBuild, OdometerUpdateBlank, ConstraintSolver {
  protected final VectorIterator<VectorOdometer> iterator;
  protected final ProbabilityConstraint constraint;
  protected final List<Double> errors = new ArrayList<>();
  protected final boolean[] outerIterationIsEvidence;
  protected final Accumulators acm = new Accumulators();

  public ConstraintSolverBase(ProbabilityConstraint constraint, JunctionTreeTable table) {
    this.constraint = constraint;
    this.iterator = new VectorIterator<>(table.getVector(), this, VectorOdometer::new);
    this.outerIterationIsEvidence = ResetLogicUtils.preBuildEvidenceCheckArray(iterator);
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    Set<Node> events = constraint.getEventNodes();
    return node -> !events.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    Set<Node> allNodes = constraint.getAllNodes();
    return allNodes::contains;
  }

  public double adjustAndReturnError() {
    acm.resetAccumulators();
    VectorOdometer vectorOdometer = iterator.getController().getOdometer();
    double[] probabilities = vectorOdometer.getProbabilities();
    calculateProbability(probabilities);

    double expectedProb = constraint.getProbability();
    double actualProb = getRatio(acm.eventJointProb, acm.conditionJointProb);

    if (fuzzyEquals(actualProb, expectedProb)) {
      return storeError(Math.pow(actualProb - expectedProb, 2));
    }

    double complementProb = getRatio(acm.complementJointProb, acm.conditionJointProb);
    double adjustmentRatio = getRatio(expectedProb, actualProb);
    double compRatio = getRatio((1 - expectedProb), complementProb);
    adjustToRatio(adjustmentRatio, compRatio, probabilities);
    return storeError(Math.pow(actualProb - expectedProb, 2));
  }

  public void updateResults(
      Map<ProbabilityConstraint, double[]> results, int lastCycle, Set<Clique> cliques) {
    int runsPerCycle = cliques.size();
    double[] errorArray = new double[lastCycle + 1];
    int cycle = 0;
    int run = 0;
    for (double error : errors) {
      errorArray[cycle] += error;
      run++;
      if (run == runsPerCycle) {
        run = 0;
        cycle++;
      }
    }
    if (constraintInMapWithHigherError(results, constraint, errorArray)) return;
    results.put(constraint, errorArray);
  }

  private boolean constraintInMapWithHigherError(
      Map<ProbabilityConstraint, double[]> results,
      ProbabilityConstraint constraint,
      double[] errorArray) {
    if (!results.containsKey(constraint)) return false;
    double previousError = Arrays.stream(results.get(constraint)).sum();
    double currentError = Arrays.stream(errorArray).sum();
    return previousError > currentError;
  }

  private void calculateProbability(double[] probabilities) {
    acm.resetIndex();
    iterator.iterateOuter(
        () -> {
          acm.partialSum = 0;
          iterator.iterateInner((o, i) -> acm.partialSum += probabilities[i]);
          addToCorrectAccumulators(acm.partialSum);
        });
  }

  protected double getRatio(double targetProb, double actualProb) {
    return actualProb == 0 ? 0.0 : targetProb / actualProb;
  }

  private double storeError(double error) {
    errors.add(error);
    return error;
  }

  protected void adjustToRatio(double ratioIfEvent, double ratioOtherwise, double[] probabilities) {
    acm.resetIndex();
    iterator.iterateOuter(
        () -> {
          boolean isEventPosition = outerIterationIsEvidence[acm.outerIterationIndex++];
          double ratio = isEventPosition ? ratioIfEvent : ratioOtherwise;
          iterator.iterateInner((o, i) -> probabilities[i] *= ratio);
        });
  }

  protected void addToCorrectAccumulators(double partialSum) {
    boolean isEventPosition = outerIterationIsEvidence[acm.outerIterationIndex++];
    acm.conditionJointProb += partialSum;
    if (isEventPosition) {
      acm.eventJointProb += partialSum;
    } else {
      acm.complementJointProb += partialSum;
    }
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    Map<Node, NodeState> condMap = NodeUtils.generateRequest(constraint.getConditionStates());
    return node -> condMap.containsKey(node) ? condMap.get(node) : node.getStates().getFirst();
  }

  @Override
  public Function<Node, boolean[]> buildEvidenceMaps() {
    Set<Node> eventNodes = constraint.getEventNodes();
    Set<NodeState> eventStates = constraint.getEventStates();
    return ResetLogicUtils.updateEvidenceArrayFunction(eventNodes, eventStates);
  }

  protected static class Accumulators {
    protected double eventJointProb = 0;
    protected double conditionJointProb = 0;
    protected double complementJointProb = 0;
    protected double partialSum = 0;
    protected int outerIterationIndex = 0;

    protected void resetAccumulators() {
      eventJointProb = 0;
      conditionJointProb = 0;
      complementJointProb = 0;
      partialSum = 0;
    }

    protected void resetIndex() {
      outerIterationIndex = 0;
    }
  }
}
