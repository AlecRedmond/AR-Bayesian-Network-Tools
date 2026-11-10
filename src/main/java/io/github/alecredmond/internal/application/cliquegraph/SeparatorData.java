package io.github.alecredmond.internal.application.cliquegraph;

import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public class SeparatorData {
  @EqualsAndHashCode.Include private final Set<Node> nodes;
  private final JunctionTreeTable table;
  @EqualsAndHashCode.Include private final Map<Clique, Clique> connected;
  private final Map<Clique, TableTransfer> messagePassers;

  public SeparatorData(Set<Node> nodes, JunctionTreeTable table, Map<Clique, Clique> connected) {
    this.nodes = nodes;
    this.table = table;
    this.connected = connected;
    this.messagePassers = new HashMap<>();
  }
}
