package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.Predicate;

public class PositionLock<N extends CartesianVariable, S extends CartesianState>
    implements CartesianIteratorLogic {
  protected final CartesianOdometer<N, S> odometer;
  protected final boolean[] positionLocked;
  protected final OdometerInitializer initializer;
  protected Predicate<N> checkPositionLocked;

  public PositionLock(CartesianOdometer<N, S> odometer, Predicate<N> checkPositionLocked) {
    int length = odometer.getOrderedNodes().length;
    this.odometer = odometer;
    this.positionLocked = new boolean[length];
    this.checkPositionLocked = checkPositionLocked;
    this.initializer = new OdometerInitializer();
  }

  @Override
  public void reset() {
    N[] orderedNodes = odometer.getOrderedNodes();
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
