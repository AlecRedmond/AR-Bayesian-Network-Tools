package io.github.alecredmond.internal.method.cartesianvector.indexfinder;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public abstract class AbstractIndexFinder<N extends CartesianVariable, S extends CartesianState>
    implements VectorIndexFinder<S> {
  protected final CartesianVector<N, S> vector;
  protected final Function<S, N> getNodeFn;
  protected final Function<S, Integer> statePositionFinder;
  protected final Function<N, Integer> nodeIndexFinder;
  protected final IntFunction<S[]> stateArrayBuilder;

  protected AbstractIndexFinder(
      CartesianVector<N, S> vector,
      Function<S, N> getNodeFn,
      Function<S, Integer> statePositionFinder,
      Function<N, Integer> nodeIndexFinder,
      IntFunction<S[]> stateArrayBuilder) {
    this.vector = vector;
    this.getNodeFn = getNodeFn;
    this.statePositionFinder = statePositionFinder;
    this.nodeIndexFinder = nodeIndexFinder;
    this.stateArrayBuilder = stateArrayBuilder;
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

  @Override
  public S[] getStatesFromPositions(int[] statePositions) {
    final int length = checkStatePositionLength(statePositions);
    S[] statesAtPositions = stateArrayBuilder.apply(length);
    S[][] stateArrays = vector.getStateArrays();
    for (int nIdx = 0; nIdx < length; nIdx++) {
      statesAtPositions[nIdx] = stateArrays[nIdx][statePositions[nIdx]];
    }
    return statesAtPositions;
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

  @Override
  public <T extends Collection<S>, U extends T> T getStatesFromPositions(
      int[] statePositions, Supplier<U> collectionSupplier) {
    final int length = checkStatePositionLength(statePositions);
    S[][] stateArrays = vector.getStateArrays();
    T statesAtPositions = collectionSupplier.get();
    for (int nIdx = 0; nIdx < length; nIdx++) {
      statesAtPositions.add(stateArrays[nIdx][statePositions[nIdx]]);
    }
    return statesAtPositions;
  }

  protected final int checkStatePositionLength(int[] statePositions) {
    int length = vector.getOrderedNodes().length;
    if (statePositions.length != length) {
      throw new ProbabilityTableRequestException(
          "State positions had length %d, expected %d".formatted(statePositions.length, length));
    }
    return length;
  }

  protected static <T extends AbstractIndexFinder<Node, NodeState>> T build(
      ProbabilityVector vector, IndexFinderConstructor<T, Node, NodeState> builder) {
    Map<Node, Integer> idxMap = vector.getNodeIndexMap();
    Function<NodeState, Node> getNode = NodeState::getNode;
    Function<NodeState, Integer> getPosition = NodeState::getPosition;
    Function<Node, Integer> getNodeIdx = idxMap::get;
    IntFunction<NodeState[]> stateArrayBuilder = NodeState[]::new;
    return builder.build(vector, getNode, getPosition, getNodeIdx, stateArrayBuilder);
  }

  @FunctionalInterface
  protected interface IndexFinderConstructor<
      T extends AbstractIndexFinder<N, S>, N extends CartesianVariable, S extends CartesianState> {
    T build(
        CartesianVector<N, S> vector,
        Function<S, N> getNodeFn,
        Function<S, Integer> statePositionFinder,
        Function<N, Integer> nodeIndexFinder,
        IntFunction<S[]> stateArrayBuilder);
  }
}
