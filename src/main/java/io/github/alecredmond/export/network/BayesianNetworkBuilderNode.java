package io.github.alecredmond.export.network;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.alecredmond.internal.method.utils.CollectionToString;
import lombok.Data;
import lombok.NonNull;

/**
 * Data class for {@link BayesianNetworkBuilder} which defines the properties of a single {@link
 * Node} to be constructed in a {@link BayesianNetwork}.
 *
 * @see BayesianNetworkBuilder
 * @see Node
 * @see BayesianNetwork
 * @author Alec Redmond
 * @param <T> the {@link Serializable} type of the identifiers.
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@Data
public class BayesianNetworkBuilderNode<T extends Serializable> {
  /** The identifier for the {@link Node}. */
  private final T nodeId;

  /** The identifiers for the {@link NodeState} values. */
  private final List<T> stateIds;

  /** The identifiers of the parent {@link Node}s. */
  private final List<T> parentNodeIds;

  /**
   * The permutation ordering of the node and parent identifiers. Further documentation may be found
   * here: {@link #BayesianNetworkBuilderNode(Serializable, List, List, double[])}.
   */
  private final List<T> cptNodeOrder;

  /** All values of the conditional probability table associated with this {@link Node}. */
  private final double[] cptValues;

  /**
   * Constructs a new {@code BayesianNetworkBuilderNode} for a root (unparented) {@link Node}.
   *
   * @param nodeId the identifier for the root {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with the {@link
   *     Node}.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is empty or contains a {@code null} value.
   */
  public BayesianNetworkBuilderNode(@NonNull T nodeId, @NonNull List<T> stateIds) {
    assertStateIdsNotEmptyOrNull(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = null;
    this.cptNodeOrder = null;
    this.cptValues = null;
  }

  private static <T extends Serializable> void assertStateIdsNotEmptyOrNull(
      T id, List<T> stateIds) {
    if (stateIds.isEmpty()) {
      throw new IllegalArgumentException("STATE IDs EMPTY FOR NODE ID : %s".formatted(id));
    }
    if (stateIds.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("STATE IDs CONTAINED NULL FOR NODE ID %s".formatted(id));
    }
  }

  /**
   * Constructs a new {@code BayesianNetworkBuilderNode} for a root (unparented) {@link Node}.
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
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is empty or contains a {@code null} value.
   */
  public BayesianNetworkBuilderNode(T nodeId, List<T> stateIds, double[] cptValues) {
    assertStateIdsNotEmptyOrNull(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = List.of();
    this.cptNodeOrder = List.of(this.nodeId);
    this.cptValues = cptValues;
  }

  /**
   * Constructs a new {@code BayesianNetworkBuilderNode} for a conditional (parented) {@link Node}.
   *
   * <p>If the {@code parentNodeIds} parameter is an empty list, this constructor works identically
   * to {@link #BayesianNetworkBuilderNode(Serializable, List)}.
   *
   * @param nodeId the identifier for this {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with this {@link
   *     Node}.
   * @param parentNodeIds the identifiers of the parent {@link Node}s.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is empty or contains a {@code null} value.
   */
  public BayesianNetworkBuilderNode(
      @NonNull T nodeId, @NonNull List<T> stateIds, @NonNull List<T> parentNodeIds) {
    assertStateIdsNotEmptyOrNull(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = parentNodeIds;
    this.cptNodeOrder = null;
    this.cptValues = null;
  }

  /**
   * Constructs a new {@code BayesianNetworkBuilderNode} for a conditional (parented) {@link Node}.
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
   * <p>This can also be used to construct a root node if only the {@code nodeId} is contained
   * within the {@code cptNodeOrder}. In this case, it works identically to {@link
   * #BayesianNetworkBuilderNode(Serializable, List, double[])}.
   *
   * @param nodeId the identifier for this {@link Node}.
   * @param stateIds the identifiers for the {@link NodeState} values associated with this {@link
   *     Node}.
   * @param cptNodeOrder the identifiers of the node and its parents, ordered by the iteration
   *     hierarchy used for the {@code cptValues} array.
   * @param cptValues the conditional probabilities mapping to the Cartesian product of the node
   *     states.
   * @throws NullPointerException if any input parameter is {@code null}.
   * @throws IllegalArgumentException if {@code stateIds} is null or empty, or if {@code
   *     cptNodeOrder} does not contain the {@code nodeId}.
   */
  public BayesianNetworkBuilderNode(
      @NonNull T nodeId,
      @NonNull List<T> stateIds,
      @NonNull List<T> cptNodeOrder,
      @NonNull double[] cptValues) {
    assertStateIdsNotEmptyOrNull(nodeId, stateIds);
    this.nodeId = nodeId;
    this.stateIds = stateIds;
    this.parentNodeIds = buildParentIds(nodeId, cptNodeOrder);
    this.cptNodeOrder = cptNodeOrder;
    this.cptValues = cptValues;
  }

  private List<T> buildParentIds(T id, List<T> cptStrideOrderDesc) {
    List<T> parents = new ArrayList<>(cptStrideOrderDesc);
    if (parents.remove(id)) {
      return parents.isEmpty() ? null : parents;
    }
      throw new IllegalArgumentException(
        "cptNodeOrder list {%s} does not contain node id {%s}!"
            .formatted(CollectionToString.apply(cptStrideOrderDesc), id));
  }

  /**
   * Retrieves the identifier for this node.
   *
   * @return the {@link Serializable} identifier for the {@link Node}.
   */
  public Serializable getNodeId() {
    return this.nodeId;
  }

  /**
   * Retrieves the list of state identifiers associated with this node.
   *
   * @return a {@link List} of state identifiers.
   */
  public List<T> getStateIds() {
    return this.stateIds;
  }

  /**
   * Retrieves the identifiers of the parent nodes for this node.
   *
   * @return a {@link List} of parent node identifiers, or {@code null} if this is a root node.
   */
  public List<T> getParentNodeIds() {
    return this.parentNodeIds;
  }

  /**
   * Retrieves the permutation ordering used to construct the conditional probability table.
   *
   * @return a {@link List} defining the hierarchy order of the node and its parents, or {@code
   *     null} if not applicable.
   */
  public List<T> getCptNodeOrder() {
    return this.cptNodeOrder;
  }

  /**
   * Retrieves the conditional probability table values associated with this node.
   *
   * @return an array of {@code double} values representing the CPT, or {@code null} if not
   *     applicable.
   */
  public double[] getCptValues() {
    return this.cptValues;
  }
}
