package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import java.util.Collection;
import java.util.Map;

public interface InferenceGraph {
  Map<Node, NodeObservation> getObservedEvidence();

  ObservedTable[] getObservedTables();

  BayesianNetwork getNetwork();

  void observeNetwork(Collection<NodeState> observedStates);

  double getJointProbOfMeasured(Collection<NodeState> newEvidence);

  void eliminateStates(Collection<NodeState> toEliminate);

  double getJointProbability();
}
