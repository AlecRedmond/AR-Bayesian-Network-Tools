package io.github.alecredmond.export.inference;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.method.inference.InferenceEngineFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * {@code InferenceEngine} provides utilities for running direct inference on a solved {@link
 * BayesianNetwork}. The values stored in the network's Conditional Probability Tables (CPTs) are
 * mapped to a secondary inference network which can be queried for joint, marginal, or conditional
 * probabilities. The documentation for {@link InferenceAlgorithm} provides further information on
 * the available variants.
 *
 * <p>Specific {@link NodeState} values can be set as observed (always true) using {@link
 * #setObserved(Collection)} or {@link #setObservedById(Collection)}. These will be persisted in the
 * instance until new observations are given, or until the observations are cleared using {@link
 * #resetObservations()}.
 *
 * <p>Unlike Monte Carlo sampling (as used in {@link MonteCarloSampler}), which will give an
 * approximate solution, the results of direct inference are both exact and deterministic.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @see BayesSolver
 * @see InferenceAlgorithm
 * @author Alec Redmond
 */
@SuppressWarnings("UnusedReturnValue")
public interface InferenceEngine extends Observable {
  /**
   * Creates an {@code InferenceEngine} from the given {@link BayesianNetwork}, using the default
   * inference variant configured in {@code app.properties}. The active variant is controlled by
   * {@code app.bayes.inference.defaultInferenceAlgorithm} (default: {@code
   * JUNCTION_TREE_ALGORITHM}). This will automatically solve the network if it is unsolved.
   *
   * @param network the network where inference is to be run.
   * @return a new {@code InferenceEngine} instance, or {@code null} if the given network was
   *     unsolved and could not be solved.
   */
  static InferenceEngine create(BayesianNetwork network) {
    return new InferenceEngineFactory().create(network);
  }

  /**
   * Creates an {@code InferenceEngine} from a {@link BayesianNetwork} and a given {@link
   * InferenceAlgorithm} variant. This will automatically solve the network if it is unsolved. Under
   * almost all circumstances the use of {@link InferenceAlgorithm#JUNCTION_TREE_ALGORITHM} is
   * advised. Refer to the {@link InferenceAlgorithm} documentation for further information.
   *
   * @param network the network where inference is to be run.
   * @param inferenceAlgorithm the {@link InferenceAlgorithm} variant to use for this instance.
   * @return a new {@code InferenceEngine} instance, or {@code null} if the given network was
   *     unsolved and could not be solved.
   */
  static InferenceEngine create(BayesianNetwork network, InferenceAlgorithm inferenceAlgorithm) {
    return new InferenceEngineFactory().create(network, inferenceAlgorithm);
  }

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. The values on this table are not deep-copied and will change if the
   * observations on this instance change.
   *
   * @param nodeId the identifier of the {@link Node} associated with the {@link ObservedTable}.
   * @param <T> the type of the node identifier.
   * @return a {@link ObservedTable} mapping a single node's states to their current probability.
   * @throws NullPointerException if the identifier was not associated with any {@link Node}.
   */
  <T extends Serializable> ObservedTable getObservedTableById(T nodeId);

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. The values on this table are not deep-copied and will change if the
   * observations on this instance change.
   *
   * @param node the {@link Node} associated with the {@link ObservedTable}.
   * @return a {@link ObservedTable} mapping a single node's states to their current probability.
   */
  ObservedTable getObservedTable(Node node);

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. This will provide a deep copy of the table, and the probability values
   * will not change if the observations on the instance change.
   *
   * @param nodeId the identifier of the {@link Node} associated with the {@link ObservedTable}.
   * @param <T> the type of the node identifier.
   * @return a {@link ObservedTable} mapping a single node's states to their probability given the
   *     conditions when constructed.
   * @throws NullPointerException if the id was not associated with any {@link Node}.
   */
  <T extends Serializable> ObservedTable copyObservedTableById(T nodeId);

  /**
   * Returns an {@link ObservedTable} associated with a {@link Node}. This is a table containing
   * only the node's {@link NodeState} values, which are mapped to their current posterior
   * probability if this {@code InferenceEngine} has observations applied, or their prior (marginal)
   * probability otherwise. This will provide a deep copy of the table, and the probability values
   * will not change if the observations on the instance change.
   *
   * @param node the {@link Node} associated with the {@link ObservedTable}.
   * @return a {@link ObservedTable} mapping a single node's states to their probability given the
   *     conditions when constructed.
   */
  ObservedTable copyObservedTable(Node node);

  /**
   * Returns a map of all {@link Node} objects present in the network, and their associated observed
   * {@link ObservedTable}. These tables contain only the node's {@link NodeState} values, which are
   * mapped to their current posterior probability if this {@code InferenceEngine} has observations
   * applied, or their prior (marginal) probability otherwise. These tables are not deep-copied and
   * will update when changes to the current observations are made.
   *
   * @return a map associating each {@link Node} with its observed probability table.
   */
  Map<Node, ObservedTable> getObservedTables();

  /**
   * Returns the posterior probability of the given {@link NodeState} values, or their prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(S|O)} where S
   * is the intersection of all queried states and O is the current observations.
   *
   * @param measuredStates the states to be queried.
   * @return the posterior probability of all measured {@link NodeState} values.
   */
  double getPosteriorProbability(Collection<NodeState> measuredStates);

  /**
   * Returns the posterior probability of the given {@link NodeState} value, or its prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(s|O)} where s
   * is the queried state and O is the current observations.
   *
   * @param measuredState the state to be queried.
   * @return the posterior probability of the measured {@link NodeState} value.
   */
  double getPosteriorProbability(NodeState measuredState);

  /**
   * Returns the posterior probability of the given {@link NodeState} values, or their prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(S|O)} where S
   * is the intersection of all queried states and O is the current observations.
   *
   * @param measuredStateIds the identifiers of all {@link NodeState} values to be queried.
   * @param <T> the type of the measured {@link NodeState} identifiers.
   * @return the posterior probability of all measured {@link NodeState} values.
   * @throws NullPointerException if any id was not associated with a {@link NodeState}.
   */
  <T extends Serializable> double getPosteriorProbabilityById(Collection<T> measuredStateIds);

  /**
   * Returns the posterior probability of the given {@link NodeState} value, or its prior
   * probability if this {@code InferenceEngine} is unobserved. This measures {@code P(s|O)} where s
   * is the queried state and O is the current observations.
   *
   * @param measuredStateId the identifier of the {@link NodeState} to be queried.
   * @param <T> the type of the measured {@link NodeState} identifier.
   * @return the posterior probability of the measured {@link NodeState} value.
   * @throws NullPointerException if the id was not associated with any {@link NodeState}.
   */
  <T extends Serializable> double getPosteriorProbabilityById(T measuredStateId);

  /**
   * Prints the posterior probability values from all {@link ObservedTable} entries, either to a
   * {@code .txt} file or to the console. Parameters for the printer can be defined within {@code
   * app.properties} under the {@code app.bayes.printer} section.
   *
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   */
  InferenceEngine printObserved();

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.bayes.printer}
   * section.
   *
   * @param nodeIds the identifiers of all {@link Node} values where the associated {@link
   *     ObservedTable} should be printed.
   * @param <T> the type of the {@link Node} identifiers.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   * @throws NullPointerException if any identifiers were not associated with a {@link Node}.
   */
  <T extends Serializable> InferenceEngine printObservedById(Collection<T> nodeIds);

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.bayes.printer}
   * section.
   *
   * @param nodeId the identifier of the {@link Node} where the associated {@link ObservedTable}
   *     should be printed.
   * @param <T> the type of the {@link Node} identifier.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   * @throws NullPointerException if the identifier was not associated with any {@link Node}.
   */
  <T extends Serializable> InferenceEngine printObservedById(T nodeId);

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.bayes.printer}
   * section.
   *
   * @param nodes all {@link Node} values where the associated {@link ObservedTable} should be
   *     printed.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   */
  InferenceEngine printObserved(Collection<Node> nodes);

  /**
   * Prints the posterior probability values from the {@link ObservedTable} entries associated with
   * the given {@link Node}, either to a {@code .txt} file or to the console. Parameters for the
   * printer can be defined within {@code app.properties} under the {@code app.bayes.printer}
   * section.
   *
   * @param node a {@link Node} where the associated {@link ObservedTable} should be printed.
   * @return this instance for chaining.
   * @throws NetworkPrinterException if the printer is unable to successfully complete the
   *     operation.
   */
  InferenceEngine printObserved(Node node);

  /**
   * Returns the {@link BayesianNetwork} measured by this {@code InferenceEngine}.
   *
   * @return the {@link BayesianNetwork} associated with this {@code InferenceEngine}.
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
  InferenceEngine resetObservations();

  /**
   * Returns a map of each {@link Node} and its current {@link NodeObservation}. A {@link
   * NodeObservation} records the current observed and unobserved {@link NodeState}s in the node,
   * and its {@link ObservationStatus}.
   *
   * @return a map of the current observations on this instance.
   */
  InferenceEngine setObserved(Map<Node, NodeObservation> observations);

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
  InferenceEngine setObserved(Collection<NodeState> observedStates);

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
  InferenceEngine setObserved(NodeState observedState);

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
  <T extends Serializable> InferenceEngine setObservedById(Collection<T> observedStateIDs);

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
  <T extends Serializable> InferenceEngine setObservedById(T observedStateId);

  /**
   * Subtracts the input {@link NodeState} values from the observable pool. This will remove these
   * states from consideration when measuring probability values. The final probability values will
   * be conditional on these states not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a collection of {@link NodeState}s to eliminate.
   * @return this instance for chaining.
   */
  InferenceEngine eliminateStates(Collection<NodeState> toEliminate);

  /**
   * Subtracts the input {@link NodeState} value from the observable pool. This will remove this
   * state from consideration when measuring probability values. The final probability values will
   * be conditional on this state not being present, as well as the effects of any observations or
   * eliminations declared since the last reset.
   *
   * @param toEliminate a single {@link NodeState} to eliminate.
   * @return this instance for chaining.
   */
  InferenceEngine eliminateStates(NodeState toEliminate);

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
  <T extends Serializable> InferenceEngine eliminateStatesById(Collection<T> toEliminateIDs);

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
  <T extends Serializable> InferenceEngine eliminateStatesById(T toEliminateId);
}
