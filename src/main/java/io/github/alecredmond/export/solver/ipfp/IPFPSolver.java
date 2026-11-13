package io.github.alecredmond.export.solver.ipfp;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.directinput.DirectCptSolver;
import java.util.Optional;

public interface IPFPSolver extends BayesSolver {
  /**
   * Returns the results of the most recent solver run on this instance, including per-cycle error
   * and loss information for every {@link ProbabilityConstraint} on the network.
   *
   * @return the results of the most recent run, or {@code null} if no run has been performed.
   */
  IPFPResults getResults();

  /**
   * Attempts to perform an IPFP procedure on a {@link BayesianNetwork} using its current CPT
   * entries as a starting point. This is typically used for second-pass solver runs after the
   * network tables have been partially written by a {@link DirectCptSolver}.
   *
   * @param bayesianNetwork the {@link BayesianNetwork} to solve.
   * @return an {@link Optional}-wrapped {@link BayesianNetwork}, which will be empty if the solver
   *     failed.
   */
  Optional<BayesianNetwork> solveFromCurrentValues(BayesianNetwork bayesianNetwork);
}
