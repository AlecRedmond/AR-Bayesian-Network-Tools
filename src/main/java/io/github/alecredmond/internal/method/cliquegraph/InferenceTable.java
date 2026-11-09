package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

public interface InferenceTable {
  Set<Node> getNodes();

  void resetPotentials();

  default double sumPotentials() {
    return Arrays.stream(getVector().getProbabilities()).sum();
  }

  ProbabilityVector getVector();

  default double[] getPotentials() {
    return getVector().getProbabilities();
  }

  double sumPotentials(Collection<NodeState> states);

  NodeState[] getStatesFromIndex(int index);

  <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier);
}
