package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.BinaryCptDefinitionPadder;
import lombok.NonNull;

/**
 * A {@link CptDefinition} for a {@link BinaryNode} in which only the probabilities of the {@code
 * "TRUE"} state of the measured node are supplied.
 *
 * <p>The entries for the {@code "FALSE"} state are filled in by the constructor, so the caller only
 * provides half of the table.
 *
 * @author Alec Redmond
 * @see OrderedCptDefinition
 */
public class HalfCptDefinition extends OrderedCptDefinition {

  /**
   * Constructs a definition from the probabilities of the {@code "TRUE"} state of the measured
   * node.
   *
   * @param trueProbabilities the probabilities that {@code node} is {@code true}, with one entry
   *     for each combination of the states of its parents
   * @param cptStrideOrder the way that the stride length changes along the ordering of the full
   *     table
   * @param node the measured binary node of the CPT
   * @param cptNodeOrder the position of the measured node in the ordering of the full table
   * @throws NullPointerException if any argument is {@code null}
   */
  public HalfCptDefinition(
      @NonNull double[] trueProbabilities,
      @NonNull CptStrideOrder cptStrideOrder,
      @NonNull BinaryNode node,
      @NonNull CptNodeOrder cptNodeOrder) {
    super(new double[trueProbabilities.length * 2], cptStrideOrder, node, cptNodeOrder);
    new BinaryCptDefinitionPadder(node, vector, trueProbabilities).writeVectorAndPad();
  }

  /**
   * Constructs a CPT definition. This constructor determines the measured node from the list -
   * which must include the node itself and its parents - and indexes the flat probability array
   * assuming a stride order of {@link CptStrideOrder#DESCENDING_STRIDE_LENGTH}.
   *
   * @param trueProbabilities the probabilities that {@code node} is {@code true}, with one entry
   *     for each combination of the states of its parents
   * @param nodes the nodes that index the vector, which must include the measured node.
   * @throws IllegalArgumentException if the measured node can't be determined.
   * @throws NullPointerException if any argument is {@code null}
   */
  public HalfCptDefinition(double[] trueProbabilities, BinaryNode... nodes) {
    this(trueProbabilities, CptStrideOrder.DESCENDING_STRIDE_LENGTH, nodes);
  }

  /**
   * Constructs a CPT definition. This constructor determines the measured node from the list -
   * which must include the node itself and its parents - and indexes the flat probability array.
   *
   * @param trueProbabilities the probabilities that {@code node} is {@code true}, with one entry
   *     for each combination of the states of its parents
   * @param strideOrder the way that the stride length changes along the ordering of the full table
   * @param nodes the nodes that index the vector, which must include the measured node.
   * @throws IllegalArgumentException if the measured node can't be determined.
   * @throws NullPointerException if any argument is {@code null}
   */
  public HalfCptDefinition(
      @NonNull double[] trueProbabilities,
      @NonNull CptStrideOrder strideOrder,
      @NonNull BinaryNode... nodes) {
    super(new double[trueProbabilities.length * 2], strideOrder, nodes);
    new BinaryCptDefinitionPadder((BinaryNode) measuredNode, vector, trueProbabilities)
        .writeVectorAndPad();
  }
}
