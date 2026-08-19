package io.github.alecredmond.export.node;

import static io.github.alecredmond.internal.method.network.changehandlers.NetworkPropertyChangeEvent.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a variable within a {@link BayesianNetwork}.
 *
 * <p>Each node contains a list of its {@link NodeState}s as well as its parents and children. A
 * {@link NodeState} is a state that a {@code Node} can exhibit, such as {@code "NODE:TRUE"} or
 * {@code "NODE:FALSE"}. A {@code Node} is conditionally dependent on its parents, while its
 * children are conditionally dependent on it.
 *
 * <p><b>Note:</b> All nodes in a network MUST be registered in the {@link BayesianNetwork} instance
 * before attempting to connect them to their parents or children through the methods defined here.
 *
 * @see NodeState
 * @see BayesianNetwork
 * @see BayesianNetworkBuilder
 * @see ProbabilityConstraint
 * @author Alec Redmond
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
public class Node {

  /**
   * The identifier for this {@code Node}. This value must be unique within its associated {@link
   * BayesianNetwork}.
   */
  @EqualsAndHashCode.Include private final Serializable id;

  /**
   * The property change support utility. Used within the {@link BayesianNetwork} to listen for
   * field changes in this {@code Node}.
   */
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  /** The ordered list of {@link NodeState} values that this {@code Node} can exhibit. */
  private List<NodeState> nodeStates;

  /** The parents of this {@code Node} in the {@link BayesianNetwork} structure. */
  private List<Node> parents;

  /** The children of this {@code Node} in the {@link BayesianNetwork} structure. */
  private List<Node> children;

  /**
   * Constructs a new {@code Node} using its identifier and a collection of state identifiers.
   *
   * @param nodeId the {@link Serializable} identifier of the {@code Node}.
   * @param stateIDs the collection of {@link Serializable} identifiers for the {@link NodeState}
   *     values.
   * @param <T> the type of the node identifier.
   * @param <E> the type of the state identifiers.
   * @throws NullPointerException if the node identifier or state identifier collection is {@code
   *     null}.
   */
  public <T extends Serializable, E extends Serializable> Node(
      @NonNull T nodeId, @NonNull Collection<E> stateIDs) {
    this.id = nodeId;
    this.parents = List.of();
    this.children = List.of();
    this.nodeStates = stateIDs.stream().map(stateId -> new NodeState(stateId, this)).toList();
    writeStatePositions();
  }

  /**
   * Sets the position in each {@link NodeState} to equal its index in {@link #getNodeStates()}.
   * This method runs internally whenever {@link #nodeStates} is updated.
   */
  void writeStatePositions() {
    int position = 0;
    for (NodeState state : nodeStates) {
      state.setPosition(position++);
    }
  }

  /**
   * Constructs a new {@code Node} using only its identifier.
   *
   * @param nodeId the {@link Serializable} identifier of the {@code Node}
   * @param <T> the type of the node identifier.
   * @throws NullPointerException if the node identifier is {@code null}.
   */
  public <T extends Serializable> Node(@NonNull T nodeId) {
    this.id = nodeId;
    this.parents = List.of();
    this.children = List.of();
    this.nodeStates = List.of();
  }

  /**
   * Sets a list of {@link Node}s as the new parents for this {@code Node}.
   *
   * <p>This method fires the property change event {@code "NODE_PARENTS_UPDATED"}. If validation of
   * the new list fails, the error will be logged and the previous parent list will be restored.
   *
   * @param parents the new list of parents connected to this {@code Node}.
   * @return {@code true} if the new list was successfully set, or {@code false} if validation
   *     failed.
   */
  public boolean setParents(List<Node> parents) {
    List<Node> oldParents = this.parents;
    this.parents = Collections.unmodifiableList(parents);
    try {
      support.firePropertyChange(NODE_PARENTS_UPDATED.name(), oldParents, parents);
      return true;
    } catch (BayesNetIDException e) {
      log.error("Error setting parents, '{}' reverting...", e.getMessage());
      this.parents = oldParents;
      return false;
    }
  }

  /**
   * Sets a list of {@link Node}s as the new children for this {@code Node}.
   *
   * <p>This method fires the property change event {@code "NODE_CHILDREN_UPDATED"}. If validation
   * of the new list fails, the error will be logged and the previous children list will be
   * restored.
   *
   * @param children the new list of children connected to this {@code Node}.
   * @return {@code true} if the new list was successfully set, or {@code false} if validation
   *     failed.
   */
  public boolean setChildren(List<Node> children) {
    List<Node> oldChildren = this.children;
    this.children = Collections.unmodifiableList(children);
    try {
      support.firePropertyChange(NODE_CHILDREN_UPDATED.name(), oldChildren, children);
      return true;
    } catch (BayesNetIDException e) {
      log.error("Error setting children, '{}' reverting...", e.getMessage());
      this.children = oldChildren;
      return false;
    }
  }

  /**
   * Adds a new state to this {@code Node} using the provided identifier.
   *
   * @param stateID the identifier for the new {@link NodeState}.
   * @param <T> the type of the state identifier.
   * @return {@code true} if the new state was successfully added, or {@code false} if validation
   *     failed.
   */
  public <T extends Serializable> boolean addState(@NonNull T stateID) {
    NodeState newState = new NodeState(stateID, this);
    return setNodeStates(NodeUtils.addToList(nodeStates, newState));
  }

  /**
   * Sets a list of {@link NodeState} values as the new active states for this {@code Node}.
   *
   * <p>This method fires a property change event with the label {@code "NODE_STATES_UPDATED"}. If
   * validation of the new list fails, the error will be logged and the previous {@link NodeState}
   * list will be restored.
   *
   * @param nodeStates the new list of {@link NodeState}s associated with this {@code Node}.
   * @return {@code true} if the new list was successfully set, or {@code false} if validation
   *     failed.
   */
  public boolean setNodeStates(List<NodeState> nodeStates) {
    List<NodeState> oldStates = this.nodeStates;
    this.nodeStates = Collections.unmodifiableList(nodeStates);
    try {
      support.firePropertyChange(NODE_STATES_UPDATED.name(), oldStates, nodeStates);
      writeStatePositions();
      return true;
    } catch (BayesNetIDException e) {
      log.error("Error setting states, '{}' reverting...", e.getMessage());
      this.nodeStates = oldStates;
      return false;
    }
  }

  /**
   * Removes an existing state from this {@code Node} matching the provided identifier.
   *
   * @param stateID the identifier of the {@link NodeState} to be removed.
   * @param <E> the type of the state identifier.
   * @return {@code true} if the {@link NodeState} was successfully removed, or {@code false} if
   *     validation failed.
   */
  public <E extends Serializable> boolean removeState(E stateID) {
    return setNodeStates(NodeUtils.statesWithoutId(nodeStates, stateID));
  }

  /**
   * Adds a {@link Node} as a parent to this node.
   *
   * @param parent the {@link Node} to add as a parent.
   */
  public void addParent(Node parent) {
    NodeUtils.addParent(this, parent);
  }

  /**
   * Removes a specific {@link Node} from this node's list of parents.
   *
   * @param parent the {@link Node} parent to remove.
   */
  public void removeParent(Node parent) {
    NodeUtils.removeParent(this, parent);
  }

  /**
   * Adds this {@code Node} to a {@link PropertyChangeListener} and fires a property change event.
   *
   * <p>Fires {@code "NODE_ADDED_TO_NETWORK"}. This method is used internally by the implementation
   * of {@link BayesianNetwork}.
   *
   * @param listener the {@link PropertyChangeListener} instance to attach
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
    support.firePropertyChange(NODE_ADDED_TO_NETWORK.name(), null, this);
  }

  /**
   * Removes this {@code Node} from a {@link PropertyChangeListener} and fires a property change
   * event.
   *
   * <p>Fires {@code "NODE_REMOVED_FROM_NETWORK"}. This method is used internally by the
   * implementation of {@link BayesianNetwork}.
   *
   * @param listener the {@link PropertyChangeListener} instance to remove
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.firePropertyChange(NODE_REMOVED_FROM_NETWORK.name(), this, null);
    support.removePropertyChangeListener(listener);
  }

  /**
   * Returns a string representation of this node, which corresponds to its identifier.
   *
   * @return the string representation of the node identifier.
   */
  @Override
  public String toString() {
    return id.toString();
  }

  /**
   * Returns the unique identifier for this {@code Node}.
   *
   * @return the {@link Serializable} identifier of this node.
   */
  public Serializable getId() {
    return this.id;
  }

  /**
   * Returns the property change support utility used to listen for field changes.
   *
   * @return the {@link PropertyChangeSupport} instance attached to this node.
   */
  public PropertyChangeSupport getSupport() {
    return this.support;
  }

  /**
   * Returns the ordered list of {@link NodeState} values that this {@code Node} can exhibit.
   *
   * @return an unmodifiable {@link List} of the node's states.
   */
  public List<NodeState> getNodeStates() {
    return this.nodeStates;
  }

  /**
   * Returns the parent nodes of this {@code Node} in the network structure.
   *
   * @return an unmodifiable {@link List} of parent nodes.
   */
  public List<Node> getParents() {
    return this.parents;
  }

  /**
   * Returns the child nodes of this {@code Node} in the network structure.
   *
   * @return an unmodifiable {@link List} of child nodes.
   */
  public List<Node> getChildren() {
    return this.children;
  }
}
