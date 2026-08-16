package io.github.alecredmond.internal.method.junctiontree;

import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.junctiontree.Separator;
import io.github.alecredmond.internal.application.solver.SolverConfigs;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class JunctionTreeAlgorithm {
  private final JunctionTreeData data;
  private final JTANetworkWriter networkWriter;
  private final MessagePasser messagePasser;

  public JunctionTreeAlgorithm(JunctionTreeData data) {
    this.data = data;
    this.networkWriter = new JTANetworkWriter(data);
    this.messagePasser = new MessagePasser(data);
    networkWriter.initializeJunctionTreeFromNetwork();
  }

  public static JunctionTreeAlgorithm buildForSolver(
      BayesianNetworkData bnd, SolverConfigs configs) {
    return new JunctionTreeAlgorithm(
        new JTADataBuilder().buildNewSolverConfiguration(bnd, configs));
  }

  public static JunctionTreeAlgorithm buildForInference(
      BayesianNetworkData bnd, InferenceAlgorithm inferenceAlgorithm) {
    return new JunctionTreeAlgorithm(
        new JTADataBuilder().buildNewInferenceConfiguration(bnd, inferenceAlgorithm));
  }

  public void rebuildJTA(BayesianNetworkData bnd, InferenceAlgorithm inferenceAlgorithm) {
    new JTADataBuilder().buildInferenceConfiguration(data, bnd, inferenceAlgorithm);
    networkWriter.initializeJunctionTreeFromNetwork();
  }

  public void observeNetwork(Collection<NodeState> observedStates) {
    Map<Node, Set<NodeState>> observed = NodeUtils.generateMultiRequest(observedStates);
    resetObservations();
    if (observed.isEmpty()) passMessages(data.getCliques()[0]);
    else applyObservationActions(observed, Clique::setObserved);
    setObservedEvidence(observedStates);
    data.setJointProbability(getJointProbOfMeasured(new HashSet<>()));
    networkWriter.writeObservations();
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

  private void setObservedEvidence(Collection<NodeState> observed) {
    if (observed.isEmpty()) this.data.setObservedEvidence(data.getUnobservedBackup());
    buildNewObservedMap(observed, NodeObservation::observe);
  }

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

  private void buildNewObservedMap(
      Collection<NodeState> observedStates,
      BiFunction<Map<Node, NodeObservation>, Collection<NodeState>, Map<Node, NodeObservation>>
          observationFunction) {
    data.setObservedEvidence(observationFunction.apply(data.getObservedEvidence(), observedStates));
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

  public void eliminateStates(Collection<NodeState> toEliminate) {
    if (toEliminate.isEmpty()) return;
    Map<Node, Set<NodeState>> eliminationMap = NodeUtils.generateMultiRequest(toEliminate);
    applyObservationActions(eliminationMap, Clique::eliminateStates);
    buildNewObservedMap(toEliminate, NodeObservation::eliminate);
    data.setJointProbability(getJointProbOfMeasured(new HashSet<>()));
    networkWriter.writeObservations();
  }

  public void normalizeTables() {
    Arrays.stream(data.getCliques()).forEach(Clique::normalizeTable);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }

  public void writeTablesToNetwork() {
    networkWriter.writeBackToCPTs();
  }

  public double getJointProbability() {
    return data.getJointProbability();
  }

  public void sumTransfer(Clique clique) {
    messagePasser.distributeMessages(clique);
  }

  private record ObservationOverlap(
      Clique clique, Set<Node> nodeOverlap, Set<NodeState> evidenceStates) {}
}
