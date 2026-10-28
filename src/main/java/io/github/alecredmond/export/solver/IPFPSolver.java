package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.solver.ipfpresults.IPFPResults;

public interface IPFPSolver extends BayesSolver {
  /**
   * Returns the results of the most recent solver run on this instance, including per-cycle error
   * and loss information for every {@link ProbabilityConstraint} on the network.
   *
   * @return the results of the most recent run, or {@code null} if no run has been performed.
   */
  IPFPResults getResults();
}
