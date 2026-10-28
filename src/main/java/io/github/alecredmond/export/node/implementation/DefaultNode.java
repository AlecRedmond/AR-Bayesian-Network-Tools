package io.github.alecredmond.export.node.implementation;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class DefaultNode extends AbstractBayesNode {

  public DefaultNode(Serializable id, String name, List<NodeState> states, List<Node> parents) {
    super(id, name, states, parents);
  }

  public DefaultNode(String name, List<String> stateNames, List<Node> parents) {
    setId(UUID.randomUUID());
    setName(name);
    setParents(parents);
    setStates(NodeState.createStates(this, stateNames));
  }
}
