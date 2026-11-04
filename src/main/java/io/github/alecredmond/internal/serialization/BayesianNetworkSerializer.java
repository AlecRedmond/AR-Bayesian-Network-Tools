package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.serializer.NodeSerializer;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.apache.commons.lang3.SerializationException;

public class BayesianNetworkSerializer {
  private final ProbabilityConstraintSerializer constraintSerializer;
  private final List<NodeSerializer<?>> serializers;

  public BayesianNetworkSerializer(NodeSerializer<?>... additionalSerializers) {
    constraintSerializer = new ProbabilityConstraintSerializer();
    serializers = new ArrayList<>();
    Arrays.stream(DefaultSerializerType.values())
        .map(DefaultSerializerType::getSupplier)
        .map(Supplier::get)
        .forEach(serializers::add);
    serializers.addAll(Arrays.asList(additionalSerializers));
  }

  public SerializedBayesianNetwork serializeNetwork(BayesianNetwork network) {
    return new SerializedBayesianNetwork(
        network.getNetworkName(),
        serializeAllNodes(network.getNodes(), network.getNetworkTables()),
        constraintSerializer.serialize(network.getConstraints()));
  }

  private SerializedNode[] serializeAllNodes(Node[] nodes, NetworkTable[] networkTables) {
    return IntStream.range(0, nodes.length)
        .mapToObj(i -> serializeNode(nodes[i], copyProbabilityArray(networkTables[i])))
        .toArray(SerializedNode[]::new);
  }

  private SerializedNode serializeNode(Node node, double[] cptVals) {
    return serializers.stream()
        .filter(serializer -> serializer.canSerialize(node))
        .findAny()
        .map(serializer -> serializer.serialize(node, cptVals))
        .orElseThrow(
            () ->
                new SerializationException(
                    "Node could not be serialized = "
                        + node
                        + "\nNo serializer declared for type : "
                        + node.getClass().getName()));
  }

  private double[] copyProbabilityArray(NetworkTable networkTable) {
    double[] p = networkTable.getProbabilities();
    return Arrays.copyOf(p, p.length);
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
      Node node = deSerializeNode(serializedNode, parents);
      nodeIdMap.put(node.getId(), node);
      node.getStates().forEach(state -> stateIdMap.put(state.getId(), state));
      nodes[i] = node;
      nodeSet.add(node);
    }
    return nodes;
  }

  private Node deSerializeNode(SerializedNode serializedNode, List<Node> parents) {
    return serializers.stream()
        .filter(serializer -> serializer.canDeSerialize(serializedNode))
        .findAny()
        .map(serializer -> serializer.deSerialize(serializedNode, parents))
        .orElseThrow(
            () ->
                new SerializationException(
                    "Serialized node could not be deserialized! = "
                        + serializedNode
                        + "\n No serializer declared with identifier : "
                        + serializedNode.serializerId()));
  }
}
