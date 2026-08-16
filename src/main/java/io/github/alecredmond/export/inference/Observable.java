package io.github.alecredmond.export.inference;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface Observable {

  /**
   * Returns a map of each {@link Node} and its current {@link NodeObservation}. A {@link
   * NodeObservation} records the current observed and unobserved {@link NodeState}s in the node,
   * and its {@link ObservationStatus}.
   *
   * @return a map of the current observations on this instance.
   */
  Map<Node, NodeObservation> getCurrentObservations();

  /**
   * Removes any observed states from the inference network, returning it to its unobserved
   * configuration. All measured probability values in this configuration will become prior
   * (unconditional) probabilities.
   *
   * @return this instance for chaining.
   */
  Observable resetObservations();

  // TODO - JAVADOC
  Observable setObserved(Map<Node, NodeObservation> observations);

  /**
   * Replaces the current observed states in the inference network with the given states. Each
   * {@link NodeState} will lock its associated {@link Node} to that value. This will change all
   * measured probability values in this instance to posterior probabilities, conditional on these
   * states.
   *
   * @param observedStates the collection of states to be observed.
   * @return this instance for chaining.
   * @throws NodeStateConflictException if multiple {@link NodeState} values would map to the same
   *     {@link Node}.
   */
  Observable setObserved(Collection<NodeState> observedStates);

  /**
   * Replaces the current observed states in the inference network with the given {@link NodeState}.
   * This will lock its associated {@link Node} to that value. This will change all measured
   * probability values in this instance to posterior probabilities, conditional on this state.
   *
   * @param observedState the single state to be observed.
   * @return this instance for chaining.
   */
  Observable setObserved(NodeState observedState);

  /**
   * Replaces the current observed states in the inference network with the given {@link NodeState},
   * referenced by its identifier. This will lock its associated {@link Node} to that value. This
   * will change all measured probability values in this instance to posterior probabilities,
   * conditional on this state.
   *
   * @param observedStateId the identifier of the single state to be observed.
   * @param <T> the type of the state identifier.
   * @return this instance for chaining.
   */
  <T extends Serializable> Observable setObservedById(T observedStateId);

  /**
   * Replaces the current observed states in the inference network with the given states, identified
   * by their identifiers. Each {@link NodeState} will lock its associated {@link Node} to that
   * value. This will change all measured probability values in this instance to posterior
   * probabilities, conditional on these states.
   *
   * @param observedStateIDs the collection of identifiers associated with the states to be
   *     observed.
   * @param <T> the type of the state identifiers.
   * @return this instance for chaining.
   * @throws NodeStateConflictException if multiple {@link NodeState} values would map to the same
   *     {@link Node}.
   */
  <T extends Serializable> Observable setObservedById(Collection<T> observedStateIDs);

  // TODO - JAVADOC
  Observable eliminateStates(Collection<NodeState> toEliminate);

  // TODO - JAVADOC
  Observable eliminateStates(NodeState toEliminate);

  // TODO - JAVADOC
  <T extends Serializable> Observable eliminateStatesById(Collection<T> toEliminateIDs);

  // TODO - JAVADOC
  <T extends Serializable> Observable eliminateStatesById(T toEliminateIDs);
}
