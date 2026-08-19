package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
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

public class JunctionTableSummer implements OdometerResetDefault, OdometerUpdateBlank {
  private final VectorIterator<VectorOdometer> iterator;
  private final JunctionTreeTable table;
  private final double[] adder = {0.0};
  private final VectorOdometer odometer;
  private Set<Node> requestNodes;
  private Set<NodeState> requestStates;

  public JunctionTableSummer(JunctionTreeTable table) {
    this.table = table;
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.odometer = new VectorOdometer(table.getVector());
    this.iterator = new VectorIterator<>(odometer, this);
  }

  public double sum(Collection<NodeState> states) {
    this.requestStates = new HashSet<>(states);
    this.requestNodes = NodeUtils.getNodes(states);
    iterator.reset();

    double[] p = table.getProbabilities();
    int[] stateIndexes = odometer.getStateIndexes();
    boolean[][] stateIsEvent = odometer.getNodeStateEvidenceArray();

    adder[0] = 0.0;
    iterator.iterateOuter(
        () -> {
          if (!checkIsEvidence(stateIndexes, stateIsEvent)) return;
          iterator.iterateInner((o, i) -> adder[0] += p[i]);
        });
    return adder[0];
  }

  protected boolean checkIsEvidence(int[] stateIndexes, boolean[][] stateIsEvent) {
    return ResetLogicUtils.checkIsEvidence(stateIndexes, stateIsEvent);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        requestNodes.contains(node)
            ? node.getNodeStates().stream()
                .filter(requestStates::contains)
                .findFirst()
                .orElseThrow()
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
