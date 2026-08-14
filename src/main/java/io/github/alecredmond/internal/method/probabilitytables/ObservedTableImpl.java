package io.github.alecredmond.internal.method.probabilitytables;

import static io.github.alecredmond.internal.method.probabilitytables.TableUtils.buildObservedTableName;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.probabilitytables.ObservedTableData;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.UnconditionalMatrixGenerator;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ObservedTableBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ObservedTableImpl extends ProbabilityTableBase<ObservedTableData>
    implements ObservedTable {
  public ObservedTableImpl(ObservedTableData tableData) {
    super(tableData);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }

  @Override
  protected PrinterMatrixGeneratorBase buildMatrixGenerator() {
    return new UnconditionalMatrixGenerator(tableData);
  }

  @Override
  public Node getMeasuredNode() {
    return tableData.getMeasuredNode();
  }

  @Override
  public Map<Node, NodeObservation> getObservations() {
    return tableData.getObservations();
  }

  @Override
  public ObservedTable copyTable() {
    return new ObservedTableBuilder().copyTable(this);
  }

  @Override
  public Double getProbability(NodeState state) {
    return super.getProbability(List.of(state));
  }

  @Override
  public Double getProbabilityById(Serializable stateId) {
    return super.getProbabilityFromIDs(List.of(stateId));
  }

  @Override
  public Map<NodeState, Double> buildProbabilityMap() {
    return TableUtils.buildMarginalProbMap(tableData);
  }

  public void setObservations(Map<Node, NodeObservation> observationMap) {
    tableData.setObservations(observationMap);
    tableData.setTableName(buildObservedTableName(getMeasuredNode(), observationMap));
  }
}
