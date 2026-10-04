package io.github.alecredmond.internal.method.vectoriterator;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.IteratorFactoryData;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.application.vectoriterator.evidencetest.OdometerEvidenceChecker;
import io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter.InitialPositionSetter;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.PositionLock;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;

import java.util.function.BooleanSupplier;
import java.util.function.ObjIntConsumer;
import lombok.Getter;

@Getter
public class VectorIterator extends CartesianVectorIterator<Node, NodeState, VectorOdometer> {

  private final PositionLock<Node, NodeState, VectorOdometer> outerLocks;
  private final PositionLock<Node, NodeState, VectorOdometer> innerLocks;
  private final OdometerEvidenceChecker<Node, NodeState, VectorOdometer> evidenceChecker;

  public VectorIterator(
      VectorOdometer odometer,
      UpdateStateArrayLogic stateUpdateLogic,
      InitialPositionSetter<Node, NodeState, VectorOdometer> setter,
      PositionLock<Node, NodeState, VectorOdometer> outerLocks,
      PositionLock<Node, NodeState, VectorOdometer> innerLocks,
      OdometerEvidenceChecker<Node, NodeState, VectorOdometer> evidenceChecker) {
    super(odometer, stateUpdateLogic, setter, outerLocks, innerLocks, evidenceChecker);
    this.outerLocks = outerLocks;
    this.innerLocks = innerLocks;
    this.evidenceChecker = evidenceChecker;
  }

  public VectorIterator(IteratorFactoryData data) {
    super(
        data.odometer(),
        data.updateStateArrayLogic(),
        data.setter(),
        data.outerLocks(),
        data.innerLocks(),
        data.evidenceChecker());
    this.outerLocks = data.outerLocks();
    this.innerLocks = data.innerLocks();
    this.evidenceChecker = data.evidenceChecker();
  }

  public void iterateOuter(Runnable runnable) {
    super.iterateOuter(runnable, outerLocks);
  }

  public void iterateOuter(ObjIntConsumer<VectorOdometer> indexConsumer) {
    super.iterate(indexConsumer, outerLocks);
  }

  public void iterateInner(ObjIntConsumer<VectorOdometer> indexConsumer) {
    super.iterate(indexConsumer, innerLocks);
  }

  public BooleanSupplier getIsEvidenceCheck(){
      return evidenceChecker.getTest();
  }
}
