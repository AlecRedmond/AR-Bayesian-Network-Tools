package io.github.alecredmond.internal.application.vectoriterator.positionlocker;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter.InitialPositionSetter;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerInitializerUtils;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ConstantPositionLock<
        N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>>
    extends PositionLock<N, S, T> {
  protected boolean hasResetOnce;

  public ConstantPositionLock(
      T odometer,
      InitialPositionSetter<N,S,T> initialPositions,
      Predicate<N> checkPositionLocked) {
    super(odometer, initialPositions, checkPositionLocked);
    this.hasResetOnce = false;
  }

    @Override
    public ResetLogicType getResetLogicType() {
        return ResetLogicType.CONSTANT;
    }

    @Override
  public OdometerInitializer getInitializer() {
    OdometerInitializerUtils.updateStartIndex(initializer, odometer);
    return initializer;
  }

  @Override
  public void reset() {
    if (hasResetOnce) return;
    super.commonResetLogic();
    hasResetOnce = true;
  }
}
