package io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter;

import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class RootConstraintSorter extends CptConstraintSorter<MarginalConstraint, RootNodeTable> {

  public RootConstraintSorter(RootNodeTable networkTable) {
    super(networkTable);
  }

  @Override
  protected Function<ProbabilityConstraint, MarginalConstraint> buildConversionFunc() {
    return MarginalConstraint.class::cast;
  }

  @Override
  protected Predicate<ProbabilityConstraint> buildConstraintInstanceChecker() {
    return MarginalConstraint.class::isInstance;
  }

  @Override
  protected BiPredicate<MarginalConstraint, RootNodeTable> buildCptEntryChecker() {
    return (marginalConstraint, table) ->
        marginalConstraint.getEventNode().equals(table.getNetworkNode());
  }
}
