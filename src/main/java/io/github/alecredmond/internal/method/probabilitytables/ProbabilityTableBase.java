package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.FastIndexFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.SafeIndexFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.VectorIndexFinder;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ProbabilityTableBase<D extends ProbabilityTableData> {
  @EqualsAndHashCode.Include protected D tableData;
  protected VectorIndexFinder<NodeState> indexFinder;
  protected boolean safeMode = true;

  protected ProbabilityTableBase(D table) {
    this.tableData = table;
    setSafeMode(true);
  }

  public void setSafeMode(boolean safeMode) {
    this.safeMode = safeMode;
    this.indexFinder =
        safeMode
            ? SafeIndexFinder.create(tableData.getVector())
            : FastIndexFinder.create(tableData.getVector());
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

  public <S extends Serializable> Double getProbabilityFromIDs(Collection<S> stateIds) {
    try {
      return getProbability(getStates(stateIds));
    } catch (NodeStateConflictException | ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public Double getProbability(Collection<NodeState> states) {
    try {
      int idx = indexFinder.getIndexFromStates(states);
      return tableData.getVector().getProbabilities()[idx];
    } catch (ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  protected final <S extends Serializable> Collection<NodeState> getStates(Collection<S> stateIds) {
    Map<Serializable, NodeState> stateIdMap = tableData.getNodeStateIDMap();
    return stateIds.stream().filter(stateIdMap::containsKey).map(stateIdMap::get).toList();
  }

  public Set<NodeState> getStatesFromIndex(int index) {
    try {
      return indexFinder.getStatesFromIndex(index);
    } catch (IndexOutOfBoundsException e) {
      log.error(e.getMessage());
      return new HashSet<>();
    }
  }

  public PrinterStringMatrix generatePrinterMatrix(PrinterPropertyConfigs configs) {
    return Optional.ofNullable(buildMatrixGenerator()).map(g -> g.generate(configs)).orElse(null);
  }

  protected abstract PrinterMatrixGeneratorBase buildMatrixGenerator();
}
