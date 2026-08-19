package io.github.alecredmond.internal.method.inference;

import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.method.junctiontree.JunctionTreeAlgorithm;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import java.io.Serializable;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class InferenceEngineImpl implements InferenceEngine {
  private final BayesianNetwork network;
  private final BayesSolver solver;
  private final JunctionTreeAlgorithm junctionTree;
  private final InferenceAlgorithm inferenceAlgorithm;

  public InferenceEngineImpl(
      BayesianNetwork network,
      BayesSolver solver,
      JunctionTreeAlgorithm junctionTree,
      InferenceAlgorithm inferenceAlgorithm) {
    this.network = network;
    this.junctionTree = junctionTree;
    this.solver = solver;
    this.inferenceAlgorithm = inferenceAlgorithm;
    resetObservations();
  }

  @Override
  public InferenceEngineImpl resetObservations() {
    return this.setObserved(List.of());
  }

  @Override
  public InferenceEngine setObserved(Map<Node, NodeObservation> observations) {
    return eliminateStates(NetworkDataUtils.convertToEliminations(this, network, observations));
  }

  @Override
  public InferenceEngine eliminateStates(Collection<NodeState> toEliminate) {
    if (!ensureSolved()) {
      return this;
    }
    junctionTree.eliminateStates(toEliminate);
    return this;
  }

  @Override
  public InferenceEngine eliminateStates(NodeState toEliminate) {
    return eliminateStates(List.of(toEliminate));
  }

  @Override
  public InferenceEngineImpl setObserved(Collection<NodeState> observed) {
    if (!ensureSolved()) {
      return this;
    }
    junctionTree.observeNetwork(observed);
    return this;
  }

  private boolean ensureSolved() {
    if (solver.isSolved()) {
      return true;
    }
    log.info(
        "Modifications were detected on network {}, solver will be re-run",
        network.getNetworkData().getNetworkName());
    if (solver.solve()) {
      junctionTree.rebuildJTA(network.getNetworkData(), inferenceAlgorithm);
      return true;
    }
    log.error("Could not solve network {}!", network.getNetworkData().getNetworkName());
    return false;
  }
@Override
  public InferenceEngine setObserved(NodeState observedState) {
    return this.setObserved(List.of(observedState));
  }@Override
  public <T extends Serializable> InferenceEngine setObservedById(T observedStateId) {
    return setObservedById(List.of(observedStateId));
  }

    @Override
  public <T extends Serializable> InferenceEngine setObservedById(Collection<T> observedStateIDs) {
    return this.setObserved(
        NetworkDataUtils.getStatesByID(observedStateIDs, network.getNetworkData()));
  }





  @Override
  public Map<Node, NodeObservation> getCurrentObservations() {
    return ensureSolved() ? junctionTree.getData().getObservedEvidence() : new HashMap<>();
  }

  @Override
  public <T extends Serializable> InferenceEngine eliminateStatesById(
      Collection<T> toEliminateIDs) {
    return eliminateStates(
        NetworkDataUtils.getStatesByID(toEliminateIDs, network.getNetworkData()));
  }

  @Override
  public <T extends Serializable> InferenceEngine eliminateStatesById(T toEliminateIDs) {
    return eliminateStatesById(List.of(toEliminateIDs));
  }

  @Override
  public <T extends Serializable> ObservedTable getObservedTableById(T nodeId) {
    Node node = network.getNode(nodeId);
    return junctionTree.getData().getObservedTablesMap().get(node);
  }

  @Override
  public ObservedTable getObservedTable(Node node) {
    return ensureSolved() ? junctionTree.getData().getObservedTablesMap().get(node) : null;
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
  public Map<Node, ObservedTable> getObservedTables() {
    return ensureSolved() ? junctionTree.getData().getObservedTablesMap() : new HashMap<>();
  }

  @Override
  public double getPosteriorProbability(Collection<NodeState> measuredStates) {
    if (!ensureSolved()) {
      return 0.0;
    }
    double currentJointProb = junctionTree.getJointProbability();
    if (currentJointProb == 0.0) return 0.0;
    double newJointProb = junctionTree.getJointProbOfMeasured(measuredStates);
    return newJointProb / currentJointProb;
  }

  @Override
  public double getPosteriorProbability(NodeState measuredState) {
    return getPosteriorProbability(List.of(measuredState));
  }

  @Override
  public <T extends Serializable> double getPosteriorProbabilityById(
      Collection<T> measuredStateIds) {
    return getPosteriorProbability(
        NetworkDataUtils.getStatesByID(measuredStateIds, network.getNetworkData()));
  }

  @Override
  public <T extends Serializable> double getPosteriorProbabilityById(T measuredStateId) {
    return getPosteriorProbabilityById(List.of(measuredStateId));
  }

  @Override
  public InferenceEngine printObserved() {
    if (!ensureSolved()) return this;
    Map<Node, NodeObservation> observed = getCurrentObservations();
    List<Node> conditionalOnObserved =
        network.getNetworkData().getNodes().stream()
            .filter(node -> !observed.get(node).status().equals(ObservationStatus.OBSERVED))
            .toList();
    printObserved(conditionalOnObserved);
    return this;
  }

  @Override
  public <T extends Serializable> InferenceEngine printObservedById(Collection<T> nodeIds) {
    return printObserved(network.getNodes(nodeIds));
  }

  @Override
  public <T extends Serializable> InferenceEngine printObservedById(T nodeId) {
    return printObservedById(List.of(nodeId));
  }

  @Override
  public InferenceEngine printObserved(Collection<Node> nodes) {
    if (!ensureSolved()) return this;
    Map<Node, ObservedTable> observedTables = junctionTree.getData().getObservedTablesMap();
    Map<Node, ObservedTable> toPrint = new LinkedHashMap<>();
    nodes.stream()
        .filter(observedTables::containsKey)
        .forEach(node -> toPrint.put(node, observedTables.get(node)));
    new NetworkPrinter(this).printTables(toPrint, "OBSERVED");
    return this;
  }

  @Override
  public InferenceEngine printObserved(Node node) {
    return printObserved(List.of(node));
  }
}
