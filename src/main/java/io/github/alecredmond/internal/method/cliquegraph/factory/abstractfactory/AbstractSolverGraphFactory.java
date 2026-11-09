package io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataSolver;
import io.github.alecredmond.export.inference.Clique;
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
  public Map<Clique, CliqueData> commonBuildLogic() {
    Map<Clique, CliqueData> cliqueDataMap = super.commonBuildLogic();
    cliqueGraphData.setSolversPerClique(
        buildIPFPSolvers(cliqueGraphData.getNetwork().getConstraints(), cliqueDataMap));
    return cliqueDataMap;
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
      Collection<ProbabilityConstraint> constraints, Map<Clique, CliqueData> map) {
    return map.entrySet().stream()
        .map(e -> Map.entry(e.getKey(), matchConstraints(e.getValue(), constraints)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private List<ConstraintSolver> matchConstraints(
      CliqueData cliqueData, Collection<ProbabilityConstraint> constraints) {
    ConstraintRegistry registry = new ConstraintRegistry();
    return constraints.stream()
        .filter(constraint -> cliqueData.getNodes().containsAll(constraint.getAllNodes()))
        .map(constraint -> buildConstraintHandler(constraint, cliqueData, registry))
        .toList();
  }

  private <T extends ProbabilityConstraint> ConstraintSolver buildConstraintHandler(
      @NonNull T constraint, CliqueData cliqueData, ConstraintRegistry registry) {
    return registry.getStrategy(constraint).buildSolverHandler(cliqueData.getTable(), constraint);
  }

  public SolverGraph build(BayesianNetwork network) {
    return super.build(network, new GraphDataSolver());
  }
}
