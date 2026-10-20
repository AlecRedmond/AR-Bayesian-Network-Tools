package io.github.alecredmond.internal.application.cartesianvector;

/** A single state within a {@link CartesianVariable} set. */
public interface CartesianState {
  /**
   * Returns the parent variable of this {@code CartesianState}.
   *
   * @return this instance's parent {@link CartesianVariable}.
   */
  CartesianVariable getNode();
}
