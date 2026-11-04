package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import java.util.Map;

public interface InferenceGraph {
  Map<Node, NodeObservation> getObservedEvidence();

  ObservedTable[] getObservedTables();

  BayesianNetwork getNetwork();

  void setObservedStates(NodeState... observedStates);

  void eliminateStates(NodeState... toEliminate);

  void resetObservations();

  double getPosteriorProbability(NodeState... measured);
}
