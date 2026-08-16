package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import lombok.Data;

@Data
public class VectorOdometer {
  private double[] probabilities;
  private Node[] nodeArray;
  private NodeState[][] stateArrays;
  private int[] numberOfStates;
  private int[] strideLengths;
  private int[] stateIndexes;
  private NodeState[] states;
  private boolean[] outerIteratorLocks;
  private boolean[] innerIteratorLocks;
  private boolean[][] nodeStateEvidenceArray;

  public VectorOdometer(ProbabilityVector vector) {
    int keyLength = vector.getNodeArray().length;
    probabilities = vector.getProbabilities();
    nodeArray = vector.getNodeArray();
    stateArrays = vector.getStateArrays();
    numberOfStates = vector.getNumberOfStates();
    strideLengths = vector.getStrideLengths();
    stateIndexes = new int[keyLength];
    states = new NodeState[keyLength];
    outerIteratorLocks = new boolean[keyLength];
    innerIteratorLocks = new boolean[keyLength];
    nodeStateEvidenceArray = new boolean[keyLength][];
  }
}
