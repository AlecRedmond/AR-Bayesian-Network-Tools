package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import java.util.Optional;

public class MultiStageSolver implements IPFPSolver {
  private final DirectCptWriter directCptWriter;
  private final IPFPSolver ipfpSolver;

  public MultiStageSolver() {
    this(new IPFPJunctionTreeSolver());
  }

  public MultiStageSolver(IPFPSolver ipfpSolver) {
    this.directCptWriter = new DirectCptWriter();
    this.ipfpSolver = ipfpSolver;
  }

  @Override
  public IPFPResults getResults() {
    return ipfpSolver.getResults();
  }

  @Override
  public Optional<BayesianNetwork> solveFromCurrentValues(BayesianNetwork bayesianNetwork) {
    return ipfpSolver.solveFromCurrentValues(bayesianNetwork);
  }

  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork) {
    return directCptWriter
        .solveNetwork(bayesianNetwork)
        .or(() -> ipfpSolver.solveFromCurrentValues(bayesianNetwork));
  }
}
