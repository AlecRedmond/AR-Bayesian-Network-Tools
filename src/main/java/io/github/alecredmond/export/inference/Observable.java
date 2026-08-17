package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * The root interface for a Bayesian inference type where the observed state can be modified. An
 * {@code Observable} type allows {@link NodeState} values to either be set as observed (resetting
 * the previous observations) or added to a pool of eliminated states (maintaining the other
 * previous observations). All probability functions will be conditional on only the non-eliminated
 * {@link NodeState} values currently active.
 *
 * @see InferenceEngine
 * @see MonteCarloSampler
 * @see NodeObservation
 * @author Alec Redmond
 */
public interface Observable {

  /**
   * Returns the {@link BayesianNetwork} measured by this {@code Observable}.
   *
   * @return the {@link BayesianNetwork} associated with this {@code Observable}.
   */
  BayesianNetwork getNetwork();

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

  /**
   * Sets the observed state to match the given input. This method will only apply key-value pairs
   * where the given {@link Node} is present within the {@link BayesianNetwork} which backs this
   * {@code Observable}. Other pairs will be discarded and undeclared {@link Node}s will be set as
   * {@link ObservationStatus#UNOBSERVED}.
   *
   * @return this instance for chaining.
   */
  Observable setObserved(Map<Node, NodeObservation> observations);

  /**
   * Replaces the current observed states in the inference network with the given states. Each
   * {@link NodeState} will lock its associated {@link Node} to measure only itself and any other
   * sibling states declared in the collection. This will change all measured probability values in
   * this instance to posterior probabilities, conditional on these states.
   *
   * <p><i>NOTE: This method calls {@link #resetObservations()} before applying the new
   * observations.</i>
   *
   * @param observedStates the collection of states to be observed.
   * @return this instance for chaining.
   */
  Observable setObserved(Collection<NodeState> observedStates);

  /**
   * Replaces the current observed states in the inference network with the given {@link NodeState}.
   * This will lock its associated {@link Node} to that value. This will change all measured
   * probability values in this instance to posterior probabilities, conditional on this state.
   *
   * <p><i>NOTE: This method calls {@link #resetObservations()} before applying the new
   * observations.</i>
   *
   * @param observedState the single state to be observed.
   * @return this instance for chaining.
   */
  Observable setObserved(NodeState observedState);

  /**
   * Replaces the current observed states in the inference network with the given states. Each
   * {@link NodeState} will lock its associated {@link Node} to measure only itself and any other
   * sibling states declared in the collection. This will change all measured probability values in
   * this instance to posterior probabilities, conditional on these states.
   *
   * <p><i>NOTE: This method calls {@link #resetObservations()} before applying the new
   * observations.</i>
   *
   * @param observedStateIDs the collection of identifiers associated with the states to be
   *     observed.
   * @param <T> the type of the state identifiers.
   * @return this instance for chaining.
   */
  <T extends Serializable> Observable setObservedById(Collection<T> observedStateIDs);

  /**
   * Replaces the current observed states in the inference network with the given {@link NodeState},
   * referenced by its identifier. This will lock its associated {@link Node} to that value. This
   * will change all measured probability values in this instance to posterior probabilities,
   * conditional on this state.
   *
   * <p><i>NOTE: This method calls {@link #resetObservations()} before applying the new
   * observations.</i>
   *
   * @param observedStateId the identifier of the single state to be observed.
   * @param <T> the type of the state identifier.
   * @return this instance for chaining.
   */
  <T extends Serializable> Observable setObservedById(T observedStateId);

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a collection of {@link NodeState}s to eliminate.
   * @return this instance for chaining.
   */
  Observable eliminateStates(Collection<NodeState> toEliminate);

  /**
   * Subtracts the input {@link NodeState} value from the observable pool. This will remove this
   * state from consideration when measuring probability values. The final probability values will
   * be conditional on this state not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a single {@link NodeState} to eliminate.
   * @return this instance for chaining.
   */
  Observable eliminateStates(NodeState toEliminate);

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminateIDs a collection of identifiers for {@link NodeState}s to eliminate.
   * @param <T> the type of the state identifiers.
   * @return this instance for chaining.
   */
  <T extends Serializable> Observable eliminateStatesById(Collection<T> toEliminateIDs);

  /**
   * Subtracts the input {@link NodeState} value from the observable pool. This will remove this
   * state from consideration when measuring probability values. The final probability values will
   * be conditional on this state not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminateId the identifier of a single {@link NodeState} to eliminate.
   * @param <T> the type of the state identifier.
   * @return this instance for chaining.
   */
  <T extends Serializable> Observable eliminateStatesById(T toEliminateId);
}
