package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.method.solver.BayesSolverImpl;

/**
 * A solver that provides the best fit solution for a {@link BayesianNetwork} given its constraints.
 * {@code BayesSolver} applies a variation of the Iterative Proportional Fitting Procedure (IPFP) to
 * iteratively adjust the network's distribution until it satisfies all defined constraints. After
 * solving, per-constraint error and loss results can be reviewed via {@link #getResults()}.
 *
 * <p>Two IPFP variants are currently available; a full joint-distribution procedure ({@link
 * SolverAlgorithm#SINGLE_TABLE_IPFP}), and a Junction Tree Algorithm-derived procedure ({@link
 * SolverAlgorithm#JUNCTION_TREE_IPFP}). The default variant is controlled by {@code
 * app.bayes.solver.defaultSolverAlgorithm} in {@code app.properties} (default: {@code
 * JUNCTION_TREE_IPFP}).
 *
 * <p>Additional solver properties - including the maximum number of cycles, time limits, and
 * convergence thresholds - are configurable under the {@code app.bayes.solver} section of {@code
 * app.properties}.
 *
 * @see SolverResults
 * @see SolverAlgorithm
 * @author Alec Redmond
 */
public interface BayesSolver {

  /**
   * Creates a new {@code BayesSolver} instance linked to the given network.
   *
   * @param network the Bayesian network to solve.
   * @return a new {@code BayesSolver} instance.
   */
  static BayesSolver create(BayesianNetwork network) {
    return new BayesSolverImpl(network.getNetworkData());
  }

  /**
   * Runs the solver using the IPFP variant configured in {@code app.properties}, unless the network
   * has already been solved. The active variant is controlled by {@code
   * app.bayes.solver.defaultSolverAlgorithm} (default: {@code JUNCTION_TREE_IPFP}). Any exception
   * thrown during the process is caught and logged rather than propagated.
   *
   * <p><i>Note: This method will first call {@link #writeCPTsFromConstraints()}, which will run in
   * place of the IPFP algorithm in cases where all constraints can be mapped directly to the
   * network's CPTs. To force the use of an IPFP variant, use the method {@link
   * #forceSolve(SolverAlgorithm)}.</i>
   *
   * @return {@code true} if the solver procedure completes successfully, or the network was already
   *     solved; {@code false} if an error was encountered.
   */
  boolean solve();

  /**
   * Runs the solver using the specified IPFP variant, unless the network has already been solved.
   * Any exception thrown during the solving process is caught and logged rather than propagated.
   *
   * @param solverAlgorithm the IPFP variant to use.
   * @return {@code true} if the solver completes successfully, or the network was already solved;
   *     {@code false} if an error was encountered.
   */
  boolean solve(SolverAlgorithm solverAlgorithm);

  /**
   * Runs the solver using the IPFP variant configured in {@code app.properties}, regardless of
   * whether the network has already been solved. The active variant is controlled by {@code
   * app.bayes.solver.defaultSolverAlgorithm} (default: {@code JUNCTION_TREE_IPFP}). Any exception
   * thrown during the solving process is caught and logged rather than propagated.
   *
   * <p><i>Note: This method will first call {@link #writeCPTsFromConstraints()}, which will run in
   * place of the IPFP algorithm in cases where all constraints can be mapped directly to the
   * network's CPTs. To force the use of an IPFP variant, use the method {@link
   * #forceSolve(SolverAlgorithm)}.</i>
   *
   * @return {@code true} if the solver completes successfully; {@code false} if an error was
   *     encountered.
   */
  boolean forceSolve();

  /**
   * Runs the solver using the specified IPFP variant, regardless of whether the network has already
   * been solved. Any exception thrown during the solving process is caught and logged rather than
   * propagated.
   *
   * @param solverAlgorithm the IPFP variant to use.
   * @return {@code true} if the solver completes successfully; {@code false} if an error was
   *     encountered.
   */
  boolean forceSolve(SolverAlgorithm solverAlgorithm);

  /**
   * Returns whether the network has already been solved.
   *
   * @return {@code true} if the network is already solved; {@code false} otherwise.
   */
  boolean isSolved();

  /**
   * Maps all constraints in the {@link BayesianNetwork} to their CPT entries, if it is possible to
   * do so. For this to succeed every constraint must be either:
   *
   * <ul>
   *   <li>A {@link MarginalConstraint} mapping an entry in a valid {@link RootNodeTable}.
   *   <li>A {@link ConditionalConstraint} mapping an entry in a valid {@link ConditionalTable}.
   * </ul>
   *
   * For every condition {@link NodeState} combination in a given table, there must be at least
   * {@code n - 1} valid constraints where {@code n} is the number of {@link NodeState}s in the
   * table's event {@link Node}. The final constraint will be filled in automatically.
   *
   * <p><i>Note: This is run automatically when {@link #solve()} or {@link #forceSolve()} are called
   * and will bypass IPFP if successful. To force the use of an IPFP variant, use the method {@link
   * #forceSolve(SolverAlgorithm)}.</i>
   *
   * @return {@code true} if all constraints were successfully mapped directly to the network's
   *     CPTs.
   */
  boolean writeCPTsFromConstraints();

  /**
   * Returns the results of the most recent solver run on this instance, including per-cycle error
   * and loss information for every {@link ProbabilityConstraint} on the network.
   *
   * @return the results of the most recent run, or {@code null} if no run has been performed.
   */
  SolverResults getResults();
}
