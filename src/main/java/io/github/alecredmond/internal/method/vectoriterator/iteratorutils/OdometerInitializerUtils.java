package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import lombok.Data;

@Data
public class OdometerInitializerUtils {

  private OdometerInitializerUtils() {}

  public static <N extends CartesianVariable, S extends CartesianState> void resetInitializer(
      OdometerInitializer initializer,
      boolean[] lockedPositionArray,
      CartesianOdometer<N, S, ?> odometer) {
    int fastestPos = findFastestPosition(lockedPositionArray);
    boolean fireOnlyOnce = fastestPos < 0;
    int[] strideLengths = odometer.getStrideLengths();
    int baseStride = fireOnlyOnce ? 0 : strideLengths[fastestPos];

    initializer.setLockedPositions(lockedPositionArray);
    initializer.setFastestPosition(fastestPos);
    initializer.setFireOnlyOnce(fireOnlyOnce);
    initializer.setBaseStride(baseStride);
  }

  private static int findFastestPosition(boolean[] positionLocked) {
    int fastestPosition = -1;
    for (int i = positionLocked.length - 1; i >= 0; i--) {
      if (!positionLocked[i]) {
        fastestPosition = i;
        break;
      }
    }
    return fastestPosition;
  }

  public static <S extends CartesianState, N extends CartesianVariable>
      void setUnlockedToInitialPositions(
          CartesianOdometer<N, S, ?> odometer, boolean[] positionLocked, int[] initialPositions) {
    int[] statePositions = odometer.getStatePositions();
    final int length = statePositions.length;
    for (int i = 0; i < length; i++) {
      if (positionLocked[i]) continue;
      statePositions[i] = initialPositions[i];
    }
  }

  public static <S extends CartesianState, N extends CartesianVariable> void updateStartIndex(
      OdometerInitializer initializer, CartesianOdometer<N, S, ?> odometer) {
    initializer.setInitialIndex(
        computeStartIndex(odometer.getStatePositions(), odometer.getStrideLengths()));
  }

  private static int computeStartIndex(int[] statePositions, int[] strideLengths) {
    int index = 0;
    for (int i = 0; i < statePositions.length; i++) {
      index += statePositions[i] * strideLengths[i];
    }
    return index;
  }

  public static int[] buildStrideIfLocked(CartesianVector<?, ?> vector) {
    return getInts(vector.getNumberOfStates(), vector.getStrideLengths());
  }

  private static int[] getInts(int[] numberOfStates, int[] strideLengths) {
    int[] strideIfLocked = new int[numberOfStates.length];
    for (int i = 0; i < numberOfStates.length; i++) {
      strideIfLocked[i] = (numberOfStates[i] - 1) * strideLengths[i];
    }
    return strideIfLocked;
  }
}
