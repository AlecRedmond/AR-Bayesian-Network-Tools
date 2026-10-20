package io.github.alecredmond.internal.method.solver.cptmapper;

import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.application.solver.CptMappingReport;
import io.github.alecredmond.internal.method.constraints.types.conditionalconstraint.ConditionalConstraintValidator;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.MarginalConstraintValidator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.ConditionalCPTMapperIterator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.CptMapperIterator;
import io.github.alecredmond.internal.method.solver.cptmapper.iterator.RootCPTMapperIterator;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectCptMapper {
  private final ConditionalConstraintValidator conditionalValidator;
  private final MarginalConstraintValidator marginalValidator;
  private final BayesianNetworkData networkData;

  public DirectCptMapper(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.conditionalValidator = new ConditionalConstraintValidator();
    this.marginalValidator = new MarginalConstraintValidator();
  }

  public boolean tryDirectImpute() {
    List<CptMappingReport> reports = performDirectImpute();
    Collection<ProbabilityConstraint> constraints = networkData.getConstraints();
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
    return networkData.getNetworkTablesMap().values().parallelStream()
        .map(this::buildMapperIterator)
        .map(CptMapperIterator::directMapCPTs)
        .toList();
  }

  private boolean onlyConditionalAndMarginalConstraintsInNetwork() {
    return networkData.getConstraints().stream()
        .allMatch(p -> p instanceof MarginalConstraint || p instanceof ConditionalConstraint);
  }

  private String getLogString(boolean allCorrect) {
    if (allCorrect) {
      return "All CPT entries for Network %s can be inferred from the given constraints; no further solving required.%n"
          .formatted(networkData.getNetworkName());
    } else {
      return "CPT entries for Network %s cannot be directly inferred from the given constraints; IPFP run required.%n"
          .formatted(networkData.getNetworkName());
    }
  }

  private CptMapperIterator<?, ?> buildMapperIterator(NetworkTable networkTable) {
    return switch (networkTable) {
      case RootNodeTable rnt ->
          new RootCPTMapperIterator(rnt, networkData.getConstraints(), marginalValidator);
      case ConditionalTable ct ->
          new ConditionalCPTMapperIterator(ct, networkData.getConstraints(), conditionalValidator);
      default -> throw new IllegalStateException("Unexpected value: " + networkTable);
    };
  }
}
