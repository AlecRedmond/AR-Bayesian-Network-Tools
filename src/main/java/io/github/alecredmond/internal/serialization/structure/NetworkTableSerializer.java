package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.serialized.SerializedNetworkTable;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.ConditionalTableFactory;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.RootNodeTableFactory;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactory;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

public class NetworkTableSerializer {

  // TODO - SORT
  public List<SerializedNetworkTable> serializeAllTables(BayesianNetworkData networkData) {
    return null;
    //    return networkData.getNodes().stream()
    //        .map(node -> networkData.getNetworkTablesMap().get(node))
    //        .map(this::serialize)
    //        .toList();
  }

  public void deSerializeNetworkTables(
      SerializedBayesianNetwork serializedBayesianNetwork, SerializationData serializationData) {
    //    Map<Node, NetworkTable> tableMap =
    // serializationData.getNetworkData().getNetworkTablesMap();
    //    serializedBayesianNetwork.serializedCPTs().stream()
    //        .map(serializedTable -> deSerialize(serializedTable, serializationData))
    //        .forEach(networkTable -> tableMap.put(networkTable.getNetworkNode(), networkTable));
  }

  private SerializedNetworkTable serialize(NetworkTable table) {
    return new SerializedNetworkTable(
        table.getNetworkNode().getId(),
        SerializerUtils.serializeNodes(table.getConditions()),
        table.getTableName(),
        serializeProbabilities(table));
  }

  private List<Double> serializeProbabilities(NetworkTable table) {
    return Arrays.stream(table.getProbabilities()).boxed().toList();
  }

  private NetworkTable deSerialize(SerializedNetworkTable serialized, SerializationData data) {
    Node event = data.getNodeIdMap().get(serialized.networkNodeId());
    List<Node> conditions = nodeDeSerialize(serialized.conditionNodeIds(), data);
    NetworkTable table = selectBuilder(conditions).buildTable(List.of(event), conditions);
    deSerializeProbabilities(table, serialized);
    return table;
  }

  private List<Node> nodeDeSerialize(List<Serializable> ids, SerializationData data) {
    return SerializerUtils.deSerializeNodes(ids, ArrayList::new, data);
  }

  private static TableFactory<? extends NetworkTable> selectBuilder(List<Node> conditionNodes) {
    return conditionNodes.isEmpty() ? new RootNodeTableFactory() : new ConditionalTableFactory();
  }

  private void deSerializeProbabilities(NetworkTable table, SerializedNetworkTable serialized) {
    List<Double> list = serialized.probabilities();
    double[] array = table.getProbabilities();
    IntStream.range(0, array.length).forEach(i -> array[i] = list.get(i));
  }
}
