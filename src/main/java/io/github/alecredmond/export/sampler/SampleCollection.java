package io.github.alecredmond.export.sampler;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.network.BayesianNetwork;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A container for the {@link Sample} objects generated during a Monte Carlo {@link MonteCarloSampler} run. A
 * {@code SampleCollection} holds the complete set of distinct samples produced by the run, along
 * with the observations that were applied, and provides methods for querying sample frequencies and
 * filtering results by {@link NodeState}.
 *
 * <p>Instances of this interface are created by the {@code generateSamples} methods on {@link
 * MonteCarloSampler}.
 *
 * @see MonteCarloSampler
 * @see Sample
 * @author Alec Redmond
 */
public interface SampleCollection {
  /**
   * Returns a map of each {@link Node} to its {@link NodeObservation} record.
   *
   * @return an unmodifiable map of the observations used in this {@code SampleCollection}.
   */
  Map<Node, NodeObservation> getObservations();

  /**
   * Returns an array of all {@link Node}s in the {@link BayesianNetwork} that this {@code
   * SampleCollection} was sampled from. The nodes are in topological order, with root nodes at the
   * start and leaf nodes at the end.
   *
   * @return the {@link Node} array from this {@code SampleCollection}.
   */
  Node[] getNodes();

  /**
   * Returns {@code true} if this {@code SampleCollection} contains no samples. This can occur when
   * the joint probability of the observed {@link NodeState} values is zero — i.e. {@code P(obs) =
   * 0}.
   *
   * @return {@code true} if there are no samples in this collection; {@code false} otherwise.
   */
  boolean isEmpty();

  /**
   * Returns the total count of all {@link Sample} frequencies in this {@code SampleCollection}.
   * This equals the number of samples specified for the {@link MonteCarloSampler} run.
   *
   * @return the total sample count in this {@code SampleCollection}.
   */
  int countSamples();

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain every {@link NodeState} in the given collection. This is equivalent to {@code P(X|obs)
   * * n}, where {@code X} is the set of queried {@link NodeState} values, {@code obs} is the set of
   * observations used during the {@link MonteCarloSampler} run, and {@code n} is the total number of samples
   * generated.
   *
   * @param states the {@link NodeState} values to query.
   * @return the combined frequency of all {@link Sample}s containing every specified state.
   */
  int countSamplesIncludingStates(Collection<NodeState> states);

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain the given {@link NodeState}. This is equivalent to {@code P(x|obs) * n}, where {@code
   * x} is the queried {@link NodeState}, {@code obs} is the set of observations used during the
   * {@link MonteCarloSampler} run, and {@code n} is the total number of samples generated.
   *
   * @param state the {@link NodeState} value to query.
   * @return the combined frequency of all {@link Sample}s containing the specified state.
   */
  int countSamplesIncludingStates(NodeState state);

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain every {@link NodeState} identified by the given identifiers. This is equivalent to
   * {@code P(X|obs) * n}, where {@code X} is the set of queried {@link NodeState} values, {@code
   * obs} is the set of observations used during the {@link MonteCarloSampler} run, and {@code n} is the total
   * number of samples generated.
   *
   * @param stateIds the identifiers of the {@link NodeState} values to query.
   * @param <T> the type of the {@link NodeState} identifiers.
   * @return the combined frequency of all {@link Sample}s containing every specified state.
   */
  <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds);

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain the {@link NodeState} identified by the given identifier. This is equivalent to {@code
   * P(x|obs) * n}, where {@code x} is the queried {@link NodeState}, {@code obs} is the set of
   * observations used during the {@link MonteCarloSampler} run, and {@code n} is the total number of samples
   * generated.
   *
   * @param stateId the identifier of the {@link NodeState} value to query.
   * @param <T> the type of the {@link NodeState} identifier.
   * @return the combined frequency of all {@link Sample}s containing the specified state.
   * @throws NullPointerException if {@code stateId} is not mapped to any valid {@link NodeState}.
   */
  <T extends Serializable> int countSamplesIncludingStateIds(T stateId);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection}. A {@link
   * Sample} contains a unique combination of {@link NodeState} values, with the frequency of their
   * occurrence in the {@link MonteCarloSampler} run.
   *
   * @return an unmodifiable {@link List} of {@link Sample} objects.
   */
  List<Sample> getSamples();

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} that contain
   * every {@link NodeState} in the given collection. Each {@link Sample} contains a unique
   * combination of {@link NodeState} values and the frequency of its occurrence in the {@link
   * MonteCarloSampler} run.
   *
   * @param states the {@link NodeState} values to query.
   * @return an unmodifiable {@link List} of the matching {@link Sample} objects.
   */
  List<Sample> getSamplesIncludingStates(Collection<NodeState> states);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} that contain
   * the given {@link NodeState}. Each {@link Sample} contains a unique combination of {@link
   * NodeState} values and the frequency of its occurrence in the {@link MonteCarloSampler} run.
   *
   * @param state the {@link NodeState} value to query.
   * @return an unmodifiable {@link List} of the matching {@link Sample} objects.
   */
  List<Sample> getSamplesIncludingStates(NodeState state);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} that contain
   * every {@link NodeState} identified by the given identifiers. Each {@link Sample} contains a
   * unique combination of {@link NodeState} values and the frequency of its occurrence in the
   * {@link MonteCarloSampler} run.
   *
   * @param stateIds the identifiers of the {@link NodeState} values to query.
   * @param <T> the type of the {@link NodeState} identifiers.
   * @return an unmodifiable {@link List} of the matching {@link Sample} objects.
   */
  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(Collection<T> stateIds);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} that contain
   * the {@link NodeState} identified by the given identifier. Each {@link Sample} contains a unique
   * combination of {@link NodeState} values and the frequency of its occurrence in the {@link
   * MonteCarloSampler} run.
   *
   * @param stateId the identifier of the {@link NodeState} value to query.
   * @param <T> the type of the {@link NodeState} identifier.
   * @return an unmodifiable {@link List} of the matching {@link Sample} objects.
   * @throws NullPointerException if {@code stateId} is not mapped to any valid {@link NodeState}.
   */
  <T extends Serializable> List<Sample> getSamplesIncludingStatesById(T stateId);

  /**
   * Sets all {@link Node}s as displayed in each {@link Sample}. This resets any display
   * restrictions applied via {@link #setDisplayedNodes(Collection)} or {@link
   * #setDisplayedNodes(Node)}, restoring the default state where every {@link Node} in the sampled
   * {@link BayesianNetwork} is displayed.
   */
  void displayAllNodes();

  /**
   * Restricts the displayed {@link Node}s in each {@link Sample} to the given collection. After
   * this call, {@link Sample} methods such as {@link Sample#getDisplayedStates()} will only return
   * the {@link NodeState}s associated with the specified {@link Node}s. This restriction can be
   * reset by calling {@link #displayAllNodes()} on this {@code SampleCollection}, or on a
   * per-sample basis by calling {@link Sample#displayAllNodes()}. This only affects the states
   * exported on a per-sample basis and does not affect count methods such as {@link
   * #countSamplesIncludingStates(Collection)}.
   *
   * @param nodes the {@link Node} objects each {@link Sample} will be restricted to.
   * @see #displayAllNodes()
   */
  void setDisplayedNodes(Collection<Node> nodes);

  /**
   * Restricts the displayed {@link Node} in each {@link Sample} to a single given instance. After
   * this call, {@link Sample} methods such as {@link Sample#getDisplayedStates()} will only return
   * the {@link NodeState} associated with the specified {@link Node}. This restriction can be reset
   * by calling {@link #displayAllNodes()} on this {@code SampleCollection}, or on a per-sample
   * basis by calling {@link Sample#displayAllNodes()}. This only affects the states exported on a
   * per-sample basis and does not affect count methods such as {@link
   * #countSamplesIncludingStates(Collection)}.
   *
   * @param node the {@link Node} each {@link Sample} will be restricted to.
   * @see #displayAllNodes()
   */
  void setDisplayedNodes(Node node);

  /**
   * Restricts the displayed {@link Node}s in each {@link Sample} to those identified by the given
   * identifiers. After this call, {@link Sample} methods such as {@link
   * Sample#getDisplayedStates()} will only return the {@link NodeState}s associated with the
   * specified {@link Node}s. This restriction can be reset by calling {@link #displayAllNodes()} on
   * this {@code SampleCollection}, or on a per-sample basis by calling {@link
   * Sample#displayAllNodes()}. This only affects the states exported on a per-sample basis and does
   * not affect count methods such as {@link #countSamplesIncludingStates(Collection)}.
   *
   * @param nodeIds the identifiers of the {@link Node} objects each {@link Sample} will be
   *     restricted to.
   * @param <T> the type of the {@link Node} identifiers.
   * @see #displayAllNodes()
   */
  <T extends Serializable> void setDisplayedNodesById(Collection<T> nodeIds);

  /**
   * Restricts the displayed {@link Node} in each {@link Sample} to the single instance identified
   * by the given identifier. After this call, {@link Sample} methods such as {@link
   * Sample#getDisplayedStates()} will only return the {@link NodeState} associated with the
   * specified {@link Node}. This restriction can be reset by calling {@link #displayAllNodes()} on
   * this {@code SampleCollection}, or on a per-sample basis by calling {@link
   * Sample#displayAllNodes()}. This only affects the states exported on a per-sample basis and does
   * not affect count methods such as {@link #countSamplesIncludingStates(Collection)}.
   *
   * @param nodeId the identifier of the {@link Node} each {@link Sample} will be restricted to.
   * @param <T> the type of the {@link Node} identifier.
   * @throws NullPointerException if {@code nodeId} is not mapped to any valid {@link Node}.
   * @see #displayAllNodes()
   */
  <T extends Serializable> void setDisplayedNodesById(T nodeId);
}
