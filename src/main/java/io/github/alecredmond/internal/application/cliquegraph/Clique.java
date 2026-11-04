package io.github.alecredmond.internal.application.cliquegraph;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Clique {
  @EqualsAndHashCode.Include private final Set<Node> nodes;
  private final JunctionTreeTable table;
  private Map<Clique, Separator> separatorMap;
  private List<TableTransfer> writeFromCPTs;
  private List<TableTransfer> writeToExternal;
  @EqualsAndHashCode.Include private int cliqueIndex;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.separatorMap = new HashMap<>();
    this.writeFromCPTs = new ArrayList<>();
    this.writeToExternal = new ArrayList<>();
  }

  public void normalizeTable() {
    table.normalizeTable();
  }

  public void resetObservations() {
    table.resetObservations();
  }

  public void applyState(Map<Node, NodeObservation> finalInferenceState) {
    table.setObserved(filterContains(finalInferenceState));
  }

  private NodeObservation[] filterContains(Map<Node, NodeObservation> observationMap) {
    return observationMap.entrySet().stream()
        .filter(e -> nodes.contains(e.getKey()))
        .map(Map.Entry::getValue)
        .toArray(NodeObservation[]::new);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
