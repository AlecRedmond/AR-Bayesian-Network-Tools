package io.github.alecredmond.internal.method.cliquegraph.implementation;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.cliquegraph.InferenceGraph;
import io.github.alecredmond.internal.method.cliquegraph.externalwriters.InferenceExternalWriter;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InferenceGraphImpl extends CliqueGraph<GraphDataInference> implements InferenceGraph {

  public InferenceGraphImpl(GraphDataInference data, InferenceExternalWriter externalWriter) {
    super(data, externalWriter);
  }

  @Override
  public Map<Node, NodeObservation> getObservedEvidence() {
    return data.getObservedEvidence();
  }

  private void setObservedEvidence(Collection<NodeState> observed) {
    if (observed.isEmpty()) this.data.setObservedEvidence(data.getUnobservedBackup());
    data.setObservedEvidence(NodeObservation.observe(data.getObservedEvidence(), observed));
  }

  @Override
  public ObservedTable[] getObservedTables() {
    return data.getObservedTables();
  }

  @Override
  public BayesianNetwork getNetwork() {
    return data.getNetwork();
  }

  @Override
  public void observeNetwork(Collection<NodeState> observedStates) {
    Map<Node, Set<NodeState>> observed = NodeUtils.generateMultiRequest(observedStates);
    resetObservations();
    if (observed.isEmpty()) passMessages(data.getCliques()[0]);
    else applyObservationActions(observed, Clique::setObserved);
    setObservedEvidence(observedStates);
    data.setJointProbability(getJointProbOfMeasured(new HashSet<>()));
    externalWriter.writeToExternal();
  }

  private void resetObservations() {
    Arrays.stream(data.getCliques()).forEach(Clique::resetObservations);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  private void passMessages(Clique clique) {
    messagePasser.collectMessages(clique);
    messagePasser.distributeMessages(clique);
  }

  private void applyObservationActions(
      Map<Node, Set<NodeState>> stateMap, BiConsumer<Clique, Set<NodeState>> function) {
    Set<Node> nodesRemaining = new HashSet<>(stateMap.keySet());
    while (!nodesRemaining.isEmpty()) {
      ObservationOverlap overlap = findLargestOverlap(nodesRemaining, stateMap);
      nodesRemaining.removeAll(overlap.nodeOverlap);
      Clique clique = overlap.clique;
      function.accept(clique, overlap.evidenceStates);
      passMessages(clique);
    }
  }

  @Override
  public double getJointProbOfMeasured(Collection<NodeState> newEvidence) {
    double separatorSums = productOfSums(data.getSeparators(), Separator::getTable, newEvidence);
    if (separatorSums == 0.0) return 0.0;
    double cliqueSums = productOfSums(data.getCliques(), Clique::getTable, newEvidence);
    return cliqueSums / separatorSums;
  }

  private ObservationOverlap findLargestOverlap(
      Set<Node> nodesRemaining, Map<Node, Set<NodeState>> stateMap) {
    return Arrays.stream(data.getCliques())
        .map(c -> buildObservationOverlap(c, nodesRemaining, stateMap))
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(c -> c.nodeOverlap.size()))
        .orElseThrow();
  }

  private <T> double productOfSums(
      T[] array, Function<T, JunctionTreeTable> tableFunction, Collection<NodeState> newEvidence) {
    return Arrays.stream(array)
        .map(tableFunction)
        .mapToDouble(table -> table.sumProbabilities(newEvidence))
        .reduce(1.0, (x, y) -> x * y);
  }

  private ObservationOverlap buildObservationOverlap(
      Clique clique, Set<Node> nodesRemaining, Map<Node, Set<NodeState>> stateMap) {
    Set<Node> nodeOverlap = NodeUtils.getOverlap(clique.getNodes(), nodesRemaining);
    if (nodeOverlap.isEmpty()) return null;
    Set<NodeState> states =
        nodeOverlap.stream()
            .map(stateMap::get)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    return new ObservationOverlap(clique, nodeOverlap, states);
  }

  @Override
  public void eliminateStates(Collection<NodeState> toEliminate) {
    if (toEliminate.isEmpty()) return;
    Map<Node, Set<NodeState>> eliminationMap = NodeUtils.generateMultiRequest(toEliminate);
    applyObservationActions(eliminationMap, Clique::eliminateStates);
    data.setObservedEvidence(NodeObservation.eliminate(data.getObservedEvidence(), toEliminate));
    data.setJointProbability(getJointProbOfMeasured(new HashSet<>()));
    externalWriter.writeToExternal();
  }

  @Override
  public double getJointProbability() {
    return data.getJointProbability();
  }

  private record ObservationOverlap(
      Clique clique, Set<Node> nodeOverlap, Set<NodeState> evidenceStates) {}
}
