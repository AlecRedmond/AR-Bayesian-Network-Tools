package io.github.alecredmond.internal.method.junctiontree.treebuilding;

import static io.github.alecredmond.export.inference.InferenceAlgorithm.JUNCTION_TREE_ALGORITHM;
import static io.github.alecredmond.export.solver.SolverAlgorithm.JUNCTION_TREE_IPFP;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.JunctionTreeTableBuilder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CliqueBuilder {

  public void buildCliques(JunctionTreeData jtd) {
    if (checkUseJta(jtd)) buildJtaCliques(jtd);
    else buildIPFPClique(jtd);
    indexCliques(jtd);
    new CliqueJoiner().joinCliques(jtd);
  }

  private void indexCliques(JunctionTreeData jtd) {
    Clique[] cliques = jtd.getCliques();
    for (int i = 0; i < cliques.length; i++) {
      cliques[i].setCliqueIndex(i);
    }
  }

  private void triangulate(Map<Node, Set<Node>> edgeGraph) {
    new GraphTriangulator<Node>()
        .getFillInEdges(edgeGraph)
        .forEach((node, fillIns) -> edgeGraph.get(node).addAll(fillIns));
  }

  private boolean checkUseJta(JunctionTreeData jtd) {
    return jtd.isSolverConfig()
        ? jtd.getSolverAlgorithm().equals(JUNCTION_TREE_IPFP)
        : jtd.getInferenceAlgorithm().equals(JUNCTION_TREE_ALGORITHM);
  }

  private void buildIPFPClique(JunctionTreeData jtd) {
    BayesianNetworkData bnd = jtd.getNetworkData();
    Clique[] cliques = new Clique[1];
    Set<Node> linkedNodes = new LinkedHashSet<>(bnd.getNodes());
    new TreewidthValidator().verifyClique(linkedNodes, jtd);
    JunctionTreeTable jointTable = new JunctionTreeTableBuilder().buildTable(linkedNodes, bnd);
    cliques[0] = new Clique(linkedNodes, jointTable);
    jtd.setCliques(cliques);
  }

  private void buildJtaCliques(JunctionTreeData jtd) {
    BayesianNetworkData bnd = jtd.getNetworkData();
    Map<Node, Set<Node>> edgeGraph = initializeGraph(bnd);
    moralizeGraph(edgeGraph, bnd);
    triangulate(edgeGraph);
    Set<Set<Node>> maximalSets = findMaximalCliques(edgeGraph);
    new TreewidthValidator().verifyCliques(maximalSets, jtd);
    jtd.setCliques(buildCliqueArray(maximalSets, bnd));
  }

  private Clique[] buildCliqueArray(Set<Set<Node>> maximalCliques, BayesianNetworkData bnd) {
    JunctionTreeTableBuilder tableBuilder = new JunctionTreeTableBuilder();
    return maximalCliques.stream()
        .map(nodes -> new Clique(nodes, tableBuilder.buildTable(nodes, bnd)))
        .toArray(Clique[]::new);
  }

  private Set<Node> intersectionOf(Set<Node> setA, Set<Node> setB) {
    return NodeUtils.getOverlap(setA, setB);
  }

  private Map<Node, Set<Node>> initializeGraph(BayesianNetworkData data) {
    Map<Node, Set<Node>> edges = new HashMap<>();
    for (Node node : data.getNodes()) {
      Set<Node> connected = new HashSet<>(node.getParents());
      connected.addAll(node.getChildren());
      addConstraintsIfUnsolved(data, node, connected);
      edges.put(node, connected);
    }
    return edges;
  }

  private void addConstraintsIfUnsolved(BayesianNetworkData data, Node node, Set<Node> connected) {
    if (data.isSolved()) return;
    data.getConstraints().stream()
        .map(ProbabilityConstraint::getAllNodes)
        .filter(allNodes -> allNodes.contains(node))
        .flatMap(Collection::stream)
        .distinct()
        .filter(n -> !node.equals(n))
        .forEach(connected::add);
  }

  private void moralizeGraph(Map<Node, Set<Node>> edges, BayesianNetworkData data) {
    for (Node node : data.getNodes()) {
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
