package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public interface InitialPositionSetter extends CartesianIteratorLogic {
  static <N extends CartesianVariable, S extends CartesianState> InitialPositionSetter create(
      CartesianVector<N, S> vector,
      CartesianOdometer odometer,
      Function<N, S> initialStateFunction,
      ToIntFunction<S> statePositionFinder) {
    return new InitialPositionSetterImpl<>(
        vector, odometer, initialStateFunction, statePositionFinder);
  }
}
