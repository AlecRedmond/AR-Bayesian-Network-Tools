package io.github.alecredmond.export.solver;

import io.github.alecredmond.internal.method.cliquegraph.factory.SolverGraphFactorySTA;
import io.github.alecredmond.internal.method.solver.AbstractIPFPSolver;

/**
 * An {@link IPFPSolver} that runs the Iterative Proportional Fitting Procedure (IPFP) over a single
 * joint probability table. Outside some niche uses, {@link IPFPJunctionTreeSolver} is preferable.
 *
 * <p>Single-Joint-Table IPFP is the most basic form of iterative proportional fitting. The
 * Cartesian product of all node states is mapped to a single joint probability table, and all
 * constraints are applied to that table.
 *
 * <p><b>Time complexity: O(2<sup>n</sup>), where n is the number of nodes in the network.</b>
 *
 * <p>This method has a low overhead and may converge faster for small networks where the joint
 * table would contain fewer than 2<sup>10</sup> entries. Performance degrades exponentially beyond
 * this threshold. The absolute upper limit is 2<sup>31</sup>&minus;1 entries, the maximum length of
 * a Java array.
 *
 * @author Alec Redmond
 * @see IPFPJunctionTreeSolver
 * @see MultiStageSolver
 */
public class IPFPSingleTableSolver extends AbstractIPFPSolver {

  /** Constructs a new {@code IPFPSingleTableSolver}. */
  public IPFPSingleTableSolver() {
    super(new SolverGraphFactorySTA());
  }

  /**
   * Returns the name of the IPFP algorithm that this solver runs.
   *
   * @return the name of the algorithm, {@code "SINGLE TABLE IPFP"}
   */
  @Override
  protected String algorithmName() {
    return "SINGLE TABLE IPFP";
  }
}
