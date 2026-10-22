package io.github.alecredmond.internal.method.cartesianvector.indexfinder;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractIndexFinder<N extends CartesianVariable, S extends CartesianState>
    implements VectorIndexFinder<S> {
  protected final CartesianVector<N, S> vector;
  protected final Function<S, N> getNodeFn;
  protected final Function<S, Integer> statePositionFinder;
  protected final Function<N, Integer> nodeIndexFinder;

  protected AbstractIndexFinder(
      CartesianVector<N, S> vector,
      Function<S, N> getNodeFn,
      Function<S, Integer> statePositionFinder,
      Function<N, Integer> nodeIndexFinder) {
    this.vector = vector;
    this.getNodeFn = getNodeFn;
    this.statePositionFinder = statePositionFinder;
    this.nodeIndexFinder = nodeIndexFinder;
  }

  @Override
  public int getIndexFromPositions(int[] statePositions) {
    int length = checkStatePositionLength(statePositions);
    int[] numberOfStates = vector.getNumberOfStates();
    int[] strideLengths = vector.getStrideLengths();
    int idx = 0;
    for (int i = 0; i < length; i++) {
      int sIdx = statePositions[i];
      if (sIdx >= numberOfStates[i]) {
        throw new ProbabilityTableRequestException(
            "State Position %d exceeds size limit %d".formatted(sIdx, numberOfStates[i]));
      }
      idx += sIdx * strideLengths[i];
    }
    return idx;
  }

  protected final int checkStatePositionLength(int[] statePositions) {
    int length = vector.getOrderedNodes().length;
    if (statePositions.length != length) {
      throw new ProbabilityTableRequestException(
          "State positions had length %d, expected %d".formatted(statePositions.length, length));
    }
    return length;
  }

  @Override
  public Set<S> getStatesFromPositions(int[] statePositions) {
    final int length = checkStatePositionLength(statePositions);
    Set<S> states = new LinkedHashSet<>();
    S[][] stateArrays = vector.getStateArrays();
    for (int nIdx = 0; nIdx < length; nIdx++) {
      states.add(stateArrays[nIdx][statePositions[nIdx]]);
    }
    return states;
  }

  @Override
  public int[] getPositionsFromIndex(int index) {
    if (index >= vector.getRank() || index < 0) {
      throw new IndexOutOfBoundsException(index);
    }
    final int length = vector.getOrderedNodes().length;
    int[] statePositions = new int[length];
    int[] strideLengths = vector.getStrideLengths();
    for (int i = 0; i < length; i++) {
      int stride = strideLengths[i];
      if (index < stride) continue;
      statePositions[i] = index / stride;
      index %= stride;
    }
    return statePositions;
  }

  protected static <T extends AbstractIndexFinder<Node, NodeState>> T build(
      ProbabilityVector vector, IndexFinderConstructor<T, Node, NodeState> builder) {
    Map<Node, Integer> idxMap = vector.getNodeIndexMap();
    Function<NodeState, Node> getNode = NodeState::getNode;
    Function<NodeState, Integer> getPosition = NodeState::getPosition;
    Function<Node, Integer> getNodeIdx = idxMap::get;
    return builder.build(vector, getNode, getPosition, getNodeIdx);
  }

  @FunctionalInterface
  protected interface IndexFinderConstructor<
      T extends AbstractIndexFinder<N, S>, N extends CartesianVariable, S extends CartesianState> {
    T build(
        CartesianVector<N, S> vector,
        Function<S, N> getNodeFn,
        Function<S, Integer> statePositionFinder,
        Function<N, Integer> nodeIndexFinder);
  }
}
