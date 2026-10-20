package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Function;

class InitialPositionSetterImpl<N extends CartesianVariable, S extends CartesianState>
    implements InitialPositionSetter {
  protected final CartesianVector<N, S> vector;
  protected final CartesianOdometer odometer;
  protected final int[] initialPosition;
  protected final Function<N, S> initialStateFunction;

  public InitialPositionSetterImpl(
      CartesianVector<N, S> vector,
      CartesianOdometer odometer,
      Function<N, S> initialStateFunction) {
    this.vector = vector;
    this.odometer = odometer;
    this.initialPosition = new int[vector.getOrderedNodes().length];
    this.initialStateFunction = initialStateFunction;
  }

  @Override
  public void reset() {
    final int length = initialPosition.length;
    N[] orderedNodes = vector.getOrderedNodes();
    int[] statePositions = odometer.getStatePositions();
    for (int i = 0; i < length; i++) {
      N node = orderedNodes[i];
      S initialState = initialStateFunction.apply(node);
      int position = vector.getStatePosition(initialState);
      initialPosition[i] = position;
      statePositions[i] = position;
    }
  }
}
