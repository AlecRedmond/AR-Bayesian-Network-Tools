package io.github.alecredmond.internal.application.junctiontree;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
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
  private List<TableTransfer> writeToCPTs;
  private List<TableTransfer> writeToObserved;
  private int cliqueIndex;

  public Clique(Set<Node> nodes, JunctionTreeTable table) {
    this.nodes = nodes;
    this.table = table;
    this.separatorMap = new HashMap<>();
    this.writeFromCPTs = new ArrayList<>();
    this.writeToCPTs = new ArrayList<>();
    this.writeToObserved = new ArrayList<>();
  }

  public void normalizeTable() {
    table.normalizeTable();
  }

  public void resetObservations() {
    table.resetObservations();
  }

  public void setObserved(Set<NodeState> observedStates) {
    table.setObserved(observedStates);
  }

  public void eliminateStates(Set<NodeState> toEliminate) {
    table.eliminateStates(toEliminate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Clique : ");
    nodes.forEach(node -> sb.append(node.toString()).append(" "));
    return sb.toString();
  }
}
