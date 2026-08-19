package io.github.alecredmond.internal.method.junctiontree;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.junctiontree.Separator;
import io.github.alecredmond.internal.method.probabilitytables.ObservedTableImpl;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class JTANetworkWriter {
  private final JunctionTreeData jtd;

  public JTANetworkWriter(JunctionTreeData jtd) {
    this.jtd = jtd;
  }

  public void initializeJunctionTreeFromNetwork() {
    for (Clique clique : jtd.getCliques()) {
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
    Arrays.stream(jtd.getCliques())
        .map(Clique::getTable)
        .forEach(
            jtt -> {
              double[] solvedProbabilities = jtt.getVector().getProbabilities();
              double[] backup = jtt.getBackupVector().getProbabilities();
              System.arraycopy(solvedProbabilities, 0, backup, 0, backup.length);
            });
  }

  private void resetSeparators() {
    Arrays.stream(jtd.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeObservations() {
    Arrays.stream(jtd.getCliques())
        .map(Clique::getWriteToObserved)
        .flatMap(Collection::stream)
        .parallel()
        .forEach(TableTransfer::transfer);

    Collection<ObservedTable> observedTables = jtd.getObservedTablesMap().values();

    observedTables.parallelStream().forEach(ProbabilityTable::normalizeTable);

    Map<Node, NodeObservation> observationMap =
        Collections.unmodifiableMap(jtd.getObservedEvidence());

    observedTables.stream()
        .filter(ObservedTableImpl.class::isInstance)
        .map(ObservedTableImpl.class::cast)
        .forEach(oti -> oti.setObservations(observationMap));
  }

  public void writeBackToCPTs() {
    BayesianNetworkData bnd = jtd.getNetworkData();

    Arrays.stream(jtd.getCliques())
        .flatMap(c -> c.getWriteToCPTs().stream())
        .parallel()
        .forEach(TableTransfer::transfer);

    bnd.getNetworkTablesMap().values().parallelStream().forEach(ProbabilityTable::normalizeTable);
  }
}
