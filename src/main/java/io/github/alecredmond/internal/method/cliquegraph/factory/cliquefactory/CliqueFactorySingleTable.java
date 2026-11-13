package io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory;

import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.TreewidthValidator;
import io.github.alecredmond.internal.method.cliquegraph.implementation.CliqueImpl;
import io.github.alecredmond.internal.method.cliquegraph.implementation.SeparatorImpl;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class CliqueFactorySingleTable implements CliqueFactory {

  @Override
  public Map<Clique, CliqueData> buildCliques(GraphData graphData) {
    BayesianNetwork network = graphData.getNetwork();
    Clique[] cliques = new Clique[1];
    Set<Node> linkedNodes = network.getNodes(LinkedHashSet::new);
    new TreewidthValidator().verifyClique(linkedNodes, graphData);
    JunctionTreeTable jointTable = new JunctionTreeTableFactory().buildTable(linkedNodes, network);
    CliqueData cliqueData = new CliqueData(jointTable);
    cliqueData.setCliqueIndex(0);
    cliques[0] = new CliqueImpl(cliqueData);
    graphData.setCliques(cliques);
    graphData.setSeparators(new SeparatorImpl[0]);
    return Map.of(cliques[0], cliqueData);
  }
}
