package io.github.alecredmond.internal.application.cartesianvector;

import lombok.Data;

@Data
public abstract class CartesianVector<N extends CartesianVariable, S extends CartesianState> {
  protected final N[] orderedNodes;
  protected final S[][] stateArrays;
  protected final int[] numberOfStates;
  protected final int[] strideLengths;
  protected final int rank;
}
