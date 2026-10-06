package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface CartesianIteratorTemplate<
    N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>> {
  Function<N, S> initialStatePositionSetter();

  Predicate<N> checkLockOuter();

  Predicate<N> checkLockInner();

  Function<N, boolean[]> updateEvidenceArrays();

  default Consumer<CartesianOdometer<N, S>> stateUpdateFunction() {
    return o -> {};
  }

  default Function<V, CartesianOdometer<N, S>> createOdometer() {
    return CartesianOdometer::new;
  }

  default Function<N, boolean[]> updateEvidenceArraysCommon(
      Set<N> evidenceNodes, Set<S> evidenceStates, Function<N, List<S>> getStateFn) {
    return node -> {
      if (!evidenceNodes.contains(node)) {
        return new boolean[0];
      }
      List<S> states = getStateFn.apply(node);
      boolean[] isEvidence = new boolean[states.size()];
      int bound = states.size();
      for (int y = 0; y < bound; y++) {
        isEvidence[y] = evidenceStates.contains(states.get(y));
      }
      return isEvidence;
    };
  }
}
