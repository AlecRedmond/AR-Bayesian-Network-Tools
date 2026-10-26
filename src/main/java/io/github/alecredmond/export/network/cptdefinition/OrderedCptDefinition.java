package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.FastIndexFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.VectorIndexFinder;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

public class OrderedCptDefinition implements CptDefinition {
  protected final Node measuredNode;
  protected final ProbabilityVector vector;
  protected final VectorIndexFinder<NodeState> indexFinder;

  @NonNull
  public OrderedCptDefinition(
      Node node, double[] probabilities, CptNodeOrder cptNodeOrder, CptStrideOrder cptStrideOrder) {
    List<Node> combinationOrder = buildCombinationOder(node, cptNodeOrder);
    this.vector = buildVector(combinationOrder, cptStrideOrder, probabilities);
    this.indexFinder = FastIndexFinder.create(vector);
    this.measuredNode = node;
  }

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

  protected ProbabilityVector buildVector(
      List<Node> nodeCombination, CptStrideOrder cptStrideOrder, double[] probabilities) {
    ProbabilityVectorFactory factory = new ProbabilityVectorFactory();
    return switch (cptStrideOrder) {
      case DESCENDING_STRIDE_LENGTH -> factory.build(nodeCombination, probabilities);
      case ASCENDING_STRIDE_LENGTH -> factory.build(nodeCombination.reversed(), probabilities);
    };
  }

  @Override
  public double[] probabilities() {
    return vector.getProbabilities();
  }

  @Override
  public ProbabilityVector getVector() {
    return vector;
  }

  @Override
  public Node measuredNode() {
    return measuredNode;
  }

  @Override
  public Node[] orderedNodes() {
    return vector.getOrderedNodes();
  }
}
