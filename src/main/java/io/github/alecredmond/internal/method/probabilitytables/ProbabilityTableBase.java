package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ProbabilityTableBase<D extends ProbabilityTableData> {
  @EqualsAndHashCode.Include protected D tableData;
  protected boolean safeMode = true;

  protected ProbabilityTableBase(D table) {
    this.tableData = table;
  }

  public Map<Serializable, NodeState> getNodeStateIDMap() {
    return tableData.getNodeStateIDMap();
  }

  public Map<Serializable, Node> getNodeIDMap() {
    return tableData.getNodeIDMap();
  }

  public ProbabilityVector getVector() {
    return tableData.getVector();
  }

  public Set<Node> getNodes() {
    return tableData.getNodes();
  }

  public Set<Node> getEvents() {
    return tableData.getEvents();
  }

  public Set<Node> getConditions() {
    return tableData.getConditions();
  }

  public Serializable getTableName() {
    return tableData.getTableName();
  }

  public void setTableName(Serializable tableName) {
    tableData.setTableName(tableName);
  }

  public double[] getProbabilities() {
    return tableData.getProbabilities();
  }

  public Double getProbability(Collection<NodeState> states) {
    try {
      Collection<NodeState> verified =
          safeMode ? TableUtils.assertAllNodesPresent(states, tableData.getNodes()) : states;
      return TableUtils.getProbability(verified, tableData);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public <S extends Serializable> Double getProbabilityFromIDs(Collection<S> stateIds) {
    try {
      return TableUtils.getProbability(getStates(stateIds, tableData.getNodes()), tableData);
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  protected <S extends Serializable> Collection<NodeState> getStates(
      Collection<S> stateIds, Set<Node> expectedNodes) {
    return safeMode
        ? TableUtils.assertAllIdsPresent(stateIds, expectedNodes, tableData)
        : TableUtils.convertIdsToStates(stateIds, tableData);
  }

  public PrinterStringMatrix generatePrinterMatrix(PrinterPropertyConfigs configs) {
    return Optional.ofNullable(buildMatrixGenerator()).map(g -> g.generate(configs)).orElse(null);
  }

  protected abstract PrinterMatrixGeneratorBase buildMatrixGenerator();
}
