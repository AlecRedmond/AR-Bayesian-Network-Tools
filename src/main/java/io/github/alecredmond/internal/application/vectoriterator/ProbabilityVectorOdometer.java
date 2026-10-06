package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProbabilityVectorOdometer extends CartesianOdometer<Node, NodeState> {

  public ProbabilityVectorOdometer(ProbabilityVector vector) {
    super(vector);
  }

}
