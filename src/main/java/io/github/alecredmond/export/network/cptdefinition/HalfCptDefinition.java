package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.implementation.BinaryNode;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.BinaryCptDefinitionPadder;

public class HalfCptDefinition extends OrderedCptDefinition {

  public HalfCptDefinition(
      BinaryNode node,
      double[] trueProbabilities,
      CptNodeOrder cptNodeOrder,
      CptStrideOrder cptStrideOrder) {
    super(node, new double[trueProbabilities.length * 2], cptNodeOrder, cptStrideOrder);
    new BinaryCptDefinitionPadder(node, vector, trueProbabilities).writeVectorAndPad();
  }
}
