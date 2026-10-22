package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.application.probabilitytables.RootNodeTableData;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.UnconditionalMatrixGenerator;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.RootNodeTableFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.CptConditionIterator;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RootNodeTableImpl extends NetworkTableBase<RootNodeTableData>
    implements RootNodeTable {

  public RootNodeTableImpl(RootNodeTableData table) {
    super(table);
  }

  @Override
  public void normalizeTable() {
    TableUtils.marginalizeJointTable(this);
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> condition) {
    return buildProbabilityMap();
  }

  @Override
  public <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs) {
    return buildProbabilityMap();
  }

  @Override
  public Map<NodeState, Double> buildProbabilityMap() {
    return TableUtils.buildMarginalProbMap(tableData);
  }

  @Override
  public RootNodeTable copyTable() {
    return new RootNodeTableFactory().copyTable(this);
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
  protected PrinterMatrixGeneratorBase buildMatrixGenerator() {
    return new UnconditionalMatrixGenerator(tableData);
  }

  @Override
  protected CptConditionIterator supplyConditionIterator() {
    return new CptConditionIterator(this);
  }
}
