package io.github.alecredmond.internal.method.solver.cptmapper.iterator;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.application.solver.CptMappingReport;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.constraints.strategy.CPTConstraintValidator;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter.CptConstraintSorter;
import io.github.alecredmond.internal.method.utils.DoublePrecision;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetOnlyOnBuild;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class CptMapperIterator<T extends NetworkTable, P extends ProbabilityConstraint>
    implements OdometerResetOnlyOnBuild {
  protected final T networkTable;
  protected final List<P> constraints;
  protected final VectorIterator iterator;
  protected final CPTConstraintValidator<P, ?> validator;
  protected final CptMappingReport report;

  protected CptMapperIterator(
      T networkTable,
      Collection<ProbabilityConstraint> allConstraints,
      CPTConstraintValidator<P, ?> validator,
      CptConstraintSorter<P, T> sorter) {
    this.networkTable = networkTable;
    this.validator = validator;
    this.constraints = sorter.sortConstraints(allConstraints);
    this.iterator = StandardIteratorFactory.create(this,networkTable.getVector());
    this.report = new CptMappingReport(networkTable, constraints);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return ResetLogicUtils.initializeToFirstNodeStates();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return networkTable.getEvents()::contains;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return networkTable.getConditions()::contains;
  }

  @Override
  public UpdateStateArrayLogic updateConsumerType() {
    return UpdateStateArrayLogic.WRITE_STATES_TO_ARRAY;
  }

  public CptMappingReport directMapCPTs() {
    MissingEntryCheck entryCheck = new MissingEntryCheck(constraints, buildRowConstraintsArray());
    List<P> addedConstraints = new ArrayList<>();
    iterator.iterateOuter(
        (odom, rowStartIndex) -> {
          entryCheck.setNewRow(rowStartIndex);
          iterator.iterateInner((o, i) -> checkRowEntry(o, i, entryCheck));
          boolean canBeMapped = validateRowAndBuildMissing(entryCheck, addedConstraints, odom);
          report.incrementRow(canBeMapped);
        });
    addedConstraints.forEach(report::addConstraint);
    return report;
  }

  protected abstract P[] buildRowConstraintsArray();

  private void checkRowEntry(VectorOdometer odometer, int index, MissingEntryCheck entryCheck) {
    NodeState[] states = odometer.getStates();
    Optional<P> constraintOpt = getNextConstraintIfEntryMatches(entryCheck, states);
    if (constraintOpt.isPresent()) {
      P constraint = constraintOpt.get();
      double probability = constraint.getProbability();
      entryCheck.remainder = entryCheck.remainder.subtract(BigDecimal.valueOf(probability));
      entryCheck.addConstraint(constraint, index);
    } else {
      entryCheck.addMissingConstraint(states, index);
    }
  }

  private boolean validateRowAndBuildMissing(
      MissingEntryCheck entryCheck, List<P> addedConstraints, VectorOdometer odometer) {
    return switch (entryCheck.missingRowIndexes.size()) {
      case 0 -> directMapFullRow(entryCheck, odometer);
      case 1 -> directMapWithOneMissing(entryCheck, addedConstraints, odometer);
      default -> directMapOnlyIfRemainderIsZero(entryCheck, addedConstraints, odometer);
    };
  }

  private Optional<P> getNextConstraintIfEntryMatches(
      MissingEntryCheck entryCheck, NodeState[] states) {
    return Optional.ofNullable(entryCheck.constraintQueue.peek())
        .filter(c -> Arrays.stream(states).allMatch(c.getAllStates()::contains))
        .map(c -> entryCheck.constraintQueue.poll());
  }

  private boolean directMapFullRow(MissingEntryCheck entryCheck, VectorOdometer odometer) {
    if (!DoublePrecision.fuzzyEquals(entryCheck.remainder.doubleValue(), 0)) {
      throw new IllegalStateException(getIllegalSumString(entryCheck));
    }
    return writeFullConstraintRow(entryCheck, odometer);
  }

  private boolean directMapWithOneMissing(
      MissingEntryCheck entryCheck, List<P> addedConstraints, VectorOdometer odometer) {
    addedConstraints.add(validateAndInsertMissing(entryCheck));
    return writeFullConstraintRow(entryCheck, odometer);
  }

  private boolean directMapOnlyIfRemainderIsZero(
      MissingEntryCheck entryCheck, List<P> addedConstraints, VectorOdometer odometer) {
    if (!DoublePrecision.fuzzyEquals(entryCheck.remainder.doubleValue(), 0.0)) {
      return writeNormalizedConstraintRow(entryCheck, odometer);
    }
    addedConstraints.addAll(addZeroProbabilityConstraints(entryCheck));
    return writeFullConstraintRow(entryCheck, odometer);
  }

  protected abstract String getIllegalSumString(MissingEntryCheck entryCheck);

  private boolean writeFullConstraintRow(MissingEntryCheck entryCheck, VectorOdometer odometer) {
    double[] probabilities = odometer.getProbabilities();
    P[] rowConstraints = entryCheck.rowConstraints;
    for (int i = 0; i < rowConstraints.length; i++) {
      probabilities[i + entryCheck.rowStartIndex] = rowConstraints[i].getProbability();
    }
    return true;
  }

  protected P validateAndInsertMissing(MissingEntryCheck entryCheck) {
    double probability = entryCheck.remainder.doubleValue();
    int missingRowIndex = entryCheck.missingRowIndexes.getFirst();
    NodeState[] missingStates = entryCheck.statesMissingConstraints[missingRowIndex];
    P constraint = buildAndValidateConstraint(missingStates, probability).getConstraint();
    entryCheck.rowConstraints[missingRowIndex] = constraint;
    return constraint;
  }

  private boolean writeNormalizedConstraintRow(
      MissingEntryCheck entryCheck, VectorOdometer odometer) {
    double[] probabilities = odometer.getProbabilities();
    P[] rowConstraints = entryCheck.rowConstraints;
    double normalizedRemainder = getNormalizedRemainder(entryCheck);
    for (int i = 0; i < rowConstraints.length; i++) {
      if (rowConstraints[i] != null) {
        probabilities[i + entryCheck.rowStartIndex] = rowConstraints[i].getProbability();
      } else {
        probabilities[i + entryCheck.rowStartIndex] = normalizedRemainder;
      }
    }
    return DoublePrecision.fuzzyEquals(normalizedRemainder, 0.0);
  }

  protected Collection<P> addZeroProbabilityConstraints(MissingEntryCheck entryCheck) {
    List<P> added = new ArrayList<>();
    for (int missingRowIndex : entryCheck.missingRowIndexes) {
      NodeState[] missingStates = entryCheck.statesMissingConstraints[missingRowIndex];
      P constraint = buildAndValidateConstraint(missingStates, 0.0).getConstraint();
      entryCheck.rowConstraints[missingRowIndex] = constraint;
      added.add(constraint);
    }
    return added;
  }

  protected abstract ValidatedConstraint<P> buildAndValidateConstraint(
      NodeState[] missingStates, double probability);

  private double getNormalizedRemainder(MissingEntryCheck entryCheck) {
    return entryCheck.remainder.doubleValue() / entryCheck.missingRowIndexes.size();
  }

  protected class MissingEntryCheck {
    protected final Queue<P> constraintQueue;
    protected BigDecimal remainder;
    protected int rowStartIndex;
    protected P[] rowConstraints;
    protected NodeState[][] statesMissingConstraints;
    protected List<Integer> missingRowIndexes;

    protected MissingEntryCheck(List<P> constraints, P[] rowConstraintsArray) {
      constraintQueue = new ArrayDeque<>(constraints);
      rowConstraints = rowConstraintsArray;
      statesMissingConstraints = new NodeState[rowConstraints.length][];
      missingRowIndexes = new ArrayList<>();
    }

    public void addConstraint(P constraint, int index) {
      rowConstraints[index - rowStartIndex] = constraint;
    }

    public void addMissingConstraint(NodeState[] states, int index) {
      int rowIndex = index - rowStartIndex;
      statesMissingConstraints[rowIndex] = Arrays.copyOf(states, states.length);
      missingRowIndexes.add(rowIndex);
    }

    protected void setNewRow(int rowStartIndex) {
      this.rowStartIndex = rowStartIndex;
      this.remainder = BigDecimal.ONE;
      this.missingRowIndexes.clear();
      Arrays.fill(rowConstraints, null);
      Arrays.fill(statesMissingConstraints, null);
    }
  }
}
