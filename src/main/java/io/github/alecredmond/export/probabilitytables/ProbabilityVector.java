package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Stores the flat probability values for a {@link ProbabilityTable}, mapping the Cartesian product
 * of one or more {@link Node}s to a one-dimensional {@code double} array.
 *
 * <p>The flat array index for a given combination of states is calculated as:
 *
 * <pre>{@code
 * // S = {N0_i, N1_j, ..., Nn_z}: state i of Node 0, state j of Node 1, ..., state z of Node n
 * index = (i * strideLengths[0]) + (j * strideLengths[1]) + ... + (z * strideLengths[n])
 * P(S) = probabilities[index]
 * }</pre>
 *
 * <p>Node ordering is defined by {@link #nodeArray}. The same index positions map consistently
 * across {@link #stateArrays}, {@link #numberOfStates}, and {@link #strideLengths}.
 *
 * @see ProbabilityTable
 * @author Alec Redmond
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@EqualsAndHashCode
public class ProbabilityVector {

  /** The ordered array of nodes managed by this probability vector. */
  private final Node[] nodeArray;

  /**
   * The states available for each node, parallel to {@link #nodeArray}. {@code stateArrays[i]}
   * contains all {@link NodeState} values for {@code nodeArray[i]}. <br>
   * Accessing {@code stateArrays[i][j]} is equivalent to {@code
   * nodeArray[i].getNodeStates().get(j)}.
   */
  private final NodeState[][] stateArrays;

  /**
   * The number of {@link NodeState} values for each node, parallel to {@link #nodeArray}.
   * Equivalent to {@code stateArrays[i].length} for each index {@code i}.
   */
  private final int[] numberOfStates;

  /**
   * The stride values used to compute an index into {@link #probabilities} for a given state
   * combination, parallel to {@link #nodeArray}. The multiplier decreases along the length of the
   * array such that:<br>
   * {@code strideLengths[i+1] = strideLengths[i] / numberOfStates[i]} <br>
   * The final element will always equal 1.<br>
   * See the class-level documentation for the full indexing formula.
   */
  private final int[] strideLengths;

  /**
   * The probability value for each {@link NodeState} combination in the Cartesian product. The
   * maximum length of this array, and therefore the absolute entry limit for a {@code
   * ProbabilityVector}, is 2<sup>31</sup>&minus;1.
   */
  private final double[] probabilities;

  /** Maps each {@link Node} in this vector to its index position in {@link #nodeArray}. */
  private final Map<Node, Integer> nodeIndexMap;

  /**
   * Constructs a new {@code ProbabilityVector}. This constructor is used internally.
   *
   * @param nodeArray the ordered array of nodes in this vector.
   * @param stateArrays the multidimensional array of states parallel to the node array.
   * @param numberOfStates the number of states available for each node.
   * @param strideLengths the stride values used to compute the probability index.
   * @param probabilities the flat double array containing probability entries.
   * @param nodeIndexMap a look-up map pointing from nodes to their structural array indexes.
   */
  public ProbabilityVector(
      Node[] nodeArray,
      NodeState[][] stateArrays,
      int[] numberOfStates,
      int[] strideLengths,
      double[] probabilities,
      Map<Node, Integer> nodeIndexMap) {
    this.nodeArray = nodeArray;
    this.stateArrays = stateArrays;
    this.numberOfStates = numberOfStates;
    this.strideLengths = strideLengths;
    this.probabilities = probabilities;
    this.nodeIndexMap = nodeIndexMap;
  }

  /**
   * Returns ordered array of {@link Node}s managed by this probability vector. The index of each
   * {@link Node} is used to access its relevant data in:
   *
   * <ul>
   *   <li>{@link #stateArrays}
   *   <li>{@link #numberOfStates}
   *   <li>{@link #strideLengths}
   * </ul>
   *
   * @return the parallel node array.
   */
  public Node[] getNodeArray() {
    return this.nodeArray;
  }

  /**
   * Returns the states available for each node, parallel to {@link #nodeArray}. {@code
   * stateArrays[i]} contains all {@link NodeState} values for {@code nodeArray[i]}. <br>
   * Accessing {@code stateArrays[i][j]} is equivalent to {@code
   * nodeArray[i].getNodeStates().get(j)}.
   *
   * @return the parallel matrix of node states.
   */
  public NodeState[][] getStateArrays() {
    return this.stateArrays;
  }

  /**
   * Returns the number of {@link NodeState} values for each node, parallel to {@link #nodeArray}.
   * Equivalent to {@code stateArrays[i].length} for each index {@code i}.
   *
   * @return the parallel array of state counts.
   */
  public int[] getNumberOfStates() {
    return this.numberOfStates;
  }

  /**
   * Returns the stride values used to compute an index into {@link #probabilities}. The multiplier
   * decreases along the length of the array such that:<br>
   * {@code strideLengths[i+1] = strideLengths[i] / numberOfStates[i]} <br>
   * The final element will always equal 1.<br>
   * See the class-level documentation for the full indexing formula.
   *
   * @return the parallel array of stride lengths.
   */
  public int[] getStrideLengths() {
    return this.strideLengths;
  }

  /**
   * The probability value for each {@link NodeState} combination in the Cartesian product. The
   * maximum length of this array, and therefore the absolute entry limit for a {@code
   * ProbabilityVector}, is 2<sup>31</sup>&minus;1.
   *
   * @return the complete backing double array.
   */
  public double[] getProbabilities() {
    return this.probabilities;
  }

  /**
   * Returns an index lookup map, linking each {@link Node} in this vector to its index position in
   * {@link #nodeArray}.
   *
   * @return a map linking nodes to their absolute array index positions.
   */
  public Map<Node, Integer> getNodeIndexMap() {
    return this.nodeIndexMap;
  }
}
