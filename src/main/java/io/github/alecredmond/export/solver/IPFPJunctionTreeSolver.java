package io.github.alecredmond.export.solver;

import io.github.alecredmond.internal.method.cliquegraph.factory.SolverGraphFactoryJTA;
import io.github.alecredmond.internal.method.solver.AbstractIPFPSolver;

public class IPFPJunctionTreeSolver extends AbstractIPFPSolver {
  public IPFPJunctionTreeSolver() {
    super(new SolverGraphFactoryJTA());
  }

  @Override
  protected String algorithmName() {
    return "JUNCTION TREE IPFP";
  }
}
