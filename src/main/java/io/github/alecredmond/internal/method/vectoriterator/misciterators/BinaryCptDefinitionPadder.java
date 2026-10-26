package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.ProbabilityIteratorTemplate;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardCartesianIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class BinaryCptDefinitionPadder implements ProbabilityIteratorTemplate {
  private final BinaryNode measuredNode;
  private final double[] vectorProbabilities;
  private final double[] trueProbabilities;
  private final StandardCartesianIterator iterator;
  private final ToIntFunction<Integer> complementFinder;

  public BinaryCptDefinitionPadder(
      BinaryNode node, ProbabilityVector vector, double[] trueProbabilities) {
    this.measuredNode = node;
    this.vectorProbabilities = vector.getProbabilities();
    this.trueProbabilities = trueProbabilities;
    this.iterator = StandardCartesianIterator.create(this, vector);
    this.complementFinder = buildComplementFinder(vector);
  }

  private ToIntFunction<Integer> buildComplementFinder(ProbabilityVector vector) {
    int[] strideLengths = vector.getStrideLengths();
    int measuredNodeIdx = vector.getNodeIndexMap().get(measuredNode);
    int measuredNodeStride = strideLengths[measuredNodeIdx];
    int trueStatePos = measuredNode.trueState().getPosition();
    return switch (trueStatePos) {
      case 0 -> i -> i + measuredNodeStride;
      case 1 -> i -> i - measuredNodeStride;
      default -> throw new IllegalStateException("Unexpected value: " + trueStatePos);
    };
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        node.equals(measuredNode) ? measuredNode.trueState() : node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> false;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return measuredNode::equals;
  }

  @Override
  public Function<Node, boolean[]> updateEvidenceArrays() {
    return node -> null;
  }

  public void writeVectorAndPad() {
    int[] trueProbIdx = {0};
    iterator.iterateInner(
        i -> {
          double probOfTrue = trueProbabilities[trueProbIdx[0]++];
          if (probOfTrue < 0 || probOfTrue > 1) {
            throw new IllegalArgumentException(
                "Probability value [%d] in Node %s was out of bounds!"
                    .formatted(--trueProbIdx[0], measuredNode));
          }
          vectorProbabilities[i] = probOfTrue;
          vectorProbabilities[complementFinder.applyAsInt(i)] = 1 - probOfTrue;
        });
  }
}
