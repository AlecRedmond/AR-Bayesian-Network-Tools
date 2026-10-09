package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StateCombinationGenerator extends StateUpdateBase<Node, NodeState>
    implements ProbabilityIteratorTemplate {
  private final StandardCartesianIterator iterator;
  private final ProbabilityVector vector;
  private final Set<Node> includedNodes;

  public StateCombinationGenerator(ProbabilityVector vector) {
    super(vector, NodeState[]::new);
    this.includedNodes = new HashSet<>();
      this.iterator = StandardCartesianIterator.create(this, vector);
    this.vector = vector;
  }

  public <T extends Collection<NodeState>, R extends T> List<T> generateCombos(
      Set<Node> includedNodes, Supplier<R> supplier) {
    this.includedNodes.clear();
    this.includedNodes.addAll(includedNodes);
    iterator.reset();
    int[] includedPositions = buildIncludedPositions(includedNodes);
    List<T> stateCombinations = new ArrayList<>();
    iterator.iterateInner(
        i ->
            stateCombinations.add(
                Arrays.stream(includedPositions)
                    .mapToObj(x -> states[x])
                    .collect(Collectors.toCollection(supplier))));
    return stateCombinations;
  }

  private int[] buildIncludedPositions(Set<Node> includedNodes) {
    Node[] nodeArray = vector.getOrderedNodes();
    return IntStream.range(0, nodeArray.length)
        .filter(x -> includedNodes.contains(nodeArray[x]))
        .toArray();
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
      return node -> node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> !includedNodes.contains(node);
  }

  public Function<Node, boolean[]> updateEvidenceArrays() {
    return node -> null;
  }
}
