package io.github.alecredmond.internal.method.cliquegraph.externalwriters;

import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataSolver;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.Arrays;

public class SolverExternalWriter extends AbstractExternalTableWriter<GraphDataSolver> {
  public SolverExternalWriter(GraphDataSolver cliqueGraphData) {
    super(cliqueGraphData);
  }

  @Override
  public void writeToExternal() {
    NetworkTable[] tables = cliqueGraphData.getNetwork().getNetworkTables();

    for (NetworkTable table : tables) {
      Arrays.fill(table.getProbabilities(), 1.0);
    }

    for (Clique clique : cliqueGraphData.getCliques()) {
      for (TableTransfer networkTableWriter : clique.getWriteToExternal()) {
        networkTableWriter.transfer();
      }
    }

    for (NetworkTable table : tables) {
      table.normalizeTable();
    }
  }
}
