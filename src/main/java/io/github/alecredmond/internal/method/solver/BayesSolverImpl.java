package io.github.alecredmond.internal.method.solver;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.SolverAlgorithm;
import io.github.alecredmond.export.solver.SolverResults;
import io.github.alecredmond.internal.application.solver.SolverConfigs;
import io.github.alecredmond.internal.method.solver.cptmapper.DirectCptMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesSolverImpl extends JTASolver implements BayesSolver {
  private final DirectCptMapper directCptMapper;
  private final SolverValidator validator;
  private SolverResults results;

  public BayesSolverImpl(BayesianNetwork network) {
    super(network, new SolverConfigs());
    this.results = null;
    this.directCptMapper = new DirectCptMapper(network.getNetworkData());
    this.validator = new SolverValidator(network);
  }

  @Override
  public boolean solve() {
    if (isSolved()) return true;
    return forceSolve();
  }

  @Override
  public boolean solve(SolverAlgorithm solverAlgorithm) {
    if (isSolved()) return true;
    return forceSolve(solverAlgorithm);
  }

  @Override
  public boolean forceSolve(SolverAlgorithm solverAlgorithm) {
    try {
      configs.updateConfigs();
      configs.setSolverAlgorithm(solverAlgorithm);
      validator.validateDataBuilt();
      validator.resetNetworkTables();
      return forceSolveCommon();
    } catch (Exception e) {
      results = null;
      log.error(e.getLocalizedMessage(), e);
      return false;
    }
  }

  @Override
  public boolean isSolved() {
    return network.isSolved();
  }

  @Override
  public boolean forceSolve() {
    try {
      configs.updateConfigs();
      validator.validateDataBuilt();
      if (tryDirectCPTInput()) return true;
      return forceSolveCommon();
    } catch (Exception e) {
      results = null;
      log.error(e.getLocalizedMessage(), e);
      return false;
    }
  }

  private boolean tryDirectCPTInput() {
    boolean mapped = directCptMapper.tryDirectImpute();
    if (mapped) {
      network.getNetworkData().setSolved(true);
    }
    return mapped;
  }

  private boolean forceSolveCommon() {
    BayesianNetworkData data = network.getNetworkData();
    data.setSolved(false);
    results = solveNetwork();
    data.setSolved(true);
    return true;
  }

  public boolean writeCPTsFromConstraints() {
    validator.validateDataBuilt();
    return tryDirectCPTInput();
  }

  @Override
  public SolverResults getResults() {
    if (results == null) {
      log.warn("NO RESULTS FROM CURRENT SOLVER");
    }
    return results;
  }
}
