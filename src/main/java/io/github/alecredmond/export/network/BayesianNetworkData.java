package io.github.alecredmond.export.network;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.solver.BayesSolver;
import java.io.Serializable;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Container for data used in a {@link BayesianNetwork}.
 *
 * <p>This includes the {@link NetworkTable}s, {@link ProbabilityConstraint}s, and maps linking all
 * declared identifiers to their {@link Node} and {@link NodeState} objects.
 *
 * <p><strong>WARNING:</strong> Modifying this data directly may cause undefined behaviour! The
 * methods provided in {@link BayesianNetwork} should be used to add nodes, modify the network's
 * structure, add constraints, and perform other actions that would modify this data.
 *
 * @see BayesianNetwork
 * @author Alec Redmond
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@Data
public class BayesianNetworkData {
  /**
   * The list of {@link Node}s present within the {@link BayesianNetwork}. When this data is built
   * using {@link BayesianNetwork#buildNetworkData()}, the nodes will be in topological order, with
   * root nodes at the start and leaf nodes at the end.
   */
  private final List<Node> nodes;

  /**
   * A map from each {@link Node} identifier to its corresponding {@link Node} instance within the
   * {@link BayesianNetwork}.
   */
  private final Map<Serializable, Node> nodeIDsMap;

  /**
   * A map from each {@link NodeState} identifier to its corresponding {@link NodeState} instance
   * within the {@link BayesianNetwork}.
   */
  private final Map<Serializable, NodeState> nodeStateIDsMap;

  /**
   * A map from each {@link Node} to its corresponding {@link NetworkTable} (CPT) instance within
   * the {@link BayesianNetwork}. These are built when calling {@link
   * BayesianNetwork#buildNetworkData()}.
   */
  private final Map<Node, NetworkTable> networkTablesMap;

  /**
   * A {@link LinkedHashSet} of all {@link ProbabilityConstraint}s on the {@link BayesianNetwork}.
   */
  private final Set<ProbabilityConstraint> constraints;

  /** The name of the {@link BayesianNetwork}. */
  private String networkName;

  /**
   * {@code true} if a successful {@link BayesSolver} run has been carried out on the {@link
   * BayesianNetwork}, otherwise {@code false}.
   */
  private boolean solved;

  @EqualsAndHashCode.Exclude @ToString.Exclude private BayesianNetwork bayesianNetwork;

  /**
   * Constructs a new, empty {@code BayesianNetworkData} instance.
   *
   * <p>Initializes all underlying collections and maps to their empty states. The network name is
   * initialized to "UNNAMED NETWORK" and the solved state is set to {@code false}.
   */
  public BayesianNetworkData() {
    this.constraints = new LinkedHashSet<>();
    this.networkTablesMap = new LinkedHashMap<>();
    this.nodeStateIDsMap = new HashMap<>();
    this.nodeIDsMap = new HashMap<>();
    this.nodes = new ArrayList<>();
    this.networkName = "UNNAMED NETWORK";
    this.solved = false;
    this.bayesianNetwork = null;
  }

  /**
   * Returns the network's conditional probability table (CPT) associated with the given {@link
   * Node}'s identifier.
   *
   * <p>This will be a {@link RootNodeTable} if referencing a root node, or a {@link
   * ConditionalTable} otherwise.
   *
   * @param <T> the type of the {@link Node} identifier.
   * @param nodeID an identifier associated with a {@link Node}.
   * @return the network CPT associated with the {@link Node}.
   */
  public <T extends Serializable> NetworkTable getNetworkTableById(T nodeID) {
    return networkTablesMap.get(nodeIDsMap.get(nodeID));
  }

  /**
   * Retrieves the list of nodes present within the network. When this data is built using {@link
   * BayesianNetwork#buildNetworkData()}, the nodes will be in topological order, with root nodes at
   * the start and leaf nodes at the end.
   *
   * @return a {@link List} of {@link Node} objects.
   */
  public List<Node> getNodes() {
    return this.nodes;
  }

  /**
   * Retrieves the mapping of node identifiers to their respective {@link Node} instances.
   *
   * @return a {@link Map} linking {@link Serializable} identifiers to {@link Node}s.
   */
  public Map<Serializable, Node> getNodeIDsMap() {
    return this.nodeIDsMap;
  }

  /**
   * Retrieves the mapping of node state identifiers to their respective {@link NodeState}
   * instances.
   *
   * @return a {@link Map} linking {@link Serializable} state identifiers to {@link NodeState}s.
   */
  public Map<Serializable, NodeState> getNodeStateIDsMap() {
    return this.nodeStateIDsMap;
  }

  /**
   * Retrieves the mapping of nodes to their Conditional Probability Tables (CPTs). Each value will
   * be a {@link RootNodeTable} if referencing a root node, or a {@link ConditionalTable} otherwise.
   *
   * @return a {@link Map} linking {@link Node}s to their respective {@link NetworkTable}s.
   */
  public Map<Node, NetworkTable> getNetworkTablesMap() {
    return this.networkTablesMap;
  }

  /**
   * Retrieves the set of constraints applied to this network.
   *
   * @return a {@link Set} of {@link ProbabilityConstraint}s.
   */
  public Set<ProbabilityConstraint> getConstraints() {
    return this.constraints;
  }

  /**
   * Retrieves the assigned name of the network.
   *
   * @return the network's name as a {@link String}.
   */
  public String getNetworkName() {
    return this.networkName;
  }

  /**
   * Checks whether the network has been successfully solved.
   *
   * @return {@code true} if a {@link BayesSolver} has successfully run on this network, {@code
   *     false} otherwise.
   */
  public boolean isSolved() {
    return this.solved;
  }
}
