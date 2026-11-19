package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.Optional;

/**
 * Finds the best distribution of a {@link BayesianNetwork} from its given {@link
 * ProbabilityConstraint}s.
 */
public interface ConstraintFitter {

  /**
   * Attempts to fit the CPTs in a {@link BayesianNetwork} from its given constraints.
   *
   * @param bayesianNetwork the network to be solved
   * @return An {@link Optional} containing the new {@link BayesianNetwork}, or an empty {@code
   *     Optional} if the network could not be built
   */
  Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork);
}
