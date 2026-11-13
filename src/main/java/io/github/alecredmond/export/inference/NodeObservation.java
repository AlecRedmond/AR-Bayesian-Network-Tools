package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.*;

/**
 * An observed configuration of a single {@link Node} within an {@link Observable} instance. This
 * measures which {@link NodeState} values in the given {@link Node}'s state list are observed and
 * eliminated. Typically, this will be given in a map as the value, with the key being the measured
 * {@link Node}.
 *
 * @see Observable
 * @see InferenceEngine
 * @see MonteCarloSampler
 * @author Alec Redmond
 */
public interface NodeObservation {

  /**
   * Returns the measured {@link Node} in this {@code NodeObservation}.
   *
   * @return the measured {@link Node}.
   */
  Node node();

  /**
   * Returns all {@link NodeState}s within the {@link Node} which are set to be observable.
   *
   * @return an unmodifiable {@link LinkedHashSet} of the observed states in this {@code
   *     NodeObservation}.
   */
  Set<NodeState> observedStates();

  /**
   * Returns all {@link NodeState}s within the {@link Node} which are set as eliminated.
   *
   * @return an unmodifiable {@link LinkedHashSet} of the eliminated states in this {@code
   *     NodeObservation}.
   */
  Set<NodeState> eliminatedStates();

  /**
   * Returns the {@link ObservationStatus} of this {@code NodeObservation}. This can take the
   * following values:
   *
   * <ul>
   *   <li>{@code UNOBSERVED}: All {@link NodeState}s associated with the {@link Node} are active
   *       within the {@link Observable}.
   *   <li>{@code PARTIALLY_OBSERVED}: Some {@link NodeState}s associated with the {@link Node} have
   *       been eliminated, but more than one active state remains.
   *   <li>{@code OBSERVED}: A single {@link NodeState} associated with the {@link Node} remains
   *       active, with all other states eliminated.
   *   <li>{@code NEGATED}: All {@link NodeState}s associated with the {@link Node} have been
   *       eliminated. This will set the joint probability of the entire {@link Observable} to 0.
   * </ul>
   *
   * @return the {@link ObservationStatus} of this {@code NodeObservation}.
   */
  ObservationStatus status();
}
