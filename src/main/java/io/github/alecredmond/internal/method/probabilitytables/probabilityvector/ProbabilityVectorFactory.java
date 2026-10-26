package io.github.alecredmond.internal.method.probabilitytables.probabilityvector;

import io.github.alecredmond.exceptions.ProbabilityVectorFactoryException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.cartesianvector.CartesianVectorFactory;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;
import java.util.function.IntFunction;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class ProbabilityVectorFactory
    extends CartesianVectorFactory<Node, NodeState, ProbabilityVector> {

  public ProbabilityVector build(List<Node> orderedNodes, double[] probabilities) {
    ProbabilityVector vector = super.build(orderedNodes);
    int vectorRank = vector.getRank();
    assertArrayLengthsEqual(orderedNodes, probabilities.length, vectorRank);
    System.arraycopy(probabilities, 0, vector.getProbabilities(), 0, vectorRank);
    return vector;
  }

  private static void assertArrayLengthsEqual(
      List<Node> orderedNodes, int probabilityLength, int vectorRank) {
    if (vectorRank == probabilityLength) return;
    throw new ProbabilityVectorFactoryException(
        "Nodes %s : Probability array has length %d, Vector has rank %d"
            .formatted(CollectionToString.apply(orderedNodes), probabilityLength, vectorRank));
  }

  @Override
  protected IntFunction<Node[]> nodeArraySupplier() {
    return Node[]::new;
  }

  @Override
  protected IntFunction<NodeState[][]> state2DArraySupplier() {
    return NodeState[][]::new;
  }

  @Override
  protected ProbabilityVector instanceSpecificBuildLogic(
      Node[] nodeArray,
      NodeState[][] stateArrays,
      int[] numberOfStates,
      int[] strideLengths,
      int rank) {
    double[] probabilities = new double[rank];
    Arrays.fill(probabilities, 1.0);
    return new ProbabilityVector(
        nodeArray,
        stateArrays,
        numberOfStates,
        strideLengths,
        probabilities,
        NodeUtils.buildNodeIndexMap(nodeArray),
        rank);
  }

  @Override
  protected IntFunction<NodeState[]> stateArraySupplier() {
    return NodeState[]::new;
  }
}
