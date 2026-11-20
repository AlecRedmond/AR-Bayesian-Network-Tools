package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.Optional;

/**
 * Fits the conditional probability tables (CPTs) of a {@link BayesianNetwork} to the {@link
 * ProbabilityConstraint}s assigned to it, finding the distribution that best satisfies them.
 *
 * <p>Implementations include {@link DirectCptWriter}, which writes CPT entries straight from the
 * constraints, {@link IPFPSolver}, which fits the tables iteratively, and {@link MultiStageSolver},
 * which combines the two.
 *
 * @author Alec Redmond
 * @see DirectCptWriter
 * @see IPFPSolver
 * @see MultiStageSolver
 */
public interface ConstraintFitter {

  /**
   * Attempts to fit the CPTs of a {@link BayesianNetwork} to the constraints assigned to it.
   *
   * @param bayesianNetwork the network to be solved
   * @return an {@link Optional} containing the solved {@code BayesianNetwork}, or an empty {@code
   *     Optional} if the network could not be built
   */
  Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork);
}
