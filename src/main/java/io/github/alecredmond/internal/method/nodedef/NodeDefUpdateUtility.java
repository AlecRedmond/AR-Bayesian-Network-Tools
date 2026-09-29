package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.nodedef.NodeDefinition;
import io.github.alecredmond.export.nodedef.StateDef;
import io.github.alecredmond.internal.method.network.changehandlers.CollectionChangeAnalyzer;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NodeDefUpdateUtility {
  private NodeDefUpdateUtility() {}

  public static void updateNode(NodeDefinition nodeDef) {
    Optional.ofNullable(nodeDef.getNode())
        .ifPresent(
            node -> {
              checkId(nodeDef, node);
              checkName(nodeDef, node);
              checkNodeStates(nodeDef, node);
              checkParents(nodeDef, node);
            });
  }

  private static void checkId(NodeDefinition nodeDef, Node node) {
    if (nodeDef.getId().equals(node.getId())) return;
    throw new BayesNetIDException(
        "Node Definition %s has changed ID, Node %s has been invalidated!"
            .formatted(nodeDef, node));
  }

  private static void checkName(NodeDefinition nodeDef, Node node) {
    if (nodeDef.getName().equals(node.getName())) return;
    node.setName(nodeDef.getName());
  }

  private static void checkNodeStates(NodeDefinition nodeDef, Node node) {
    CollectionChangeAnalyzer<Serializable> analyzer =
        new CollectionChangeAnalyzer<>(
            NodeUtils.getNodeStateIds(node.getNodeStates()),
            NodeUtils.getDefStateIds(nodeDef.getStateDefs()));

    if (analyzer.isCollectionIdenticallyOrdered()) return;

    Map<Serializable, NodeState> stateMap = new HashMap<>();
    node.getNodeStates().forEach(state -> stateMap.put(state.getId(), state));
    analyzer.getRemoved().forEach(stateMap::remove);

    List<NodeState> newStates =
        nodeDef.getStateDefs().stream()
            .map(stateDef -> addOrCreateNodeState(stateDef, node, stateMap))
            .toList();

    node.setNodeStates(newStates);
  }

  private static void checkParents(NodeDefinition nodeDef, Node node) {
    List<Serializable> defParentIds = NodeUtils.getDefIds(nodeDef.getParentDefs());

    CollectionChangeAnalyzer<Serializable> analyzer =
        new CollectionChangeAnalyzer<>(NodeUtils.getNodeIds(node.getParents()), defParentIds);

    if (analyzer.isCollectionHasSameElements()) return;

    BayesianNetwork network = node.getNetwork();
    List<Node> newParents = defParentIds.stream().map(network::getNode).toList();
    node.setParents(newParents);
  }

  private static NodeState addOrCreateNodeState(
      StateDef stateDef, Node node, Map<Serializable, NodeState> stateMap) {
    return stateMap.containsKey(stateDef.getId())
        ? stateMap.get(stateDef.getId())
        : DiscreteNodeDefMapper.createNodeState(node, stateDef);
  }
}
