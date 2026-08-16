package io.github.alecredmond.export.sampler;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.Observable;
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

  @Override
  MonteCarloSampler resetObservations();

  @Override
  MonteCarloSampler setObserved(Map<Node, NodeObservation> observations);

  @Override
  MonteCarloSampler setObserved(Collection<NodeState> observedStates);

  @Override
  MonteCarloSampler setObserved(NodeState observedState);

  @Override
  <T extends Serializable> MonteCarloSampler setObservedById(T observedStateId);

  @Override
  <T extends Serializable> MonteCarloSampler setObservedById(Collection<T> observedStateIDs);

  @Override
  MonteCarloSampler eliminateStates(Collection<NodeState> toEliminate);

  @Override
  MonteCarloSampler eliminateStates(NodeState toEliminate);

  @Override
  <T extends Serializable> MonteCarloSampler eliminateStatesById(Collection<T> toEliminateIDs);

  @Override
  <T extends Serializable> MonteCarloSampler eliminateStatesById(T toEliminateIDs);

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
}
