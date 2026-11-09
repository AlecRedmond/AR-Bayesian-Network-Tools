package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTableData;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.JunctionTableSummer;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ObservationCopier;
import java.util.Collection;
import java.util.List;
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
  public void setObserved(List<NodeObservation> nodeObservations) {
    copier.observeStates(nodeObservations);
  }

  @Override
  public void resetObservations() {
    copier.resetObservations();
  }

  @Override
  public ProbabilityVector getVector() {
    return tableData;
  }

  @Override
  protected PrinterMatrixGeneratorBase buildMatrixGenerator() {
    return null;
  }

  @Override
  public ProbabilityTable copyTable() {
    return new JunctionTreeTableFactory().copyTable(this);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }
}
