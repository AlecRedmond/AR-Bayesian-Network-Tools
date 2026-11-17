package io.github.alecredmond.internal.method.cliquegraph.implementation;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.Clique;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.Separator;
import io.github.alecredmond.export.probabilitytables.tableentry.TableEntry;
import io.github.alecredmond.internal.application.cliquegraph.SeparatorData;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.TableCellMatrix;
import io.github.alecredmond.internal.method.printer.LineGenerator;
import io.github.alecredmond.internal.method.printer.PrintableTable;
import io.github.alecredmond.internal.method.printer.TableMatrixFactory;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SeparatorImpl implements Separator, PrintableTable {
  private final SeparatorData data;

  public SeparatorImpl(SeparatorData separatorData) {
    this.data = separatorData;
  }

  @Override
  public void passMessageFrom(Clique start) {
    data.getMessagePassers().get(start).transfer();
  }

  @Override
  public String getTableName() {
    return data.getTable().getTableName();
  }

  @Override
  public Double getProbability(Collection<NodeState> states) {
    return data.getTable().getProbability(states);
  }

  @Override
  public void normalizeTable() {
    data.getTable().normalizeTable();
  }

  @Override
  public void setSafeMode(boolean safeMode) {
    data.getTable().setSafeMode(safeMode);
  }

  @Override
  public TableEntry[] getEntries() {
    return data.getTable().getEntries();
  }

  @Override
  public void resetPotentials() {
    JunctionTreeTable table = data.getTable();
    double marginalised = 1.0 / table.getProbabilities().length;
    Arrays.fill(table.getProbabilities(), marginalised);
  }

  @Override
  public ProbabilityVector getVector() {
    return data.getTable().getVector();
  }

  @Override
  public NodeState[] getStatesFromIndex(int index) {
    return data.getTable().getStatesFromIndex(index);
  }

  @Override
  public <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier) {
    return data.getTable().getStatesFromIndex(index, collectionSupplier);
  }

  @Override
  public double sumPotentials(Collection<NodeState> states) {
    return data.getTable().sumProbabilities(states);
  }

  @Override
  public Set<Node> getNodes() {
    return data.getNodes();
  }

  @Override
  public List<String> generateLines(PrinterPropertyConfigs configs) {
    return new LineGenerator<>(this, configs).generateLines(buildCellMatrix(configs));
  }

  @Override
  public TableCellMatrix buildCellMatrix(PrinterPropertyConfigs configs) {
    return new TableMatrixFactory(this, configs).buildMatrix();
  }
}
