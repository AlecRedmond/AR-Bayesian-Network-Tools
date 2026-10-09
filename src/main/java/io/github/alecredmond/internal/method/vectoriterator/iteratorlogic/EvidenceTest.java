package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public interface EvidenceTest extends CartesianIteratorLogic {
  BooleanSupplier getTest();

  static <N extends CartesianVariable, S extends CartesianState> EvidenceTest create(
      CartesianVector<N, S> vector,
      CartesianOdometer odometer,
      Function<N, boolean[]> evidencePerNode) {
    return new EvidenceTestImpl<>(vector, odometer, evidencePerNode);
  }
}
