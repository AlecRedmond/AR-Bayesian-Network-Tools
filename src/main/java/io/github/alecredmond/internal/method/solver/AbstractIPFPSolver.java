package io.github.alecredmond.internal.method.solver;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.solver.IPFPSolver;
import io.github.alecredmond.export.solver.ipfpresults.IPFPResults;
import io.github.alecredmond.internal.application.solver.SolverConfigs;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.internal.method.cliquegraph.SolverGraph;
import io.github.alecredmond.internal.method.cliquegraph.factory.SolverGraphFactory;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractIPFPSolver implements IPFPSolver {
  private final SolverConfigs configs;
  private final SolverGraphFactory solverGraphFactory;
  private IPFPResults results;

  protected AbstractIPFPSolver(SolverGraphFactory solverGraphFactory) {
    this.solverGraphFactory = solverGraphFactory;
    this.configs = new SolverConfigs();
    this.results = null;
  }

  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork) {
    try {
      configs.updateConfigs();
      new SolverValidator(bayesianNetwork).validateDataBuilt();
      results = performSolverRun(solverGraphFactory.build(bayesianNetwork));
      return Optional.of(bayesianNetwork);
    } catch (Exception e) {
      results = null;
      log.error(e.getLocalizedMessage(), e);
      return Optional.empty();
    }
  }

  private IPFPResults performSolverRun(SolverGraph solverGraph) {
    final boolean writeLogs = configs.isLogSolverProgress();
    final int timeLimitSeconds = configs.getTimeLimitSeconds();
    final int logIntervalSeconds = configs.getLogIntervalSeconds();
    final int cycleLimit = configs.getCyclesLimit();
    final double convergeThreshold = configs.getConvergeThreshold();

    final Instant start = Instant.now();
    final Instant endTime = start.plus(timeLimitSeconds, ChronoUnit.SECONDS);
    Instant now = start;
    Instant nextLogTime = now.plus(logIntervalSeconds, ChronoUnit.SECONDS);

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    boolean thresholdReached = false;
    boolean timeLimitReached = false;

    Map<Clique, List<ConstraintSolver>> solversPerClique = solverGraph.getSolversPerClique();

    if (writeLogs) {
      log.info("STARTING SOLVER IN MODE = {}", algorithmName());
    }

    solverGraph.normalizeTables();

    int cycle;
    for (cycle = 0; cycle < cycleLimit; cycle++) {
      lastError = error;
      error = runSolverCycleAndReturnError(solverGraph, solversPerClique);
      converge = error - lastError;

      now = Instant.now();
      thresholdReached = Math.abs(converge) <= convergeThreshold;
      timeLimitReached = now.isAfter(endTime);

      if (thresholdReached || timeLimitReached || Double.isNaN(error)) {
        break;
      }

      if (writeLogs && now.isAfter(nextLogTime)) {
        nextLogTime = now.plus(logIntervalSeconds, ChronoUnit.SECONDS);
        logCycleComplete(cycle, converge, error);
      }
    }

    if (writeLogs) {
      logEndStatement(
          thresholdReached,
          timeLimitReached,
          cycle + 1 == cycleLimit,
          now.toEpochMilli() - start.toEpochMilli());
      logCycleComplete(cycle, converge, error);
    }

    solverGraph.writeTablesToNetwork();
    return writeResults(solversPerClique, cycle, now, start);
  }

  protected abstract String algorithmName();

  private double runSolverCycleAndReturnError(
      SolverGraph solverGraph, Map<Clique, List<ConstraintSolver>> solversPerClique) {
    double sum = 0;
    for (Clique clique : solversPerClique.keySet()) {
      sum += solversPerClique.values().parallelStream().mapToDouble(this::solveForClique).sum();
      solverGraph.sumTransfer(clique);
    }
    return sum;
  }

  private void logCycleComplete(int cycle, double loss, double error) {
    String logString = String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", cycle, loss, error);
    log.info(logString);
  }

  private void logEndStatement(
      boolean thresholdReached,
      boolean timeLimitReached,
      boolean cycleLimitReached,
      long runTimeMs) {
    String statement;
    Consumer<String> logType;
    if (thresholdReached) {
      statement = "FOUND A SOLUTION";
      logType = log::info;
    } else if (cycleLimitReached) {
      statement = "REACHED MAX CYCLES";
      logType = log::warn;
    } else if (timeLimitReached) {
      statement = "TIMED OUT";
      logType = log::warn;
    } else {
      statement = "ENDED ABNORMALLY";
      logType = log::error;
    }
    logType.accept("SOLVER %s, ELAPSED TIME %d ms".formatted(statement, runTimeMs));
  }

  private IPFPResults writeResults(
      Map<Clique, List<ConstraintSolver>> constraintMap, int cycle, Instant now, Instant start) {
    Map<ProbabilityConstraint, double[]> resultsMap = new HashMap<>();
    constraintMap.values().stream()
        .flatMap(Collection::stream)
        .forEach(handler -> handler.updateResults(resultsMap, cycle, constraintMap.keySet()));
    return new SolverResultsBuilder().buildResults(cycle, resultsMap, Duration.between(start, now));
  }

  private double solveForClique(List<ConstraintSolver> solvers) {
    return solvers.stream().mapToDouble(ConstraintSolver::adjustAndReturnError).sum();
  }

  @Override
  public IPFPResults getResults() {
    if (results == null) {
      log.warn("NO RESULTS FROM CURRENT SOLVER");
    }
    return results;
  }
}
