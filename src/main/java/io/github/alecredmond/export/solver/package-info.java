/**
 * Provides the constraint fitters that complete the conditional probability tables of a Bayesian
 * network from its probability constraints.
 *
 * <p>A {@link io.github.alecredmond.export.solver.ConstraintFitter} fits the tables of a network to
 * the constraints assigned to it. The available implementations are:
 *
 * <ul>
 *   <li>{@link io.github.alecredmond.export.solver.DirectCptWriter}, which writes table entries
 *       straight from the constraints that describe them
 *   <li>{@link io.github.alecredmond.export.solver.IPFPSolver} implementations, which fit the
 *       tables with the Iterative Proportional Fitting Procedure (IPFP): {@link
 *       io.github.alecredmond.export.solver.IPFPJunctionTreeSolver} works over a junction tree and
 *       {@link io.github.alecredmond.export.solver.IPFPSingleTableSolver} works over a single joint
 *       table
 *   <li>{@link io.github.alecredmond.export.solver.MultiStageSolver}, which tries the direct writer
 *       first and falls back to an IPFP solver
 * </ul>
 *
 * <p>After an IPFP run, the {@link io.github.alecredmond.export.solver.IPFPResults} of the solver
 * report how well each constraint was satisfied.
 *
 * @see io.github.alecredmond.export.solver.constraint
 */
package io.github.alecredmond.export.solver;
