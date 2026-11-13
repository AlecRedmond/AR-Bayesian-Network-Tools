package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;

/** Thrown to indicate an illegal {@link ProbabilityConstraint} configuration was found. */
public class ConstraintValidationException extends IllegalArgumentException {
  /**
   * Constructs a {@code ConstraintValidationException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public ConstraintValidationException(String message) {
    super(message);
  }
}
