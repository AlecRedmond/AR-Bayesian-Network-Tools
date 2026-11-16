package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

public interface InferenceTable extends ProbabilityTable {
  void resetPotentials();

  default double sumPotentials() {
    return Arrays.stream(getProbabilities()).sum();
  }

  @Override
  default double[] getProbabilities() {
    return getVector().getProbabilities();
  }

  ProbabilityVector getVector();

  NodeState[] getStatesFromIndex(int index);

  <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier);

  default double sumPotentials(NodeState... states) {
    return sumPotentials(Arrays.asList(states));
  }

  double sumPotentials(Collection<NodeState> states);

  Set<Node> getNodes();

  @Override
  default Set<Node> getConditions() {
    return Set.of();
  }

  @Override
  default Set<Node> getEvents() {
    return getNodes();
  }
}
