package io.github.alecredmond.internal.method.vectoriterator.iteratorlogic;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import lombok.Getter;

class EvidenceTestImpl<N extends CartesianVariable, S extends CartesianState>
    implements EvidenceTest {
  protected final N[] nodes;
  protected final boolean[][] stateIsEvidence;
  protected final int[] statePositions;
  protected final Function<N, boolean[]> evidencePerNode;
  protected final List<Integer> evidenceRowIndexes;
  @Getter protected final BooleanSupplier test;

  public EvidenceTestImpl(
      CartesianVector<N, S> vector,
      CartesianOdometer odometer,
      Function<N, boolean[]> evidencePerNode) {
    this.nodes = vector.getOrderedNodes();
    this.stateIsEvidence = new boolean[nodes.length][];
    this.statePositions = odometer.getStatePositions();
    this.evidencePerNode = evidencePerNode;
    this.evidenceRowIndexes = new ArrayList<>();
    this.test = this::testCurrentPosition;
  }

  private boolean testCurrentPosition() {
    for (int i : evidenceRowIndexes) {
      if (!stateIsEvidence[i][statePositions[i]]) {
        return false;
      }
    }
    return true;
  }

  public void reset() {
    evidenceRowIndexes.clear();
    // Reverse so the fastest iterating position is checked first
    for (int i = nodes.length - 1; i >= 0; i--) {
      boolean[] evidence = evidencePerNode.apply(nodes[i]);
      stateIsEvidence[i] = evidence;
      if (evidence != null && evidence.length != 0) {
        evidenceRowIndexes.add(i);
      }
    }
  }
}
