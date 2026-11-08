package io.github.alecredmond.internal.method.cartesianvector.indexfinder;

import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import java.util.Collection;
import java.util.function.Supplier;

public interface VectorIndexFinder<S extends CartesianState> {
  default int getIndexFromStates(Collection<S> states) {
    return getIndexFromPositions(getPositionsFromStates(states));
  }

  int getIndexFromPositions(int[] statePositions);

  int[] getPositionsFromStates(Collection<S> states);

  default S[] getStatesFromIndex(int index) {
    return getStatesFromPositions(getPositionsFromIndex(index));
  }

  S[] getStatesFromPositions(int[] positions);

  int[] getPositionsFromIndex(int index);

  default <T extends Collection<S>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collector) {
    return getStatesFromPositions(getPositionsFromIndex(index), collector);
  }

  <T extends Collection<S>, U extends T> T getStatesFromPositions(
      int[] positions, Supplier<U> collectionSupplier);
}
