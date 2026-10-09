package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class StateUpdateBase<N extends CartesianVariable, S extends CartesianState> {
  protected final S[] states;
  protected final S[][] stateArrays;

  protected StateUpdateBase(CartesianVector<N, S> vector, IntFunction<S[]> stateArraySupplier) {
    this.stateArrays = vector.getStateArrays();
    this.states = Arrays.stream(stateArrays).map(arr -> arr[0]).toArray(stateArraySupplier);
  }

  public Consumer<CartesianOdometer> stateUpdateFunction() {
    return odometer -> {
      int[] stateIndexes = odometer.getStatePositions();
      int length = states.length;
      for (int x = 0; x < length; x++) {
        states[x] = stateArrays[x][stateIndexes[x]];
      }
    };
  }
}
