package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.ipfpresults.IPFPResults;
import java.util.Optional;

public class MultiStageSolver implements IPFPSolver {
  private final DirectCptSolver directCptSolver;
  private final IPFPSolver ipfpSolver;

  public MultiStageSolver() {
    this.directCptSolver = new DirectCptSolver();
    this.ipfpSolver = new IPFPJunctionTreeSolver();
  }

  public MultiStageSolver(IPFPSolver ipfpSolver) {
    this.directCptSolver = new DirectCptSolver();
    this.ipfpSolver = ipfpSolver;
  }

  @Override
  public IPFPResults getResults() {
    return ipfpSolver.getResults();
  }

  @Override
  public Optional<BayesianNetwork> solveFromCurrentValues(BayesianNetwork bayesianNetwork) {
    return solveNetwork(bayesianNetwork);
  }

  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork) {
    return directCptSolver
        .solveNetwork(bayesianNetwork)
        .or(() -> ipfpSolver.solveFromCurrentValues(bayesianNetwork));
  }
}
