package io.github.alecredmond.export.cartesianvector;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public class CartesianVectorUtils {
  private CartesianVectorUtils() {
    /* This utility class should not be instantiated */
  }

  @SuppressWarnings("unchecked")
  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      int[] getStatePositions(
          V cartesianVector, Collection<S> states, boolean performSafetyChecks) {
    ToIntFunction<N> getNodeOrderIndex = cartesianVector.orderedIndexOfNode();
    ToIntFunction<S> getStateOrderIndex = cartesianVector.orderedPositionOfState();
    N[] orderedNodes = cartesianVector.getOrderedNodes();
    if (performSafetyChecks) {
      performStateSafetyChecks(states, orderedNodes, getNodeOrderIndex);
    }
    int[] statePositions = new int[orderedNodes.length];
    for (S state : states) {

      statePositions[getNodeOrderIndex.applyAsInt((N) state.getNode())] =
          getStateOrderIndex.applyAsInt(state);
    }
    return statePositions;
  }

  @SuppressWarnings("unchecked")
  private static <N extends CartesianVariable, S extends CartesianState>
      void performStateSafetyChecks(
          Collection<S> states, N[] orderedNodes, ToIntFunction<N> getNodeOrderIndex) {
    final int length = orderedNodes.length;
    boolean[] nodeProcessed = new boolean[length];
    for (S state : states) {
      N node = (N) state.getNode();
      int nodeIdx = getNodeOrderIndex.applyAsInt(node);
      boolean firstTimeProcessed = (nodeProcessed[nodeIdx] = !nodeProcessed[nodeIdx]);
      if (!firstTimeProcessed) {
        throw new ProbabilityTableRequestException(
            "Node %s was processed twice during safety checks".formatted(node));
      }
    }
    for (int i = 0; i < length; i++) {
      if (!nodeProcessed[i]) {
        throw new ProbabilityTableRequestException(
            "Node %s had no associated state in request %s".formatted(orderedNodes[i], states));
      }
    }
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      int getIndexFromPositions(V cartesianVector, int[] statePositions) {
    final int length = checkStatePositionLength(cartesianVector, statePositions);
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
