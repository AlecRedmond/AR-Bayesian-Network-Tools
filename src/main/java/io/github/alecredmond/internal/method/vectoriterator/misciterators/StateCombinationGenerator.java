package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetDefault;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StateCombinationGenerator implements OdometerResetDefault {
  private final VectorIterator iterator;
  private final VectorOdometer odometer;
  private Set<Node> includedNodes;

  public StateCombinationGenerator(ProbabilityVector vector) {
    this.includedNodes = new HashSet<>();
    this.iterator = StandardIteratorFactory.create(this, vector);
    this.odometer = iterator.getOdometer();
  }

  public <T extends Collection<NodeState>, R extends T> List<T> generateCombos(
      Set<Node> includedNodes, Supplier<R> supplier) {
    this.includedNodes = includedNodes;
    iterator.reset();
    int[] includedPositions = buildIncludedPositions(includedNodes);
    NodeState[] states = odometer.getStates();
    List<T> stateCombinations = new ArrayList<>();
    iterator.iterateInner(
        (o, i) ->
            stateCombinations.add(
                Arrays.stream(includedPositions)
                    .mapToObj(x -> states[x])
                    .collect(Collectors.toCollection(supplier))));
    return stateCombinations;
  }

  private int[] buildIncludedPositions(Set<Node> includedNodes) {
    Node[] nodeArray = odometer.getOrderedNodes();
    return IntStream.range(0, nodeArray.length)
        .filter(x -> includedNodes.contains(nodeArray[x]))
        .toArray();
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return ResetLogicUtils.initializeToFirstNodeStates();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> !includedNodes.contains(node);
  }

  @Override
  public UpdateStateArrayLogic updateConsumerType() {
    return UpdateStateArrayLogic.WRITE_STATES_TO_ARRAY;
  }
}
