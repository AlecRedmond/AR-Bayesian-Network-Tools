package io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.application.cliquegraph.SeparatorData;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.inference.Separator;
import io.github.alecredmond.internal.method.cliquegraph.implementation.SeparatorImpl;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferIteratorFactory;
import java.util.Map;
import java.util.Set;

public abstract class AbstractSeparatorFactory implements SeparatorFactory {
  protected final TransferIteratorFactory iteratorFactory = new TransferIteratorFactory();
  private final JunctionTreeTableFactory tableBuilder = new JunctionTreeTableFactory();
  private final GraphData graphData;

  protected AbstractSeparatorFactory(GraphData graphData) {
    this.graphData = graphData;
  }

  @Override
  public Separator buildSeparator(
      Clique cliqueA, Clique cliqueB, Map<Clique, CliqueData> cliqueDataMap) {
    Map<Clique, Clique> connected = mapConnectedCliques(cliqueA, cliqueB);
    Set<Node> nodes = findCommonNodes(cliqueA, cliqueB);
    JunctionTreeTable table = buildTable(graphData.getNetwork(), nodes);
    SeparatorData separatorData = new SeparatorData(nodes, table, connected);
    Separator separator = new SeparatorImpl(separatorData);
    buildMessagePassers(cliqueA, cliqueB, separatorData, cliqueDataMap);
    return separator;
  }

  private Map<Clique, Clique> mapConnectedCliques(Clique cliqueA, Clique cliqueB) {
    return Map.of(cliqueA, cliqueB, cliqueB, cliqueA);
  }

  private Set<Node> findCommonNodes(Clique cliqueA, Clique cliqueB) {
    return NodeUtils.getOverlap(cliqueA.getNodes(), cliqueB.getNodes());
  }

  private JunctionTreeTable buildTable(BayesianNetwork network, Set<Node> nodes) {
    return tableBuilder.buildTable(nodes, network);
  }

  protected void buildMessagePassers(
      Clique cliqueA,
      Clique cliqueB,
      SeparatorData separatorData,
      Map<Clique, CliqueData> cliqueDataMap) {
    JunctionTreeTable cliqueATable = cliqueDataMap.get(cliqueA).getTable();
    JunctionTreeTable cliqueBTable = cliqueDataMap.get(cliqueB).getTable();
    JunctionTreeTable separatorTable = separatorData.getTable();
    TableTransfer forward = buildTableTransfer(cliqueATable, cliqueBTable, separatorTable);
    TableTransfer reverse = buildTableTransfer(cliqueBTable, cliqueATable, separatorTable);
    separatorData.getMessagePassers().putAll(Map.of(cliqueA, forward, cliqueB, reverse));
  }

  protected abstract TableTransfer buildTableTransfer(
      JunctionTreeTable read, JunctionTreeTable write, JunctionTreeTable separator);
}
