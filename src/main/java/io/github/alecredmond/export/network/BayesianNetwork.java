package io.github.alecredmond.export.network;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.JFileChooser;

/**
 * Provides a toolset to structure and query a Bayesian Network.
 *
 * <p>A Bayesian Network is a directed acyclic graph that represents the probability of a group of
 * {@link Node} variables, which can each exhibit a set of mutually exclusive {@link NodeState}s.
 * Each node is associated with a conditional probability table (CPT) containing the discrete
 * probability distribution {@code P(X|Pa(X))}, where {@code X} is a single node in the graph,
 * {@code Pa(X)} are its parent nodes.
 *
 * <p>In a {@code BayesianNetwork} instance, the CPTs are not directly input; instead an individual
 * CPT entry is entered as a {@link ProbabilityConstraint}. A {@link BayesSolver} uses an Iterative
 * Proportional Fitting Algorithm (IPFP) to provide a CPT mapping that best fits these constraints.
 * This allows finding the best fit CPTs from incomplete domain data, or from known conditional or
 * marginal probabilities which do not follow the graph structure ordering {@code P(X|Pa(X))}.
 * <i>Note: Constraints that span conditionally independent nodes may lose their exact definitions
 * when mapped back to the network's CPTs after solving.</i>
 *
 * <p>If all CPTs are known in advance, the construction of the constraints may be expedited by
 * building a {@code BayesianNetwork} instance using a {@link BayesianNetworkBuilder}.
 *
 * <p>The hard limit on each CPT in the network is 2<sup>31</sup>&minus;1 entries. In practice this
 * limit will be smaller as the solver combines multiple tables during the IPFP process. The solver
 * may be run manually by calling {@link #solveNetwork()}, or will automatically be run when calling
 * a method that requires the network in a solved state, such as {@link #buildInferenceEngine()}.
 *
 * <p>A {@code BayesianNetwork} requires all relevant {@link Node} values to be present within the
 * network before graph structure/parent-child relations, and all relevant {@link NodeState} values
 * to be present before adding {@link ProbabilityConstraint}s. It is advisable to use only the
 * {@code BayesianNetwork} interface to manipulate the graph structure, but if it is necessary to
 * call structuring methods on individual {@link Node} objects, care should be taken to ensure all
 * associated nodes already exist in the {@code BayesianNetwork}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @see BayesianNetworkBuilder
 * @see BayesSolver
 * @see InferenceEngine
 * @see MonteCarloSampler
 * @see NetworkTable
 * @author Alec Redmond
 * @version 1.0.0 RELEASE
 */
@SuppressWarnings({"UnusedReturnValue"})
public interface BayesianNetwork {

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Creates a new {@code BayesianNetwork} with the name "UNNAMED NETWORK".
   *
   * @return a new {@code BayesianNetwork}.
   */
  static BayesianNetwork newNetwork() {
    return new BayesianNetworkImpl();
  }

  /**
   * Creates a new {@code BayesianNetwork} with the specified name.
   *
   * @param networkName the name for the new network.
   * @return a new {@code BayesianNetwork}.
   */
  static BayesianNetwork newNetwork(String networkName) {
    return new BayesianNetworkImpl(networkName);
  }

  /**
   * Loads a saved {@code BayesianNetwork} from a {@code .bayes} file on the disk. This will open a
   * {@link JFileChooser} window.
   *
   * @return a new {@code BayesianNetwork}, loaded from the file.
   */
  static BayesianNetwork loadNetworkFromFile() {
    return new NetworkFileIO(new BayesianNetworkSerializer()).loadNetwork();
  }

  /**
   * Loads a saved {@code BayesianNetwork} from a {@code .bayes} file on the disk.
   *
   * @param file a {@code .bayes} file.
   * @return a new {@code BayesianNetwork}, loaded from the file.
   */
  static BayesianNetwork loadNetworkFromFile(File file) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).loadNetwork(file);
  }

  /**
   * Loads a saved {@code BayesianNetwork} from a {@code .bayes} file on the disk.
   *
   * @param filePath the absolute path to a {@code .bayes} file.
   * @return a new {@code BayesianNetwork}, loaded from the file.
   */
  static BayesianNetwork loadNetworkFromFile(String filePath) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).loadNetwork(filePath);
  }

  /**
   * De-serializes and loads a new {@code BayesianNetwork} from a {@link SerializedBayesianNetwork}.
   * {@link SerializedBayesianNetwork}s are constructed by calling {@link #serializeNetwork()} on an
   * active {@link BayesianNetwork}, which provides a deep-copy of the network in a format suitable
   * for file I/O operations.
   *
   * @param serializedNetwork a {@link SerializedBayesianNetwork} instance.
   * @return a new copy of the serialized {@code BayesianNetwork}.
   */
  static BayesianNetwork loadNetwork(SerializedBayesianNetwork serializedNetwork) {
    return new BayesianNetworkSerializer().deSerialize(serializedNetwork);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FILE IO---------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Serializes and saves this {@code BayesianNetwork} to the disk.
   *
   * @param file the file to write.
   * @return {@code true} if the save operation was successful.
   */
  boolean saveNetworkToFile(File file);

  /**
   * Serializes and saves this {@code BayesianNetwork} to the disk.
   *
   * @param filePath path to the new file.
   * @return {@code true} if the save operation was successful.
   */
  boolean saveNetworkToFile(String filePath);

  /**
   * Serializes and saves this {@code BayesianNetwork} to the disk. This will open a {@link
   * JFileChooser} window.
   *
   * @return {@code true} if the save operation was successful.
   */
  boolean saveNetworkToFile();

  /**
   * Returns a new {@link Serializable} version of this {@code BayesianNetwork}. A {@link
   * SerializedBayesianNetwork} is, in essence, a deep copy of the network's {@link
   * BayesianNetworkData} object, with every {@link Node} and {@link NodeState} replaced with their
   * identifiers. This saves the current state of the {@code BayesianNetwork}, and is used in file
   * I/O operations such as {@link #saveNetworkToFile()} and {@link #loadNetworkFromFile()}.
   *
   * @return a new {@link SerializedBayesianNetwork} mapping of this {@code BayesianNetwork}.
   */
  SerializedBayesianNetwork serializeNetwork();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE/STATE GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns a {@link Node} from its identifier in this {@code BayesianNetwork}.
   *
   * @param <T> class of the {@link Node} identifier.
   * @param nodeID the {@link Node} identifier.
   * @return the {@link Node} associated with the identifier, or {@code null} if no associated
   *     {@link Node} was found in this {@code BayesianNetwork}.
   */
  <T extends Serializable> Node getNodeById(T nodeID);

  /**
   * Returns a set of {@link Node}s from their identifiers in this {@code BayesianNetwork}.
   *
   * @param <T> class of the {@link Node} identifiers.
   * @param nodeIDs the {@link Node} identifiers.
   * @return the {@link Node} associated with the identifiers.
   */
  <T extends Serializable> Set<Node> getNodesByIds(Collection<T> nodeIDs);

  Node getNodeByName(String name);

  Set<Node> getNodesByName(Collection<String> nodeNames);

  /**
   * Returns a set of all {@link Node}s in this {@code BayesianNetwork}
   *
   * @return a new set containing every {@link Node} in this {@code BayesianNetwork}.
   */
  Set<Node> getAllNodes();

  /**
   * Returns a {@link NodeState} from its identifier in this {@code BayesianNetwork}.
   *
   * @param <E> class of the {@link NodeState} identifier.
   * @param nodeStateID {@link NodeState} identifier.
   * @return the {@link NodeState} associated with the identifier, or {@code null} if no associated
   *     {@link NodeState} was found in this {@code BayesianNetwork}.
   */
  <E extends Serializable> NodeState getNodeStateById(E nodeStateID);

  /**
   * Returns a set of {@link NodeState}s from their identifiers in this {@code BayesianNetwork}.
   *
   * @param <E> class of the {@link NodeState} identifiers.
   * @param nodeStateIDs the {@link NodeState} identifiers.
   * @return a new set of {@link NodeState}s associated with the identifiers.
   */
  <E extends Serializable> Set<NodeState> getNodeStatesById(Collection<E> nodeStateIDs);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Runs a {@link BayesSolver} instance to find the best fit for the constraints provided. Other
   * methods that involve sampling, observing, and printing from the network implicitly run this
   * method. If analysis of the solver convergence is required, consider creating a new {@link
   * BayesSolver} instance using {@code BayesSolver.create(network)}.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork solveNetwork();

  /**
   * Checks if this {@code BayesianNetwork} is in a solved configuration.
   *
   * @return {@code true} if this {@code BayesianNetwork} has been solved.
   */
  boolean isSolved();

  /**
   * Prints the CPTs of this {@code BayesianNetwork}, either to a {@code .txt} file or to the
   * console. Parameters for the printer can be defined within {@code app.properties} under the
   * {@code app.bayes.printer} section.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork printNetwork();

  /**
   * Retrieves the data object containing all network information pertinent to this {@code
   * BayesianNetwork}. It is advisable to not modify this data in any way.
   *
   * @return the data object for this {@code BayesianNetwork}.
   */
  BayesianNetworkData getNetworkData();

  /**
   * Retrieves the Conditional Probability Table (CPT) for a given node.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the probability table for the specified node.
   */
  <T extends Serializable> NetworkTable getNetworkTableById(T nodeID);

  /**
   * Returns a new map connecting each {@link Node} in this {@code BayesianNetwork} to its
   * associated Conditional Probability Table (CPT).
   *
   * @return a new map containing each {@link Node} and its associated {@link NetworkTable} CPT.
   */
  Map<Node, NetworkTable> getNetworkTables();

  /**
   * Builds a new {@link InferenceEngine} from the current {@code BayesianNetwork}. An {@link
   * InferenceEngine} provides utilities for running direct inference on a network, including
   * setting observations, querying prior and posterior probabilities, and printing posterior
   * probability tables. This method will construct the default variant set in {@code
   * app.properties}, see the {@link InferenceAlgorithm} documentation for further information.
   *
   * @return a new {@link InferenceEngine} referencing this {@code BayesianNetwork}.
   */
  InferenceEngine buildInferenceEngine();

  /**
   * Creates a new Monte Carlo {@link MonteCarloSampler} from this {@code BayesianNetwork}. A {@link
   * MonteCarloSampler} provides utilities for running indirect inference on a {@code
   * BayesianNetwork}.
   *
   * @return a new {@link MonteCarloSampler} referencing this {@code BayesianNetwork}.
   */
  MonteCarloSampler buildSampler();
}
