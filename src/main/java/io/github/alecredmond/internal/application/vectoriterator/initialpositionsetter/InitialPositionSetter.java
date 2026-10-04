package io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import lombok.Getter;

public class InitialPositionSetter<
        N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>>
    implements CartesianIteratorLogic<N, S, T> {
  @Getter protected final T odometer;
  @Getter protected final int[] initialPosition;
  protected final Function<N, S> initialStateFunction;

  public InitialPositionSetter(T odometer, Function<N, S> initialStateFunction) {
    this.odometer = odometer;
    this.initialPosition = new int[odometer.getOrderedNodes().length];
    this.initialStateFunction = initialStateFunction;
  }

  @Override
  public HandlerType getHandlerType() {
    return HandlerType.POSITION_SETTER;
  }

  @Override
  public ResetLogicType getResetLogicType() {
    return ResetLogicType.VARIABLE;
  }

  @Override
  public void reset() {
    final int length = initialPosition.length;
    N[] orderedNodes = odometer.getOrderedNodes();
    int[] statePositions = odometer.getStatePositions();
    S[] states = odometer.getStates();
    ToIntFunction<S> statePositionFn = odometer.getVector().orderedPositionOfState();
    for (int i = 0; i < length; i++) {
      N node = orderedNodes[i];
      S initialState = initialStateFunction.apply(node);
      states[i] = initialState;
      int position = statePositionFn.applyAsInt(initialState);
      initialPosition[i] = position;
      statePositions[i] = position;
    }
  }
}
