package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.Optional;

/**
 * A {@link ConstraintFitter} that fits the conditional probability tables of a {@link
 * BayesianNetwork} to its constraints with the Iterative Proportional Fitting Procedure (IPFP).
 *
 * <p>After each run, the solver keeps the {@link IPFPResults} of that run, which can be retrieved
 * with {@link #getResults()}.
 *
 * @author Alec Redmond
 * @see IPFPJunctionTreeSolver
 * @see IPFPSingleTableSolver
 * @see MultiStageSolver
 */
public interface IPFPSolver extends ConstraintFitter {
  /**
   * Returns the results of the most recent solver run on this instance, including per-cycle error
   * and loss information for every {@link ProbabilityConstraint} on the network.
   *
   * @return the results of the most recent run, or {@code null} if no run has been performed
   */
  IPFPResults getResults();

  /**
   * Attempts to perform an IPFP on a {@link BayesianNetwork}, using its current CPT entries as the
   * starting point. This is typically used for second-pass solver runs after the network tables
   * have been partially written by a {@link DirectCptWriter}.
   *
   * @param bayesianNetwork the network to solve
   * @return an {@link Optional} containing the solved {@code BayesianNetwork}, or an empty {@code
   *     Optional} if the solver failed
   */
  Optional<BayesianNetwork> solveFromCurrentValues(BayesianNetwork bayesianNetwork);
}
