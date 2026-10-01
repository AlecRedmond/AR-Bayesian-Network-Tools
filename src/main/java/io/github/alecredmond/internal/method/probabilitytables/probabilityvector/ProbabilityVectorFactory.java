package io.github.alecredmond.internal.method.probabilitytables.probabilityvector;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.cartesianvector.CartesianVectorFactory;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.function.IntFunction;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class ProbabilityVectorFactory
    extends CartesianVectorFactory<Node, NodeState, ProbabilityVector> {

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
