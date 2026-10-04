package io.github.alecredmond.internal.application.vectoriterator.positionlocker;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter.InitialPositionSetter;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.Predicate;
import lombok.Data;

@Data
public abstract class PositionLock<
        N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>>
    implements CartesianIteratorLogic<N, S, T> {
  protected final T odometer;
  protected final boolean[] positionLocked;
  protected final int[] initialPositions;
  protected final OdometerInitializer initializer;
  protected Predicate<N> checkPositionLocked;

  protected PositionLock(
      T odometer,
      InitialPositionSetter<N, S, T> initialPositionSetter,
      Predicate<N> checkPositionLocked) {
    int length = odometer.getOrderedNodes().length;
    if (length != initialPositionSetter.getInitialPosition().length) {
      throw new IllegalArgumentException(
          "Expected positions array with length %d, got length %d"
              .formatted(length, initialPositionSetter.getInitialPosition().length));
    }
    this.odometer = odometer;
    this.positionLocked = new boolean[length];
    this.initialPositions = initialPositionSetter.getInitialPosition();
    this.checkPositionLocked = checkPositionLocked;
    this.initializer = new OdometerInitializer();
  }

  @Override
  public HandlerType getHandlerType() {
    return HandlerType.POSITION_LOCKER;
  }

  public abstract ResetLogicType getResetLogicType();

  public abstract OdometerInitializer getInitializer();

  protected final void commonResetLogic() {
    N[] orderedNodes = odometer.getOrderedNodes();
    final int bound = orderedNodes.length;
    for (int i = 0; i < bound; i++) {
      positionLocked[i] = checkPositionLocked.test(orderedNodes[i]);
    }
    OdometerInitializerUtils.resetInitializer(initializer, positionLocked, odometer);
  }
}
