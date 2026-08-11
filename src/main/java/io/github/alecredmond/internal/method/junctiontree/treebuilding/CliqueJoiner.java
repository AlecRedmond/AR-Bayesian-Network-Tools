package io.github.alecredmond.internal.method.junctiontree.treebuilding;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.junctiontree.Separator;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
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
  public void joinCliques(JunctionTreeData jtd) {
    SeparatorFactory separatorFactory = new SeparatorFactory(jtd);
    Clique[] cliques = jtd.getCliques();

    if (cliques.length <= 1) {
      jtd.setSeparators(new Separator[0]);
      return;
    }

    Map<Clique, Clique> branchRoots = new HashMap<>();
    Map<Clique, List<Clique>> branches = new HashMap<>();
    List<Separator> finalSeparators = new ArrayList<>();
    initBranches(cliques, branches, branchRoots);

    for (CliqueEdge edge : orderCandidateEdges(cliques)) {
      if (branches.size() == 1) break;
      Clique cliqueA = edge.cliqueA;
      Clique cliqueB = edge.cliqueB;

      Clique rootOfA = branchRoots.get(cliqueA);
      Clique rootOfB = branchRoots.get(cliqueB);

      if (rootOfB.equals(rootOfA)) continue;

      mergeAndRemoveShorterBranch(branches, branchRoots, rootOfA, rootOfB);

      finalSeparators.add(separatorFactory.buildSeparator(cliqueA, cliqueB));
    }
    jtd.setSeparators(finalSeparators.toArray(Separator[]::new));
  }

  private void initBranches(
      Clique[] cliques, Map<Clique, List<Clique>> branches, Map<Clique, Clique> branchRoots) {
    for (Clique clique : cliques) {
      branches.put(clique, new ArrayList<>(List.of(clique)));
      branchRoots.put(clique, clique);
    }
  }

  private List<CliqueEdge> orderCandidateEdges(Clique[] cliques) {
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

  private List<List<Clique>> findCliquesBySharedNodes(Clique[] cliqueArray) {
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
    return TableUtils.getCommonNodes(cliqueA.getTable(), cliqueB.getTable()).size();
  }

  private record CliqueEdge(Clique cliqueA, Clique cliqueB, int weight) {}
}
