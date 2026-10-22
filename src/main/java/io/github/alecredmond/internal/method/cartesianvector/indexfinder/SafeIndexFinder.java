package io.github.alecredmond.internal.method.cartesianvector.indexfinder;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cartesianvector.CartesianState;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class SafeIndexFinder<N extends CartesianVariable, S extends CartesianState>
    extends AbstractIndexFinder<N, S> {

  public static SafeIndexFinder<Node, NodeState> create(ProbabilityVector vector) {
    return build(vector, SafeIndexFinder::new);
  }

  public SafeIndexFinder(
      CartesianVector<N, S> vector,
      Function<S, N> getNodeFn,
      Function<S, Integer> statePositionFinder,
      Function<N, Integer> nodeIndexFinder) {
    super(vector, getNodeFn, statePositionFinder, nodeIndexFinder);
  }

  @Override
  public int[] getPositionsFromStates(Collection<S> states) {
    ProcessedPositions pp = convertStatesToPositions(states);
    if (pp.numberProcessed == vector.getOrderedNodes().length) {
      return pp.statePositions;
    }
    throwUnassociatedNodeError(states, pp, vector.getOrderedNodes().length);
    return new int[0];
  }

  protected final ProcessedPositions convertStatesToPositions(Collection<S> states) {
    int arrayLength = vector.getOrderedNodes().length;
    boolean[] nodeProcessed = new boolean[arrayLength];
    int numberProcessed = 0;
    int[] statePositions = new int[arrayLength];
    for (S state : states) {
      N node = getNodeFn.apply(state);
      Integer nodeIndex = nodeIndexFinder.apply(node);
      if (nodeIndex == null) continue;
      boolean firstTimeProcessed = (nodeProcessed[nodeIndex] = !nodeProcessed[nodeIndex]);
      if (!firstTimeProcessed) {
        throw new ProbabilityTableRequestException(
            "Node %s was processed twice during safety checks".formatted(node));
      }
      statePositions[nodeIndex] = statePositionFinder.apply(state);
      numberProcessed++;
    }
    return new ProcessedPositions(statePositions, nodeProcessed, numberProcessed);
  }

  protected final void throwUnassociatedNodeError(
      Collection<S> states, ProcessedPositions pp, int lastIdx) {
    N[] orderedNodes = vector.getOrderedNodes();
    boolean[] nodeProcessed = pp.nodeProcessed;
    List<N> unassociated =
        IntStream.range(0, lastIdx)
            .filter(i -> !nodeProcessed[i])
            .mapToObj(i -> orderedNodes[i])
            .toList();
    throw new ProbabilityTableRequestException(
        "Unassociated nodes on vector %s in request %s"
            .formatted(CollectionToString.apply(unassociated), CollectionToString.apply(states)));
  }

  protected record ProcessedPositions(
      int[] statePositions, boolean[] nodeProcessed, int numberProcessed) {}
}
