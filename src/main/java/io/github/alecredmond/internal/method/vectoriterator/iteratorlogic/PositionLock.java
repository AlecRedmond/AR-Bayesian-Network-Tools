package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import java.util.function.Predicate;

public interface PositionLock extends CartesianIteratorLogic {
  OdometerInitializer getInitializer();

  static <N extends CartesianVariable, S extends CartesianState> PositionLock create(
      CartesianVector<N, S> vector, CartesianOdometer odometer, Predicate<N> checkPositionLocked) {
    return new PositionLockImpl<>(vector, odometer, checkPositionLocked);
  }
}
