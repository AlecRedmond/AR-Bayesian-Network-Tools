package io.github.alecredmond.export.node;

import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Abstract common base for a {@link Node} interface. Contains the basic fields required during for
 * {@link Node} processing, as well as {@link HashMap}s to access each {@link NodeState} from its
 * name or identifier. The setters automatically convert any {@link List} values to be unmodifiable
 * views, and the Equals/Hashcode includes only the node's identifier, name, and its states.
 *
 * @see Node
 * @see DefaultNode
 * @see DefaultBinaryNode
 * @author Alec Redmond
 */
@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
abstract class AbstractBayesNode implements Node {
  @EqualsAndHashCode.Include @ToString.Include protected Serializable id;
  @EqualsAndHashCode.Include @ToString.Include protected String name;
  @EqualsAndHashCode.Include @ToString.Include protected List<NodeState> states;

  protected Map<String, NodeState> stateNameMap;
  protected Map<Serializable, NodeState> stateIdMap;
  protected List<Node> parents;
  protected List<Node> children;

  protected AbstractBayesNode(
      Serializable id, String name, List<NodeState> states, List<Node> parents) {
    this.id = id;
    this.name = name;
    this.parents = Collections.unmodifiableList(parents);
    this.children = List.of();
    this.states = Collections.unmodifiableList(states);
  }

  public Serializable getId() {
    return this.id;
  }

  public void setId(Serializable id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

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

  @Override
  public void setChildren(List<Node> children) {
    this.children = Collections.unmodifiableList(children);
  }

  @Override
  public NodeState getStateByName(String stateName) {
    return stateNameMap.get(stateName);
  }

  @Override
  public NodeState getStateById(Serializable stateId) {
    return stateIdMap.get(stateId);
  }

  public void setParents(List<Node> parents) {
    this.parents = Collections.unmodifiableList(parents);
  }

  public void setStates(List<NodeState> states) {
    this.states = Collections.unmodifiableList(states);
    this.stateNameMap = new HashMap<>();
    this.stateIdMap = new HashMap<>();
    NodeUtils.buildStateNameAndIdMap(states, stateNameMap, stateIdMap);
  }
}
