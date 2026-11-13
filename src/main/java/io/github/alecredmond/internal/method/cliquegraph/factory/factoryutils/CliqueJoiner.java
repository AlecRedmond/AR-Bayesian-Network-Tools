package io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils;

import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.export.inference.direct.Separator;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory.SeparatorFactory;
import io.github.alecredmond.internal.method.cliquegraph.implementation.SeparatorImpl;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CliqueJoiner {

  /**
   * Pairs cliques together using separators to form a junction tree structure. The algorithm works
   * as follows:
   *
   * <ol>
   *   <li>A branch for every clique is initialized, containing a list of itself.
   *   <li>Potential edges are computed between cliques with shared nodes, ordered width-first.
   *   <li>Each edge in order is connected if the edge pair do not share the same branch.
   *   <li>The smaller connected branch is removed and its cliques transferred to the larger branch.
   *   <li>The algorithm completes when a single branch, containing all cliques, remains.
   * </ol>
   *
   * Assuming a group of properly triangulated cliques is input, the joined tree will not contain
   * loops and should form the optimal structure.
   */
  public void joinCliques(
      Map<Clique, CliqueData> cliqueDataMap,
      SeparatorFactory separatorFactory,
      GraphData graphData) {
    if (cliqueDataMap.size() <= 1) {
      Clique[] cliques = cliqueDataMap.keySet().toArray(Clique[]::new);
      SeparatorImpl[] separators = new SeparatorImpl[0];
      graphData.setCliques(cliques);
      graphData.setSeparators(separators);
      return;
    }
    joinCliquesWithSeparators(cliqueDataMap, separatorFactory);
    orderAndSetCliquesAndSeparators(graphData, cliqueDataMap);
  }

  private void joinCliquesWithSeparators(
      Map<Clique, CliqueData> cliqueDataMap, SeparatorFactory separatorFactory) {
    List<Clique> cliques = new ArrayList<>(cliqueDataMap.keySet());
    Map<Clique, Clique> branchRoots = new HashMap<>();
    Map<Clique, List<Clique>> branches = new HashMap<>();
    initBranches(cliques, branches, branchRoots);

    for (CliqueEdge edge : orderCandidateEdges(cliques)) {
      if (branches.size() == 1) break;
      Clique cliqueA = edge.cliqueA;
      Clique cliqueB = edge.cliqueB;

      Clique rootOfA = branchRoots.get(cliqueA);
      Clique rootOfB = branchRoots.get(cliqueB);

      if (rootOfB.equals(rootOfA)) continue;

      mergeAndRemoveShorterBranch(branches, branchRoots, rootOfA, rootOfB);
      Separator separator = separatorFactory.buildSeparator(cliqueA, cliqueB, cliqueDataMap);
      cliqueDataMap.get(cliqueA).getSeparatorMap().put(cliqueB, separator);
      cliqueDataMap.get(cliqueB).getSeparatorMap().put(cliqueA, separator);
    }
  }

  private void orderAndSetCliquesAndSeparators(
      GraphData cliqueData, Map<Clique, CliqueData> cliqueDataMap) {
    Clique smallestLeaf =
        cliqueDataMap.entrySet().stream()
            .filter(e -> e.getValue().getSeparatorMap().size() == 1)
            .min(Comparator.comparingInt(e -> e.getValue().getTable().getProbabilities().length))
            .map(Map.Entry::getKey)
            .orElseThrow();

    Queue<Clique> queue = new LinkedList<>(List.of(smallestLeaf));
    Set<Clique> finalCliques = new LinkedHashSet<>();
    Set<Separator> finalSeparators = new LinkedHashSet<>();
    int i = 0;
    while (!queue.isEmpty()) {
      Clique current = queue.poll();
      if (finalCliques.contains(current)) continue;
      cliqueDataMap.get(current).setCliqueIndex(i++);
      finalCliques.add(current);
      current
          .getSeparatorMap()
          .forEach(
              (clique, separator) -> {
                if (finalCliques.contains(clique)) return;
                finalSeparators.add(separator);
                queue.add(clique);
              });
    }
    cliqueData.setCliques(finalCliques.toArray(Clique[]::new));
    cliqueData.setSeparators(finalSeparators.toArray(Separator[]::new));
  }

  private void initBranches(
      List<Clique> cliques, Map<Clique, List<Clique>> branches, Map<Clique, Clique> branchRoots) {
    for (Clique clique : cliques) {
      branches.put(clique, new ArrayList<>(List.of(clique)));
      branchRoots.put(clique, clique);
    }
  }

  private List<CliqueEdge> orderCandidateEdges(List<Clique> cliques) {
    Set<Set<Clique>> visitedPairs = new HashSet<>();
    return findCliquesBySharedNodes(cliques).stream()
        .map(commonCliques -> buildEdgeCandidates(commonCliques, visitedPairs))
        .flatMap(Collection::stream)
        .sorted(Comparator.comparingInt(CliqueEdge::weight).reversed())
        .toList();
  }

  private void mergeAndRemoveShorterBranch(
      Map<Clique, List<Clique>> branches,
      Map<Clique, Clique> branchRoots,
      Clique rootOfA,
      Clique rootOfB) {
    List<Clique> shorterFirst = sortByBranchLengthAsc(branches, rootOfA, rootOfB);
    Clique rootOfSmaller = shorterFirst.getFirst();
    Clique rootOfLarger = shorterFirst.getLast();
    List<Clique> smallerBranch = branches.get(rootOfSmaller);
    List<Clique> largerBranch = branches.get(rootOfLarger);
    smallerBranch.forEach(
        clique -> {
          largerBranch.add(clique);
          branchRoots.put(clique, rootOfLarger);
        });
    branches.remove(rootOfSmaller);
  }

  private List<List<Clique>> findCliquesBySharedNodes(List<Clique> cliqueArray) {
    Map<Node, List<Clique>> sharedNodes = new HashMap<>();
    for (Clique clique : cliqueArray) {
      for (Node node : clique.getNodes()) {
        sharedNodes.computeIfAbsent(node, n -> new ArrayList<>()).add(clique);
      }
    }
    return sharedNodes.values().stream().filter(l -> l.size() > 1).toList();
  }

  private List<CliqueEdge> buildEdgeCandidates(
      List<Clique> sharedCliques, Set<Set<Clique>> visitedPairs) {
    List<CliqueEdge> candidates = new ArrayList<>();
    for (int i = 0; i < sharedCliques.size(); i++) {
      for (int j = i + 1; j < sharedCliques.size(); j++) {
        Clique cliqueA = sharedCliques.get(i);
        Clique cliqueB = sharedCliques.get(j);
        if (!visitedPairs.add(Set.of(cliqueA, cliqueB))) continue;
        candidates.add(new CliqueEdge(cliqueA, cliqueB, getCommonNodeCount(cliqueA, cliqueB)));
      }
    }
    return candidates;
  }

  private List<Clique> sortByBranchLengthAsc(
      Map<Clique, List<Clique>> branches, Clique rootOfA, Clique rootOfB) {
    return Stream.of(rootOfA, rootOfB)
        .sorted(Comparator.comparingInt(c -> branches.get(c).size()))
        .toList();
  }

  private int getCommonNodeCount(Clique cliqueA, Clique cliqueB) {
    return NodeUtils.getOverlap(cliqueA.getNodes(), cliqueB.getNodes()).size();
  }

  private record CliqueEdge(Clique cliqueA, Clique cliqueB, int weight) {}
}
