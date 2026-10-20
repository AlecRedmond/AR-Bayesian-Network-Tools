package io.github.alecredmond.internal.method.solver;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.solver.SolverConstraintResult;
import io.github.alecredmond.export.solver.SolverResults;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SolverResultsBuilder {

  public SolverResults buildResults(
      int cycle, Map<ProbabilityConstraint, double[]> resultsMap, Duration duration) {
    Map<ProbabilityConstraint, SolverConstraintResult> solverResultMap =
        resultsMap.entrySet().stream()
            .map(e -> Map.entry(e.getKey(), buildConstraintResult(e)))
            .sorted(byLastErrorReversed())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

    double lastError =
        solverResultMap.values().stream().mapToDouble(SolverConstraintResult::lastError).sum();
    return new SolverResults(cycle, solverResultMap, lastError, duration);
  }

  private SolverConstraintResult buildConstraintResult(
      Map.Entry<ProbabilityConstraint, double[]> entry) {
    ProbabilityConstraint constraint = entry.getKey();
    double[] errors = entry.getValue();
    double lastError = errors[errors.length - 1];
    double[] losses = new double[errors.length];
    IntStream.range(1, errors.length).forEach(i -> losses[i] = Math.abs(errors[i] - errors[i - 1]));
    return new SolverConstraintResult(constraint, lastError, errors, losses);
  }

  private static Comparator<Map.Entry<ProbabilityConstraint, SolverConstraintResult>>
      byLastErrorReversed() {
    return Comparator.comparingDouble(
            (Map.Entry<ProbabilityConstraint, SolverConstraintResult> e) ->
                e.getValue().lastError())
        .reversed();
  }
}
