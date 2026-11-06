package io.github.alecredmond.export.node.implementation;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Setter
public class DefaultBinaryNode extends AbstractBayesNode implements BinaryNode {
  public static final String TRUE_STATE_NAME = "TRUE";
  public static final String FALSE_STATE_NAME = "FALSE";
  private NodeState falseState;
  private NodeState trueState;
  private StateOrder stateOrder;

  public DefaultBinaryNode(
      Serializable id, String name, List<Node> parents, StateOrder stateOrder) {
    setId(id);
    setName(name);
    setParents(parents);
    setStatesByOrder(stateOrder);
    this.stateOrder = stateOrder;
  }

  private void setStatesByOrder(StateOrder stateOrder) {
    switch (stateOrder) {
      case FALSE_FIRST -> {
        setStates(NodeState.createStates(this, FALSE_STATE_NAME, TRUE_STATE_NAME));
        this.falseState = getStates().getFirst();
        this.trueState = getStates().getLast();
      }
      case TRUE_FIRST -> {
        setStates(NodeState.createStates(this, TRUE_STATE_NAME, FALSE_STATE_NAME));
        this.falseState = getStates().getLast();
        this.trueState = getStates().getFirst();
      }
    }
  }

  public DefaultBinaryNode(String name, List<Node> parents, StateOrder stateOrder) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(parents);
    setStatesByOrder(stateOrder);
  }

  public DefaultBinaryNode(String name, StateOrder stateOrder) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(List.of());
    setStatesByOrder(stateOrder);
  }

  @Override
  public NodeState trueState() {
    return trueState;
  }

  @Override
  public NodeState falseState() {
    return falseState;
  }

  @Override
  public StateOrder getStateOrder() {
    return stateOrder;
  }
}
