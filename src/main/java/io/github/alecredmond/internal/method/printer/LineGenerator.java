package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.TableCellMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LineGenerator<T extends ProbabilityTable> {
  protected static final String RIGHT_ALIGN_FORMAT = "%%%ds";
  protected static final String LEFT_ALIGN_FORMAT = "%%-%ds";
  protected static final String PROBABILITY_COLUMN_HEADER = "PROBABILITY";
  protected final PrinterPropertyConfigs configs;
  protected final T table;

  public LineGenerator(T table, PrinterPropertyConfigs configs) {
    this.table = table;
    this.configs = configs;
  }

  public List<String> generateLines(TableCellMatrix matrix) {
    alignCells(matrix.cells(), matrix.columnWidths());
    String internalLineBreak = createLineBreak(matrix.columnWidths());
    String boundingLineBreak = "-".repeat(internalLineBreak.length());
    return cellsToString(matrix.cells(), internalLineBreak, boundingLineBreak, matrix.title());
  }

  private void alignCells(String[][] cells, int[] columnWidths) {
    final int rows = cells.length;
    final int cols = columnWidths.length;
    String[] format = getFormatArray(columnWidths, cols);
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        cells[row][col] = format[col].formatted(cells[row][col]);
      }
    }
  }

  private String createLineBreak(int[] columnWidths) {
    StringBuilder sb = new StringBuilder("|");
    for (int width : columnWidths) {
      sb.repeat("-", width).append("|");
    }
    return sb.toString();
  }

  protected List<String> cellsToString(
      String[][] cells, String internalLineBreak, String boundingLineBreak, String title) {
    List<String> lines = insertHeaders(cells, internalLineBreak, boundingLineBreak, title);
    final int rows = cells.length;
    for (int i = 1; i < rows; i++) {
      lines.add(rowToString(cells[i]));
    }
    lines.add(boundingLineBreak);
    lines.add("");
    return lines;
  }

  private static String[] getFormatArray(int[] columnWidths, int cols) {
    final int lastCol = cols - 1;
    return IntStream.range(0, cols)
        .mapToObj(
            col ->
                col == lastCol
                    ? RIGHT_ALIGN_FORMAT.formatted(columnWidths[col])
                    : LEFT_ALIGN_FORMAT.formatted(columnWidths[col]))
        .toArray(String[]::new);
  }

  protected List<String> insertHeaders(
      String[][] cells, String internalLineBreak, String boundingLineBreak, String title) {
    List<String> lines = new ArrayList<>();
    lines.add(title);
    lines.add(boundingLineBreak);
    lines.add(rowToString(cells[0]));
    lines.add(internalLineBreak);
    return lines;
  }

  protected final String rowToString(String[] row) {
    StringBuilder sb = new StringBuilder("|");
    for (String s : row) {
      sb.append(s).append("|");
    }
    return sb.toString();
  }
}
