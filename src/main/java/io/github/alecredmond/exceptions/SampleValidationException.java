package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;

/** Thrown if samples generated in {@link MonteCarloSampler} failed validation. */
public class SampleValidationException extends RuntimeException {
  /**
   * Constructs a {@code SampleValidationException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public SampleValidationException(String message) {
    super(message);
  }
}
