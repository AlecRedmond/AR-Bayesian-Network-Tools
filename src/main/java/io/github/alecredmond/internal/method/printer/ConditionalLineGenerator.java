package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import java.util.List;

public class ConditionalLineGenerator extends LineGenerator<ConditionalTable> {

  public ConditionalLineGenerator(ConditionalTable table, PrinterPropertyConfigs configs) {
    super(table, configs);
  }

  @Override
  protected List<String> cellsToString(
      String[][] cells, String internalLineBreak, String boundingLineBreak, String title) {
    final int eventLength = table.getNetworkNode().getStates().size();
    List<String> lines = insertHeaders(cells, internalLineBreak, boundingLineBreak, title);
    final int rows = cells.length;
    int eventNum = 0;
    for (int i = 1; i < rows; i++) {
      lines.add(rowToString(cells[i]));
      if (++eventNum < eventLength) continue;
      eventNum = 0;
      lines.add(internalLineBreak);
    }
    lines.removeLast();
    lines.add(boundingLineBreak);
    lines.add("");
    return lines;
  }
}
