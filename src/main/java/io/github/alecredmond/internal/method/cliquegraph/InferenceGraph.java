package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.inference.Separator;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import java.util.Map;
import java.util.Set;

public interface InferenceGraph {
  Map<Node, NodeObservation> getObservedEvidence();

  ObservedTable[] getObservedTables();

  BayesianNetwork getNetwork();

  void setObservedStates(Map<Node, Set<NodeState>> observedStateRequest);

  void eliminateStates(Map<Node, Set<NodeState>> toEliminateRequest);

  void resetObservations();

  double getPosteriorProbability(Map<Node, Set<NodeState>> measureRequest);

  Clique getRootClique();

  Clique[] getCliques();

  Separator[] getSeparators();
}
