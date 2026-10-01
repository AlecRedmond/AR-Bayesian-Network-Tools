package io.github.alecredmond.export.cartesianvector;

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

  protected abstract int getNodeOrderIndex(N n);

  protected abstract int getStatePositionIndex(S s);

}
