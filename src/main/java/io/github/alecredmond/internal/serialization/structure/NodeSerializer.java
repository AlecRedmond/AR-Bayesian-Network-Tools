package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.Arrays;
import java.util.List;

public class NodeSerializer {

  // TODO - REFACTOR
  public Node createNewBase(SerializedNode sto) {
    return null;
    // return new Node(sto.id(), sto.stateIds());
  }

  public List<SerializedNode> serializeAllNodes(BayesianNetworkData networkData) {
    return Arrays.stream(networkData.getNodes()).map(this::serialize).toList();
  }

  public SerializedNode serialize(Node node) {
    return new SerializedNode(
        node.getId(),
        SerializerUtils.serializeNodeStates(node.getStates()),
        SerializerUtils.serializeNodes(node.getParents()),
        SerializerUtils.serializeNodes(node.getChildren()));
  }

  public void deSerializeNodes(
      SerializedBayesianNetwork serializedBayesianNetwork, SerializationData serializationData) {
//    List<SerializedNode> serializedNodes = serializedBayesianNetwork.serializedNodes();
//    List<Node> nodes = serializationData.getNetworkData().getNodes();
//    serializedNodes.stream()
//        .map(serializedNode -> deSerialize(serializedNode, serializationData))
//        .forEach(nodes::add);
  }

  public Node deSerialize(SerializedNode sto, SerializationData data) {
    return null;
    //    Node node = data.getNodeIdMap().get(sto.id());
    //    node.setParents(SerializerUtils.deSerializeNodes(sto.parentIds(), ArrayList::new, data));
    //    node.setChildren(SerializerUtils.deSerializeNodes(sto.childIds(), ArrayList::new, data));
    //    return node;
  }
}
