package io.github.alecredmond.internal.application.junctiontree;

import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.solver.SolverAlgorithm;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JunctionTreeData {
  private BayesianNetworkData networkData;
  private Clique[] cliques;
  private Separator[] separators;
  private Map<Clique, List<ConstraintSolver>> solversPerClique;
  private Map<Node, ObservedTable> observedTablesMap;
  private Map<Node, NodeObservation> observedEvidence;
  private Map<Node, NodeObservation> unobservedBackup;
  private boolean solverConfig;
  private double jointProbability;
  private SolverAlgorithm solverAlgorithm;
  private InferenceAlgorithm inferenceAlgorithm;
  private double equivalentTreeWidth;
  private Runnable[][] collectionRuns;
  private Runnable[][] distributionRuns;
}
