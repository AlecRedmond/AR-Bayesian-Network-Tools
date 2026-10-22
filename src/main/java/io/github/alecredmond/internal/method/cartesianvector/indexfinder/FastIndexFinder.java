package io.github.alecredmond.internal.method.cartesianvector.indexfinder;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import java.util.Collection;
import java.util.function.Function;

public class FastIndexFinder<N extends CartesianVariable, S extends CartesianState>
    extends AbstractIndexFinder<N, S> {

  public static FastIndexFinder<Node, NodeState> create(ProbabilityVector vector) {
    return build(vector, FastIndexFinder::new);
  }

  public FastIndexFinder(
      CartesianVector<N, S> vector,
      Function<S, N> getNodeFn,
      Function<S, Integer> statePositionFinder,
      Function<N, Integer> nodeIndexFinder) {
    super(vector, getNodeFn, statePositionFinder, nodeIndexFinder);
  }

  @Override
  public int[] getPositionsFromStates(Collection<S> states) {
    int[] statePositions = new int[vector.getOrderedNodes().length];
    for (S state : states) {
      Integer nodeIndex = nodeIndexFinder.apply(getNodeFn.apply(state));
      if (nodeIndex == null) continue;
      statePositions[nodeIndex] = statePositionFinder.apply(state);
    }
    return statePositions;
  }
}
