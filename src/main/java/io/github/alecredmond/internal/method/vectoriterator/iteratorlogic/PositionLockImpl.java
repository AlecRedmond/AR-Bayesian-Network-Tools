package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.Predicate;

class PositionLockImpl<N extends CartesianVariable, S extends CartesianState>
    implements PositionLock {
  protected final CartesianOdometer odometer;
  protected final boolean[] positionLocked;
  protected final OdometerInitializer initializer;
  protected final N[] orderedNodes;
  protected final Predicate<N> checkPositionLocked;

  public PositionLockImpl(
      CartesianVector<N, S> vector, CartesianOdometer odometer, Predicate<N> checkPositionLocked) {
    this.orderedNodes = vector.getOrderedNodes();
    this.odometer = odometer;
    this.positionLocked = new boolean[orderedNodes.length];
    this.checkPositionLocked = checkPositionLocked;
    this.initializer = new OdometerInitializer();
  }

  @Override
  public void reset() {
    final int bound = orderedNodes.length;
    for (int i = 0; i < bound; i++) {
      positionLocked[i] = checkPositionLocked.test(orderedNodes[i]);
    }
    OdometerInitializerUtils.resetInitializer(initializer, positionLocked, odometer);
  }

  public OdometerInitializer getInitializer() {
    OdometerInitializerUtils.updateStartIndex(initializer, odometer);
    return initializer;
  }
}
