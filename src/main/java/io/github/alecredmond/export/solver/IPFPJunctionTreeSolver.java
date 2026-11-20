package io.github.alecredmond.export.solver;

import io.github.alecredmond.internal.method.cliquegraph.factory.SolverGraphFactoryJTA;
import io.github.alecredmond.internal.method.solver.AbstractIPFPSolver;

/**
 * An {@link IPFPSolver} that runs the Iterative Proportional Fitting Procedure (IPFP) over the
 * cliques of a junction tree.
 *
 * <p>The network is decomposed into a junction tree of smaller cliques, each with its own joint
 * sub-table, connected by separators formed from their shared nodes. Constraints are batched per
 * clique, and all cliques solved locally in parallel. The marginal sums are then transferred from
 * one of the cliques through the separators. A single cycle completes when every clique has been
 * used as the distribution point.
 *
 * <p><b>Time complexity: O(c &times; 2<sup>t</sup>), where t is the treewidth and c is the number
 * of cliques in the junction tree.</b>
 *
 * <p>This variant carries higher overhead but scales with treewidth rather than total node count,
 * typically yielding an exponential speed improvement over {@link IPFPSingleTableSolver} as nodes
 * are added. It is also the only option for networks whose full joint Cartesian product would
 * exceed 2<sup>31</sup>&minus;1 entries.
 *
 * @author Alec Redmond
 * @see IPFPSingleTableSolver
 * @see MultiStageSolver
 */
public class IPFPJunctionTreeSolver extends AbstractIPFPSolver {

  /** Constructs a new {@code IPFPJunctionTreeSolver}. */
  public IPFPJunctionTreeSolver() {
    super(new SolverGraphFactoryJTA());
  }

  /**
   * Returns the name of the IPFP algorithm that this solver runs.
   *
   * @return the name of the algorithm, {@code "JUNCTION TREE IPFP"}
   */
  @Override
  protected String algorithmName() {
    return "JUNCTION TREE IPFP";
  }
}
