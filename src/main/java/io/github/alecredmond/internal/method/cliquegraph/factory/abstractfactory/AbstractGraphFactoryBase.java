package io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactory;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferIteratorFactory;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractGraphFactoryBase<T, U extends GraphData> {
  protected final TransferIteratorFactory iteratorFactory = new TransferIteratorFactory();
  protected NetworkTable[] networkTables;
  protected U cliqueGraphData;

  public T build(BayesianNetwork network, U graphData) {
    graphData.setNetwork(network);
    this.cliqueGraphData = graphData;
    this.networkTables = network.getNetworkTables();
    commonBuildLogic();
    logBuilt();
    return constructCliqueGraph();
  }

  protected Map<Clique, CliqueData> commonBuildLogic() {
    Map<Clique, CliqueData> cliqueDataMap = getCliqueFactory().buildCliques(cliqueGraphData);
    buildWritersToExternalTables(cliqueDataMap);
    buildCollectionDistributionPlaceholders();
    return cliqueDataMap;
  }

  private void logBuilt() {
    log.info(
        "NETWORK '{}' SUCCESSFULLY BUILT IN {} CONFIGURATION; No. CLIQUES {}; EQUIVALENT TREEWIDTH = 2^{}",
        cliqueGraphData.getNetwork().getNetworkName(),
        getAlgorithmType(),
        cliqueGraphData.getCliques().length,
        "%.2f".formatted(cliqueGraphData.getEquivalentTreeWidth()));
  }

  protected abstract T constructCliqueGraph();

  protected abstract CliqueFactory getCliqueFactory();

  private void buildWritersToExternalTables(Map<Clique, CliqueData> cliqueDataMap) {
    Clique[] cliques = cliqueGraphData.getCliques();
    ProbabilityTable[] writeBackTables = getWriteBackTables();
    int length = networkTables.length;
    for (int nodeOrdinal = 0; nodeOrdinal < length; nodeOrdinal++) {
      NetworkTable multiplyInFrom = networkTables[nodeOrdinal];
      ProbabilityTable writeBackTo = writeBackTables[nodeOrdinal];
      Clique bestClique = getContainsScope(cliques, multiplyInFrom.getNodes());
      CliqueData cliqueData = cliqueDataMap.get(bestClique);
      JunctionTreeTable bestCliqueTable = cliqueData.getTable();

      cliqueData
          .getWriteFromCPTs()
          .add(iteratorFactory.buildMultiplyInTransfer(multiplyInFrom, bestCliqueTable));

      cliqueData
          .getWriteToExternal()
          .add(iteratorFactory.buildMarginalTransfer(bestCliqueTable, writeBackTo));
    }
  }

  private void buildCollectionDistributionPlaceholders() {
    int cliquesLength = cliqueGraphData.getCliques().length;
    cliqueGraphData.setDistributionRuns(new Runnable[cliquesLength][]);
    cliqueGraphData.setCollectionRuns(new Runnable[cliquesLength][]);
  }

  protected abstract String getAlgorithmType();

  protected abstract ProbabilityTable[] getWriteBackTables();

  private Clique getContainsScope(Clique[] cliques, Set<Node> nodesInScope) {
    return Arrays.stream(cliques)
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }
}
