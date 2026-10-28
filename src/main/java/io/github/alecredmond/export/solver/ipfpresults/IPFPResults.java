package io.github.alecredmond.export.solver.ipfpresults;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.solver.BayesSolver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the results of a {@link BayesSolver} Iterative Proportional Fitting Procedure (IPFP).
 *
 * <p>This includes the total number of cycles completed, the aggregate R-squared error calculated
 * on the final cycle, and a per-constraint breakdown linking each active {@link
 * ProbabilityConstraint} to its respective {@link IPFPConstraintResult}.
 *
 * @see IPFPConstraintResult
 * @see BayesSolver
 * @author Alec Redmond
 * @param cycles The total number of cycles completed by the solver.
 * @param constraintResults A map that connects each constraint to its specific result, including
 *     details of per-cycle error and loss rates. The map is a {@link LinkedHashMap}, sorted in
 *     reverse order of final cycle R-squared error.
 * @param finalError The final aggregate R-squared error between the fitted data and the expected
 *     constraint data, measured over the last cycle of the solver's execution.
 * @param solverRunDuration The total duration of the solver's IPFP run.
 */
@SuppressWarnings("unused")
public record IPFPResults(
    int cycles,
    Map<ProbabilityConstraint, IPFPConstraintResult> constraintResults,
    double finalError,
    Duration solverRunDuration) {

  /**
   * Returns the detailed result for a single constraint over the course of the solver run,
   * including per-cycle error and loss.
   *
   * @param constraint a constraint that was active in the network during solving.
   * @return the {@link IPFPConstraintResult} for the given constraint, or {@code null} if the
   *     constraint was not present in this run.
   */
  public IPFPConstraintResult getResult(ProbabilityConstraint constraint) {
    return constraintResults.get(constraint);
  }

  /**
   * Returns a list of constraint results responsible for a cumulative percentage of the total
   * error.
   *
   * <p>The returned elements are structured in descending order of final-cycle error, accumulating
   * up to the specified percentage threshold. This is useful for identifying constraints that
   * cannot be satisfied within the data, or that conflict with other constraints.
   *
   * <p>Typically, highly conflicting outliers break outward by orders of magnitude and can be found
   * within the worst 95.45% (2 standard deviations) of total aggregate error. The fewer entries
   * there are in the returned list, the greater the likelihood that the constraints in the list
   * have caused overwrite conflicts within the IPFP run.
   *
   * @param percent the cumulative percentage threshold of total error to isolate, expressed as a
   *     value between 0 and 100 (e.g., {@code 95} to isolate the constraints driving 95% of total
   *     error).
   * @return a list of {@link IPFPConstraintResult}s sorted in descending order of final-cycle error
   *     whose sum roughly approximates the requested percentage target.
   */
  public List<IPFPConstraintResult> getWorstNthPercentile(Number percent) {
    double goal = finalError * (percent.doubleValue() / 100.0);
    List<IPFPConstraintResult> worstResults = new ArrayList<>();
    for (IPFPConstraintResult result : constraintResults.values()) {
      if (goal <= 0.0) break;
      worstResults.add(result);
      goal -= result.lastError();
    }
    return worstResults;
  }

  /**
   * Returns the total count of execution cycles completed during the IPFP operation.
   *
   * @return the total number of integer cycles.
   */
  @Override
  public int cycles() {
    return this.cycles;
  }

  /**
   * Returns the complete sorted mapping of active constraints to their IPFP metrics.
   *
   * @return a new {@link Map} sorted in descending order of final cycle R-squared error.
   */
  @Override
  public Map<ProbabilityConstraint, IPFPConstraintResult> constraintResults() {
    return Map.copyOf(this.constraintResults);
  }

  /**
   * Returns the aggregate final-cycle R-squared error evaluated across all constraints.
   *
   * @return the final aggregate error value as a double.
   */
  @Override
  public double finalError() {
    return this.finalError;
  }

  /**
   * Returns the total duration of the solver's IPFP execution. The timer starts immediately before
   * the construction of the joint probability table(s) and finishes immediately after the final
   * IPFP cycle completes. This represents the total processing time once control is given to the
   * specific solver algorithm, and may be used to compare different IPFP variants under the same
   * conditions.
   *
   * @return a {@link Duration} representing solver execution time.
   */
  @Override
  public Duration solverRunDuration() {
    return this.solverRunDuration;
  }
}
