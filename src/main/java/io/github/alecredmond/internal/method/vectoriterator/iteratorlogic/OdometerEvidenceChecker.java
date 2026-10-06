package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.Getter;

public class OdometerEvidenceChecker<N extends CartesianVariable, S extends CartesianState>
    implements CartesianIteratorLogic {
  protected final N[] nodes;
  protected final boolean[][] stateIsEvidence;
  protected final int[] statePositions;
  protected final Function<N, boolean[]> evidencePerNode;
  protected int[] evidenceRowIndexes;
  @Getter protected BooleanSupplier test;

  public OdometerEvidenceChecker(
      CartesianOdometer<N, S> odometer, Function<N, boolean[]> evidencePerNode) {
    this.nodes = odometer.getOrderedNodes();
    this.stateIsEvidence = new boolean[odometer.getOrderedNodes().length][];
    this.statePositions = odometer.getStatePositions();
    this.evidencePerNode = evidencePerNode;
  }

  public void reset() {
    final int length = nodes.length;
    for (int i = 0; i < length; i++) {
      stateIsEvidence[i] = evidencePerNode.apply(nodes[i]);
    }
    this.evidenceRowIndexes =
        IntStream.range(0, length)
            .filter(this::nonZeroArrayExistsAtPosition)
            .boxed()
            // Reverse so the fastest iterating position is checked first
            .sorted(Comparator.reverseOrder())
            .mapToInt(Integer::intValue)
            .toArray();

    this.test = this::testWhenEvidenceExists;
  }

  private boolean nonZeroArrayExistsAtPosition(int i) {
    return Optional.ofNullable(stateIsEvidence[i]).map(array -> array.length != 0).orElse(false);
  }

  private boolean testWhenEvidenceExists() {
    for (int i : evidenceRowIndexes) {
      if (!stateIsEvidence[i][statePositions[i]]) {
        return false;
      }
    }
    return true;
  }
}
