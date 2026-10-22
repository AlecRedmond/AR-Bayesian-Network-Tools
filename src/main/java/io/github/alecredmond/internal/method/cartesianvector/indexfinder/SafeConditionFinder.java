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
import java.util.function.Function;

public class SafeConditionFinder<N extends CartesianVariable, S extends CartesianState>
    extends SafeIndexFinder<N, S> {

  public static SafeConditionFinder<Node, NodeState> create(ProbabilityVector vector) {
    return build(vector, SafeConditionFinder::new);
  }

  public SafeConditionFinder(
      CartesianVector<N, S> vector,
      Function<S, N> getNodeFn,
      Function<S, Integer> statePositionFinder,
      Function<N, Integer> nodeIndexFinder) {
    super(vector, getNodeFn, statePositionFinder, nodeIndexFinder);
  }

  @Override
  public int[] getPositionsFromStates(Collection<S> states) {
    ProcessedPositions pp = convertStatesToPositions(states);
    int lastIdx = vector.getOrderedNodes().length - 1;
    boolean correctNumberProcessed = pp.numberProcessed() == lastIdx;
    boolean eventNodeUnprocessed = !pp.nodeProcessed()[lastIdx];
    if (correctNumberProcessed && eventNodeUnprocessed) {
      return pp.statePositions();
    } else if (eventNodeUnprocessed) {
      throwUnassociatedNodeError(states, pp, lastIdx);
    }
    throw new ProbabilityTableRequestException(
        "Conditional position request {%s} contained an event node!"
            .formatted(CollectionToString.apply(states)));
  }
}
