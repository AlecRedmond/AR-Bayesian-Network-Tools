package io.github.alecredmond.export.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.network.NetworkInputBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Streamlines the construction of a {@link BayesianNetwork}.
 *
 * <p>This builder allows network configuration to be defined on a per-{@link Node} basis. This
 * approach bypasses the strict input ordering required when manually constructing a {@link
 * BayesianNetwork} via the standard interface. A node configuration relies on:
 *
 * <ul>
 *   <li>The {@link Serializable} identifier for the {@link Node}.
 *   <li>The {@link Serializable} identifiers for the node's {@link NodeState}s.
 *   <li>The identifiers of the node's parent nodes (for conditional nodes).
 *   <li>The Conditional Probability Table (CPT) values, structured as a {@code double[]} array
 *       (optional).
 * </ul>
 *
 * @see BayesianNetwork
 * @author Alec Redmond
 */
@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed", "unused"})
public class BayesianNetworkBuilder {
  /**
   * A list of {@link BayesianNetworkBuilderNode} objects, each containing the information for a
   * {@link Node} to be built in the new {@link BayesianNetwork}.
   */
  private final List<BayesianNetworkBuilderNode<?>> nodeInputs = new ArrayList<>();

  /** The name of the new {@link BayesianNetwork} to be built. */
  private String networkName;

  /**
   * Constructs an empty {@code BayesianNetworkBuilder} with the default network name {@code
   * "UNNAMED NETWORK"}.
   */
  public BayesianNetworkBuilder() {
    this.networkName = "UNNAMED NETWORK";
  }

  /**
   * Constructs an empty {@code BayesianNetworkBuilder} with the specified network name.
   *
   * @param networkName the name to assign to the resulting {@link BayesianNetwork}.
   */
  public BayesianNetworkBuilder(String networkName) {
    this.networkName = networkName;
  }

  /**
   * Constructs and returns the {@link BayesianNetwork} using the current node inputs.
   *
   * @return a fully constructed {@link BayesianNetwork}.
   * @throws BayesNetIDException if any identifier is incorrectly mapped or duplicated.
   * @throws NetworkStructureException if the network contains disconnected sections, or if the
   *     parent/child relationships form a cyclic graph.
   * @throws ConstraintValidationException if a provided CPT array is of incorrect length, or if the
   *     CPT probabilities represent an illegal configuration.
   */
  public BayesianNetwork build() {
    return new NetworkInputBuilder().buildNetwork(networkName, nodeInputs);
  }

  /**
   * Adds a root (unparented) {@link Node} configuration to this builder.
   *
   * @param nodeId the identifier for the root {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with the {@link
   *     Node}.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @return this builder instance for method chaining.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty.
   */
  public <T extends Serializable> BayesianNetworkBuilder addNode(T nodeId, List<T> stateIds) {
    nodeInputs.add(new BayesianNetworkBuilderNode<>(nodeId, stateIds));
    return this;
  }

  /**
   * Adds a root (unparented) {@link Node} configuration, including its CPT values, to this builder.
   *
   * <p>For root nodes, the length of the {@code cptValues} array must exactly match the size of the
   * {@code stateIds} list. The probabilities must be provided in the same order as the declared
   * states. For example:
   *
   * <pre>{@code
   * String nodeId = "RAIN";
   * List<String> stateIds = List.of("RAIN:T", "RAIN:F");
   * double[] rainCPT = {0.2, 0.8}; // 20% chance of RAIN:T, 80% chance of RAIN:F
   * bayesianNetworkBuilder.addRootNode(nodeId, stateIds, rainCPT);
   * }</pre>
   *
   * @param nodeId the identifier for the root {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with the {@link
   *     Node}.
   * @param cptValues the marginal probabilities for this root {@link Node}.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @return this builder instance for method chaining.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty.
   */
  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T nodeId, List<T> stateIds, double[] cptValues) {
    nodeInputs.add(new BayesianNetworkBuilderNode<>(nodeId, stateIds, cptValues));
    return this;
  }

  /**
   * Adds a conditional (parented) {@link Node} configuration to this builder.
   *
   * @param nodeId the identifier for this {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with this {@link
   *     Node}.
   * @param parentIds the identifiers of the parent {@link Node}s. These must correspond to nodes
   *     added to this builder before calling {@link #build()}.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @return this builder instance for method chaining.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty.
   */
  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T nodeId, List<T> stateIds, List<T> parentIds) {
    nodeInputs.add(new BayesianNetworkBuilderNode<>(nodeId, stateIds, parentIds));
    return this;
  }

  /**
   * Adds a conditional (parented) {@link Node} configuration, including its CPT values, to this
   * builder.
   *
   * <p>For conditional nodes, the length of the {@code cptValues} array must equal the product of
   * the number of states for this node and all of its parents.
   *
   * <p>The correct ordering of the {@code cptValues} array is determined by the {@code
   * cptNodeOrder} parameter, which must contain the identifiers of this node and all its parents.
   * The builder iterates through the Cartesian product of the states for these nodes. The iteration
   * cycles fastest through the right-most (last) node in the {@code cptNodeOrder} list,
   * incrementing the node to its left whenever it overflows.
   *
   * <p>Consider three nodes representing {@code P(WET_GRASS | RAIN, SPRINKLER)}, each with {@code
   * :T} and {@code :F} states:
   *
   * <pre>{@code
   * String nodeId = "WET_GRASS";
   * List<String> stateIds = List.of("WET_GRASS:T", "WET_GRASS:F");
   * // If the longest stride is "RAIN", and the shortest is "WET_GRASS":
   * List<String> cptNodeOrder = List.of("RAIN", "SPRINKLER", "WET_GRASS");
   * // "WET_GRASS" will iterate states at each index
   * // "SPRINKLER" will iterate states when "WET GRASS" cycles all its states (every 2 indices)
   * // "RAIN" will iterate states when "SPRINKLER" cycles all its states (every 4 indices)
   * double[] wetGrassCpt = new double[]{
   * 0.99, // RAIN:T, SPRINKLER:T, WET_GRASS:T
   * 0.01, // RAIN:T, SPRINKLER:T, WET_GRASS:F
   * 0.80, // RAIN:T, SPRINKLER:F, WET_GRASS:T
   * 0.20, // RAIN:T, SPRINKLER:F, WET_GRASS:F
   * 0.90, // RAIN:F, SPRINKLER:T, WET_GRASS:T
   * 0.10, // RAIN:F, SPRINKLER:T, WET_GRASS:F
   * 0.00, // RAIN:F, SPRINKLER:F, WET_GRASS:T
   * 1.00  // RAIN:F, SPRINKLER:F, WET_GRASS:F
   * };
   * bayesianNetworkBuilder.addNode(nodeId, stateIds, cptNodeOrder, wetGrassCpt);
   * }</pre>
   *
   * @param nodeId the identifier for this {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with this {@link
   *     Node}.
   * @param cptNodeOrder the identifiers of the node and its parents, ordered by the iteration
   *     hierarchy used for the {@code cptValues} array. The parent identifiers must correspond to
   *     nodes added to this builder before calling {@link #build()}.
   * @param cptValues the conditional probabilities mapping to the Cartesian product of the node
   *     states.
   * @param <T> the {@link Serializable} type of the identifiers.
   * @return this builder instance for method chaining.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} or empty, or if {@code cptStrideOrderDesc}
   *     does not contain the {@code nodeId}.
   */
  public <T extends Serializable> BayesianNetworkBuilder addNode(
      T nodeId, List<T> stateIds, List<T> cptNodeOrder, double[] cptValues) {
    nodeInputs.add(new BayesianNetworkBuilderNode<>(nodeId, stateIds, cptNodeOrder, cptValues));
    return this;
  }

  /**
   * Retrieves the name of the {@link BayesianNetwork} this builder will construct.
   *
   * @return the name of the new network.
   */
  public String getNetworkName() {
    return this.networkName;
  }

  /**
   * Sets the name of the {@link BayesianNetwork} this builder will construct.
   *
   * @param networkName the name of the new network.
   */
  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }

  /**
   * Returns the list of node builder objects in this {@code BayesianNetworkBuilder}. Each {@link
   * BayesianNetworkBuilderNode} contains all the information required to construct a single {@link
   * Node} within the new {@link BayesianNetwork}.
   *
   * @return the current list of {@link BayesianNetworkBuilderNode}s.
   */
  public List<BayesianNetworkBuilderNode<?>> getNodeInputs() {
    return this.nodeInputs;
  }
}
