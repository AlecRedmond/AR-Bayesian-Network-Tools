package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.nodedef.DiscreteNodeDef;
import io.github.alecredmond.export.nodedef.NodeDef;
import io.github.alecredmond.export.nodedef.StateDef;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscreteNodeDefMapper implements NodeDefMapper<DiscreteNodeDef> {

  @Override
  public Node createNode(BayesianNetwork network, DiscreteNodeDef nodeDef) {
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
  public void addParents(BayesianNetwork network, DiscreteNodeDef nodeDef) {
    List<Serializable> parentIds = nodeDef.getParentDefs().stream().map(NodeDef::getId).toList();
    List<Node> parentNodes = new ArrayList<>(network.getNodes(parentIds));
    Node child = nodeDef.getNode();
    child.setParents(parentNodes);
  }
}
