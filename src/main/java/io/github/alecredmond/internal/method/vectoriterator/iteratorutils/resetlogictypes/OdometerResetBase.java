package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerResetLogic;
import java.util.function.Function;
import java.util.function.Predicate;

public interface OdometerResetBase extends OdometerResetLogic<VectorOdometer> {
  @Override
  default void resetOdometer(VectorOdometer vectorOdometer) {
    Node[] nodeArray = vectorOdometer.getNodeArray();
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    NodeState[] states = vectorOdometer.getStates();
    boolean[] outerIteratorLocks = vectorOdometer.getOuterIteratorLocks();
    boolean[] innerIteratorLocks = vectorOdometer.getInnerIteratorLocks();
    boolean[][] isEvidenceArray = vectorOdometer.getNodeStateEvidenceArray();

    Function<Node, NodeState> initialStateMapper = initialStatePositionSetter();
    Function<Node, boolean[]> evidenceMapper = buildEvidenceMaps();
    Predicate<Node> outerFn = checkLockOuter();
    Predicate<Node> innerFn = checkLockInner();

    for (int x = 0; x < nodeArray.length; x++) {
      Node node = nodeArray[x];
      NodeState state = initialStateMapper.apply(node);
      states[x] = state;
      stateIndexes[x] = state.getPosition();
      outerIteratorLocks[x] = outerFn.test(node);
      innerIteratorLocks[x] = innerFn.test(node);
      isEvidenceArray[x] = evidenceMapper.apply(node);
    }
  }

  Function<Node, NodeState> initialStatePositionSetter();

  Function<Node, boolean[]> buildEvidenceMaps();
}
