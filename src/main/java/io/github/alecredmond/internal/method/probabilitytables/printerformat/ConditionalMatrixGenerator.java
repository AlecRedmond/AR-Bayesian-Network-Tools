package io.github.alecredmond.internal.method.probabilitytables.printerformat;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import java.util.List;
import java.util.Set;

public class ConditionalMatrixGenerator extends PrinterMatrixGeneratorBase
    implements PrinterMatrixGenerator {

  private final ConditionalTable table;

  public ConditionalMatrixGenerator(ConditionalTable table) {
    this.table = table;
  }

  @Override
  protected PrinterStateMatrix buildPrinterStateMatrix() {
    List<NodeState> states = table.getNetworkNode().getStates();
    int cols = states.size();
    int rows = table.getProbabilities().length / cols;
    int conditionSize = table.getConditions().size();

    String tableName = table.getTableName().toString();
    NodeState[][] rowStates = new NodeState[rows][conditionSize];
    NodeState[][] colStates = new NodeState[1][cols];
    double[][] probabilities2D = new double[rows][cols];

    for (int col = 0; col < cols; col++) {
      colStates[0][col] = states.get(col);
    }

    int[] rowIndexAccumulator = {0};
    table.iterateOverConditions(
        cptRow ->
            fillRowStatesAndProbabilities(
                cptRow.conditionStates(),
                cptRow.rowEntries(),
                rowIndexAccumulator[0]++,
                cols,
                rowStates,
                probabilities2D));

    return new PrinterStateMatrix(tableName, colStates, rowStates, probabilities2D);
  }

  private static void fillRowStatesAndProbabilities(
      Set<NodeState> conditionStates,
      List<CptEntry> entries,
      int row,
      int cols,
      NodeState[][] rowStates,
      double[][] probabilities2D) {
    rowStates[row] = conditionStates.toArray(NodeState[]::new);
    for (int col = 0; col < cols; col++) {
      probabilities2D[row][col] = entries.get(col).probability();
    }
  }
}
