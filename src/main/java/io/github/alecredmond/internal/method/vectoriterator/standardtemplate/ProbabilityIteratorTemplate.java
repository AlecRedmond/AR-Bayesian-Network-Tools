package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.ProbabilityVectorOdometer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ProbabilityIteratorTemplate
    extends CartesianIteratorTemplate<Node, NodeState, ProbabilityVector> {
  Function<Node, NodeState> initialStatePositionSetter();

  Predicate<Node> checkLockOuter();

  Predicate<Node> checkLockInner();

  Function<Node, boolean[]> updateEvidenceArrays();

  @Override
  default Function<ProbabilityVector, CartesianOdometer<Node, NodeState>> createOdometer() {
    return ProbabilityVectorOdometer::new;
  }
}
