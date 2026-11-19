package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.BinaryCptDefinitionPadder;

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
   * @param node the measured binary node of the CPT
   * @param trueProbabilities the probabilities that {@code node} is {@code true}, with one entry
   *     for each combination of the states of its parents
   * @param cptNodeOrder the position of the measured node in the ordering of the full table
   * @param cptStrideOrder the way that the stride length changes along the ordering of the full
   *     table
   * @throws NullPointerException if any argument is {@code null}
   */
  public HalfCptDefinition(
      BinaryNode node,
      double[] trueProbabilities,
      CptNodeOrder cptNodeOrder,
      CptStrideOrder cptStrideOrder) {
    super(node, new double[trueProbabilities.length * 2], cptNodeOrder, cptStrideOrder);
    new BinaryCptDefinitionPadder(node, vector, trueProbabilities).writeVectorAndPad();
  }
}
