package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.util.Collection;
import java.util.Set;

public interface JunctionTreeTable extends ProbabilityTable {
  ProbabilityVector getBackupVector();

  double sumProbabilities(Collection<NodeState> states);

  void setObserved(Set<NodeState> evidenceInTable);

  void eliminateStates(Set<NodeState> toEliminate);

  void resetObservations();

  ProbabilityVector getVector();
}
