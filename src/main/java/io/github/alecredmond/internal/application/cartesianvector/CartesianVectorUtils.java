package io.github.alecredmond.internal.application.cartesianvector;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class CartesianVectorUtils {
  private CartesianVectorUtils() {
    /* This utility class should not be instantiated */
  }

  @SuppressWarnings("unchecked")
  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      int[] getStatePositions(V vector, Collection<S> states, boolean performSafetyChecks) {
    if (performSafetyChecks) {
      performStateSafetyChecks(states, vector);
    }
    int[] statePositions = new int[vector.getOrderedNodes().length];
    for (S state : states) {
      statePositions[vector.getNodeIndex((N) state.getNode())] = vector.getStatePosition(state);
    }
    return statePositions;
  }

  @SuppressWarnings("unchecked")
  private static <N extends CartesianVariable, S extends CartesianState>
      void performStateSafetyChecks(Collection<S> states, CartesianVector<N, S> vector) {
    N[] orderedNodes = vector.getOrderedNodes();
    int length = orderedNodes.length;
    boolean[] nodeProcessed = new boolean[length];
    int statesChecked = 0;
    for (S state : states) {
      N node = (N) state.getNode();
      int idx = vector.getNodeIndex(node);
      boolean firstTimeProcessed = (nodeProcessed[idx] = !nodeProcessed[idx]);
      if (!firstTimeProcessed) {
        throw new ProbabilityTableRequestException(
            "Node %s was processed twice during safety checks".formatted(node));
      }
      statesChecked++;
    }
    if (statesChecked == length) {
      return;
    }
    List<N> unassociated =
        IntStream.range(0, length)
            .filter(i -> !nodeProcessed[i])
            .mapToObj(i -> orderedNodes[i])
            .toList();
    throw new ProbabilityTableRequestException(
        "Node %s had no associated state in request %s"
            .formatted(CollectionToString.apply(unassociated), CollectionToString.apply(states)));
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      int getIndexFromPositions(V cartesianVector, int[] statePositions) {
    int length = checkStatePositionLength(cartesianVector, statePositions);
    int[] numberOfStates = cartesianVector.getNumberOfStates();
    int[] strideLengths = cartesianVector.getStrideLengths();
    int idx = 0;
    for (int i = 0; i < length; i++) {
      int sIdx = statePositions[i];
      if (sIdx >= numberOfStates[i]) {
        throw new IllegalArgumentException(
            "Index %d exceeds size limit %d".formatted(sIdx, numberOfStates[i]));
      }
      idx += sIdx * strideLengths[i];
    }
    return idx;
  }

  private static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      int checkStatePositionLength(V cartesianVector, int[] statePositions) {
    int length = cartesianVector.getOrderedNodes().length;
    if (statePositions.length != length) {
      throw new IllegalArgumentException(
          "State positions had length %d, expected %d".formatted(statePositions.length, length));
    }
    return length;
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      Set<S> getStates(V cartesianVector, int index) {
    return getStates(cartesianVector, convertIndexToStatePositions(cartesianVector, index));
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      Set<S> getStates(V cartesianVector, int[] statePositions) {
    final int length = checkStatePositionLength(cartesianVector, statePositions);
    Set<S> states = new LinkedHashSet<>();
    S[][] stateArrays = cartesianVector.getStateArrays();
    for (int nIdx = 0; nIdx < length; nIdx++) {
      states.add(stateArrays[nIdx][statePositions[nIdx]]);
    }
    return states;
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      int[] convertIndexToStatePositions(V cartesianVector, int index) {
    if (index >= cartesianVector.getRank() || index < 0) {
      throw new IndexOutOfBoundsException(index);
    }
    final int length = cartesianVector.getOrderedNodes().length;
    int[] statePositions = new int[length];
    int[] strideLengths = cartesianVector.getStrideLengths();
    for (int i = 0; i < length; i++) {
      int stride = strideLengths[i];
      if (index < stride) continue;
      statePositions[i] = index / stride;
      index %= stride;
    }
    return statePositions;
  }
}
