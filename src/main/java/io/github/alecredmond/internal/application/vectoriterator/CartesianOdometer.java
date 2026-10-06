package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import lombok.Data;

@Data
public class CartesianOdometer<N extends CartesianVariable, S extends CartesianState> {
  protected final CartesianVector<N, S> vector;
  protected final int[] statePositions;
  protected final int[] strideOverValues;

  public <V extends CartesianVector<N, S>> CartesianOdometer(V vector) {
    this.vector = vector;
    this.statePositions = new int[vector.getOrderedNodes().length];
    this.strideOverValues = OdometerInitializerUtils.buildStrideIfLocked(vector);
  }

  public int[] getStrideLengths() {
    return vector.getStrideLengths();
  }

  public N[] getOrderedNodes() {
    return vector.getOrderedNodes();
  }

  public int[] getNumberOfStates() {
    return vector.getNumberOfStates();
  }
}
