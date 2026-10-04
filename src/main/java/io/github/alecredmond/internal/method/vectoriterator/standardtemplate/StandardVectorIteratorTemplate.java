package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic.ResetLogicType;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;

import java.util.function.Function;
import java.util.function.Predicate;

public interface StandardVectorIteratorTemplate {
  Function<Node, NodeState> initialStatePositionSetter();

  ResetLogicType getResetLogicType();

  Predicate<Node> checkLockOuter();

  Predicate<Node> checkLockInner();

  Function<Node, boolean[]> evidenceChecker();

  UpdateStateArrayLogic updateConsumerType();
}
