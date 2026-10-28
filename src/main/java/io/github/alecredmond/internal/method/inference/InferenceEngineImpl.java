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
  private final InferenceGraph inferenceGraph;

  protected InferenceEngineImpl(
      BayesianNetwork network, InferenceGraphFactory inferenceGraphFactory) {
    this.network = network;
    this.inferenceGraph = inferenceGraphFactory.build(network);
    resetObservations();
  }

  @Override
  public InferenceEngineImpl resetObservations() {
    return this.setObserved(List.of());
  }

  @Override
  public InferenceEngine setObserved(Map<Node, NodeObservation> observations) {
    return eliminateStates(ObservableUtils.convertToEliminations(this, network, observations));
  }

  @Override
  public InferenceEngine eliminateStates(Collection<NodeState> toEliminate) {
    inferenceGraph.eliminateStates(toEliminate);
    return this;
  }

  @Override
  public InferenceEngine eliminateStates(NodeState toEliminate) {
    return eliminateStates(List.of(toEliminate));
  }

  @Override
  public InferenceEngineImpl setObserved(Collection<NodeState> observed) {
    inferenceGraph.observeNetwork(observed);
    return this;
  }

  @Override
  public InferenceEngine setObserved(NodeState observedState) {
    return this.setObserved(List.of(observedState));
  }

  @Override
  public <T extends Serializable> InferenceEngine setObservedById(T observedStateId) {
    return setObservedById(List.of(observedStateId));
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
  public <T extends Serializable> InferenceEngine eliminateStatesById(
      Collection<T> toEliminateIDs) {
    return eliminateStates(network.getNodeStatesById(toEliminateIDs));
  }

  @Override
  public <T extends Serializable> InferenceEngine eliminateStatesById(T toEliminateIDs) {
    return eliminateStatesById(List.of(toEliminateIDs));
  }

  @Override
  public <T extends Serializable> ObservedTable getObservedTableById(T nodeId) {
    return inferenceGraph.getObservedTables()[network.getNodeOrdinalById(nodeId)];
  }

  @Override
  public ObservedTable getObservedTableByNodeName(String nodeName) {
    return inferenceGraph.getObservedTables()[network.getNodeOrdinalByName(nodeName)];
  }

  @Override
  public ObservedTable getObservedTable(Node node) {
    return getObservedTableById(node.getId());
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
    double currentJointProb = inferenceGraph.getJointProbability();
    if (currentJointProb == 0.0) return 0.0;
    double newJointProb = inferenceGraph.getJointProbOfMeasured(measuredStates);
    return newJointProb / currentJointProb;
  }

  @Override
  public double getPosteriorProbability(NodeState measuredState) {
    return getPosteriorProbability(List.of(measuredState));
  }

  @Override
  public <T extends Serializable> double getPosteriorProbabilityById(
      Collection<T> measuredStateIds) {
    return getPosteriorProbability(network.getNodeStatesById(measuredStateIds));
  }

  @Override
  public <T extends Serializable> double getPosteriorProbabilityById(T measuredStateId) {
    return getPosteriorProbabilityById(List.of(measuredStateId));
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
  public <T extends Serializable> InferenceEngine printObservedById(T nodeId) {
    return printObservedById(List.of(nodeId));
  }

  @Override
  public InferenceEngine printObserved(Collection<Node> nodes) {
    ObservedTable[] observedTables = inferenceGraph.getObservedTables();
    Map<Node, ObservedTable> toPrint = new LinkedHashMap<>();
    nodes.stream()
        .mapToInt(network::getNodeOrdinal)
        .mapToObj(i -> observedTables[i])
        .forEach(table -> toPrint.put(table.getMeasuredNode(), table));
    new NetworkPrinter(this).printTables(toPrint, "OBSERVED");
    return this;
  }

  @Override
  public InferenceEngine printObserved(Node node) {
    return printObserved(List.of(node));
  }
}
