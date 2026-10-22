package io.github.alecredmond.internal.application.cartesianvector;

import java.util.List;

/**
 * A variable holding a set of mutually exclusive {@link CartesianState}s. Named for the Cartesian
 * Product, which is used extensively within this library.
 */
public interface CartesianVariable {
  /**
   * Returns the list of {@link CartesianState}s associated with this {@code CartesianVariable}.
   *
   * @return the {@link CartesianState}s in this {@code CartesianVariable} instance.
   */
  List<? extends CartesianState> getStates();
}
