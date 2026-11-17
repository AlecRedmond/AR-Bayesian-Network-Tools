package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.tableentry.TableEntry;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.TableCellMatrix;
import lombok.NonNull;

public class TableMatrixFactory {
  private final ProbabilityTable table;
  private final PrinterPropertyConfigs configs;

  public TableMatrixFactory(ProbabilityTable table, PrinterPropertyConfigs configs) {
    this.table = table;
    this.configs = configs;
  }

  public TableCellMatrix buildMatrix() {
    Node[] orderedNodes = table.getVector().getOrderedNodes();
    TableEntry[] entries = table.getEntries();
    String tableName = table.getTableName();
    int numEntries = entries.length;
    if (entries.length == 0) return null;
    int stateLength = orderedNodes.length;
    String[][] cells = new String[numEntries + 1][stateLength + 1];
    insertHeader(orderedNodes, cells);
    fillEntries(entries, cells, configs.getProbabilityFormatter());
    int[] maxCellWidths = new int[stateLength + 1];
    fillColumnWidths(orderedNodes, maxCellWidths, configs.getProbabilityCharLength());
    return new TableCellMatrix(cells, tableName, maxCellWidths);
  }

  private void insertHeader(Node[] orderedNodes, String[][] strings) {
    int row = 0;
    int col = 0;
    for (Node node : orderedNodes) {
      strings[row][col++] = node.getName();
    }
    strings[row][col] = LineGenerator.PROBABILITY_COLUMN_HEADER;
  }

  private void fillEntries(@NonNull TableEntry[] entries, String[][] cells, String pFormatter) {
    int row = 1;
    for (TableEntry entry : entries) {
      int col = 0;
      for (NodeState state : entry.states()) {
        cells[row][col++] = state.getName();
      }
      cells[row++][col] = pFormatter.formatted(entry.probability());
    }
  }

  private void fillColumnWidths(Node[] orderedNodes, int[] maxCellWidths, int pCharLength) {
    int col = 0;
    for (Node node : orderedNodes) {
      int max = node.getName().length();
      for (NodeState state : node.getStates()) {
        int stateNameLength = state.getName().length();
        if (stateNameLength > max) max = stateNameLength;
      }
      maxCellWidths[col++] = max;
    }
    maxCellWidths[col] = Math.max(pCharLength, LineGenerator.PROBABILITY_COLUMN_HEADER.length());
  }
}
