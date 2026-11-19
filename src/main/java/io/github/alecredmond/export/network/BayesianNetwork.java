package io.github.alecredmond.export.network;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.direct.JunctionTreeAlgorithm;
import io.github.alecredmond.export.inference.indirect.LikelihoodWeightingSampler;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialization.NodeSerializer;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.network.BayesianNetworkBuilderImpl;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a Bayesian network, and provides the tools to query its structure, save it to disk,
 * and run inference on it.
 *
 * <p>A Bayesian network is a directed acyclic graph that represents the joint probability
 * distribution of a group of {@link Node} variables, each of which can exhibit one of a set of
 * mutually exclusive {@link NodeState}s. Each node is associated with a conditional probability
 * table (CPT) that holds the discrete probability distribution {@code P(X|Pa(X))}, where {@code X}
 * is a single node in the graph and {@code Pa(X)} are the parent nodes of {@code X}.
 *
 * <p>A network is created with {@link #builder()} or loaded from disk with {@link
 * #loadNetworkFromFile(File)}. Inference is then available through {@link #buildInferenceEngine()}
 * and {@link #buildSampler()}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @version 1.0.0 RELEASE
 * @see BayesianNetworkBuilder
 * @see BayesSolver
 * @see InferenceEngine
 * @see MonteCarloSampler
 * @see NetworkTable
 */
@SuppressWarnings({"UnusedReturnValue"})
public interface BayesianNetwork {

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Creates a new builder for defining and constructing a {@code BayesianNetwork}.
   *
   * @return a new {@link BayesianNetworkBuilder}
   */
  static BayesianNetworkBuilder builder() {
    return new BayesianNetworkBuilderImpl();
  }

  /**
   * Loads a saved {@code BayesianNetwork} from a {@code .bayes} file on the disk.
   *
   * <p>If the saved network used {@link Node} types declared outside this library, ensure that the
   * relevant {@link NodeSerializer}s are registered with {@link
   * NodeSerializer#register(NodeSerializer[])} before loading.
   *
   * @param file a {@code .bayes} file
   * @return a new {@code BayesianNetwork} loaded from the file
   */
  static BayesianNetwork loadNetworkFromFile(File file) {
    return loadNetwork(new NetworkFileIO().loadNetwork(file));
  }

  /**
   * Deserializes a new {@code BayesianNetwork} from a {@link SerializedBayesianNetwork}.
   *
   * <p>A {@code SerializedBayesianNetwork} is created by calling {@link #serializeNetwork()} on an
   * active network. It is a deep copy of that network in a format suitable for file I/O.
   *
   * <p>If the serialized network used {@link Node} types declared outside this library, ensure that
   * the relevant {@link NodeSerializer}s are registered with {@link
   * NodeSerializer#register(NodeSerializer[])} before loading.
   *
   * @param serializedNetwork the serialized network to load
   * @return a new {@code BayesianNetwork} that is a copy of the serialized network
   */
  static BayesianNetwork loadNetwork(SerializedBayesianNetwork serializedNetwork) {
    return new BayesianNetworkSerializer().deSerializeNetwork(serializedNetwork);
  }

  /**
   * Loads a saved {@code BayesianNetwork} from a {@code .bayes} file on the disk.
   *
   * <p>If the saved network used {@link Node} types declared outside this library, ensure that the
   * relevant {@link NodeSerializer}s are registered with {@link
   * NodeSerializer#register(NodeSerializer[])} before loading.
   *
   * @param filePath the absolute path to a {@code .bayes} file
   * @return a new {@code BayesianNetwork} loaded from the file
   */
  static BayesianNetwork loadNetworkFromFile(String filePath) {
    return loadNetwork(new NetworkFileIO().loadNetwork(filePath));
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FILE IO---------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Serializes this {@code BayesianNetwork} and saves it to a file on the disk.
   *
   * <p>If this network uses {@link Node} types declared outside this library, ensure that the
   * relevant {@link NodeSerializer}s are registered with {@link
   * NodeSerializer#register(NodeSerializer[])} before saving.
   *
   * @param file the file to write
   * @return {@code true} if the save operation was successful; {@code false} otherwise
   */
  boolean saveNetworkToFile(File file);

  /**
   * Serializes this {@code BayesianNetwork} and saves it to a file on the disk.
   *
   * <p>If this network uses {@link Node} types declared outside this library, ensure that the
   * relevant {@link NodeSerializer}s are registered with {@link
   * NodeSerializer#register(NodeSerializer[])} before saving.
   *
   * @param filePath the path of the file to write
   * @return {@code true} if the save operation was successful; {@code false} otherwise
   */
  boolean saveNetworkToFile(String filePath);

  /**
   * Creates a serializable deep copy of this {@code BayesianNetwork}.
   *
   * <p>The result can be passed to {@link #loadNetwork(SerializedBayesianNetwork)} to create a new
   * network.
   *
   * @return a new {@link SerializedBayesianNetwork} that is a snapshot of this network
   */
  SerializedBayesianNetwork serializeNetwork();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------DATA GETTERS -----------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns the ordinal of a {@link Node} from its identifier in this {@code BayesianNetwork}. The
   * ordinal is the position of the node in the array returned by {@link #getNodes()}.
   *
   * @param <T> the class of the node identifier
   * @param nodeId the identifier of the node
   * @return the ordinal of the node, or {@code null} if this network contains no node with the
   *     identifier
   */
  <T extends Serializable> Integer getNodeOrdinalById(T nodeId);

  /**
   * Returns the ordinal of a {@link Node} from its name in this {@code BayesianNetwork}. The
   * ordinal is the position of the node in the array returned by {@link #getNodes()}.
   *
   * @param nodeName the name of the node
   * @return the ordinal of the node, or {@code null} if this network contains no node with the name
   */
  Integer getNodeOrdinalByName(String nodeName);

  /**
   * Returns the ordinal of a {@link Node} in this {@code BayesianNetwork}. The ordinal is the
   * position of the node in the array returned by {@link #getNodes()}.
   *
   * @param node the node
   * @return the ordinal of the node, or {@code null} if this network does not contain the node
   */
  Integer getNodeOrdinal(Node node);

  /**
   * Returns a {@link Node} from its identifier in this {@code BayesianNetwork}.
   *
   * @param <T> the class of the node identifier
   * @param nodeID the identifier of the node
   * @return the node associated with the identifier, or {@code null} if this network contains no
   *     such node
   */
  <T extends Serializable> Node getNodeById(T nodeID);

  /**
   * Returns a set of {@link Node}s from their identifiers in this {@code BayesianNetwork}.
   *
   * @param <T> the class of the node identifiers
   * @param nodeIDs the identifiers of the nodes
   * @return a set of the nodes associated with the identifiers
   */
  <T extends Serializable> Set<Node> getNodesByIds(Collection<T> nodeIDs);

  /**
   * Returns a {@link Node} from its name in this {@code BayesianNetwork}.
   *
   * @param name the name of the node
   * @return the node with the specified name, or {@code null} if this network contains no such node
   */
  Node getNodeByName(String name);

  /**
   * Returns a set of {@link Node}s from their names in this {@code BayesianNetwork}.
   *
   * @param nodeNames the names of the nodes
   * @return a set of the nodes with the specified names
   */
  Set<Node> getNodesByName(Collection<String> nodeNames);

  /**
   * Returns a topologically ordered array of all {@link Node}s in this {@code BayesianNetwork}.
   *
   * @return a new array of every node in this network
   */
  Node[] getNodes();

  /**
   * Returns a stream of all {@link Node}s in this {@code BayesianNetwork}.
   *
   * @return a stream of every node in this network
   */
  Stream<Node> streamNodes();

  /**
   * Returns all {@link Node}s in this {@code BayesianNetwork}, collected in a collection supplied
   * by the caller.
   *
   * @param <T> the collection type of the result
   * @param collectionSupplier a supplier of the empty collection that receives the nodes
   * @return the collection created by the supplier, containing every node in this network
   */
  <T extends Collection<Node>> T getNodes(Supplier<T> collectionSupplier);

  /**
   * Returns a {@link NodeState} from its identifier in this {@code BayesianNetwork}.
   *
   * @param <E> the class of the node state identifier
   * @param nodeStateID the identifier of the node state
   * @return the node state associated with the identifier, or {@code null} if this network contains
   *     no such node state
   */
  <E extends Serializable> NodeState getNodeStateById(E nodeStateID);

  /**
   * Returns a set of {@link NodeState}s from their identifiers in this {@code BayesianNetwork}.
   *
   * @param <E> the class of the node state identifiers
   * @param nodeStateIDs the identifiers of the node states
   * @return a new set of the node states associated with the identifiers
   */
  <E extends Serializable> Set<NodeState> getNodeStatesById(Collection<E> nodeStateIDs);

  /**
   * Returns the {@link ProbabilityConstraint}s assigned to this {@code BayesianNetwork}.
   *
   * @return the set of constraints of this network
   */
  Set<ProbabilityConstraint> getConstraints();

  /**
   * Returns the name of this {@code BayesianNetwork}.
   *
   * @return the name of this network
   */
  String getNetworkName();

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Prints the CPTs of this {@code BayesianNetwork}, either to a {@code .txt} file or to the
   * console. Parameters for the printer can be defined within {@code app.properties} under the
   * {@code app.bayes.printer} section.
   *
   * @return this instance for method chaining
   */
  BayesianNetwork printNetwork();

  /**
   * Returns the conditional probability table (CPT) of a {@link Node}, from its identifier.
   *
   * @param <T> the class of the node identifier
   * @param nodeID the identifier of the node
   * @return the probability table of the specified node
   */
  <T extends Serializable> NetworkTable getNetworkTableByNodeId(T nodeID);

  /**
   * Returns the conditional probability table (CPT) of a {@link Node}, from its name.
   *
   * @param nodeName the name of the node
   * @return the probability table of the specified node
   */
  NetworkTable getNetworkTableByNodeName(String nodeName);

  /**
   * Returns the conditional probability table (CPT) of a {@link Node}.
   *
   * @param node the node
   * @return the probability table of the specified node
   */
  NetworkTable getNetworkTable(Node node);

  /**
   * Returns the conditional probability tables (CPTs) of all {@link Node}s in this {@code
   * BayesianNetwork}.
   *
   * @return a new array of the probability table of every node in this network
   */
  NetworkTable[] getNetworkTables();

  /**
   * Returns the conditional probability tables (CPTs) of all {@link Node}s in this {@code
   * BayesianNetwork}, collected in a collection supplied by the caller.
   *
   * @param <T> the collection type created by the supplier
   * @param collectionSupplier a supplier of the empty collection that receives the tables
   * @return the collection created by the supplier, containing the probability table of every node
   *     in this network
   */
  <T extends Collection<NetworkTable>> T getNetworkTables(Supplier<T> collectionSupplier);

  /**
   * Creates a new {@link InferenceEngine} from this {@code BayesianNetwork}. An {@code
   * InferenceEngine} provides utilities for running direct inference on a network, including
   * setting observations, querying prior and posterior probabilities, and printing posterior
   * probability tables.
   *
   * <p>This method creates a default {@link JunctionTreeAlgorithm}, a form of direct inference
   * where computational complexity scales with the treewidth of the network, rather than the size
   * of the network.
   *
   * @return a new {@code InferenceEngine} referencing this network
   */
  default InferenceEngine buildInferenceEngine() {
    return new JunctionTreeAlgorithm(this);
  }

  /**
   * Creates a new {@link MonteCarloSampler} from this {@code BayesianNetwork}. A {@code
   * MonteCarloSampler} provides utilities for running indirect inference on a network.
   *
   * <p>This method creates a default {@link LikelihoodWeightingSampler}, which performs a random
   * walk down the network's CPTs to generate weighted samples.
   *
   * @return a new {@code MonteCarloSampler} referencing this network
   */
  default MonteCarloSampler buildSampler() {
    return new LikelihoodWeightingSampler(this);
  }
}
