package io.github.alecredmond.export.cartesianvector;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.ToIntFunction;

@AllArgsConstructor
@Data
public abstract class CartesianVector<N extends CartesianVariable, S extends CartesianState> {
  protected final N[] orderedNodes;
  protected final S[][] stateArrays;
  protected final int[] numberOfStates;
  protected final int[] strideLengths;
  protected final int rank;

  public abstract ToIntFunction<N> orderedIndexOfNode();

  public abstract ToIntFunction<S> orderedPositionOfState();

}
