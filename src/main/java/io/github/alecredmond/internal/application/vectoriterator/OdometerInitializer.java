package io.github.alecredmond.internal.application.vectoriterator;

import lombok.Data;

@Data
public class OdometerInitializer {
  private boolean[] lockedPositions;
  private int fastestPosition;
  private boolean fireOnlyOnce;
  private int baseStride;
  private int initialIndex;

  public OdometerInitializer(){}

}
