package io.github.alecredmond.internal.method.probabilitytables.printerformat;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.PrinterStringMatrix;
import java.util.Arrays;

public abstract class PrinterMatrixGeneratorBase implements PrinterMatrixGenerator {

  public PrinterStringMatrix generate(PrinterPropertyConfigs configs) {
    PrinterStateMatrix matrix = buildPrinterStateMatrix();
    AxisLabels rowLabels = buildRowLabels(matrix);
    AxisLabels columnLabels = buildColumnLabels(matrix, configs);
    return new PrinterStringMatrix(
        matrix.tableTitle(),
        rowLabels.labels,
        rowLabels.widths,
        columnLabels.labels,
        columnLabels.widths,
        matrix.probabilities2D);
  }

  protected abstract PrinterStateMatrix buildPrinterStateMatrix();

  private static void fillLabelArray(NodeState[][] states, int[] labelWidths, String[][] labels) {
    for (int row = 0; row < states.length; row++) {
      String[] subStrings = statesToString(states[row]);
      checkReplaceMaxWidth(subStrings, labelWidths);
      labels[row] = subStrings;
    }
  }

  private static String[] statesToString(NodeState[] states) {
    return Arrays.stream(states)
        .map(state -> state.getNode().getName() + ":" + state.getName())
        .toArray(String[]::new);
  }

  private static void checkReplaceMaxWidth(String[] subStrings, int[] rowWidths) {
    for (int j = 0; j < subStrings.length; j++) {
      String stateString = subStrings[j];
      if (stateString.length() <= rowWidths[j]) continue;
      rowWidths[j] = stateString.length();
    }
  }

  private AxisLabels buildRowLabels(PrinterStateMatrix matrix) {
    NodeState[][] rowStates = matrix.rowStates();
    String[][] labels = new String[rowStates.length][];
    if (labels.length == 0) {
      return new AxisLabels(labels, new int[0]);
    }
    int[] labelWidths = new int[rowStates[0].length];
    fillLabelArray(rowStates, labelWidths, labels);
    return new AxisLabels(labels, labelWidths);
  }

  private AxisLabels buildColumnLabels(PrinterStateMatrix matrix, PrinterPropertyConfigs configs) {
    NodeState[][] columnStates = matrix.columnStates();
    int probabilityCharLength = configs.getProbabilityCharLength();
    if (columnStates.length == 0) {
      return new AxisLabels(new String[][] {{""}}, new int[] {probabilityCharLength});
    }
    String[][] labels = new String[columnStates.length][];
    int[] labelWidths = new int[columnStates[0].length];
    Arrays.fill(labelWidths, probabilityCharLength);
    fillLabelArray(columnStates, labelWidths, labels);
    return new AxisLabels(labels, labelWidths);
  }

  protected record AxisLabels(String[][] labels, int[] widths) {}

  protected record PrinterStateMatrix(
      String tableTitle,
      NodeState[][] columnStates,
      NodeState[][] rowStates,
      double[][] probabilities2D) {}
}
