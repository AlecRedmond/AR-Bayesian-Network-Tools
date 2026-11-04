package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.observable.ObservationStatus;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.method.cliquegraph.InferenceGraph;
import io.github.alecredmond.internal.method.cliquegraph.factory.InferenceGraphFactory;
import io.github.alecredmond.internal.method.network.BayesNetIdConverter;
import io.github.alecredmond.internal.method.observable.ObservableUtils;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class InferenceEngineImpl implements InferenceEngine {
  private final BayesianNetwork network;
  private final BayesNetIdConverter converter;
  private final InferenceGraph inferenceGraph;

  protected InferenceEngineImpl(
      BayesianNetwork network, InferenceGraphFactory inferenceGraphFactory) {
    this.network = network;
    this.converter = new BayesNetIdConverter(network);
    this.inferenceGraph = inferenceGraphFactory.build(network);
    resetObservations();
  }

  @Override
  public InferenceEngineImpl resetObservations() {
    inferenceGraph.resetObservations();
    return this;
  }

  @Override
  public InferenceEngine setObserved(Map<Node, NodeObservation> observations) {
    return eliminateStates(ObservableUtils.convertToEliminations(this, network, observations));
  }

  @Override
  public InferenceEngine eliminateStates(Collection<NodeState> toEliminate) {
    inferenceGraph.eliminateStates(toEliminate.toArray(NodeState[]::new));
    return this;
  }

  @Override
  public <T extends Serializable> InferenceEngine eliminateStatesById(
      Collection<T> toEliminateIDs) {
    return eliminateStates(network.getNodeStatesById(toEliminateIDs));
  }

  @Override
  public InferenceEngineImpl setObserved(Collection<NodeState> observed) {
    inferenceGraph.setObservedStates(observed.toArray(NodeState[]::new));
    return this;
  }

  @Override
  public <T extends Serializable> InferenceEngine setObservedById(Collection<T> observedStateIDs) {
    return this.setObserved(network.getNodeStatesById(observedStateIDs));
  }

  @Override
  public Map<Node, NodeObservation> getCurrentObservations() {
    return new LinkedHashMap<>(inferenceGraph.getObservedEvidence());
  }

  @Override
  public <T extends Serializable> ObservedTable getObservedTableById(T nodeId) {
    return converter.accessByNodeId(nodeId, inferenceGraph.getObservedTables());
  }

  @Override
  public ObservedTable getObservedTableByNodeName(String nodeName) {
    return converter.accessByNodeName(nodeName, inferenceGraph.getObservedTables());
  }

  @Override
  public ObservedTable getObservedTable(Node node) {
    return converter.accessByNode(node, inferenceGraph.getObservedTables());
  }

  @Override
  public <T extends Serializable> ObservedTable copyObservedTableById(T nodeId) {
    return getObservedTableById(nodeId).copyTable();
  }

  @Override
  public ObservedTable copyObservedTable(Node node) {
    return getObservedTable(node).copyTable();
  }

  @Override
  public ObservedTable[] getObservedTables() {
    ObservedTable[] tables = inferenceGraph.getObservedTables();
    return Arrays.copyOf(tables, tables.length);
  }

  @Override
  public double getPosteriorProbability(Collection<NodeState> measuredStates) {
    return inferenceGraph.getPosteriorProbability(measuredStates.toArray(NodeState[]::new));
  }

  @Override
  public <T extends Serializable> double getPosteriorProbabilityById(
      Collection<T> measuredStateIds) {
    return getPosteriorProbability(network.getNodeStatesById(measuredStateIds));
  }

  @Override
  public InferenceEngine printObserved() {
    Map<Node, NodeObservation> observed = getCurrentObservations();
    List<Node> conditionalOnObserved =
        network
            .streamNodes()
            .filter(node -> !observed.get(node).status().equals(ObservationStatus.OBSERVED))
            .toList();
    printObserved(conditionalOnObserved);
    return this;
  }

  @Override
  public <T extends Serializable> InferenceEngine printObservedById(Collection<T> nodeIds) {
    return printObserved(network.getNodesByIds(nodeIds));
  }

  @Override
  public InferenceEngine printObserved(Collection<Node> nodes) {
    ObservedTable[] observedTables = inferenceGraph.getObservedTables();
    Map<Node, ObservedTable> toPrint = new LinkedHashMap<>();
    nodes.stream()
        .map(n -> converter.accessByNode(n, observedTables))
        .forEach(table -> toPrint.put(table.getMeasuredNode(), table));
    new NetworkPrinter(this).printTables(toPrint, "OBSERVED");
    return this;
  }

  @Override
  public InferenceEngine printObserved(Node node) {
    return printObserved(List.of(node));
  }
}
