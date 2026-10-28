package io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.TreewidthValidator;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import java.util.LinkedHashSet;
import java.util.Set;

public class CliqueFactorySingleTable implements CliqueFactory {

  @Override
  public void buildCliques(GraphData graphData) {
    BayesianNetwork network = graphData.getNetwork();
    Clique[] cliques = new Clique[1];
    Set<Node> linkedNodes = network.getNodes(LinkedHashSet::new);
    new TreewidthValidator().verifyClique(linkedNodes, graphData);
    JunctionTreeTable jointTable = new JunctionTreeTableFactory().buildTable(linkedNodes, network);
    Clique clique = new Clique(linkedNodes, jointTable);
    clique.setCliqueIndex(0);
    cliques[0] = clique;
    graphData.setCliques(cliques);
    graphData.setSeparators(new Separator[0]);
  }
}
