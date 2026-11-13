package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * The root interface for a Bayesian inference type where the observed state can be modified. An
 * {@code Observable} type allows {@link NodeState} values to either be set as observed (resetting
 * the previous observations) or added to a pool of eliminated states (maintaining the other
 * previous observations). All probability functions will be conditional on only the non-eliminated
 * {@link NodeState} values currently active.
 *
 * @param <T> the type of the observable, for instance chaining.
 * @see InferenceEngine
 * @see MonteCarloSampler
 * @see NodeObservation
 * @author Alec Redmond
 */
public interface Observable<T extends Observable<T>> {

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
  T resetObservations();

  /**
   * Sets the observed state to match the given input. This method will only apply key-value pairs
   * where the given {@link Node} is present within the {@link BayesianNetwork} which backs this
   * {@code Observable}. Other pairs will be discarded and undeclared {@link Node}s will be set as
   * {@link ObservationStatus#UNOBSERVED}.
   *
   * @param observations the observation map to set.
   * @return this instance for chaining.
   */
  T setObserved(Map<Node, NodeObservation> observations);

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
  default T setObserved(NodeState... observedStates) {
    return setObserved(Arrays.asList(observedStates));
  }

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
  T setObserved(Collection<NodeState> observedStates);

  /**
   * Replaces the current observed states in the inference network with the given states. Each
   * {@link NodeState} will lock its associated {@link Node} to measure only itself and any other
   * sibling states declared in the collection. This will change all measured probability values in
   * this instance to posterior probabilities, conditional on these states.
   *
   * <p><i>NOTE: This method calls {@link #resetObservations()} before applying the new
   * observations.</i>
   *
   * @param observedStateIds the collection of identifiers associated with the states to be
   *     observed.
   * @return this instance for chaining.
   */
  default T setObservedById(Serializable... observedStateIds) {
    return setObservedById(Arrays.asList(observedStateIds));
  }

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
   * @param <S> the type of the state identifiers.
   * @return this instance for chaining.
   */
  <S extends Serializable> T setObservedById(Collection<S> observedStateIDs);

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a collection of {@link NodeState}s to eliminate.
   * @return this instance for chaining.
   */
  default T eliminateStates(NodeState... toEliminate) {
    return eliminateStates(Arrays.asList(toEliminate));
  }

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a collection of {@link NodeState}s to eliminate.
   * @return this instance for chaining.
   */
  T eliminateStates(Collection<NodeState> toEliminate);

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminateIds a collection of identifiers for {@link NodeState}s to eliminate.
   * @return this instance for chaining.
   */
  default T eliminateStatesById(Serializable... toEliminateIds) {
    return eliminateStatesById(Arrays.asList(toEliminateIds));
  }

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminateIDs a collection of identifiers for {@link NodeState}s to eliminate.
   * @param <S> the type of the state identifiers.
   * @return this instance for chaining.
   */
  <S extends Serializable> T eliminateStatesById(Collection<S> toEliminateIDs);
}
