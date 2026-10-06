package io.github.alecredmond.internal.method.constraints.base;

import static io.github.alecredmond.internal.method.utils.DoublePrecision.*;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstraintSolverBase implements ConstraintSolver, ProbabilityIteratorTemplate {
  protected final ProbabilityConstraint constraint;
  protected final List<Double> errors = new ArrayList<>();
  protected final boolean[] outerIterationIsEvidence;
  protected final Accumulators acm = new Accumulators();
  protected final int[][] cachedIndexesPerRow;
  protected final double[] probabilities;

  public ConstraintSolverBase(ProbabilityConstraint constraint, JunctionTreeTable table) {
    this.constraint = constraint;
    StandardCartesianIterator<Node, NodeState> iterator =
        StandardCartesianIterator.create(this, table.getVector());
    this.outerIterationIsEvidence = iterator.preBuildEvidenceArray();
    this.cachedIndexesPerRow = iterator.cacheIndexesOverOuterRuns();
    this.probabilities = table.getProbabilities();
  }

  public Function<Node, NodeState> initialStatePositionSetter() {
    Map<Node, NodeState> condMap = NodeUtils.generateRequest(constraint.getConditionStates());
    return node -> condMap.containsKey(node) ? condMap.get(node) : node.getStates().getFirst();
  }

  public Predicate<Node> checkLockOuter() {
    Set<Node> events = constraint.getEventNodes();
    return node -> !events.contains(node);
  }

  public Predicate<Node> checkLockInner() {
    Set<Node> allNodes = constraint.getAllNodes();
    return allNodes::contains;
  }

  @Override
  public Function<Node, boolean[]> updateEvidenceArrays() {
    Set<Node> requestNodes = constraint.getEventNodes();
    Set<NodeState> evidenceStates = constraint.getEventStates();
    return updateEvidenceArraysCommon(requestNodes, evidenceStates, Node::getStates);
  }

  public double adjustAndReturnError() {
    acm.resetAccumulators();
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
    for (int[] cachedSubIndex : cachedIndexesPerRow) {
      double partialSum = 0;
      for (int index : cachedSubIndex) {
        partialSum += probabilities[index];
      }
      addToCorrectAccumulators(partialSum);
    }
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
    for (int[] cachedSubIndex : cachedIndexesPerRow) {
      boolean isEventPosition = outerIterationIsEvidence[acm.outerIterationIndex++];
      double ratio = isEventPosition ? ratioIfEvent : ratioOtherwise;
      for (int index : cachedSubIndex) {
        probabilities[index] *= ratio;
      }
    }
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

  protected static class Accumulators {
    protected double eventJointProb = 0;
    protected double conditionJointProb = 0;
    protected double complementJointProb = 0;
    protected int outerIterationIndex = 0;

    protected void resetAccumulators() {
      eventJointProb = 0;
      conditionJointProb = 0;
      complementJointProb = 0;
    }

    protected void resetIndex() {
      outerIterationIndex = 0;
    }
  }
}
