package io.github.alecredmond.export.sampler;

import io.github.alecredmond.exceptions.NodeStateConflictException;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
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
public interface MonteCarloSampler {

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
   * containing the results. No observations are applied, so the resulting sample set will converge
   * toward the prior distribution of the {@link BayesianNetwork} as {@code numberOfSamples}
   * increases.
   *
   * @param currentObservations
   * @param numberOfSamples     the number of sampling cycles to run, which equals the total sample
   *                            count in the returned {@link SampleCollection}.
   * @return a new {@link SampleCollection} representing the prior distribution.
   */
  SampleCollection generateSamples(Map<Node, NodeObservation> currentObservations, int numberOfSamples);

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results. The current observations from the given {@link InferenceEngine} are
   * applied as conditions, so the resulting sample set will converge toward the posterior
   * distribution of the {@link BayesianNetwork} conditional on those observations as {@code
   * numberOfSamples} increases.
   *
   * <p>This is equivalent to calling {@link #generateSamples(Collection, int)} with {@code
   * inferenceEngine.getCurrentObservations().values()} as the {@link NodeState} collection.
   *
   * @param engine an {@link InferenceEngine} whose current observations are used as conditions.
   * @param numberOfSamples the number of sampling cycles to run, which equals the total sample
   *     count in the returned {@link SampleCollection}.
   * @return a new {@link SampleCollection} representing the posterior distribution.
   */
  SampleCollection generateSamples(InferenceEngine engine, int numberOfSamples);

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results. The given {@link NodeState} values are applied as observations, so the
   * resulting sample set will converge toward the posterior distribution of the {@link
   * BayesianNetwork} conditional on those states as {@code numberOfSamples} increases.
   *
   * @param observedStates the {@link NodeState} values to treat as observations; these will be
   *     present in every generated {@link Sample}.
   * @param numberOfSamples the number of sampling cycles to run, which equals the total sample
   *     count in the returned {@link SampleCollection}.
   * @return a new {@link SampleCollection} representing the posterior distribution.
   * @throws NodeStateConflictException if two or more {@link NodeState}s in {@code observedStates}
   *     belong to the same {@link Node}.
   */
  SampleCollection generateSamples(Collection<NodeState> observedStates, int numberOfSamples);

  /**
   * Runs the sampler for the given number of cycles and returns a {@link SampleCollection}
   * containing the results. The {@link NodeState} values identified by the given identifiers are
   * applied as observations, so the resulting sample set will converge toward the posterior
   * distribution of the {@link BayesianNetwork} conditional on those states as {@code
   * numberOfSamples} increases.
   *
   * @param observedStateIds the identifiers of the {@link NodeState} values to treat as
   *     observations; the corresponding states will be present in every generated {@link Sample}.
   * @param numberOfSamples the number of sampling cycles to run, which equals the total sample
   *     count in the returned {@link SampleCollection}.
   * @param <T> the type of the {@link NodeState} identifiers.
   * @return a new {@link SampleCollection} representing the posterior distribution.
   * @throws NodeStateConflictException if two or more resolved {@link NodeState}s belong to the
   *     same {@link Node}.
   */
  <T extends Serializable> SampleCollection generateSamplesById(
      Collection<T> observedStateIds, int numberOfSamples);

  /**
   * Returns the {@link BayesianNetwork} sampled by this {@code MonteCarloSampler}.
   *
   * @return the {@link BayesianNetwork} used by this {@code MonteCarloSampler}.
   */
  BayesianNetwork getNetwork();
}
