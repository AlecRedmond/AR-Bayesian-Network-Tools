package io.github.alecredmond.export.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.nodedef.NodeDef;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.method.nodedef.NodeDefNetworkBuilder;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
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
 * call structuring methods on individual {@link Node} objects, for example {@link
 * Node#setParents(List)}, care should be taken to ensure all associated nodes already exist in the
 * {@code BayesianNetwork}.
 *
 * <p>Instances of this interface are not thread-safe. External synchronisation is required for
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
@SuppressWarnings("unused")
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

  static <T extends NodeDef> BayesianNetwork createFromNodeDefs(
      Collection<T> nodeDefs, String networkName) {
    return new NodeDefNetworkBuilder().createNetwork(nodeDefs, networkName);
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
  // ----------------------------------NODE IN/OUT-------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Adds a {@link Node} to this {@code BayesianNetwork}. A {@link Node} is associated with a single
   * conditional probability table (CPT) within this {@code BayesianNetwork}.
   *
   * @param node the {@link Node} to be added to this {@code BayesianNetwork}
   * @throws BayesNetIDException if the {@link Node} identifier is not unique within this {@code
   *     BayesianNetwork}.
   * @return this instance for method chaining.
   */
  BayesianNetwork addNode(Node node);

  /**
   * Adds a new {@link Node} to this {@code BayesianNetwork}. A {@link Node} is associated with a
   * single conditional probability table (CPT) within this {@code BayesianNetwork}.
   *
   * @param nodeID the new {@link Node} identifier.
   * @param <T> the type of the new {@link Node} identifier.
   * @throws BayesNetIDException if the {@link Node} identifier is not unique within this {@code
   *     BayesianNetwork}.
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork addNewNode(T nodeID);

  /**
   * Adds a new {@link Node}, with the specified list of {@link NodeState}s, to this {@code
   * BayesianNetwork}. A {@link Node} is associated with a single conditional probability table
   * (CPT) within this {@code BayesianNetwork}. The {@link NodeState}s represent the mutually
   * exclusive states the {@link Node} can exhibit.
   *
   * <p>Every identifier in this {@code BayesianNetwork} must be unique. If using descriptive {@link
   * String} identifiers, it is recommended to pre-append the {@link Node} identifier to each {@link
   * NodeState} identifier, for example:<br>
   * {@code List<String> rainStateIds = List.of("RAIN:TRUE","RAIN:FALSE");}
   *
   * @param nodeID the new {@link Node} identifier.
   * @param nodeStateIDs the new {@link NodeState} identifiers.
   * @param <T> the type of the new {@link Node} identifier.
   * @param <E> the type of the new {@link NodeState} identifiers.
   * @throws BayesNetIDException if the {@link Node} identifier or any {@link NodeState} identifier
   *     is not unique within this {@code BayesianNetwork}.
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addNewNode(
      T nodeID, Collection<E> nodeStateIDs);

  /**
   * Removes a {@link Node} and all associated edges from this {@code BayesianNetwork}.
   *
   * @param node the {@link Node} to remove.
   * @return {@code true} if this {@code BayesianNetwork} contained the specified {@link Node}.
   */
  boolean removeNode(Node node);

  /**
   * Removes a {@link Node} and all associated edges from this {@code BayesianNetwork}.
   *
   * @param nodeID the identifier of the {@link Node} to remove.
   * @param <T> the type of the {@link Node} identifier.
   * @return {@code true} if this {@code BayesianNetwork} contained the specified {@link Node}.
   */
  <T extends Serializable> boolean removeNodeByID(T nodeID);

  /**
   * Removes all {@link Node}s from this {@code BayesianNetwork}.
   *
   * @return {@code true} if this {@code BayesianNetwork} contained any {@link Node}s.
   */
  boolean removeAllNodes();

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
  <T extends Serializable> Node getNode(T nodeID);

  /**
   * Returns a set of {@link Node}s from their identifiers in this {@code BayesianNetwork}.
   *
   * @param <T> class of the {@link Node} identifiers.
   * @param nodeIDs the {@link Node} identifiers.
   * @return the {@link Node} associated with the identifiers.
   */
  <T extends Serializable> Set<Node> getNodes(Collection<T> nodeIDs);

  /**
   * Returns a set of all {@link Node}s in this {@code BayesianNetwork}
   *
   * @return a new set containing every {@link Node} in this {@code BayesianNetwork}.
   */
  Set<Node> getNodes();

  /**
   * Returns a {@link NodeState} from its identifier in this {@code BayesianNetwork}.
   *
   * @param <E> class of the {@link NodeState} identifier.
   * @param nodeStateID {@link NodeState} identifier.
   * @return the {@link NodeState} associated with the identifier, or {@code null} if no associated
   *     {@link NodeState} was found in this {@code BayesianNetwork}.
   */
  <E extends Serializable> NodeState getNodeState(E nodeStateID);

  /**
   * Returns a set of {@link NodeState}s from their identifiers in this {@code BayesianNetwork}.
   *
   * @param <E> class of the {@link NodeState} identifiers.
   * @param nodeStateIDs the {@link NodeState} identifiers.
   * @return a new set of {@link NodeState}s associated with the identifiers.
   */
  <E extends Serializable> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs);

  // ----------------------------------------------------------------------------------------------
  // ---------------------------------NODE PARENT/CHILD RELATIONS----------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Defines parent-child relationships by adding directed edges from parent {@link Node}s to a
   * child {@link Node}.
   *
   * @param child the child {@link Node}.
   * @param parents a collection of parent {@link Node}s.
   * @throws NetworkStructureException if the {@link Node} would parent itself or cause a cycle in
   *     the graph
   * @return this instance for method chaining.
   */
  BayesianNetwork addParents(Node child, Collection<Node> parents);

  /**
   * Defines parent-child relationships by adding directed edges from parent {@link Node}s to a
   * child {@link Node}.
   *
   * @param childID the identifier of the child {@link Node}.
   * @param parentIDs a collection of identifiers for the parent {@link Node}s.
   * @param <T> the type of the Child {@link Node} identifier.
   * @param <E> the type of the Parent {@link Node} identifiers.
   * @throws NetworkStructureException if the {@link Node} would parent itself or cause a cycle in
   *     the graph
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addParents(
      T childID, Collection<E> parentIDs);

  /**
   * Defines a parent-child relationship by adding a directed edge from a parent {@link Node} to a
   * child {@link Node}.
   *
   * @param child the child {@link Node}.
   * @param parent the parent {@link Node}.
   * @throws NetworkStructureException if the {@link Node} would parent itself or cause a cycle in
   *     the graph
   * @return this instance for method chaining.
   */
  BayesianNetwork addParents(Node child, Node parent);

  /**
   * Defines a parent-child relationship by adding a directed edge from a parent {@link Node} to a
   * child {@link Node}.
   *
   * @param childID the identifier of the child {@link Node}.
   * @param parentID the identifier of the parent {@link Node}.
   * @param <T> the type of the Child {@link Node} identifier.
   * @param <E> the type of the Parent {@link Node} identifier.
   * @throws NetworkStructureException if the {@link Node} would parent itself or cause a cycle in
   *     the graph.
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addParents(
      T childID, E parentID);

  /**
   * Removes a directed edge between a parent and a child {@link Node}.
   *
   * @param child the child {@link Node}.
   * @param parent the {@link Node} to remove.
   * @return this instance for method chaining.
   */
  BayesianNetwork removeParent(Node child, Node parent);

  /**
   * Removes a directed edge between a parent and a child {@link Node}.
   *
   * @param childID the identifier of the child {@link Node}.
   * @param parentID the identifier of the parent {@link Node} to remove.
   * @param <T> the type of the Child {@link Node} identifier.
   * @param <E> the type of the Parent {@link Node} identifier.
   * @return this instance for method chaining.
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork removeParent(
      T childID, E parentID);

  /**
   * Removes all parent relationships for a given child {@link Node}.
   *
   * @param child the child {@link Node} whose parents will be removed.
   * @return this instance for method chaining.
   */
  BayesianNetwork removeParents(Node child);

  /**
   * Removes all parent relationships for a given child {@link Node}.
   *
   * @param childID the identifier of the child {@link Node} whose parents will be removed.
   * @param <T> the type of the Child {@link Node} identifier.
   * @return this instance for method chaining.
   */
  <T extends Serializable> BayesianNetwork removeParents(T childID);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT ADDERS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Adds a new {@link MarginalConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>{@link MarginalConstraint}s are unconditional constraints given in the form {@code P(e) =
   * p}, where:
   *
   * <ul>
   *   <li>{@code e} is the event {@link NodeState}.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventStateID the identifier of the event {@link NodeState} in the constraint.
   * @param probability the probability of the events given the conditions.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  <T extends Serializable> BayesianNetwork addConstraint(T eventStateID, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventStateID the identifier of the event {@link NodeState} in the constraint.
   * @param conditionStateId the identifier of the condition {@link NodeState} in the constraint.
   * @param probability the probability of the events given the conditions.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @param <E> the type of the condition {@link NodeState} identifier.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      T eventStateID, E conditionStateId, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventStateID the identifier of the event {@link NodeState} in the constraint.
   * @param conditionStateIDs the identifiers of the condition {@link NodeState}s in the constraint.
   * @param probability the probability of the events given the conditions.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @param <E> the type of the condition {@link NodeState} identifiers.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventStateIDs the identifiers of the event {@link NodeState}s in the constraint.
   * @param conditionStateIDs the identifiers of the condition {@link NodeState}s in the constraint.
   * @param probability the probability of the events given the conditions.
   * @param <T> the type of the event {@link NodeState} identifiers.
   * @param <E> the type of the condition {@link NodeState} identifiers.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  <T extends Serializable, E extends Serializable> BayesianNetwork addConstraint(
      Collection<T> eventStateIDs, Collection<E> conditionStateIDs, double probability);

  /**
   * Adds a {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param probabilityConstraint a {@link ProbabilityConstraint} object containing {@link
   *     NodeState}s from this {@code BayesianNetwork}.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  BayesianNetwork addConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Adds a collection of {@link ProbabilityConstraint}s to this {@code BayesianNetwork}. During the
   * solving process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param probabilityConstraints a collection of {@link ProbabilityConstraint}s to be added.
   * @return this instance for method chaining.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   */
  BayesianNetwork addConstraints(Collection<ProbabilityConstraint> probabilityConstraints);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Searches this {@code BayesianNetwork} for a {@link MarginalConstraint} with an event {@link
   * NodeState} matching the given identifier.
   *
   * @param eventStateId the identifier of a {@link NodeState}.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @return the associated {@link MarginalConstraint}, or {@code null} if it does not exist.
   * @throws NullPointerException if the parameter is {@code null}.
   */
  <T extends Serializable> MarginalConstraint getConstraint(T eventStateId);

  /**
   * Searches this {@code BayesianNetwork} for a {@link ProbabilityConstraint} with event and
   * condition {@link NodeState}s matching the given identifiers.
   *
   * @param eventStateId the identifier of the event {@link NodeState}.
   * @param conditionStateIds the identifiers of the condition {@link NodeState}s.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @param <E> the type of the condition {@link NodeState} identifiers.
   * @return the associated {@link ProbabilityConstraint}, or {@code null} if it does not exist.
   * @throws NullPointerException if any parameter is {@code null}.
   */
  <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      T eventStateId, Collection<E> conditionStateIds);

  /**
   * Searches this {@code BayesianNetwork} for a {@link ProbabilityConstraint} with event and
   * condition {@link NodeState}s matching the given identifiers.
   *
   * @param eventStateIds the identifiers of the event {@link NodeState}s.
   * @param conditionStateIds the identifiers of the condition {@link NodeState}s.
   * @param <T> the type of the event {@link NodeState} identifiers.
   * @param <E> the type of the condition {@link NodeState} identifiers.
   * @return the associated {@link ProbabilityConstraint}, or {@code null} if it does not exist.
   * @throws NullPointerException if any parameter is {@code null}.
   */
  <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      Collection<T> eventStateIds, Collection<E> conditionStateIds);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT REMOVERS-----------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Removes a {@link ProbabilityConstraint} from this {@code BayesianNetwork}.
   *
   * @param probabilityConstraint the {@link ProbabilityConstraint} to remove.
   * @return {@code true} if the {@link ProbabilityConstraint} existed in the network.
   */
  boolean removeConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Removes a {@link MarginalConstraint} matching the given event {@link NodeState}'s identifier
   * from this {@code BayesianNetwork}.
   *
   * @param eventStateId the identifier of the event {@link NodeState} in the {@link
   *     MarginalConstraint}.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @return {@code true} if a matching {@link MarginalConstraint} existed in the network
   */
  <T extends Serializable> boolean removeConstraint(T eventStateId);

  /**
   * Removes a {@link ProbabilityConstraint} matching the given inputs from this {@code
   * BayesianNetwork}.
   *
   * @param eventStateId the identifier of the event {@link NodeState} in the {@link
   *     ProbabilityConstraint}.
   * @param conditionStateIds the collected ids of all condition {@link NodeState}s in the {@link
   *     ProbabilityConstraint}.
   * @param <T> the type of the event {@link NodeState} identifier.
   * @param <E> the type of the condition {@link NodeState} identifiers.
   * @return {@code true} if a matching {@link ProbabilityConstraint} existed in the network
   */
  <T extends Serializable, E extends Serializable> boolean removeConstraint(
      T eventStateId, Collection<E> conditionStateIds);

  /**
   * Removes a {@link ProbabilityConstraint} matching the given inputs from this {@code
   * BayesianNetwork}.
   *
   * @param eventStateIds the identifiers of the event {@link NodeState}s in the {@link
   *     ProbabilityConstraint}.
   * @param conditionStateIds the collected ids of all condition {@link NodeState}s in the {@link
   *     ProbabilityConstraint}.
   * @param <T> the type of the event {@link NodeState} identifiers.
   * @param <E> the type of the condition {@link NodeState} identifiers.
   * @return {@code true} if a matching {@link ProbabilityConstraint} existed in the network
   */
  <T extends Serializable, E extends Serializable> boolean removeConstraint(
      Collection<T> eventStateIds, Collection<E> conditionStateIds);

  /**
   * Removes all {@link ProbabilityConstraint}s from this {@code BayesianNetwork}.
   *
   * @return {@code true} if any {@link ProbabilityConstraint}s existed in the network.
   */
  boolean removeAllConstraints();

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
   * Builds the underlying data object containing all network information. Only runs if the network
   * is not solved.
   *
   * @return this instance for method chaining.
   */
  BayesianNetwork buildNetworkData();

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
  <T extends Serializable> NetworkTable getNetworkTable(T nodeID);

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

  /** Removes all data from this {@code BayesianNetwork}, excluding the network name. */
  void resetAllData();
}
