package io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter;

import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.solver.constraint.ConditionalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConditionalTableConstraintSorter
    extends CptConstraintSorter<ConditionalConstraint, ConditionalTable> {

  public ConditionalTableConstraintSorter(ConditionalTable networkTable) {
    super(networkTable);
  }

  @Override
  protected Function<ProbabilityConstraint, ConditionalConstraint> buildConversionFunc() {
    return ConditionalConstraint.class::cast;
  }

  @Override
  protected Predicate<ProbabilityConstraint> buildConstraintInstanceChecker() {
    return ConditionalConstraint.class::isInstance;
  }

  @Override
  protected BiPredicate<ConditionalConstraint, ConditionalTable> buildCptEntryChecker() {
    return (conditionalConstraint, conditionalTable) ->
        eventNodesMatch(conditionalConstraint, conditionalTable)
            && conditionNodesMatch(conditionalConstraint, conditionalTable);
  }

  protected boolean eventNodesMatch(ConditionalConstraint constraint, ConditionalTable table) {
    return constraint.getEventNode().equals(table.getNetworkNode());
  }

  protected boolean conditionNodesMatch(ConditionalConstraint constraint, ConditionalTable table) {
    return constraint.getConditionNodes().equals(table.getConditions());
  }
}
