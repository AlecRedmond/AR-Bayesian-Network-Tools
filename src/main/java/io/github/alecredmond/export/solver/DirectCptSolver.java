package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.application.solver.CptMappingReport;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import io.github.alecredmond.internal.method.solver.SolverValidator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.ConditionalCPTMapperIterator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.CptMapperIterator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.RootCPTMapperIterator;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectCptSolver implements BayesSolver {
  private final ConditionalConstraintValidator conditionalValidator;
  private final MarginalConstraintValidator marginalValidator;
  private BayesianNetwork network;

  public DirectCptSolver() {
    this.conditionalValidator = new ConditionalConstraintValidator();
    this.marginalValidator = new MarginalConstraintValidator();
  }

  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork network) {
    this.network = network;
    new SolverValidator(network).validateDataBuilt();
    return tryDirectImpute() ? Optional.of(network) : Optional.empty();
  }

  public boolean tryDirectImpute() {
    List<CptMappingReport> reports = performDirectImpute();
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
      constraints.addAll(report.getAddedConstraints());
      allEntriesMapped = allEntriesMapped && report.isAllCptEntriesMapped();
    }
    boolean allCorrect =
        initialConstraintsSize == enteredConstraints
            && allEntriesMapped
            && onlyConditionalAndMarginalConstraintsInNetwork();

    log.info(
        "{}{}/{} rows mapped directly to CPTs, {} complement constraints added.",
        getLogString(allCorrect),
        mappedRows,
        checkedRows,
        addedConstraints);

    return allCorrect;
  }

  private List<CptMappingReport> performDirectImpute() {
    return network.getNetworkTablesMap().values().parallelStream()
        .map(this::buildMapperIterator)
        .map(CptMapperIterator::directMapCPTs)
        .toList();
  }

  private boolean onlyConditionalAndMarginalConstraintsInNetwork() {
    return network.getConstraints().stream()
        .allMatch(p -> p instanceof MarginalConstraint || p instanceof ConditionalConstraint);
  }

  private String getLogString(boolean allCorrect) {
    if (allCorrect) {
      return "All CPT entries for Network %s were inferred from the given constraints.%n"
          .formatted(network.getNetworkName());
    } else {
      return "CPT entries for Network %s cannot be directly inferred from the given constraints; IPFP run required.%n"
          .formatted(network.getNetworkName());
    }
  }

  private CptMapperIterator<?, ?> buildMapperIterator(NetworkTable networkTable) {
    return switch (networkTable) {
      case RootNodeTable rnt ->
          new RootCPTMapperIterator(rnt, network.getConstraints(), marginalValidator);
      case ConditionalTable ct ->
          new ConditionalCPTMapperIterator(ct, network.getConstraints(), conditionalValidator);
      default -> throw new IllegalStateException("Unexpected value: " + networkTable);
    };
  }
}
