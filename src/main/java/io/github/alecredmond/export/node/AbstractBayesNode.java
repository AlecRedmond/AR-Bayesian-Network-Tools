package io.github.alecredmond.export.node;

import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Provides the state and behavior shared by the default {@link Node} implementations.
 *
 * <p>This class holds the fields that every node needs during network processing: an identifier, a
 * name, the list of {@link NodeState}s, and the lists of parent and child nodes. It also keeps two
 * {@link HashMap}s, so that a {@code NodeState} can be found from its name or from its identifier
 * without searching the state list.
 *
 * <p>Each {@link List} passed to a constructor or setter of this class is wrapped in an
 * unmodifiable view rather than copied. Callers cannot modify the stored lists through the
 * accessors, but changes made to the original list remain visible through the view.
 *
 * <p>The {@code equals} and {@code hashCode} methods use only the identifier, the name, and the
 * states of a node, and the string representation shows the same three fields. The parents, the
 * children, and the lookup maps are ignored.
 *
 * @author Alec Redmond
 * @see Node
 * @see DefaultNode
 * @see DefaultBinaryNode
 */
@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
abstract class AbstractBayesNode implements Node {
  /** The unique identifier of this node. */
  @EqualsAndHashCode.Include @ToString.Include protected Serializable id;

  /** The name of this node. */
  @EqualsAndHashCode.Include @ToString.Include protected String name;

  /** The unmodifiable list of states that this node can exhibit, in order. */
  @EqualsAndHashCode.Include @ToString.Include protected List<NodeState> states;

  /** The states of this node, keyed by state name. Rebuilt by {@link #setStates(List)}. */
  protected Map<String, NodeState> stateNameMap;

  /** The states of this node, keyed by state identifier. Rebuilt by {@link #setStates(List)}. */
  protected Map<Serializable, NodeState> stateIdMap;

  /** The unmodifiable list of parent nodes of this node. */
  protected List<Node> parents;

  /** The unmodifiable list of child nodes of this node. */
  protected List<Node> children;

  public Serializable getId() {
    return this.id;
  }

  /**
   * Sets the identifier of this node.
   *
   * <p>The identifier is part of this node's {@code equals} and {@code hashCode}, so it should not
   * be changed while the node is stored in a hash-based collection.
   *
   * @param id the new unique identifier of this node
   */
  public void setId(Serializable id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of this node.
   *
   * <p>The name is part of this node's {@code equals} and {@code hashCode}, so it should not be
   * changed while the node is stored in a hash-based collection.
   *
   * @param name the new name of this node
   */
  public void setName(String name) {
    this.name = name;
  }

  public List<NodeState> getStates() {
    return this.states;
  }

  public List<Node> getParents() {
    return this.parents;
  }

  @Override
  public List<Node> getChildren() {
    return children;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The list is wrapped in an unmodifiable view rather than copied.
   *
   * @throws NullPointerException if {@code children} is {@code null}
   */
  @Override
  public void setChildren(List<Node> children) {
    this.children = Collections.unmodifiableList(children);
  }

  @Override
  public NodeState getStateByName(String stateName) {
    return stateNameMap.get(stateName);
  }

  /**
   * Sets the parents of this node.
   *
   * <p>The list is wrapped in an unmodifiable view rather than copied. This method does not change
   * the children of the specified parents.
   *
   * @param parents the new parents of this node
   * @throws NullPointerException if {@code parents} is {@code null}
   */
  public void setParents(List<Node> parents) {
    this.parents = Collections.unmodifiableList(parents);
  }

  /**
   * Sets the states of this node and rebuilds the lookups used by {@link #getStateByName(String)}
   * and {@code getStateById}.
   *
   * <p>The list is wrapped in an unmodifiable view rather than copied. Any states set previously
   * are discarded.
   *
   * @param states the new states of this node, in order
   * @throws NullPointerException if {@code states} is {@code null}
   */
  public void setStates(List<NodeState> states) {
    this.states = Collections.unmodifiableList(states);
    this.stateNameMap = new HashMap<>();
    this.stateIdMap = new HashMap<>();
    NodeUtils.buildStateNameAndIdMap(states, stateNameMap, stateIdMap);
  }
}
