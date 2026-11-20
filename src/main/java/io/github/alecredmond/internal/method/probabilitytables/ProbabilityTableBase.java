package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.tableentry.TableEntry;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.TableCellMatrix;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.FastIndexFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.SafeIndexFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.VectorIndexFinder;
import io.github.alecredmond.internal.method.printer.LineGenerator;
import io.github.alecredmond.internal.method.printer.PrintableTable;
import io.github.alecredmond.internal.method.printer.TableMatrixFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.TableEntryGenerator;
import java.util.*;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class ProbabilityTableBase<D extends ProbabilityTableData>
    implements ProbabilityTable, PrintableTable {
  @EqualsAndHashCode.Include protected final D tableData;
  protected VectorIndexFinder<NodeState> indexFinder;
  protected boolean safeMode = true;

  protected ProbabilityTableBase(D tableData) {
    this.tableData = tableData;
    setSafeMode(true);
  }

  public void setSafeMode(boolean safeMode) {
    this.safeMode = safeMode;
    this.indexFinder =
        safeMode ? SafeIndexFinder.create(tableData) : FastIndexFinder.create(tableData);
  }

  public TableEntry[] getEntries() {
    return new TableEntryGenerator(tableData).buildEntries();
  }

  @Override
  public List<String> generateLines(PrinterPropertyConfigs configs) {
    return new LineGenerator<>(this, configs).generateLines(buildCellMatrix(configs));
  }

  @Override
  public TableCellMatrix buildCellMatrix(PrinterPropertyConfigs configs) {
    return new TableMatrixFactory(this, configs).buildMatrix();
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

  public void setTableName(String tableName) {
    tableData.setTableName(tableName);
  }
}
