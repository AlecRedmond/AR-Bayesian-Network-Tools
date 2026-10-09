package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import lombok.Data;

@Data
public class OdometerInitializerUtils {

  private OdometerInitializerUtils() {}

  public static void resetInitializer(
      OdometerInitializer initializer, boolean[] lockedPositionArray, CartesianOdometer odometer) {
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

  public static void updateStartIndex(OdometerInitializer initializer, CartesianOdometer odometer) {
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
}
