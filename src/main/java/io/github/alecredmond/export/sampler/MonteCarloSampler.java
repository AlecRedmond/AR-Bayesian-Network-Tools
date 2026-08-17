package io.github.alecredmond.export.sampler;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.Observable;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * A Monte Carlo sampler for a {@link BayesianNetwork} which generates random samples from the
 * network's conditional probability tables (CPTs). A {@code MonteCarloSampler} run returns a {@link
 * SampleCollection} of {@link Sample} objects. Each {@link Sample} contains a unique combination of
 * {@link NodeState} values and the frequency of its occurrence.
 *
 * <p>The only sampling algorithm currently available is <i>Likelihood Weighting Sampling (LWS)</i>.
 * LWS performs a random walk down each CPT in the {@link BayesianNetwork}, selecting each new
 * {@link NodeState} according to its weighted probability. When the algorithm reaches a {@link
 * Node} constrained to a specific {@link NodeState}, the weight of the sample is multiplied by the
 * conditional probability of that state given the sample's current configuration. At the end of the
 * run, the weighted samples are normalized and proportionally correct frequencies are assigned.
 *
 * <p>Monte Carlo sampling is a form of indirect inference. It relies on Java's built-in {@link
 * Random} functionality and does not produce exact or deterministic results. The margin of error is
 * proportional to {@code 1 / sqrt(n)}, where {@code n} is the number of samples generated.
 *
 * @see InferenceEngine
 * @see BayesianNetwork
 * @author Alec Redmond
 */
public interface MonteCarloSampler extends Observable {

  /**
   * Creates a new {@code MonteCarloSampler} for the given {@link BayesianNetwork}.
   *
   * @param network the {@link BayesianNetwork} to sample.
   * @return a new {@code MonteCarloSampler} for the given network.
   */
  static MonteCarloSampler create(BayesianNetwork network) {
    return new LikelihoodWeightingSampler(network);
  }

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results. This will apply the current {@link NodeObservation} restrictions
   * present in this sampler.
   *
   * @param numberOfSamples the number of sampling cycles to run, which equals the total sample
   *     count in the returned {@link SampleCollection}.
   * @return a new {@link SampleCollection} representing the posterior distribution, conditional on
   *     the current observations.
   */
  SampleCollection generateSamples(int numberOfSamples);

  /**
   * Returns the {@link BayesianNetwork} sampled by this {@code MonteCarloSampler}.
   *
   * @return the {@link BayesianNetwork} used by this {@code MonteCarloSampler}.
   */
  BayesianNetwork getNetwork();

  /**
   * Removes any observed states from the inference network, returning it to its unobserved
   * configuration. All measured probability values in this configuration will become prior
   * (unconditional) probabilities.
   *
   * @return this instance for chaining.
   */
  @Override
  MonteCarloSampler resetObservations();

  /**
   * Sets the observed state to match the given input. This method will only apply key-value pairs
   * where the given {@link Node} is present within the {@link BayesianNetwork} which backs this
   * {@code Observable}. Other pairs will be discarded and undeclared {@link Node}s will be set as
   * {@link ObservationStatus#UNOBSERVED}.
   *
   * @return this instance for chaining.
   */
  @Override
  MonteCarloSampler setObserved(Map<Node, NodeObservation> observations);

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
  @Override
  MonteCarloSampler setObserved(Collection<NodeState> observedStates);

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
  @Override
  MonteCarloSampler setObserved(NodeState observedState);

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
  @Override
  <T extends Serializable> MonteCarloSampler setObservedById(Collection<T> observedStateIDs);

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
  @Override
  <T extends Serializable> MonteCarloSampler setObservedById(T observedStateId);

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a collection of {@link NodeState}s to eliminate.
   * @return this instance for chaining.
   */
  @Override
  MonteCarloSampler eliminateStates(Collection<NodeState> toEliminate);

  /**
   * Subtracts the input {@link NodeState} value from the observable pool. This will remove this
   * state from consideration when measuring probability values. The final probability values will
   * be conditional on this state not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a single {@link NodeState} to eliminate.
   * @return this instance for chaining.
   */
  @Override
  MonteCarloSampler eliminateStates(NodeState toEliminate);

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
  @Override
  <T extends Serializable> MonteCarloSampler eliminateStatesById(Collection<T> toEliminateIDs);

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
  @Override
  <T extends Serializable> MonteCarloSampler eliminateStatesById(T toEliminateId);
}
