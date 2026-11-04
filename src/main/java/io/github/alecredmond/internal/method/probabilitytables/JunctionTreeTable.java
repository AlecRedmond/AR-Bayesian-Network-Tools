package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.util.Collection;

public interface JunctionTreeTable extends ProbabilityTable {
  ProbabilityVector getBackupVector();

  double sumProbabilities(Collection<NodeState> states);

  void setObserved(NodeObservation[] tableObservations);

  void resetObservations();

  ProbabilityVector getVector();
}
