package io.github.alecredmond.export.node.newnode;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.*;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DefaultBinaryNode extends AbstractBayesNode implements BinaryNode {
  private NodeState falseState;
  private NodeState trueState;

  public DefaultBinaryNode(
      Serializable id, String name, List<Node> parents, StateOrder stateOrder) {
    setId(id);
    setName(name);
    setParents(parents);
    setStatesByOrder(stateOrder);
  }

  private void setStatesByOrder(StateOrder stateOrder) {
    switch (stateOrder) {
      case FALSE_FIRST -> {
        setStates(NodeState.createStates(this, "FALSE", "TRUE"));
        this.falseState = getStates().getFirst();
        this.trueState = getStates().getLast();
      }
      case TRUE_FIRST -> {
        setStates(NodeState.createStates(this, "TRUE", "FALSE"));
        this.falseState = getStates().getLast();
        this.trueState = getStates().getFirst();
      }
    }
  }

  public DefaultBinaryNode(Serializable id, String name, List<Node> parents) {
    setId(id);
    setName(name);
    setParents(parents);
    setStatesByOrder(StateOrder.FALSE_FIRST);
  }

  public DefaultBinaryNode(String name, List<Node> parents, StateOrder stateOrder) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(parents);
    setStatesByOrder(stateOrder);
  }

  public DefaultBinaryNode(String name, Node parent, StateOrder stateOrder) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(List.of(parent));
    setStatesByOrder(stateOrder);
  }

  public DefaultBinaryNode(String name, List<Node> parents) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(parents);
    setStatesByOrder(StateOrder.FALSE_FIRST);
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
}
