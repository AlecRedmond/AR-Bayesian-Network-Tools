package io.github.alecredmond.internal.method.solver;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.solver.SolverResults;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.solver.SolverConfigs;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import io.github.alecredmond.internal.method.junctiontree.JunctionTreeAlgorithm;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class JTASolver {
  protected final BayesianNetworkData networkData;
  protected final SolverConfigs configs;
  protected JunctionTreeAlgorithm jta;

  protected JTASolver(BayesianNetworkData networkData, SolverConfigs configs) {
    this.configs = configs;
    this.networkData = networkData;
  }

  protected SolverResults solveNetwork() {
    boolean writeLogs = configs.isLogSolverProgress();
    Instant start = Instant.now();

    if (writeLogs) {
      log.info("STARTING SOLVER IN MODE = {}", configs.getSolverAlgorithm());
    }

    jta = JunctionTreeAlgorithm.buildForSolver(networkData, configs);
    jta.normalizeTables();

    double lastError;
    double error = Double.MAX_VALUE;
    double converge = Double.MAX_VALUE;

    Instant now = Instant.now();
    Instant endTime = now.plus(configs.getTimeLimitSeconds(), ChronoUnit.SECONDS);
    Instant nextLogTime = now.plus(configs.getLogIntervalSeconds(), ChronoUnit.SECONDS);

    Map<Clique, List<ConstraintSolver>> solversPerClique = jta.getData().getSolversPerClique();

    boolean thresholdReached = false;
    boolean timeLimitReached = false;
    int cycle;

    for (cycle = 0; cycle < configs.getCyclesLimit(); cycle++) {
      lastError = error;
      error = runSolverCycleAndReturnError(jta, solversPerClique);
      converge = error - lastError;

      now = Instant.now();
      thresholdReached = Math.abs(converge) <= configs.getConvergeThreshold();
      timeLimitReached = now.isAfter(endTime);

      if (thresholdReached || timeLimitReached || Double.isNaN(error)) {
        break;
      }

      if (writeLogs && now.isAfter(nextLogTime)) {
        nextLogTime = now.plus(configs.getLogIntervalSeconds(), ChronoUnit.SECONDS);
        logCycleComplete(cycle, converge, error);
      }
    }

    if (writeLogs) {
      logEndStatement(
          thresholdReached,
          timeLimitReached,
          cycle >= configs.getCyclesLimit(),
          now.toEpochMilli() - start.toEpochMilli());
      logCycleComplete(cycle, converge, error);
    }

    jta.writeTablesToNetwork();
    return writeResults(solversPerClique, cycle, now, start);
  }

  private double runSolverCycleAndReturnError(
      JunctionTreeAlgorithm jta, Map<Clique, List<ConstraintSolver>> solversPerClique) {
    double sum = 0;
    for (Clique clique : solversPerClique.keySet()) {
      sum += solversPerClique.values().parallelStream().mapToDouble(this::solveForClique).sum();
      jta.sumTransfer(clique);
    }
    return sum;
  }

  @SuppressWarnings("StringConcatenationArgumentToLogCall")
  private void logCycleComplete(int cycle, double loss, double error) {
    log.info(String.format("CYCLE %d : LOSS = %1.2e : ERROR = %1.2e", cycle, loss, error));
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

  private SolverResults writeResults(
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
}
