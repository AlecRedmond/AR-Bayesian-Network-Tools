package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

/**
 * A {@link CptDefinition} in which every probability of the table is supplied as a flat array.
 *
 * <p>The array is indexed by the measured {@link Node} and its parents. The position of the
 * measured node in the ordering is given by a {@link CptNodeOrder}, and the way that the stride
 * length changes along the ordering is given by a {@link CptStrideOrder}.
 *
 * @author Alec Redmond
 * @see HalfCptDefinition
 */
public class OrderedCptDefinition implements CptDefinition {
  /** The node whose CPT is defined. */
  protected final Node measuredNode;

  /** The probabilities of the CPT, indexed by the measured node and its parents. */
  protected final ProbabilityVector vector;

  /**
   * Constructs a definition from a flat array of probabilities.
   *
   * @param node the measured node of the CPT
   * @param probabilities the probabilities of the CPT, with one entry for each combination of the
   *     states of {@code node} and its parents
   * @param cptNodeOrder the position of the measured node in the ordering of the array
   * @param cptStrideOrder the way that the stride length changes along the ordering of the array
   * @throws NullPointerException if any argument is {@code null}.
   */
  @NonNull
  public OrderedCptDefinition(
      @NonNull Node node,
      @NonNull double[] probabilities,
      @NonNull CptNodeOrder cptNodeOrder,
      @NonNull CptStrideOrder cptStrideOrder) {
    List<Node> combinationOrder = buildCombinationOder(node, cptNodeOrder);
    this.vector = buildVector(combinationOrder, cptStrideOrder, probabilities);
    this.measuredNode = node;
  }

  /**
   * Returns the measured node and its parents as a single list, ordered by the specified {@link
   * CptNodeOrder}. The parents keep the order returned by {@link Node#getParents()}.
   *
   * @param node the measured node
   * @param cptNodeOrder the position of the measured node in the list
   * @return a new list containing the measured node and its parents
   * @throws NullPointerException if {@code node} or {@code cptNodeOrder} is {@code null}
   * @throws IllegalStateException if {@code cptNodeOrder} is not handled by this method
   */
  protected List<Node> buildCombinationOder(Node node, CptNodeOrder cptNodeOrder) {
    List<Node> combined = new ArrayList<>();
    switch (cptNodeOrder) {
      case CONDITIONS_BEFORE_EVENT -> {
        combined.addAll(node.getParents());
        combined.add(node);
      }
      case EVENT_BEFORE_CONDITIONS -> {
        combined.add(node);
        combined.addAll(node.getParents());
      }
      default -> throw new IllegalStateException("Unexpected value: " + cptNodeOrder);
    }
    return combined;
  }

  /**
   * Builds the {@link ProbabilityVector} for the specified nodes and probabilities.
   *
   * <p>For {@link CptStrideOrder#DESCENDING_STRIDE_LENGTH}, the first node in the list has the
   * longest stride length. For {@link CptStrideOrder#ASCENDING_STRIDE_LENGTH}, the list is reversed
   * first, so the first node in the list has the shortest stride length.
   *
   * @param nodeCombination the nodes that index the vector, in the order produced by {@link
   *     #buildCombinationOder(Node, CptNodeOrder)}
   * @param cptStrideOrder the way that the stride length changes along {@code nodeCombination}
   * @param probabilities the probabilities of the CPT
   * @return a new vector indexed by the nodes and holding the probabilities
   * @throws NullPointerException if {@code cptStrideOrder} is {@code null}
   */
  protected ProbabilityVector buildVector(
      List<Node> nodeCombination, CptStrideOrder cptStrideOrder, double[] probabilities) {
    ProbabilityVectorFactory factory = new ProbabilityVectorFactory();
    return switch (cptStrideOrder) {
      case DESCENDING_STRIDE_LENGTH -> factory.build(nodeCombination, probabilities);
      case ASCENDING_STRIDE_LENGTH -> factory.build(nodeCombination.reversed(), probabilities);
    };
  }

  @Override
  public ProbabilityVector getVector() {
    return vector;
  }

  @Override
  public Node measuredNode() {
    return measuredNode;
  }
}
