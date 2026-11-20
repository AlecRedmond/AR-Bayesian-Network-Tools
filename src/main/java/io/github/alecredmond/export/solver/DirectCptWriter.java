package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.solver.constraint.ConditionalConstraint;
import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.solver.CptMappingReport;
import io.github.alecredmond.internal.method.solver.SolverValidator;
import io.github.alecredmond.internal.method.solver.constraint.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.solver.constraint.types.marginalconstraint.MarginalConstraintValidator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.ConditionalCPTMapperIterator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.CptMapperIterator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.RootCPTMapperIterator;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link ConstraintFitter} that writes the entries of the conditional probability tables (CPTs)
 * of a {@link BayesianNetwork} directly from its constraints, without an iterative procedure.
 *
 * <p>A {@link MarginalConstraint} that describes an entry of a {@link RootNodeTable}, or a {@link
 * ConditionalConstraint} that describes an entry of a {@link ConditionalTable} in graph order
 * {@code P(X|Pa(X))}, is mapped straight to that entry. If a missing constraint can be inferred
 * from an otherwise complete row (e.g. one missing event {@code P(X=x|Pa(X)=Y)} where all other
 * values of {@code X} are defined, or {@code P(X=x|Pa(X)=Y) = 1.0} implying {@code P(X!=x|Pa(X)=Y)
 * = 0.0}), the entire row can still be mapped.
 *
 * <p>The writer succeeds only if the constraints determine every CPT entry and the network contains
 * no constraints other than {@code MarginalConstraint}s and {@code ConditionalConstraint}s.
 * Otherwise, the CPTs of the network may be left partially written. The remaining entries can then
 * be fitted by {@link IPFPSolver#solveFromCurrentValues(BayesianNetwork)}, which is how {@link
 * MultiStageSolver} uses this class.
 *
 * @author Alec Redmond
 * @see MultiStageSolver
 * @see IPFPSolver
 */
@Slf4j
public class DirectCptWriter implements ConstraintFitter {
  private final ConditionalConstraintValidator conditionalValidator;
  private final MarginalConstraintValidator marginalValidator;

  /** Constructs a new {@code DirectCptWriter}. */
  public DirectCptWriter() {
    this.conditionalValidator = new ConditionalConstraintValidator();
    this.marginalValidator = new MarginalConstraintValidator();
  }

  /**
   * {@inheritDoc}
   *
   * <p>The CPTs of the specified network are written in place, and the returned {@code Optional}
   * contains that same network. The {@code Optional} is empty if the constraints do not determine
   * every CPT, either directly or by inference.
   */
  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork network) {
    new SolverValidator(network).validateDataBuilt();
    return tryDirectImpute(network) ? Optional.of(network) : Optional.empty();
  }

  boolean tryDirectImpute(BayesianNetwork network) {
    List<CptMappingReport> reports = performDirectImpute(network);
    Collection<ProbabilityConstraint> constraints = network.getConstraints();
    int initialConstraintsSize = constraints.size();
    int enteredConstraints = 0;
    int checkedRows = 0;
    int mappedRows = 0;
    int addedConstraints = 0;
    boolean allEntriesMapped = true;
    for (CptMappingReport report : reports) {
      enteredConstraints += report.getInitialConstraints().size();
      checkedRows += report.getRowsChecked();
      mappedRows += report.getRowsMapped();
      addedConstraints += report.getAddedConstraints().size();
      allEntriesMapped = allEntriesMapped && report.isAllCptEntriesMapped();
    }
    boolean allCorrect =
        initialConstraintsSize == enteredConstraints
            && allEntriesMapped
            && onlyConditionalAndMarginalConstraintsInNetwork(network.getConstraints());

    log.info(
        "{}{}/{} rows mapped directly to CPTs, {} complement constraints added.",
        getLogString(allCorrect, network.getNetworkName()),
        mappedRows,
        checkedRows,
        addedConstraints);

    return allCorrect;
  }

  private List<CptMappingReport> performDirectImpute(BayesianNetwork network) {
    Set<ProbabilityConstraint> constraints = network.getConstraints();
    return Arrays.stream(network.getNetworkTables())
        .parallel()
        .map(networkTable -> buildMapperIterator(networkTable, constraints))
        .map(CptMapperIterator::directMapCPTs)
        .toList();
  }

  private boolean onlyConditionalAndMarginalConstraintsInNetwork(
      Set<ProbabilityConstraint> constraints) {
    return constraints.stream()
        .allMatch(p -> p instanceof MarginalConstraint || p instanceof ConditionalConstraint);
  }

  private String getLogString(boolean allCorrect, String networkName) {
    if (allCorrect) {
      return "All CPT entries for Network %s were inferred from the given constraints.%n"
          .formatted(networkName);
    } else {
      return "CPT entries for Network %s cannot be directly inferred from the given constraints; IPFP run required.%n"
          .formatted(networkName);
    }
  }

  private CptMapperIterator<?, ?> buildMapperIterator(
      NetworkTable networkTable, Set<ProbabilityConstraint> constraints) {
    return switch (networkTable) {
      case RootNodeTable rnt -> new RootCPTMapperIterator(rnt, constraints, marginalValidator);
      case ConditionalTable ct ->
          new ConditionalCPTMapperIterator(ct, constraints, conditionalValidator);
      default -> throw new IllegalStateException("Unexpected value: " + networkTable);
    };
  }
}
