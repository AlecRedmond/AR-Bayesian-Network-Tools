package io.github.alecredmond.internal.method.cliquegraph.implementation;

import static io.github.alecredmond.export.observable.ObservationStatus.UNOBSERVED;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.inference.Separator;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import java.util.function.Supplier;

public class CliqueImpl implements Clique {
  private final CliqueData data;

  public CliqueImpl(CliqueData data) {
    this.data = data;
  }

  @Override
  public void applyPotentials(Map<Node, NodeObservation> observationMap) {
    data.getTable().setObserved(filterContains(observationMap));
  }

  private List<NodeObservation> filterContains(Map<Node, NodeObservation> observationMap) {
    return getNodes().stream()
        .map(observationMap::get)
        .filter(obs -> !obs.status().equals(UNOBSERVED))
        .toList();
  }

  @Override
  public Set<Node> getNodes() {
    return data.getNodes();
  }

  @Override
  public void resetPotentials() {
    data.getTable().resetObservations();
  }

  @Override
  public ProbabilityVector getVector() {
    return data.getTable().getVector();
  }

  @Override
  public double sumPotentials(Collection<NodeState> states) {
    return data.getTable().sumProbabilities(states);
  }

  @Override
  public NodeState[] getStatesFromIndex(int index) {
    return data.getTable().getStatesFromIndex(index);
  }

  @Override
  public <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier) {
    return data.getTable().getStatesFromIndex(index, collectionSupplier);
  }

  @Override
  public Map<Clique, Separator> getSeparatorMap() {
    return data.getSeparatorMap();
  }

  @Override
  public void writePotentialsFromNetwork() {
    Arrays.fill(data.getTable().getProbabilities(), 1.0);
    data.getWriteFromCPTs().forEach(TableTransfer::transfer);
    normalizeTable();
    copyWeightsToBackup();
  }

  @Override
  public void normalizeTable() {
    data.getTable().normalizeTable();
  }

  private void copyWeightsToBackup() {
    double[] solvedProbabilities = data.getTable().getVector().getProbabilities();
    double[] backup = data.getTable().getBackupVector().getProbabilities();
    System.arraycopy(solvedProbabilities, 0, backup, 0, backup.length);
  }

  @Override
  public void exportPotentialsToTables() {
    data.getWriteToExternal().forEach(TableTransfer::transfer);
  }

  @Override
  public int getCliqueIndex() {
    return data.getCliqueIndex();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    getNodes().forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
