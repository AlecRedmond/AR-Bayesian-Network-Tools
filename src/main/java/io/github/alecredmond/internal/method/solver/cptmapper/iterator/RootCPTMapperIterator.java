package io.github.alecredmond.internal.method.solver.cptmapper.iterator;

import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter.RootConstraintSorter;
import java.util.*;

public class RootCPTMapperIterator extends CptMapperIterator<RootNodeTable, MarginalConstraint> {

  public RootCPTMapperIterator(
      RootNodeTable networkTable,
      Collection<ProbabilityConstraint> allConstraints,
      MarginalConstraintValidator validator) {
    super(networkTable, allConstraints, validator, new RootConstraintSorter(networkTable));
  }

  @Override
  protected MarginalConstraint[] buildRowConstraintsArray() {
    return new MarginalConstraint[networkTable.getNetworkNode().getStates().size()];
  }

  @Override
  protected String getIllegalSumString(MissingEntryCheck entryCheck) {
    return "Probabilities over table %s did not sum to 1.0! - was %.6f"
        .formatted(networkTable.getTableName(), 1.0 - entryCheck.remainder.doubleValue());
  }

  @Override
  protected ValidatedConstraint<MarginalConstraint> buildAndValidateConstraint(
      NodeState[] missingStates, double probability) {
    return validator.validateCPTConstraint(new MarginalConstraint(missingStates[0], probability));
  }
}
