package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetDefault;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Getter;

public class ObservationCopier implements OdometerResetDefault {
  private final ProbabilityVector mainVector;
  private final ProbabilityVector backupVector;
  private final VectorIterator iterator;
  @Getter private final VectorOdometer odometer;
  private final Set<NodeState> requestStates;
  private final Set<Node> requestNodes;

  public ObservationCopier(JunctionTreeTable table) {
    this.backupVector = table.getBackupVector();
    this.mainVector = table.getVector();
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.iterator = StandardIteratorFactory.create(this, mainVector);
    this.odometer = iterator.getOdometer();
  }

  public void observeStates(Collection<NodeState> observedStates) {
    this.requestStates.clear();
    this.requestNodes.clear();
    this.requestStates.addAll(observedStates);
    this.requestNodes.addAll(NodeUtils.getNodes(requestStates));
    if (observedStates.isEmpty()) writeFromBackupVector();
    else resetAndRunIterator();
  }

  private void writeFromBackupVector() {
    double[] backup = backupVector.getProbabilities();
    double[] observed = mainVector.getProbabilities();
    System.arraycopy(backup, 0, observed, 0, backup.length);
  }

  private void resetAndRunIterator() {
    iterator.reset();

    double[] observed = mainVector.getProbabilities();
    double[] backup = backupVector.getProbabilities();
    Arrays.fill(observed, 0.0);
    BooleanSupplier evidenceCheck = iterator.getIsEvidenceCheck();
    iterator.iterateOuter(
        () -> {
          if (evidenceCheck.getAsBoolean()) {
            iterator.iterateInner((o, i) -> observed[i] = backup[i]);
          }
        });
  }

  public void eliminateStates(Collection<NodeState> toEliminate) {
    for (NodeState nodeState : toEliminate) {
      Node node = nodeState.getNode();
      if (requestNodes.add(node)) {
        requestStates.addAll(node.getStates());
      }
      requestStates.remove(nodeState);
    }
    resetAndRunIterator();
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        requestNodes.contains(node)
            ? node.getStates().stream()
                .filter(requestStates::contains)
                .findFirst()
                .orElse(node.getStates().getFirst())
            : node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> !requestNodes.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return requestNodes::contains;
  }

  @Override
  public UpdateStateArrayLogic updateConsumerType() {
    return UpdateStateArrayLogic.NO_UPDATE;
  }

  @Override
  public Function<Node, boolean[]> evidenceChecker() {
    return ResetLogicUtils.updateEvidenceArrayFunction(requestNodes, requestStates);
  }
}
