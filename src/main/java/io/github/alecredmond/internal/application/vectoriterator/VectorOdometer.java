package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VectorOdometer extends CartesianOdometer<Node, NodeState, ProbabilityVector> {
  private final boolean[] outerIteratorLocks;
  private final boolean[] innerIteratorLocks;
  private final boolean[][] nodeStateEvidenceArray;

  public VectorOdometer(ProbabilityVector vector) {
    super(vector,NodeState[]::new);
    int keyLength = statePositions.length;
    this.outerIteratorLocks = new boolean[keyLength];
    this.innerIteratorLocks = new boolean[keyLength];
    this.nodeStateEvidenceArray = new boolean[keyLength][];
  }

  public double[] getProbabilities() {
    return vector.getProbabilities();
  }

  public boolean[] getOuterIteratorLocks() {
    return this.outerIteratorLocks;
  }

  public boolean[] getInnerIteratorLocks() {
    return this.innerIteratorLocks;
  }

  public boolean[][] getNodeStateEvidenceArray() {
    return this.nodeStateEvidenceArray;
  }
}
