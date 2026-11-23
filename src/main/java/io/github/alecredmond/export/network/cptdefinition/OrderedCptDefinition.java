package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;
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
   * @param probabilities the probabilities of the CPT, with one entry for each combination of the
   *     states of {@code node} and its parents
   * @param cptStrideOrder the way that the stride length changes along the ordering of the array
   * @param node the measured node of the CPT
   * @param cptNodeOrder the position of the measured node in the ordering of the array
   * @throws NullPointerException if any argument is {@code null}.
   */
  @NonNull
  public OrderedCptDefinition(
      @NonNull double[] probabilities,
      @NonNull CptStrideOrder cptStrideOrder,
      @NonNull Node node,
      @NonNull CptNodeOrder cptNodeOrder) {
    List<Node> combinationOrder = buildCombinationOder(node, cptNodeOrder);
    this.vector = buildVector(combinationOrder, probabilities, cptStrideOrder);
    this.measuredNode = node;
  }

  private static List<Node> buildCombinationOder(Node node, CptNodeOrder cptNodeOrder) {
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

  private static ProbabilityVector buildVector(
      List<Node> nodeCombination, double[] probabilities, CptStrideOrder cptStrideOrder) {
    ProbabilityVectorFactory factory = new ProbabilityVectorFactory();
    return switch (cptStrideOrder) {
      case DESCENDING_STRIDE_LENGTH -> factory.build(nodeCombination, probabilities);
      case ASCENDING_STRIDE_LENGTH -> factory.build(nodeCombination.reversed(), probabilities);
    };
  }

  /**
   * Constructs a CPT definition. This constructor determines the measured node from the list -
   * which must include the node itself and its parents - and indexes the flat probability array
   * assuming a stride order of {@link CptStrideOrder#DESCENDING_STRIDE_LENGTH}.
   *
   * @param probabilities the probabilities of the CPT
   * @param nodes the nodes that index the vector, which must include the measured node.
   * @throws IllegalArgumentException if the measured node can't be determined.
   */
  public OrderedCptDefinition(double[] probabilities, Node... nodes) {
    this(probabilities, CptStrideOrder.DESCENDING_STRIDE_LENGTH, Arrays.asList(nodes));
  }

  /**
   * Constructs a CPT definition. This constructor determines the measured node from the list -
   * which must include the node itself and its parents - and indexes the flat probability array
   * from the stride order.
   *
   * @param probabilities the probabilities of the CPT
   * @param strideOrder the way that the stride length changes along {@code nodes}
   * @param nodes the nodes that index the vector, which must include the measured node.
   * @throws IllegalArgumentException if the measured node can't be determined.
   */
  public OrderedCptDefinition(
      double[] probabilities, CptStrideOrder strideOrder, List<Node> nodes) {
    this.measuredNode = determineMeasuredNode(nodes);
    this.vector = buildVector(nodes, probabilities, strideOrder);
  }

  private static Node determineMeasuredNode(List<Node> nodes) {
    List<Node> candidates = new ArrayList<>();
    Set<Node> allNodes = new HashSet<>(nodes);
    for (Node node : nodes) {
      Set<Node> current = new HashSet<>();
      current.add(node);
      current.addAll(node.getParents());
      if (current.equals(allNodes)) candidates.add(node);
    }
    if (candidates.size() == 1) {
      return candidates.getFirst();
    }
    throw new IllegalArgumentException(
        "Could not determine the measured node from list : " + CollectionToString.apply(nodes));
  }

  /**
   * Constructs a CPT definition. This constructor determines the measured node from the list -
   * which must include the node itself and its parents - and indexes the flat probability array
   * from the stride order.
   *
   * @param probabilities the probabilities of the CPT
   * @param strideOrder the way that the stride length changes along {@code nodes}
   * @param nodes the nodes that index the vector, which must include the measured node.
   * @throws IllegalArgumentException if the measured node can't be determined.
   */
  public OrderedCptDefinition(double[] probabilities, CptStrideOrder strideOrder, Node... nodes) {
    this(probabilities, strideOrder, Arrays.asList(nodes));
  }

  /**
   * Constructs a CPT definition. This constructor determines the measured node from the list -
   * which must include the node itself and its parents - and indexes the flat probability array
   * assuming a stride order of {@link CptStrideOrder#DESCENDING_STRIDE_LENGTH}.
   *
   * @param probabilities the probabilities of the CPT
   * @param nodes the nodes that index the vector, which must include the measured node.
   * @throws IllegalArgumentException if the measured node can't be determined.
   */
  public OrderedCptDefinition(double[] probabilities, List<Node> nodes) {
    this(probabilities, CptStrideOrder.DESCENDING_STRIDE_LENGTH, nodes);
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
