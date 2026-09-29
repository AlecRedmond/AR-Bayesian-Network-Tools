package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.nodedef.NodeDefinition;
import io.github.alecredmond.export.nodedef.StateDef;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscreteNodeDefMapper implements NodeDefMapper {

  @Override
  public Node createNode(BayesianNetwork network, NodeDefinition nodeDef) {
    Node node = new Node(nodeDef.getId());
    node.setName(nodeDef.getName());
    node.setNodeDef(nodeDef);
    List<NodeState> states =
        nodeDef.getStateDefs().stream().map(stateDef -> createNodeState(node, stateDef)).toList();
    node.setNodeStates(states);
    network.addNode(node);
    nodeDef.setNode(node);
    return node;
  }

  public static NodeState createNodeState(Node node, StateDef stateDef) {
    NodeState state = new NodeState(stateDef.getId(), node);
    state.setStateDef(stateDef);
    state.setName(state.getName());
    return state;
  }

  @Override
  public void addParents(BayesianNetwork network, NodeDefinition nodeDef) {
    List<Serializable> parentIds = nodeDef.getParentDefs().stream().map(NodeDefinition::getId).toList();
    List<Node> parentNodes = new ArrayList<>(network.getNodes(parentIds));
    Node child = nodeDef.getNode();
    child.setParents(parentNodes);
  }
}
