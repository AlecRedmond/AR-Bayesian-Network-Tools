package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import java.util.Arrays;
import java.util.Objects;

/**
 * Holds the per-cycle error and loss data for a single {@link ProbabilityConstraint} from a {@link
 * BayesSolver} run.
 *
 * @see SolverResults
 * @author Alec Redmond
 * @param constraint The constraint whose fitting results are recorded in this object.
 * @param lastError The R-squared error between the constraint's target probability and the fitted
 *     value on the final solver cycle.
 * @param errors The R-squared error between the constraint's target probability and the fitted
 *     value at each solver cycle. Index {@code i} corresponds to cycle {@code i}.
 * @param losses The change in R-squared error between consecutive solver cycles. Index {@code i}
 *     corresponds to cycle {@code i}.
 *     <p>A negative value indicates the fit is improving; a positive value indicates divergence.
 */
public record SolverConstraintResult(
    ProbabilityConstraint constraint, double lastError, double[] errors, double[] losses) {
  /**
   * Returns the probability constraint associated with these solver results.
   *
   * @return the {@link ProbabilityConstraint} instance.
   */
  @Override
  public ProbabilityConstraint constraint() {
    return this.constraint;
  }

  /**
   * Returns the R-squared error recorded on the final cycle of the solver run.
   *
   * @return the final cycle's R-squared error as a double.
   */
  @Override
  public double lastError() {
    return this.lastError;
  }

  /**
   * Returns the historical array of R-squared errors across all completed solver cycles.
   *
   * @return an array of doubles representing per-cycle errors, where index {@code i} maps to cycle
   *     {@code i}.
   */
  @Override
  public double[] errors() {
    return this.errors;
  }

  /**
   * Returns the historical array of loss rates (error deltas) across all completed solver cycles.
   *
   * @return an array of doubles representing consecutive cycle error changes, where index {@code i}
   *     maps to cycle {@code i}.
   */
  @Override
  public double[] losses() {
    return this.losses;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    SolverConstraintResult that = (SolverConstraintResult) object;
    return Double.compare(lastError, that.lastError) == 0
        && Objects.deepEquals(errors, that.errors)
        && Objects.deepEquals(losses, that.losses)
        && Objects.equals(constraint, that.constraint);
  }

  @Override
  public int hashCode() {
    return Objects.hash(constraint, lastError, Arrays.hashCode(errors), Arrays.hashCode(losses));
  }

  @Override
  public String toString() {
    return "SolverConstraintResult{"
        + "constraint="
        + constraint
        + ", lastError="
        + lastError
        + ", errors="
        + Arrays.toString(errors)
        + ", losses="
        + Arrays.toString(losses)
        + '}';
  }
}
