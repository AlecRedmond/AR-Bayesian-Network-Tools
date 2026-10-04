package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

public interface CartesianIteratorLogic<
    N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>> {
  static <
          N extends CartesianVariable,
          S extends CartesianState,
          T extends CartesianOdometer<N, S, ?>>
      List<CartesianIteratorLogic<N, S, T>> sort(CartesianIteratorLogic<N, S, T>[] logic) {
    return Arrays.stream(logic)
        .sorted(Comparator.comparingInt(l -> l.getHandlerType().getPriority()))
        .toList();
  }

  HandlerType getHandlerType();

  ResetLogicType getResetLogicType();

  void reset();

  T getOdometer();

  enum HandlerType {
    POSITION_SETTER(0),
    POSITION_LOCKER(1),
    EVIDENCE_TESTER(2);

    @Getter final int priority;

    HandlerType(int priority) {
      this.priority = priority;
    }
  }

  enum ResetLogicType{
      CONSTANT,
      VARIABLE
  }
}
