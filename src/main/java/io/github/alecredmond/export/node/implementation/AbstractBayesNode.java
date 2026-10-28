package io.github.alecredmond.export.node.implementation;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@SuppressWarnings({"LombokGetterMayBeUsed", "LombokSetterMayBeUsed"})
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public abstract class AbstractBayesNode implements Node {
  @EqualsAndHashCode.Include @ToString.Include protected Serializable id;
  @EqualsAndHashCode.Include @ToString.Include protected String name;
  @EqualsAndHashCode.Include @ToString.Include protected List<NodeState> states;

  protected Map<String, NodeState> stateNameMap;
  protected Map<Serializable, NodeState> stateIdMap;
  protected List<Node> parents;
  protected List<Node> children;
  protected NetworkTable cpt;

  protected AbstractBayesNode(
      Serializable id, String name, List<NodeState> states, List<Node> parents) {
    this.id = id;
    this.name = name;
    this.parents = Collections.unmodifiableList(parents);
    this.children = List.of();
    this.states = Collections.unmodifiableList(states);
    this.cpt = null;
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
    this.children = children;
  }

  public NetworkTable getCpt() {
    return this.cpt;
  }

  public void setCpt(NetworkTable cpt) {
    this.cpt = cpt;
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
