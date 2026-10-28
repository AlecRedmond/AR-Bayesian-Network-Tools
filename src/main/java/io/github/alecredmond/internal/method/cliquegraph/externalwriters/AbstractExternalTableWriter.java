package io.github.alecredmond.internal.method.cliquegraph.externalwriters;

import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExternalTableWriter<T extends GraphData>
    implements ExternalTableWriter {
  protected final T cliqueGraphData;

  protected AbstractExternalTableWriter(T cliqueGraphData) {
    this.cliqueGraphData = cliqueGraphData;
    initializeJunctionTreeFromNetwork();
  }

  public void initializeJunctionTreeFromNetwork() {
    for (Clique clique : cliqueGraphData.getCliques()) {
      setProbabilitiesToUnity(clique);
      multiplyInFromCPTs(clique);
      clique.normalizeTable();
    }
    backupUnobservedData();
    resetSeparators();
  }

  private void setProbabilitiesToUnity(Clique clique) {
    Arrays.fill(clique.getTable().getProbabilities(), 1.0);
  }

  private void multiplyInFromCPTs(Clique clique) {
    clique.getWriteFromCPTs().forEach(TableTransfer::transfer);
  }

  private void backupUnobservedData() {
    Arrays.stream(cliqueGraphData.getCliques())
        .map(Clique::getTable)
        .forEach(
            jtt -> {
              double[] solvedProbabilities = jtt.getVector().getProbabilities();
              double[] backup = jtt.getBackupVector().getProbabilities();
              System.arraycopy(solvedProbabilities, 0, backup, 0, backup.length);
            });
  }

  private void resetSeparators() {
    Arrays.stream(cliqueGraphData.getSeparators()).forEach(Separator::resetSeparator);
  }
}
