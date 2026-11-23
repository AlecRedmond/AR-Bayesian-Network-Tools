package io.github.alecredmond.export.inference.indirect;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.*;

/**
 * A container for {@link Sample} objects generated during a {@link MonteCarloSampler} run. A {@code
 * SampleCollection} holds the complete set of distinct samples produced by the run, along with the
 * observations that were applied, and provides methods for querying sample frequencies and
 * filtering results by {@link NodeState}.
 *
 * <p>Instances of this interface are created by the {@code generateSamples} methods on {@link
 * MonteCarloSampler}.
 *
 * @see MonteCarloSampler
 * @see Sample
 * @author Alec Redmond
 */
public interface SampleCollection extends List<Sample> {
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
   * Returns the total count of all {@link Sample} frequencies in this {@code SampleCollection}.
   * This equals the number of samples specified for the {@link MonteCarloSampler} run.
   *
   * @return the total sample count in this {@code SampleCollection}.
   */
  int countSamples();

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain the given {@link NodeState}s. This is equivalent to {@code P(x|obs) * n}, where {@code
   * x} is the queried {@link NodeState}, {@code obs} is the set of observations used during the
   * {@link MonteCarloSampler} run, and {@code n} is the total number of samples generated.
   *
   * @param states the {@link NodeState} values to query.
   * @return the combined frequency of all {@link Sample}s containing the specified states.
   */
  default int countSamplesIncludingStates(NodeState... states) {
    return countSamplesIncludingStates(Arrays.asList(states));
  }

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain every {@link NodeState} in the given collection. This is equivalent to {@code P(X|obs)
   * * n}, where {@code X} is the set of queried {@link NodeState} values, {@code obs} is the set of
   * observations used during the {@link MonteCarloSampler} run, and {@code n} is the total number
   * of samples generated.
   *
   * @param states the {@link NodeState} values to query.
   * @return the combined frequency of all {@link Sample}s containing every specified state.
   */
  int countSamplesIncludingStates(Collection<NodeState> states);

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain every {@link NodeState} identified by the given identifiers. This is equivalent to
   * {@code P(X|obs) * n}, where {@code X} is the set of queried {@link NodeState} values, {@code
   * obs} is the set of observations used during the {@link MonteCarloSampler} run, and {@code n} is
   * the total number of samples generated.
   *
   * @param stateIds the identifier of the {@link NodeState} value to query.
   * @return the combined frequency of all {@link Sample}s containing the specified state.
   * @throws NullPointerException if {@code stateId} is not mapped to any valid {@link NodeState}.
   */
  default int countSamplesIncludingStateIds(Serializable... stateIds) {
    return countSamplesIncludingStateIds(Arrays.asList(stateIds));
  }

  /**
   * Returns the combined frequency of all {@link Sample}s in this {@code SampleCollection} that
   * contain every {@link NodeState} identified by the given identifiers. This is equivalent to
   * {@code P(X|obs) * n}, where {@code X} is the set of queried {@link NodeState} values, {@code
   * obs} is the set of observations used during the {@link MonteCarloSampler} run, and {@code n} is
   * the total number of samples generated.
   *
   * @param stateIds the identifiers of the {@link NodeState} values to query.
   * @param <T> the type of the {@link NodeState} identifiers.
   * @return the combined frequency of all {@link Sample}s containing every specified state.
   */
  <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds);

  /**
   * Returns a list of all distinct {@link Sample}s in this {@code SampleCollection} that contain
   * every {@link NodeState} in the given collection. Each {@link Sample} contains a unique
   * combination of {@link NodeState} values and the frequency of its occurrence in the {@link
   * MonteCarloSampler} run.
   *
   * @param states the {@link NodeState} values to query.
   * @return an unmodifiable {@link List} of the matching {@link Sample} objects.
   */
  default List<Sample> getSamplesIncludingStates(NodeState... states) {
    return getSamplesIncludingStates(Arrays.asList(states));
  }

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
   * every {@link NodeState} identified by the given identifiers. Each {@link Sample} contains a
   * unique combination of {@link NodeState} values and the frequency of its occurrence in the
   * {@link MonteCarloSampler} run.
   *
   * @param stateIds the identifiers of the {@link NodeState} values to query.
   * @return an unmodifiable {@link List} of the matching {@link Sample} objects.
   */
  default List<Sample> getSamplesIncludingStatesById(Serializable... stateIds) {
    return getSamplesIncludingStatesById(Arrays.asList(stateIds));
  }

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
   * Sets all {@link Node}s as displayed in each {@link Sample}. This resets any display
   * restrictions applied via {@link #setDisplayedNodes(Node...)}, restoring the default state where
   * every {@link Node} in the sampled {@link BayesianNetwork} is displayed.
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
  default void setDisplayedNodes(Node... nodes) {
    setDisplayedNodes(Arrays.asList(nodes));
  }

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
   * @see #displayAllNodes()
   */
  default void setDisplayedNodesById(Serializable... nodeIds) {
    setDisplayedNodesById(Arrays.asList(nodeIds));
  }

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
}
