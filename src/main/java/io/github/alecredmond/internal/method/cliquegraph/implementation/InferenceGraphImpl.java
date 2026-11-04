package io.github.alecredmond.internal.method.cliquegraph.implementation;

import static io.github.alecredmond.export.observable.NodeObservation.eliminate;
import static io.github.alecredmond.export.observable.NodeObservation.observe;

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
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import java.util.*;
import java.util.function.Function;

public class InferenceGraphImpl extends CliqueGraph<GraphDataInference> implements InferenceGraph {

  public InferenceGraphImpl(GraphDataInference data, InferenceExternalWriter externalWriter) {
    super(data, externalWriter);
  }

  @Override
  public void resetObservations() {
    Arrays.stream(data.getCliques()).forEach(Clique::resetObservations);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
    passMessages();
    data.setObservedEvidence(data.getUnobservedBackup());
    data.setJointProbability(calculateJointProbability());
    externalWriter.writeToExternal();
  }

  @Override
  public double getPosteriorProbability(NodeState[] measured) {
    double prior = data.getJointProbability();
    if (prior == 0.0) return prior;
    Map<Node, NodeObservation> priorEvidence = data.getObservedEvidence();
    if (negatesPriorEvidence(priorEvidence, measured)) return 0.0;
    double joint = modifyGraphState(observe(priorEvidence, measured));
    modifyGraphState(priorEvidence);
    return joint / prior;
  }

  private boolean negatesPriorEvidence(
      Map<Node, NodeObservation> priorEvidence, NodeState[] measured) {
    for (NodeState state : measured) {
      if (priorEvidence.get(state.getNode()).eliminatedStates().contains(state)) return true;
    }
    return false;
  }

  private double modifyGraphState(Map<Node, NodeObservation> finalInferenceState) {
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
    Arrays.stream(data.getCliques()).forEach(c -> c.applyState(finalInferenceState));
    passMessages();
    return calculateJointProbability();
  }

  private void passMessages() {
    Clique clique = data.getCliques()[0];
    messagePasser.collectMessages(clique);
    messagePasser.distributeMessages(clique);
  }

  private double calculateJointProbability() {
    double separatorSums = productOfSums(data.getSeparators(), Separator::getTable);
    if (separatorSums == 0.0) return 0.0;
    double cliqueSums = productOfSums(data.getCliques(), Clique::getTable);
    return cliqueSums / separatorSums;
  }

  private <T> double productOfSums(T[] array, Function<T, JunctionTreeTable> tableFunction) {
    return Arrays.stream(array)
        .map(tableFunction)
        .mapToDouble(table -> table.sumProbabilities(List.of()))
        .reduce(1.0, (x, y) -> x * y);
  }

  @Override
  public void setObservedStates(NodeState... observedStates) {
    Map<Node, NodeObservation> newState = observe(data.getUnobservedBackup(), observedStates);
    double jointProb = modifyGraphState(newState);
    data.setJointProbability(jointProb);
    data.setObservedEvidence(newState);
    externalWriter.writeToExternal();
  }

  @Override
  public void eliminateStates(NodeState... toEliminate) {
    if (toEliminate.length == 0) return;
    Map<Node, NodeObservation> newState = eliminate(getObservedEvidence(), toEliminate);
    double jointProb = modifyGraphState(newState);
    data.setObservedEvidence(newState);
    data.setJointProbability(jointProb);
    externalWriter.writeToExternal();
  }

  @Override
  public Map<Node, NodeObservation> getObservedEvidence() {
    return data.getObservedEvidence();
  }

  @Override
  public ObservedTable[] getObservedTables() {
    return data.getObservedTables();
  }

  @Override
  public BayesianNetwork getNetwork() {
    return data.getNetwork();
  }
}
