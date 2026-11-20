package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import java.util.Optional;

/**
 * An {@link IPFPSolver} that solves a {@link BayesianNetwork} in two stages.
 *
 * <p>The first stage is a {@link DirectCptWriter}, which writes the CPT entries that can be
 * determined directly from the constraints of the network. If that stage cannot complete every
 * entry, the second stage runs an Iterative Proportional Fitting Procedure (IPFP) with the
 * configured {@code IPFPSolver}, starting from the values written by the first stage.
 *
 * <p>The IPFP stage uses an {@link IPFPJunctionTreeSolver} unless another solver is supplied to the
 * constructor.
 *
 * @author Alec Redmond
 * @see DirectCptWriter
 * @see IPFPJunctionTreeSolver
 * @see IPFPSingleTableSolver
 */
public class MultiStageSolver implements IPFPSolver {
  private final DirectCptWriter directCptWriter;
  private final IPFPSolver ipfpSolver;

  /** Constructs a solver whose IPFP stage uses a new {@link IPFPJunctionTreeSolver}. */
  public MultiStageSolver() {
    this(new IPFPJunctionTreeSolver());
  }

  /**
   * Constructs a solver whose IPFP stage uses the specified solver.
   *
   * @param ipfpSolver the solver used for the IPFP stage
   */
  public MultiStageSolver(IPFPSolver ipfpSolver) {
    this.directCptWriter = new DirectCptWriter();
    this.ipfpSolver = ipfpSolver;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The results come from the IPFP stage, so they are {@code null} until that stage has run on
   * this instance. This is the case if the first stage determined every CPT entry.
   */
  @Override
  public IPFPResults getResults() {
    return ipfpSolver.getResults();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method runs the IPFP stage only. The first stage, the {@link DirectCptWriter}, is
   * skipped.
   */
  @Override
  public Optional<BayesianNetwork> solveFromCurrentValues(BayesianNetwork bayesianNetwork) {
    return ipfpSolver.solveFromCurrentValues(bayesianNetwork);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The first stage tries to write the CPTs directly with a {@link DirectCptWriter}. If that
   * fails, the IPFP stage runs on the same network, starting from the current CPT values. The first
   * stage modifies the CPTs and the constraints of the network in place, so the IPFP stage starts
   * from those changes.
   */
  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork) {
    return directCptWriter
        .solveNetwork(bayesianNetwork)
        .or(() -> ipfpSolver.solveFromCurrentValues(bayesianNetwork));
  }
}
