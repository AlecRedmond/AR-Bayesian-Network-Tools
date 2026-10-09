package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.cartesianvector.CartesianVector;
import lombok.Data;

@Data
public class CartesianOdometer {
  protected final int[] statePositions;
  protected final int[] strideOverValues;
  protected final int[] strideLengths;
  protected final int[] numberOfStates;

  public CartesianOdometer(CartesianVector<?, ?> vector) {
    this.strideLengths = vector.getStrideLengths();
    this.numberOfStates = vector.getNumberOfStates();
    this.statePositions = new int[numberOfStates.length];
    this.strideOverValues = new int[numberOfStates.length];
    for (int i = 0; i < numberOfStates.length; i++) {
      strideOverValues[i] = (numberOfStates[i] - 1) * strideLengths[i];
    }
  }
}
