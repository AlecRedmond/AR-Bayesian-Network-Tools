package io.github.alecredmond.internal.method.cliquegraph.implementation;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.export.inference.direct.InferenceTable;
import io.github.alecredmond.export.inference.direct.Separator;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.method.cliquegraph.InferenceGraph;
import io.github.alecredmond.internal.method.cliquegraph.externalwriters.InferenceExternalWriter;
import io.github.alecredmond.internal.method.inference.NodeObservationFactory;
import java.util.*;

public class InferenceGraphImpl extends CliqueGraph<GraphDataInference> implements InferenceGraph {
  private final NodeObservationFactory observationFactory = new NodeObservationFactory();

  public InferenceGraphImpl(GraphDataInference data, InferenceExternalWriter externalWriter) {
    super(data, externalWriter);
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
  public void setNewObservations(Map<Node, NodeObservation> newObservations) {
    double jointProb = modifyGraphState(newObservations);
    data.setObservedEvidence(newObservations);
    data.setJointProbability(jointProb);
    externalWriter.writeToExternal();
  }

  @Override
  public void resetObservations() {
    Arrays.stream(data.getCliques()).forEach(Clique::resetPotentials);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetPotentials);
    passMessages();
    data.setObservedEvidence(observationFactory.initializeFromNetwork(data.getNetwork()));
    data.setJointProbability(calculateJointProbability());
    externalWriter.writeToExternal();
  }

  @Override
  public double getPosteriorProbability(Map<Node, Set<NodeState>> measureRequest) {
    double prior = data.getJointProbability();
    if (prior == 0.0) return prior;
    Map<Node, NodeObservation> priorEvidence = data.getObservedEvidence();
    if (negatesPriorEvidence(priorEvidence, measureRequest)) return 0.0;
    double joint = modifyGraphState(appendObservations(priorEvidence, measureRequest));
    modifyGraphState(priorEvidence);
    return joint / prior;
  }

  @Override
  public Clique getRootClique() {
    return data.getCliques()[0];
  }

  @Override
  public Clique[] getCliques() {
    return Arrays.copyOf(data.getCliques(), data.getCliques().length);
  }

  @Override
  public Separator[] getSeparators() {
    return Arrays.copyOf(data.getSeparators(), data.getSeparators().length);
  }

  private boolean negatesPriorEvidence(
      Map<Node, NodeObservation> priorEvidence, Map<Node, Set<NodeState>> measureRequest) {
    for (Map.Entry<Node, Set<NodeState>> entry : measureRequest.entrySet()) {
      NodeObservation observation = priorEvidence.get(entry.getKey());
      ObservationStatus status = observation.status();
      if (status.equals(UNOBSERVED)) continue;
      if (status.equals(NEGATED)) return true;
      Set<NodeState> eliminated = observation.eliminatedStates();
      for (NodeState measured : entry.getValue()) {
        if (eliminated.contains(measured)) return true;
      }
    }
    return false;
  }

  private Map<Node, NodeObservation> appendObservations(
      Map<Node, NodeObservation> priorEvidence, Map<Node, Set<NodeState>> request) {
    Map<Node, NodeObservation> evidence = new LinkedHashMap<>(priorEvidence);
    request.forEach(
        ((node, toObserve) -> {
          if (!evidence.containsKey(node)) return;
          evidence.put(node, observationFactory.observeStates(node, toObserve));
        }));
    return evidence;
  }

  private double modifyGraphState(Map<Node, NodeObservation> finalInferenceState) {
    Arrays.stream(data.getSeparators()).forEach(Separator::resetPotentials);
    Arrays.stream(data.getCliques()).forEach(c -> c.applyPotentials(finalInferenceState));
    passMessages();
    return calculateJointProbability();
  }

  private void passMessages() {
    Clique clique = data.getCliques()[0];
    messagePasser.collectMessages(clique);
    messagePasser.distributeMessages(clique);
  }

  private double calculateJointProbability() {
    double separatorSums = productOfSums(data.getSeparators());
    if (separatorSums == 0.0) return 0.0;
    double cliqueSums = productOfSums(data.getCliques());
    return cliqueSums / separatorSums;
  }

  private <T extends InferenceTable> double productOfSums(T[] array) {
    return Arrays.stream(array)
        .mapToDouble(InferenceTable::sumPotentials)
        .reduce(1.0, (x, y) -> x * y);
  }
}
