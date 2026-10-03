package io.github.alecredmond.internal.application.vectoriterator.positionlocker;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.function.Predicate;
import lombok.Data;

@Data
public abstract class PositionLock<N extends CartesianVariable, S extends CartesianState> {
  protected final CartesianOdometer<N, S, ?> odometer;
  protected final N[] orderedNodes;
  protected final boolean[] positionLocked;
  protected final int[] initialPosition;
  protected final Predicate<N> checkPositionLocked;

  protected PositionLock(CartesianOdometer<N, S, ?> odometer, Predicate<N> checkPositionLocked) {
    this.odometer = odometer;
    this.orderedNodes = odometer.getVector().getOrderedNodes();
    this.positionLocked = new boolean[orderedNodes.length];
    this.initialPosition = new int[orderedNodes.length];
    this.checkPositionLocked = checkPositionLocked;
    commonResetLogic();
  }

  protected void commonResetLogic() {
    final int bound = orderedNodes.length;
    for (int i = 0; i < bound; i++) {
      resetLockByIndex(i);
    }
  }

  public void resetLockByIndex(int index) {
    positionLocked[index] = checkPositionLocked.test(orderedNodes[index]);
  }

  public abstract void resetLock();
}
