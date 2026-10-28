package io.github.alecredmond.internal.method.cliquegraph.externalwriters;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataSolver;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.Arrays;

public class SolverExternalWriter extends AbstractExternalTableWriter<GraphDataSolver> {
  public SolverExternalWriter(GraphDataSolver cliqueGraphData) {
    super(cliqueGraphData);
  }

  @Override
  public void writeToExternal() {
    BayesianNetwork network = cliqueGraphData.getNetwork();

    Arrays.stream(cliqueGraphData.getCliques())
        .flatMap(c -> c.getWriteToExternal().stream())
        .parallel()
        .forEach(TableTransfer::transfer);

    Arrays.stream(network.getAllNetworkTables())
        .parallel()
        .forEach(ProbabilityTable::normalizeTable);
  }
}
