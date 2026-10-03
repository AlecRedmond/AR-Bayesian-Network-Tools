package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.PositionLock;
import lombok.Data;

@Data
public class OdometerInitializerUtils {

  private OdometerInitializerUtils() {}

  public static <N extends CartesianVariable, S extends CartesianState> void resetInitializer(
      CartesianOdometer<N, S, ?> odometer,
      PositionLock<N, S> positionLock,
      OdometerInitializer initializer) {
    boolean[] lockedPositionArray = positionLock.getPositionLocked();
    int fastestPos = findFastestPosition(lockedPositionArray);
    boolean fireOnlyOnce = fastestPos < 0;
    int[] strideLengths = odometer.getStrideLengths();
    int startIndex = computeStartIndex(odometer.getStatePositions(), strideLengths);
    int baseStride = fireOnlyOnce ? 0 : strideLengths[fastestPos];

    initializer.setLockedPositions(lockedPositionArray);
    initializer.setInitialIndex(startIndex);
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

  private static int computeStartIndex(int[] odometerValues, int[] strideLengths) {
    int index = 0;
    for (int i = 0; i < odometerValues.length; i++) {
      index += odometerValues[i] * strideLengths[i];
    }
    return index;
  }

  public static void resetInnerInitializer(VectorOdometer odometer, OdometerInitializer initInner) {
    resetInitializer(odometer, odometer.getInnerIteratorLocks(), initInner);
  }

  public static void resetInitializer(
      VectorOdometer odometer, boolean[] positionLocked, OdometerInitializer initializer) {
    int fastestPos = findFastestPosition(positionLocked);
    boolean fireOnlyOnce = fastestPos < 0;
    int[] strideLengths = odometer.getStrideLengths();
    int startIndex = computeStartIndex(odometer.getStatePositions(), strideLengths);
    int baseStride = fireOnlyOnce ? 0 : strideLengths[fastestPos];

    initializer.setLockedPositions(positionLocked);
    initializer.setInitialIndex(startIndex);
    initializer.setFastestPosition(fastestPos);
    initializer.setFireOnlyOnce(fireOnlyOnce);
    initializer.setBaseStride(baseStride);
  }

  public static int[] buildStrideIfLocked(VectorOdometer odometer) {
    return getInts(odometer.getNumberOfStates(), odometer.getStrideLengths());
  }

  private static int[] getInts(int[] numberOfStates, int[] strideLengths) {
    int[] strideIfLocked = new int[numberOfStates.length];
    for (int i = 0; i < numberOfStates.length; i++) {
      strideIfLocked[i] = (numberOfStates[i] - 1) * strideLengths[i];
    }
    return strideIfLocked;
  }

  public static int[] buildStrideIfLocked(CartesianVector<?, ?> vector) {
    return getInts(vector.getNumberOfStates(), vector.getStrideLengths());
  }

  public static void updateStartIndex(OdometerInitializer initializer, VectorOdometer odometer) {
    initializer.setInitialIndex(
        computeStartIndex(odometer.getStatePositions(), odometer.getStrideLengths()));
  }

  public static void resetOuterInitializer(VectorOdometer odometer, OdometerInitializer initOuter) {
    resetInitializer(odometer, odometer.getOuterIteratorLocks(), initOuter);
  }
}
