package io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.CliqueJoiner;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.GraphTriangulator;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.TreewidthValidator;
import io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory.SeparatorFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory.SeparatorFactoryInference;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.JunctionTreeTableFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CliqueFactoryJTAInference implements CliqueFactory {

  @Override
  public void buildCliques(GraphData graphData) {
    buildJtaCliques(graphData);
    indexCliques(graphData);
    new CliqueJoiner().joinCliques(graphData, getSeparatorFactory(graphData));
  }

  protected SeparatorFactory getSeparatorFactory(GraphData graphData) {
    return new SeparatorFactoryInference(graphData);
  }

  protected Map<Node, Set<Node>> buildEdgeGraph(GraphData graphData) {
    Node[] nodes = graphData.getNetwork().getNodes();
    Map<Node, Set<Node>> edgeGraph = initializeGraph(nodes);
    moralizeGraph(edgeGraph, nodes);
    return edgeGraph;
  }

  private void indexCliques(GraphData graphData) {
    Clique[] cliques = graphData.getCliques();
    for (int i = 0; i < cliques.length; i++) {
      cliques[i].setCliqueIndex(i);
    }
  }

  private void triangulate(Map<Node, Set<Node>> edgeGraph) {
    new GraphTriangulator<Node>()
        .getFillInEdges(edgeGraph)
        .forEach((node, fillIns) -> edgeGraph.get(node).addAll(fillIns));
  }

  private void buildJtaCliques(GraphData graphData) {
    Map<Node, Set<Node>> edgeGraph = buildEdgeGraph(graphData);
    triangulate(edgeGraph);
    Set<Set<Node>> maximalSets = findMaximalCliques(edgeGraph);
    new TreewidthValidator().verifyCliques(maximalSets, graphData);
    graphData.setCliques(buildCliqueArray(maximalSets, graphData.getNetwork()));
  }

  private Clique[] buildCliqueArray(Set<Set<Node>> maximalCliques, BayesianNetwork network) {
    JunctionTreeTableFactory tableBuilder = new JunctionTreeTableFactory();
    return maximalCliques.stream()
        .map(nodes -> new Clique(nodes, tableBuilder.buildTable(nodes, network)))
        .toArray(Clique[]::new);
  }

  private Set<Node> intersectionOf(Set<Node> setA, Set<Node> setB) {
    return NodeUtils.getOverlap(setA, setB);
  }

  private Map<Node, Set<Node>> initializeGraph(Node[] nodes) {
    Map<Node, Set<Node>> edges = new HashMap<>();
    for (Node node : nodes) {
      Set<Node> connected = new HashSet<>(node.getParents());
      connected.addAll(node.getChildren());
      edges.put(node, connected);
    }
    return edges;
  }

  private void moralizeGraph(Map<Node, Set<Node>> edges, Node[] nodes) {
    for (Node node : nodes) {
      List<Node> parents = node.getParents();
      for (int i = 0; i < parents.size(); i++) {
        for (int j = i + 1; j < parents.size(); j++) {
          Node parent1 = parents.get(i);
          Node parent2 = parents.get(j);
          edges.get(parent1).add(parent2);
          edges.get(parent2).add(parent1);
        }
      }
    }
  }

  private Set<Set<Node>> findMaximalCliques(Map<Node, Set<Node>> edges) {
    Set<Set<Node>> maximalCliques = new HashSet<>();

    List<Node> degeneracyOrdering =
        edges.entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
            .map(Map.Entry::getKey)
            .toList();

    Set<Node> pBase = new HashSet<>(degeneracyOrdering);
    Set<Node> xBase = new HashSet<>();

    for (Node node : degeneracyOrdering) {
      Set<Node> neighbours = edges.get(node);
      Set<Node> newR = new HashSet<>(Set.of(node));
      Set<Node> newP = intersectionOf(pBase, neighbours);
      Set<Node> newX = intersectionOf(xBase, neighbours);
      bronKerbosch(newR, newP, newX, edges, maximalCliques);
      pBase.remove(node);
      xBase.add(node);
    }
    return maximalCliques;
  }

  /**
   * I have used the Bron-Kerbosch algorithm with pivoting for finding all maximal cliques in a
   * graph. Further explanation of this algorithm can be found <a
   * href="https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm#With_pivoting">here.</a>
   *
   * @param currentNodes (R) The set of nodes in the clique currently being built.
   * @param candidateNodes (P) The set of candidate nodes that can be added to extend the clique.
   * @param processedNodes (X) The set of nodes already processed and cannot be used to extend the
   *     clique.
   * @param edges The graph's adjacency list.
   * @param maximalCliques The collection to store the found maximal cliques.
   */
  private void bronKerbosch(
      Set<Node> currentNodes,
      Set<Node> candidateNodes,
      Set<Node> processedNodes,
      Map<Node, Set<Node>> edges,
      Set<Set<Node>> maximalCliques) {

    if (candidateNodes.isEmpty() && processedNodes.isEmpty()) {
      maximalCliques.add(new HashSet<>(currentNodes));
      return;
    }

    Node pivot = unionOf(candidateNodes, processedNodes).iterator().next();
    Set<Node> vertexSet = new HashSet<>(candidateNodes);
    vertexSet.removeAll(edges.get(pivot));

    for (Node node : vertexSet) {
      Set<Node> neighbours = edges.get(node);
      Set<Node> newR = unionOf(currentNodes, Set.of(node));
      Set<Node> newP = intersectionOf(candidateNodes, neighbours);
      Set<Node> newX = intersectionOf(processedNodes, neighbours);
      bronKerbosch(newR, newP, newX, edges, maximalCliques);
      candidateNodes.remove(node);
      processedNodes.add(node);
    }
  }

  private Set<Node> unionOf(Set<Node> setA, Set<Node> setB) {
    return Stream.concat(setA.stream(), setB.stream())
        .collect(Collectors.toCollection(HashSet::new));
  }
}
