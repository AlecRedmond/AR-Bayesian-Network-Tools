package io.github.alecredmond.internal.method.junctiontree.treebuilding;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.junctiontree.Separator;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferIteratorFactory;
import java.util.HashMap;
import java.util.Map;

public class SeparatorFactory {
  private final TransferIteratorFactory iteratorFactory = new TransferIteratorFactory();
  private final JunctionTreeTableFactory tableBuilder = new JunctionTreeTableFactory();
  private final JunctionTreeData jtd;

  public SeparatorFactory(JunctionTreeData jtd) {
    this.jtd = jtd;
  }

  public Separator buildSeparator(Clique cliqueA, Clique cliqueB) {
    Separator separator = new Separator();
    setConnectedCliques(cliqueA, cliqueB, separator);
    setNodes(cliqueA, cliqueB, separator);
    buildTable(jtd.getNetworkData(), separator);
    buildMessagePassers(separator, jtd, cliqueA, cliqueB);
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

  private void buildTable(BayesianNetworkData data, Separator separator) {
    separator.setTable(tableBuilder.buildTable(separator.getNodes(), data));
  }

  private void buildMessagePassers(
      Separator separator, JunctionTreeData data, Clique cliqueA, Clique cliqueB) {
    Map<Clique, TableTransfer> messagePassers =
        data.isSolverConfig()
            ? buildSolverPassers(cliqueA, cliqueB)
            : buildInferencePassers(cliqueA, cliqueB, separator);
    separator.setMessagePassers(messagePassers);
  }

  private Map<Clique, TableTransfer> buildSolverPassers(Clique cliqueA, Clique cliqueB) {
    Map<Clique, TableTransfer> map = new HashMap<>();
    map.put(cliqueA, iteratorFactory.buildMarginalTransfer(cliqueA.getTable(), cliqueB.getTable()));
    map.put(cliqueB, iteratorFactory.buildMarginalTransfer(cliqueB.getTable(), cliqueA.getTable()));
    return map;
  }

  private Map<Clique, TableTransfer> buildInferencePassers(
      Clique cliqueA, Clique cliqueB, Separator separator) {
    Map<Clique, TableTransfer> map = new HashMap<>();
    map.put(
        cliqueA,
        iteratorFactory.buildMessagePassTransfer(
            cliqueA.getTable(), cliqueB.getTable(), separator.getTable()));
    map.put(
        cliqueB,
        iteratorFactory.buildMessagePassTransfer(
            cliqueB.getTable(), cliqueA.getTable(), separator.getTable()));
    return map;
  }
}
