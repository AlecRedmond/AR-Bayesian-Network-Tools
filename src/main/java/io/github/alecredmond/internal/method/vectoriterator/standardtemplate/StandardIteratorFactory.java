package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic;
import io.github.alecredmond.internal.application.vectoriterator.IteratorFactoryData;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.application.vectoriterator.evidencetest.ConstantEvidenceChecker;
import io.github.alecredmond.internal.application.vectoriterator.evidencetest.OdometerEvidenceChecker;
import io.github.alecredmond.internal.application.vectoriterator.evidencetest.VariableEvidenceChecker;
import io.github.alecredmond.internal.application.vectoriterator.initialpositionsetter.InitialPositionSetter;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.ConstantPositionLock;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.PositionLock;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.VariablePositionLock;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.function.Function;

public class StandardIteratorFactory {
  private StandardIteratorFactory() {
    /* This utility class should not be instantiated */
  }

  public static VectorIterator create(
      StandardVectorIteratorTemplate template, ProbabilityVector vector) {
    return new VectorIterator(createFactoryData(template, vector));
  }

  public static IteratorFactoryData createFactoryData(
      StandardVectorIteratorTemplate template, ProbabilityVector vector) {
    VectorOdometer odometer = new VectorOdometer(vector);
    Function<Node, NodeState> initialStateSetterFn = template.initialStatePositionSetter();
    InitialPositionSetter<Node, NodeState, VectorOdometer> setter =
        new InitialPositionSetter<>(odometer, initialStateSetterFn);
    CartesianIteratorLogic.ResetLogicType resetLogicType = template.getResetLogicType();
    PositionLock<Node, NodeState, VectorOdometer> lockOuter = null;
    PositionLock<Node, NodeState, VectorOdometer> lockInner = null;
    OdometerEvidenceChecker<Node, NodeState, VectorOdometer> evidenceChecker = null;
    if (resetLogicType.equals(CartesianIteratorLogic.ResetLogicType.CONSTANT)) {
      lockOuter = new ConstantPositionLock<>(odometer, setter, template.checkLockOuter());
      lockInner = new ConstantPositionLock<>(odometer, setter, template.checkLockInner());
      evidenceChecker = new ConstantEvidenceChecker<>(odometer, template.evidenceChecker());
    } else if (resetLogicType.equals(CartesianIteratorLogic.ResetLogicType.VARIABLE)) {
      lockOuter = new VariablePositionLock<>(odometer, setter, template.checkLockOuter());
      lockInner = new VariablePositionLock<>(odometer, setter, template.checkLockInner());
      evidenceChecker = new VariableEvidenceChecker<>(odometer, template.evidenceChecker());
    } else {
      throw new IllegalArgumentException(
          "No Reset Logic Type was given for Iterator Template %s".formatted(template));
    }
    return new IteratorFactoryData(
        odometer, template.updateConsumerType(), setter, lockOuter, lockInner, evidenceChecker);
  }
}
