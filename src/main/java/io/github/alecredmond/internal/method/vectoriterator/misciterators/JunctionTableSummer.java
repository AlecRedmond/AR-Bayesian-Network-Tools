package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JunctionTableSummer implements ProbabilityIteratorTemplate {
  private final StandardCartesianIterator iterator;
  private final JunctionTreeTable table;
  private final double[] adder = {0.0};
  private final Set<Node> requestNodes;
  private final Set<NodeState> requestStates;

  public JunctionTableSummer(JunctionTreeTable table) {
    this.table = table;
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.iterator = StandardCartesianIterator.create(this, table.getVector());
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
          iterator.iterateInner(i -> adder[0] += p[i]);
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
  public Function<Node, boolean[]> updateEvidenceArrays() {
    return updateEvidenceArraysCommon(requestNodes, requestStates, Node::getStates);
  }
}
