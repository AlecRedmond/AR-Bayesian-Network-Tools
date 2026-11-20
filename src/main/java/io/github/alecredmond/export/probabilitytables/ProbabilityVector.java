package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
 * <p>The order of the nodes is given by {@link #getOrderedNodes()}. The same index positions map
 * consistently across {@link #getStateArrays()}, {@link #getNumberOfStates()}, and {@link
 * #getStrideLengths()}.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProbabilityVector extends CartesianVector<Node, NodeState> {

  /**
   * The probability value for each {@link NodeState} combination in the Cartesian product. The
   * maximum length of this array, and therefore the absolute entry limit for a {@code
   * ProbabilityVector}, is 2<sup>31</sup>&minus;1.
   */
  private final double[] probabilities;

  /** Maps each {@link Node} in this vector to its index position in {@link #getOrderedNodes()}. */
  private final Map<Node, Integer> nodeIndexMap;

  /**
   * Constructs a new {@code ProbabilityVector}. This constructor is used internally.
   *
   * @param orderedNodes the ordered array of nodes in this vector
   * @param stateArrays the multidimensional array of states, parallel to the node array
   * @param numberOfStates the number of states available for each node
   * @param strideLengths the stride values used to compute the probability index
   * @param probabilities the flat {@code double} array that contains the probability entries
   * @param nodeIndexMap a lookup map from each node to its index in the structural arrays
   * @param rank the length of the probabilities array
   */
  public ProbabilityVector(
      Node[] orderedNodes,
      NodeState[][] stateArrays,
      int[] numberOfStates,
      int[] strideLengths,
      double[] probabilities,
      Map<Node, Integer> nodeIndexMap,
      int rank) {
    super(orderedNodes, stateArrays, numberOfStates, strideLengths, rank);
    this.probabilities = probabilities;
    this.nodeIndexMap = nodeIndexMap;
  }

  /**
   * Returns the ordered array of {@link Node}s managed by this probability vector. The index of
   * each {@code Node} is used to access its relevant data in:
   *
   * <ul>
   *   <li>{@link #getStateArrays()}
   *   <li>{@link #getNumberOfStates()}
   *   <li>{@link #getStrideLengths()}
   * </ul>
   *
   * @return the parallel node array
   */
  @Override
  public Node[] getOrderedNodes() {
    return this.orderedNodes;
  }

  /**
   * Returns the number of {@link NodeState} values for each node, parallel to {@link
   * #getOrderedNodes()}. This is equivalent to {@code stateArrays[i].length} for each index {@code
   * i}.
   *
   * @return the parallel array of state counts
   */
  @Override
  public int[] getNumberOfStates() {
    return this.numberOfStates;
  }

  /**
   * Returns the stride values used to compute an index into the probability array. The multiplier
   * decreases along the length of the array such that {@code strideLengths[i+1] = strideLengths[i]
   * / numberOfStates[i]}, and the final element always equals 1.
   *
   * <p>See the class-level documentation for the full indexing formula.
   *
   * @return the parallel array of stride lengths
   */
  @Override
  public int[] getStrideLengths() {
    return this.strideLengths;
  }

  /**
   * Returns the states available for each node, parallel to {@link #getOrderedNodes()}. The array
   * {@code stateArrays[i]} contains all {@link NodeState} values of {@code orderedNodes[i]}, so
   * accessing {@code stateArrays[i][j]} is equivalent to {@code
   * orderedNodes[i].getStates().get(j)}.
   *
   * @return the parallel matrix of node states
   */
  @Override
  public NodeState[][] getStateArrays() {
    return this.stateArrays;
  }

  /**
   * Returns the length of the probability array in this {@code ProbabilityVector}. This is equal to
   * the size of the Cartesian product of all {@link Node}s in {@link #getOrderedNodes()}.
   *
   * @return the rank of this {@code ProbabilityVector}
   */
  @Override
  public int getRank() {
    return rank;
  }

  /**
   * Returns the probability value for each {@link NodeState} combination in the Cartesian product.
   * The maximum length of this array, and therefore the absolute entry limit for a {@code
   * ProbabilityVector}, is 2<sup>31</sup>&minus;1.
   *
   * @return the complete backing {@code double} array
   */
  public double[] getProbabilities() {
    return this.probabilities;
  }

  /**
   * Returns an index lookup map that links each {@link Node} in this vector to its index position
   * in {@link #getOrderedNodes()}.
   *
   * @return a map linking nodes to their absolute array index positions
   */
  public Map<Node, Integer> getNodeIndexMap() {
    return this.nodeIndexMap;
  }
}
