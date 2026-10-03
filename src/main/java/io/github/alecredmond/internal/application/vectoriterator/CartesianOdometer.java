package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.IntFunction;
import lombok.Data;

@Data
public abstract class CartesianOdometer<
    N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>> {
  protected final V vector;
  protected final int[] statePositions;
  protected final S[] states;
  protected final int[] strideOverValues;

  protected CartesianOdometer(V vector, IntFunction<S[]> arraySupplier) {
    this.vector = vector;
    this.statePositions = new int[vector.getOrderedNodes().length];
    this.states = arraySupplier.apply(vector.getOrderedNodes().length);
    this.strideOverValues = OdometerInitializerUtils.buildStrideIfLocked(vector);
  }

  public S[] getStates() {
    return states;
  }

  public int[] getStrideLengths() {
    return vector.getStrideLengths();
  }

  public N[] getNodeArray() {
    return vector.getOrderedNodes();
  }

  public S[][] getStateArrays() {
    return vector.getStateArrays();
  }

  public int[] getNumberOfStates() {
    return vector.getNumberOfStates();
  }

  public int[] getStatePositions() {
    return statePositions;
  }
}
