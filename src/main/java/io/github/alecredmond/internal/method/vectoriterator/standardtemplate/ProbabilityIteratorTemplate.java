package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ProbabilityIteratorTemplate
    extends CartesianIteratorTemplate<Node, NodeState> {
  Function<Node, NodeState> initialStatePositionSetter();

  Predicate<Node> checkLockOuter();

  Predicate<Node> checkLockInner();

  Function<Node, boolean[]> updateEvidenceArrays();
}
