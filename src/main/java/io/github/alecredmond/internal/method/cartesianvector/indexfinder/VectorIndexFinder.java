package io.github.alecredmond.internal.method.cartesianvector.indexfinder;

import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import java.util.Collection;
import java.util.Set;

public interface VectorIndexFinder<S extends CartesianState> {
  default int getIndexFromStates(Collection<S> states) {
    return getIndexFromPositions(getPositionsFromStates(states));
  }

  int getIndexFromPositions(int[] statePositions);

  int[] getPositionsFromStates(Collection<S> states);

  default Set<S> getStatesFromIndex(int index) {
    return getStatesFromPositions(getPositionsFromIndex(index));
  }

  Set<S> getStatesFromPositions(int[] positions);

  int[] getPositionsFromIndex(int index);
}
