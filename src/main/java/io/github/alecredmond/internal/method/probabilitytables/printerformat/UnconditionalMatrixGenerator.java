package io.github.alecredmond.internal.method.probabilitytables.printerformat;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import java.util.List;

public class UnconditionalMatrixGenerator extends PrinterMatrixGeneratorBase
    implements PrinterMatrixGenerator {

  private final SingleEventTableData tableData;

  public UnconditionalMatrixGenerator(SingleEventTableData tableData) {
    this.tableData = tableData;
  }

  @Override
  protected PrinterStateMatrix buildPrinterStateMatrix() {
    Node eventNode = tableData.getEventNode();
    List<NodeState> eventStates = eventNode.getStates();
    double[] probabilities = tableData.getProbabilities();

    int rows = eventStates.size();
    int cols = 1;

    String tableName = tableData.getTableName().toString();
    NodeState[][] colStates = new NodeState[0][0];
    NodeState[][] rowStates = new NodeState[rows][cols];
    double[][] probabilities2D = new double[rows][cols];

    for (int i = 0; i < eventStates.size(); i++) {
      rowStates[i][0] = eventStates.get(i);
      probabilities2D[i][0] = probabilities[i];
    }

    return new PrinterStateMatrix(tableName, colStates, rowStates, probabilities2D);
  }
}
