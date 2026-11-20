package io.github.alecredmond.internal.application.solver;

import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CptMappingReport {
  private final NetworkTable networkTable;
  private final List<ProbabilityConstraint> initialConstraints;
  private final List<ProbabilityConstraint> addedConstraints;
  private int rowsChecked;
  private int rowsMapped;
  private boolean allCptEntriesMapped;

  public <P extends ProbabilityConstraint> CptMappingReport(
      NetworkTable networkTable, List<P> constraints) {
    this.networkTable = networkTable;
    this.initialConstraints = constraints.stream().map(ProbabilityConstraint.class::cast).toList();
    this.addedConstraints = new ArrayList<>();
    this.rowsChecked = 0;
    this.rowsMapped = 0;
    this.allCptEntriesMapped = true;
  }

  public void incrementRow(boolean mapped) {
    this.rowsChecked++;
    this.allCptEntriesMapped = allCptEntriesMapped && mapped;
    if (mapped) {
      this.rowsMapped++;
    }
  }

  public <P extends ProbabilityConstraint> void addConstraint(P constraint) {
    addedConstraints.add(constraint);
  }
}
