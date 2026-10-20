package io.github.alecredmond.internal.application.cartesianvector;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public abstract class CartesianVector<N extends CartesianVariable, S extends CartesianState> {
  protected final N[] orderedNodes;
  protected final S[][] stateArrays;
  protected final int[] numberOfStates;
  protected final int[] strideLengths;
  protected final int rank;

  /**
   * Returns the ordinal of the given {@link CartesianState} within its parent {@link
   * CartesianVariable}'s list.
   *
   * @param state a {@link CartesianState} within the scope of this {@code CartesianVector}.
   * @return the ordinal position of the state in its parent {@link CartesianVariable}'s list.
   */
  public abstract int getStatePosition(S state);

  /**
   * Returns the ordinal of the given {@link CartesianState} within its parent {@link
   * CartesianVariable}'s list.
   *
   * @param node a {@link CartesianVariable} within the scope of this {@link CartesianVector}.
   * @return the index of the node within the {@link #orderedNodes} array.
   */
  public abstract int getNodeIndex(N node);
}
