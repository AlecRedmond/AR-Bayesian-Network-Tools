package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import lombok.Getter;

public class InitialPositionSetter<N extends CartesianVariable, S extends CartesianState>
    implements CartesianIteratorLogic {
  protected final CartesianOdometer<N, S> odometer;
  @Getter protected final int[] initialPosition;
  protected final Function<N, S> initialStateFunction;

  public InitialPositionSetter(
      CartesianOdometer<N, S> odometer, Function<N, S> initialStateFunction) {
    this.odometer = odometer;
    this.initialPosition = new int[odometer.getOrderedNodes().length];
    this.initialStateFunction = initialStateFunction;
  }

  @Override
  public void reset() {
    final int length = initialPosition.length;
    N[] orderedNodes = odometer.getOrderedNodes();
    int[] statePositions = odometer.getStatePositions();
    ToIntFunction<S> statePositionFn = odometer.getVector().orderedPositionOfState();
    for (int i = 0; i < length; i++) {
      N node = orderedNodes[i];
      S initialState = initialStateFunction.apply(node);
      int position = statePositionFn.applyAsInt(initialState);
      initialPosition[i] = position;
      statePositions[i] = position;
    }
  }
}
