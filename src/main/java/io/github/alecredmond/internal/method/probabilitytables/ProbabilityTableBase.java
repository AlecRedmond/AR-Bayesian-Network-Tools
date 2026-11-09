package io.github.alecredmond.internal.method.probabilitytables;

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
import java.util.*;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ProbabilityTableBase<D extends ProbabilityTableData> {
  @EqualsAndHashCode.Include protected final D tableData;
  protected VectorIndexFinder<NodeState> indexFinder;
  protected boolean safeMode = true;

  protected ProbabilityTableBase(D table) {
    this.tableData = table;
    setSafeMode(true);
  }

  public void setSafeMode(boolean safeMode) {
    this.safeMode = safeMode;
    this.indexFinder =
        safeMode ? SafeIndexFinder.create(tableData) : FastIndexFinder.create(tableData);
  }

  public ProbabilityVector getVector() {
    return tableData;
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

  public String getTableName() {
    return tableData.getTableName();
  }

  public void setTableName(String tableName) {
    tableData.setTableName(tableName);
  }

  public double[] getProbabilities() {
    return tableData.getProbabilities();
  }

  public Double getProbability(Collection<NodeState> states) {
    try {
      int idx = indexFinder.getIndexFromStates(states);
      return tableData.getProbabilities()[idx];
    } catch (ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public NodeState[] getStatesFromIndex(int index) {
    try {
      return indexFinder.getStatesFromIndex(index);
    } catch (IndexOutOfBoundsException e) {
      log.error(e.getMessage());
      return new NodeState[0];
    }
  }

  public <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier) {
    try {
      return indexFinder.getStatesFromIndex(index, collectionSupplier);
    } catch (IndexOutOfBoundsException e) {
      log.error(e.getMessage());
      return collectionSupplier.get();
    }
  }

  public PrinterStringMatrix generatePrinterMatrix(PrinterPropertyConfigs configs) {
    return Optional.ofNullable(buildMatrixGenerator()).map(g -> g.generate(configs)).orElse(null);
  }

  protected abstract PrinterMatrixGeneratorBase buildMatrixGenerator();
}
