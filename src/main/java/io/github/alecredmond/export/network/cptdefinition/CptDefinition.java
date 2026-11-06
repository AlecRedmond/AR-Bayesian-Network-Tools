package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;

public interface CptDefinition {
  ProbabilityVector getVector();

  Node measuredNode();
}
