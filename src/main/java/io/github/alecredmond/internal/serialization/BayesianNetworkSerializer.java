package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialization.SerializedNode;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.*;

public class BayesianNetworkSerializer {
  private final ProbabilityConstraintSerializer constraintSerializer;
  private final NodeSerializers serializers;

  public BayesianNetworkSerializer() {
    constraintSerializer = new ProbabilityConstraintSerializer();
    serializers = NodeSerializers.getInstance();
  }

  public SerializedBayesianNetwork serializeNetwork(BayesianNetwork network) {
    return new SerializedBayesianNetwork(
        network.getNetworkName(),
        serializeAllNodes(network.getNetworkTables()),
        constraintSerializer.serialize(network.getConstraints()));
  }

  private SerializedNode[] serializeAllNodes(NetworkTable[] networkTables) {
    return Arrays.stream(networkTables)
        .map(serializers::serializeFromTable)
        .toArray(SerializedNode[]::new);
  }

  public BayesianNetwork deSerializeNetwork(SerializedBayesianNetwork sbn) {
    SerializationData serializationData = new SerializationData();
    return BayesianNetwork.builder()
        .setNetworkName(sbn.networkName())
        .setNodes(createNodes(sbn, serializationData))
        .addConstraints(constraintSerializer.deserialize(sbn, serializationData))
        .build(new DeserializerSolver(sbn))
        .orElseThrow();
  }

  private Node[] createNodes(SerializedBayesianNetwork sbn, SerializationData serializationData) {
    Set<Node> nodeSet = serializationData.getNodeSet();
    Map<Serializable, Node> nodeIdMap = serializationData.getNodeIdMap();
    Map<Serializable, NodeState> stateIdMap = serializationData.getNodeStateIdMap();
    SerializedNode[] serializedNodes = sbn.serializedNodes();
    Node[] nodes = new Node[serializedNodes.length];
    for (int i = 0; i < serializedNodes.length; i++) {
      SerializedNode serializedNode = serializedNodes[i];
      List<Node> parents = Arrays.stream(serializedNode.parentIds()).map(nodeIdMap::get).toList();
      Node node = serializers.deserialize(serializedNode, parents);
      nodeIdMap.put(node.getId(), node);
      node.getStates().forEach(state -> stateIdMap.put(state.getId(), state));
      nodes[i] = node;
      nodeSet.add(node);
    }
    return nodes;
  }
}
