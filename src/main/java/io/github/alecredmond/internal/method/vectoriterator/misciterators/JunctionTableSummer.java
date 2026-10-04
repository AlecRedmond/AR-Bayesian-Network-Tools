package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JunctionTableSummer implements OdometerResetDefault {
  private final VectorIterator iterator;
  private final JunctionTreeTable table;
  private final double[] adder = {0.0};
  private final VectorOdometer odometer;
  private Set<Node> requestNodes;
  private Set<NodeState> requestStates;

  public JunctionTableSummer(JunctionTreeTable table) {
    this.table = table;
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.iterator = StandardIteratorFactory.create(this, table.getVector());
    this.odometer = iterator.getOdometer();
  }

  public double sum(Collection<NodeState> states) {
    this.requestStates.clear();
    this.requestNodes.clear();
    this.requestStates.addAll(states);
    this.requestNodes.addAll(NodeUtils.getNodes(states));
    iterator.reset();

    double[] p = table.getProbabilities();
    BooleanSupplier evidenceTest = iterator.getIsEvidenceCheck();

    adder[0] = 0.0;
    iterator.iterateOuter(
        () -> {
          if (!evidenceTest.getAsBoolean()) return;
          iterator.iterateInner((o, i) -> adder[0] += p[i]);
        });
    return adder[0];
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        requestNodes.contains(node)
            ? node.getStates().stream().filter(requestStates::contains).findFirst().orElseThrow()
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

  protected boolean checkIsEvidence(int[] stateIndexes, boolean[][] stateIsEvent) {
    return ResetLogicUtils.checkIsEvidence(stateIndexes, stateIsEvent);
  }
}
