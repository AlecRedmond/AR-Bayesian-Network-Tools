package io.github.alecredmond.export.solver;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.Arrays;
import java.util.Objects;

/**
 * Holds the per-cycle error and loss data for a single {@link ProbabilityConstraint} from an {@link
 * IPFPSolver} run.
 *
 * <p>The loss of a cycle is the change in R-squared error since the previous cycle. A negative loss
 * indicates that the fit is improving, and a positive loss indicates divergence.
 *
 * @author Alec Redmond
 * @param constraint the constraint whose fitting results are recorded in this object
 * @param lastError the R-squared error between the target probability of the constraint and the
 *     fitted value on the final solver cycle
 * @param errors the R-squared error between the target probability of the constraint and the fitted
 *     value at each solver cycle, where index {@code i} corresponds to cycle {@code i}
 * @param losses the change in R-squared error between consecutive solver cycles, where index {@code
 *     i} corresponds to cycle {@code i}
 * @see IPFPResults
 */
public record IPFPConstraintResult(
    ProbabilityConstraint constraint, double lastError, double[] errors, double[] losses) {
  /**
   * Returns the probability constraint associated with these solver results.
   *
   * @return the {@link ProbabilityConstraint} instance
   */
  @Override
  public ProbabilityConstraint constraint() {
    return this.constraint;
  }

  /**
   * Returns the R-squared error recorded on the final cycle of the solver run.
   *
   * @return the R-squared error of the final cycle
   */
  @Override
  public double lastError() {
    return this.lastError;
  }

  /**
   * Returns the R-squared errors recorded on all completed solver cycles.
   *
   * @return an array of the per-cycle errors, where index {@code i} maps to cycle {@code i}
   */
  @Override
  public double[] errors() {
    return this.errors;
  }

  /**
   * Returns the losses recorded on all completed solver cycles. The loss of a cycle is the change
   * in error since the previous cycle.
   *
   * @return an array of the per-cycle error changes, where index {@code i} maps to cycle {@code i}
   */
  @Override
  public double[] losses() {
    return this.losses;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    IPFPConstraintResult that = (IPFPConstraintResult) object;
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
