package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetDefault;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.OdometerUpdateBlank;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ObservationCopier implements OdometerResetDefault, OdometerUpdateBlank {
  private final ProbabilityVector mainVector;
  private final ProbabilityVector backupVector;
  private final VectorIterator<VectorOdometer> iterator;
  private final VectorOdometer odometer;
  private Set<NodeState> requestStates;
  private Set<Node> requestNodes;

  public ObservationCopier(JunctionTreeTable table) {
    this.backupVector = table.getBackupVector();
    this.mainVector = table.getVector();
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.iterator = new VectorIterator<>(mainVector, this, VectorOdometer::new);
    this.odometer = iterator.getController().getOdometer();
  }

  public void observeStates(Collection<NodeState> observedStates) {
    this.requestStates = new HashSet<>(observedStates);
    this.requestNodes = new HashSet<>(NodeUtils.getNodes(requestStates));
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

    int[] stateIndexes = odometer.getStateIndexes();
    boolean[][] isEvidenceArray = odometer.getNodeStateEvidenceArray();
    iterator.iterateOuter(
        () -> {
          if (ResetLogicUtils.checkIsEvidence(stateIndexes, isEvidenceArray)) {
            iterator.iterateInner((o, i) -> observed[i] = backup[i]);
          }
        });
  }

  public void eliminateStates(Collection<NodeState> toEliminate) {
    for (NodeState nodeState : toEliminate) {
      Node node = nodeState.getNode();
      if (requestNodes.add(node)) {
        requestStates.addAll(node.getNodeStates());
      }
      requestStates.remove(nodeState);
    }
    resetAndRunIterator();
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        requestNodes.contains(node)
            ? node.getNodeStates().stream()
                .filter(requestStates::contains)
                .findFirst()
                .orElse(node.getNodeStates().getFirst())
            : node.getNodeStates().getFirst();
  }

  @Override
  public Function<Node, boolean[]> buildEvidenceMaps() {
    return ResetLogicUtils.updateEvidenceArrayFunction(requestNodes, requestStates);
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> !requestNodes.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return requestNodes::contains;
  }
}
