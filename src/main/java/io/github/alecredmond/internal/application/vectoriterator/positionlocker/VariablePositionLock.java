package io.github.alecredmond.internal.application.vectoriterator.positionlocker;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter.InitialPositionSetter;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.Predicate;

public class VariablePositionLock<
        N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>>
    extends PositionLock<N, S, T> {
  protected Runnable initialPositionSetter;

  public VariablePositionLock(
      T odometer,
      InitialPositionSetter<N,S,T> initialPositions,
      Predicate<N> checkPositionLocked) {
    super(odometer, initialPositions, checkPositionLocked);
  }

  @Override
  public void reset() {
    super.commonResetLogic();
    this.initialPositionSetter = useInitialPositionSetterIfNecessary();
  }

  /**
   * If the iterator traverses only specific states per node, whose lowest position {@code N(lp) !=
   * 0}, this sets the initial position to that lowest position. This saves {@code N(lp)} iterations
   * per position-specified node.
   */
  private Runnable useInitialPositionSetterIfNecessary() {
    final int length = positionLocked.length;
    boolean isNecessary = false;
    for (int i = 0; i < length; i++) {
      if (positionLocked[i] || initialPositions[i] == 0) continue;
      isNecessary = true;
      break;
    }
    return isNecessary
        ? () ->
            OdometerInitializerUtils.setUnlockedToInitialPositions(
                odometer, positionLocked, initialPositions)
        : () -> {};
  }

    @Override
    public ResetLogicType getResetLogicType() {
        return ResetLogicType.VARIABLE;
    }

    @Override
  public OdometerInitializer getInitializer() {
    initialPositionSetter.run();
    OdometerInitializerUtils.updateStartIndex(initializer, odometer);
    return initializer;
  }
}
