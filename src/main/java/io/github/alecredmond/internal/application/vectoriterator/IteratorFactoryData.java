package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.evidencetest.OdometerEvidenceChecker;
import io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter.InitialPositionSetter;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.PositionLock;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;

public record IteratorFactoryData(
    VectorOdometer odometer,
    UpdateStateArrayLogic updateStateArrayLogic,
    InitialPositionSetter<Node, NodeState, VectorOdometer> setter,
    PositionLock<Node, NodeState, VectorOdometer> outerLocks,
    PositionLock<Node, NodeState, VectorOdometer> innerLocks,
    OdometerEvidenceChecker<Node, NodeState, VectorOdometer> evidenceChecker) {}
