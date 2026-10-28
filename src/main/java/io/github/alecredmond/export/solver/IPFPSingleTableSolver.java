package io.github.alecredmond.export.solver;

import io.github.alecredmond.internal.method.cliquegraph.factory.SolverGraphFactorySTA;
import io.github.alecredmond.internal.method.solver.AbstractIPFPSolver;

public class IPFPSingleTableSolver extends AbstractIPFPSolver {
  public IPFPSingleTableSolver() {
    super(new SolverGraphFactorySTA());
  }

  @Override
  protected String algorithmName() {
    return "SINGLE TABLE IPFP";
  }
}
