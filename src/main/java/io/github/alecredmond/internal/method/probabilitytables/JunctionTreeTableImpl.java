package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTableData;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.JunctionTreeTableBuilder;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.JunctionTableSummer;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ObservationCopier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class JunctionTreeTableImpl extends ProbabilityTableBase<JunctionTreeTableData>
    implements JunctionTreeTable {
  private final JunctionTableSummer summer;
  private final ObservationCopier copier;

  public JunctionTreeTableImpl(JunctionTreeTableData tableData) {
    super(tableData);
    this.summer = new JunctionTableSummer(this);
    this.copier = new ObservationCopier(this);
  }

  @Override
  public ProbabilityVector getBackupVector() {
    return tableData.getBackupVector();
  }

  @Override
  public double sumProbabilities(Collection<NodeState> states) {
    return summer.sum(states);
  }

  @Override
  public ProbabilityVector getVector() {
    return tableData.getVector();
  }

  @Override
  protected PrinterMatrixGeneratorBase buildMatrixGenerator() {
    return null;
  }

  @Override
  public ProbabilityTable copyTable() {
    return new JunctionTreeTableBuilder().copyTable(this);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }

  @Override
  public void setObserved(Set<NodeState> evidenceInTable) {
    if (!evidenceInTable.isEmpty()) {
      copier.observeStates(evidenceInTable);
      return;
    }
    double[] backup = tableData.getBackupVector().getProbabilities();
    double[] observed = tableData.getVector().getProbabilities();
    System.arraycopy(backup, 0, observed, 0, backup.length);
  }

  @Override
  public void eliminateStates(Set<NodeState> toEliminate) {
    if (!toEliminate.isEmpty()) {
      copier.eliminateStates(toEliminate);
    }
  }

  @Override
  public void resetObservations() {
    setObserved(new HashSet<>());
  }
}
