package io.github.alecredmond.internal.application.cliquegraph;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.inference.Separator;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.*;
import lombok.EqualsAndHashCode;

@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CliqueData {
  @EqualsAndHashCode.Include private final Set<Node> nodes;
  private final JunctionTreeTable table;
  private final Map<Clique, Separator> separatorMap;
  private final List<TableTransfer> writeFromCPTs;
  private final List<TableTransfer> writeToExternal;
  private int cliqueIndex;

  public CliqueData(JunctionTreeTable table) {
    this.nodes = table.getNodes();
    this.table = table;
    this.separatorMap = new HashMap<>();
    this.writeFromCPTs = new ArrayList<>();
    this.writeToExternal = new ArrayList<>();
  }

  public Set<Node> getNodes() {
    return this.nodes;
  }

  public Map<Clique, Separator> getSeparatorMap() {
    return this.separatorMap;
  }

  public JunctionTreeTable getTable() {
    return this.table;
  }

  public List<TableTransfer> getWriteFromCPTs() {
    return this.writeFromCPTs;
  }

  public List<TableTransfer> getWriteToExternal() {
    return this.writeToExternal;
  }

  public int getCliqueIndex() {
    return this.cliqueIndex;
  }

  public void setCliqueIndex(int cliqueIndex) {
    this.cliqueIndex = cliqueIndex;
  }
}
