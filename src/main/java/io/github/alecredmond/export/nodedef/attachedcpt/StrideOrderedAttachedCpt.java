package io.github.alecredmond.export.nodedef.attachedcpt;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.nodedef.NodeDefinition;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public interface StrideOrderedAttachedCpt extends AttachedCpt {

  @Override
  default List<ProbabilityConstraint> supplyConstraints() {
    Stream<Node> nodeStream = Arrays.stream(getOrderedNodeDefs()).map(NodeDefinition::getNode);
    List<Node> nodes;
    switch (getAddressingOrder()) {
      case LONGEST_STRIDE_FIRST -> nodes = nodeStream.toList();
      case SHORTEST_STRIDE_FIRST -> nodes = nodeStream.sorted(Collections.reverseOrder()).toList();
      default ->
          throw new IllegalStateException(
              "Stride Ordered CPT had unexpected addressing Order: " + getAddressingOrder());
    }
    ProbabilityVector vector = new ProbabilityVectorFactory().build(nodes);
    System.arraycopy(getCptArray(), 0, vector.getProbabilities(), 0, getCptArray().length);
    return new ConstraintBuilderIterator(getEventNodeDef().getNode(), vector).buildConstraints();
  }

  AddressingOrder getAddressingOrder();

  enum AddressingOrder {
    LONGEST_STRIDE_FIRST,
    SHORTEST_STRIDE_FIRST
  }
}
