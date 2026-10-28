package io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferIteratorFactory;
import java.util.Map;

public abstract class SeparatorFactory {
  protected final TransferIteratorFactory iteratorFactory = new TransferIteratorFactory();
  private final JunctionTreeTableFactory tableBuilder = new JunctionTreeTableFactory();
  private final GraphData graphData;

  protected SeparatorFactory(GraphData graphData) {
    this.graphData = graphData;
  }

  public Separator buildSeparator(Clique cliqueA, Clique cliqueB) {
    Separator separator = new Separator();
    setConnectedCliques(cliqueA, cliqueB, separator);
    setNodes(cliqueA, cliqueB, separator);
    buildTable(graphData.getNetwork(), separator);
    buildMessagePassers(cliqueA, cliqueB, separator);
    cliqueA.getSeparatorMap().put(cliqueB, separator);
    cliqueB.getSeparatorMap().put(cliqueA, separator);
    return separator;
  }

  private void setConnectedCliques(Clique cliqueA, Clique cliqueB, Separator separator) {
    separator.setConnected(Map.of(cliqueA, cliqueB, cliqueB, cliqueA));
  }

  private void setNodes(Clique cliqueA, Clique cliqueB, Separator separator) {
    separator.setNodes(TableUtils.getCommonNodes(cliqueA.getTable(), cliqueB.getTable()));
  }

  private void buildTable(BayesianNetwork network, Separator separator) {
    separator.setTable(tableBuilder.buildTable(separator.getNodes(), network));
  }

  protected void buildMessagePassers(Clique cliqueA, Clique cliqueB, Separator separator) {
    TableTransfer forward = buildTableTransfer(cliqueA, cliqueB, separator);
    TableTransfer reverse = buildTableTransfer(cliqueB, cliqueA, separator);
    separator.setMessagePassers(Map.of(cliqueA, forward, cliqueB, reverse));
  }

  protected abstract TableTransfer buildTableTransfer(
      Clique read, Clique write, Separator separator);
}
