package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.solver.BayesSolver;

/**
 * Thrown if a {@link ProbabilityTable} in {@link BayesSolver} or {@link InferenceEngine} would
 * exceed 2<sup>31</sup>&minus;1 entries.
 */
public class TreewidthException extends IllegalStateException {
  /**
   * Constructs a {@code TreewidthException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public TreewidthException(String message) {
    super(message);
  }
}
