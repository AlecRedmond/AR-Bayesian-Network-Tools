package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.util.Arrays;
import java.util.Set;

public interface CliqueGraphTable {
  Set<Node> getNodes();

  void resetPotentials();

  default double sumPotentials() {
    return Arrays.stream(getVector().getProbabilities()).sum();
  }

  ProbabilityVector getVector();
}
