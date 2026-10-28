package io.github.alecredmond.export.network;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.network.BayesianNetworkBuilderImpl;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
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
// TODO - REFORMAT JAVADOC
@SuppressWarnings({"UnusedReturnValue"})
public interface BayesianNetwork {

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  static BayesianNetworkBuilder builder() {
    return new BayesianNetworkBuilderImpl();
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

  SerializedBayesianNetwork serializeNetwork();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------DATA GETTERS -----------------------------------------------
  // ----------------------------------------------------------------------------------------------

  <T extends Serializable> int getNodeOrdinalById(T nodeId);

  int getNodeOrdinalByName(String nodeName);

  int getNodeOrdinal(Node node);

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
   * Returns a topographically ordered array of all {@link Node}s in this {@code BayesianNetwork}
   *
   * @return a new array of every {@link Node} in this {@code BayesianNetwork}.
   */
  Node[] getNodes();

  Stream<Node> streamNodes();

  <T extends Collection<Node>, U extends T> U getNodes(Supplier<U> collectionSupplier);

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

  Set<ProbabilityConstraint> getConstraints();

  String getNetworkName();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Prints the CPTs of this {@code BayesianNetwork}, either to a {@code .txt} file or to the
   * console. Parameters for the printer can be defined within {@code app.properties} under the
   * {@code app.bayes.printer} section.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork printNetwork();

  /**
   * Retrieves the Conditional Probability Table (CPT) for a given node.
   *
   * @param nodeID the identifier of the node.
   * @param <T> class of the Node ID
   * @return the probability table for the specified node.
   */
  <T extends Serializable> NetworkTable getNetworkTableById(T nodeID);

  NetworkTable getNetworkTable(Node node);

  NetworkTable[] getAllNetworkTables();

  /**
   * Returns a new map connecting each {@link Node} in this {@code BayesianNetwork} to its
   * associated Conditional Probability Table (CPT).
   *
   * @return a new map containing each {@link Node} and its associated {@link NetworkTable} CPT.
   */
  Map<Node, NetworkTable> getNetworkTablesMap();

  /**
   * Builds a new {@link InferenceEngine} from the current {@code BayesianNetwork}. An {@link
   * InferenceEngine} provides utilities for running direct inference on a network, including
   * setting observations, querying prior and posterior probabilities, and printing posterior
   * probability tables.
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
