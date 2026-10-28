package io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataSolver;
import io.github.alecredmond.internal.method.cliquegraph.SolverGraph;
import io.github.alecredmond.internal.method.cliquegraph.externalwriters.SolverExternalWriter;
import io.github.alecredmond.internal.method.cliquegraph.factory.SolverGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.implementation.SolverGraphImpl;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;

public abstract class AbstractSolverGraphFactory
    extends AbstractGraphFactoryBase<SolverGraphImpl, GraphDataSolver>
    implements SolverGraphFactory {

  @Override
  public void commonBuildLogic() {
    super.commonBuildLogic();
    cliqueGraphData.setSolversPerClique(
        buildIPFPSolvers(cliqueGraphData, cliqueGraphData.getNetwork().getConstraints()));
  }

  @Override
  protected SolverGraphImpl constructCliqueGraph() {
    return new SolverGraphImpl(cliqueGraphData, new SolverExternalWriter(cliqueGraphData));
  }

  @Override
  protected String getAlgorithmType() {
    return "SOLVER";
  }

  @Override
  protected ProbabilityTable[] getWriteBackTables() {
    return networkTables;
  }

  private Map<Clique, List<ConstraintSolver>> buildIPFPSolvers(
      GraphDataSolver cliqueGraphData, Collection<ProbabilityConstraint> constraints) {
    return Arrays.stream(cliqueGraphData.getCliques())
        .map(clique -> Map.entry(clique, matchConstraints(clique, constraints)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private List<ConstraintSolver> matchConstraints(
      Clique clique, Collection<ProbabilityConstraint> constraints) {
    ConstraintRegistry registry = new ConstraintRegistry();
    return constraints.stream()
        .filter(constraint -> clique.getNodes().containsAll(constraint.getAllNodes()))
        .map(constraint -> buildConstraintHandler(constraint, clique, registry))
        .toList();
  }

  private <T extends ProbabilityConstraint> ConstraintSolver buildConstraintHandler(
      @NonNull T constraint, Clique clique, ConstraintRegistry registry) {
    return registry.getStrategy(constraint).buildSolverHandler(clique.getTable(), constraint);
  }

  public SolverGraph build(BayesianNetwork network) {
    return super.build(network, new GraphDataSolver());
  }
}
