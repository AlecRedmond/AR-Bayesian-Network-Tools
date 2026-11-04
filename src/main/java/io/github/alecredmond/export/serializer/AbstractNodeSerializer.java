package io.github.alecredmond.export.serializer;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.implementation.DefaultNodeState;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import java.io.Serializable;
import java.util.List;
import java.util.stream.IntStream;

public abstract class AbstractNodeSerializer<T extends Node> implements NodeSerializer<T> {

  @Override
  public SerializedNode serializeGeneric(T node, double[] cptProbabilities) {
    Serializable id = node.getId();
    String name = node.getName();
    List<NodeState> states = node.getStates();
    final int numOfStates = states.size();
    Serializable[] stateIds = new Serializable[numOfStates];
    String[] stateNames = new String[numOfStates];
    for (int i = 0; i < numOfStates; i++) {
      NodeState state = states.get(i);
      stateIds[i] = state.getId();
      stateNames[i] = state.getName();
    }
    Serializable[] parentIds =
        node.getParents().stream().map(Node::getId).toArray(Serializable[]::new);
    return new SerializedNode(
        id, name, stateIds, stateNames, parentIds, cptProbabilities, serializerId());
  }

  protected List<NodeState> deserializeStates(SerializedNode serializedNode, Node defaultNode) {
    Serializable[] stateIds = serializedNode.stateIds();
    String[] stateNames = serializedNode.stateNames();
    return IntStream.range(0, stateNames.length)
        .mapToObj(i -> new DefaultNodeState(defaultNode, i, stateNames[i], stateIds[i]))
        .map(NodeState.class::cast)
        .toList();
  }
}
