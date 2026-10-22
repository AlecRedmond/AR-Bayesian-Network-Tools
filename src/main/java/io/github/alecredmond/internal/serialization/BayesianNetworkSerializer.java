package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.serialization.structure.NetworkTableSerializer;
import io.github.alecredmond.internal.serialization.structure.NodeSerializer;
import io.github.alecredmond.internal.serialization.structure.ProbabilityConstraintSerializer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BayesianNetworkSerializer {

  public SerializedBayesianNetwork serialize(BayesianNetwork network) {
    if (!network.isSolved()) network.solveNetwork();
    BayesianNetworkData networkData = network.getNetworkData();
    return new SerializedBayesianNetwork(
        networkData.getNetworkName(),
        new NodeSerializer().serializeAllNodes(networkData),
        new NetworkTableSerializer().serializeAllTables(networkData),
        new ProbabilityConstraintSerializer().serializeAll(networkData),
        networkData.isSolved());
  }

  public BayesianNetwork deSerialize(SerializedBayesianNetwork serializedBayesianNetwork) {
    SerializationData serializationData = new SerializationData();
    createNodes(serializedBayesianNetwork, serializationData);
    return new BayesianNetworkImpl(
        deSerializeNetworkData(serializedBayesianNetwork, serializationData));
  }

  private void createNodes(SerializedBayesianNetwork sbn, SerializationData serializationData) {
    NodeSerializer serializer = new NodeSerializer();
    sbn.serializedNodes()
        .forEach(
            serializedNode -> {
              Node node = serializer.createNewBase(serializedNode);
              serializationData.getNodeIdMap().put(node.getId(), node);
              node.getStates()
                  .forEach(
                      state -> serializationData.getNodeStateIdMap().put(state.getId(), state));
            });
  }

  private BayesianNetworkData deSerializeNetworkData(
      SerializedBayesianNetwork sbn, SerializationData serializationData) {
    BayesianNetworkData networkData = serializationData.getNetworkData();
    networkData.setNetworkName(sbn.networkName());
    networkData.setSolved(sbn.solved());
    new NodeSerializer().deSerializeNodes(sbn, serializationData);
    new NetworkTableSerializer().deSerializeNetworkTables(sbn, serializationData);
    new ProbabilityConstraintSerializer().deserialize(sbn, serializationData);
    return networkData;
  }
}
