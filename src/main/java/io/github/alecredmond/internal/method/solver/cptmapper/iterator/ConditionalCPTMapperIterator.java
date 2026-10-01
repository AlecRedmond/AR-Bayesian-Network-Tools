package io.github.alecredmond.internal.method.solver.cptmapper.iterator;

import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter.ConditionalTableConstraintSorter;
import io.github.alecredmond.internal.method.utils.CollectionToString;

import java.util.*;
import java.util.stream.Collectors;

public class ConditionalCPTMapperIterator
    extends CptMapperIterator<ConditionalTable, ConditionalConstraint> {

  public ConditionalCPTMapperIterator(
      ConditionalTable networkTable,
      Collection<ProbabilityConstraint> allConstraints,
      ConditionalConstraintValidator validator) {
    super(
        networkTable,
        allConstraints,
        validator,
        new ConditionalTableConstraintSorter(networkTable));
  }

  @Override
  protected ConditionalConstraint[] buildRowConstraintsArray() {
    return new ConditionalConstraint[networkTable.getNetworkNode().getStates().size()];
  }

  @Override
  protected String getIllegalSumString(MissingEntryCheck entryCheck) {
    Set<NodeState> condition = getConditionsFromConstraints(entryCheck);
      return "A probability row for condition %s in table %s does not add to exactly 1.0! - was %.6f"
        .formatted(
                CollectionToString.apply(condition),
            networkTable.getTableName(),
            1 - entryCheck.remainder.doubleValue());
  }

  private Set<NodeState> getConditionsFromConstraints(MissingEntryCheck entryCheck) {
    return Arrays.stream(entryCheck.rowConstraints)
        .filter(Objects::nonNull)
        .findAny()
        .map(ConditionalConstraint::getConditionStates)
        .orElseThrow();
  }

  @Override
  protected ValidatedConstraint<ConditionalConstraint> buildAndValidateConstraint(
      NodeState[] missingStates, double probability) {
    NodeState eventState = missingStates[missingStates.length - 1];
    Set<NodeState> conditionStates = getConditionsFromMissing(missingStates);
    return validator.validateCPTConstraint(
        new ConditionalConstraint(eventState, conditionStates, probability));
  }

  private static Set<NodeState> getConditionsFromMissing(NodeState[] missingStates) {
    return Arrays.stream(missingStates, 0, missingStates.length - 1).collect(Collectors.toSet());
  }
}
