package io.github.alecredmond.internal.method.cliquegraph;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.export.inference.direct.Separator;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import java.util.Map;
import java.util.Set;

public interface InferenceGraph {

  ObservedTable[] getObservedTables();

  BayesianNetwork getNetwork();

  void setNewObservations(Map<Node, NodeObservation> newObservations);

  void resetObservations();

  double getPosteriorProbability(Map<Node, Set<NodeState>> measureRequest);

  Clique getRootClique();

  Clique[] getCliques();

  Separator[] getSeparators();
}
