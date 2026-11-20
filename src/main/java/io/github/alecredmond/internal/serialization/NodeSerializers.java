package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.serialization.NodeSerializer;
import io.github.alecredmond.export.node.serialization.SerializedNode;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import org.apache.commons.lang3.SerializationException;

public class NodeSerializers {
  private static NodeSerializers instance;

  private final Map<Serializable, NodeSerializer<?>> deserializerMap;
  private final Map<Class<? extends Node>, NodeSerializer<?>> serializerMap;

  private NodeSerializers() {
    this.deserializerMap = new HashMap<>();
    this.serializerMap = new HashMap<>();
    buildSerializerMaps();
  }

  private void buildSerializerMaps() {
    NodeSerializer<?>[] serializers =
        Arrays.stream(DefaultSerializerType.values())
            .map(DefaultSerializerType::getSupplier)
            .map(Supplier::get)
            .toArray(NodeSerializer[]::new);
    registerSerializers(serializers);
  }

  public void registerSerializers(NodeSerializer<?>... serializers) {
    for (NodeSerializer<?> serializer : serializers) {
      Serializable id = serializer.serializerId();
      Class<? extends Node> nodeClass = serializer.nodeClass();
      checkNoIdenticalId(id, serializer);
      checkNoIdenticalClass(nodeClass, serializer);
      serializerMap.put(nodeClass, serializer);
      deserializerMap.put(id, serializer);
    }
  }

  private void checkNoIdenticalId(Serializable id, NodeSerializer<?> serializer) {
    if (!deserializerMap.containsKey(id)) {
      return;
    }
    if (deserializerMap.get(id).equals(serializer)) {
      return;
    }
    throw new IllegalArgumentException("NodeSerializer with id " + id + " already registered!");
  }

  private void checkNoIdenticalClass(
      Class<? extends Node> nodeClass, NodeSerializer<?> serializer) {
    if (!serializerMap.containsKey(nodeClass)) {
      return;
    }
    if (serializerMap.get(nodeClass).equals(serializer)) {
      return;
    }
    throw new IllegalArgumentException(
        "NodeSerializer with class " + nodeClass.getName() + " already registered!");
  }

  public static NodeSerializers getInstance() {
    if (instance == null) {
      instance = new NodeSerializers();
    }
    return instance;
  }

  public SerializedNode serializeFromTable(NetworkTable networkTable) {
    Node node = networkTable.getNetworkNode();
    double[] probabilities = networkTable.getProbabilities();
    return getSerializer(node).serialize(node, Arrays.copyOf(probabilities, probabilities.length));
  }

  private NodeSerializer<?> getSerializer(Node node) {
    return Optional.ofNullable(serializerMap.get(node.getClass()))
        .orElseThrow(
            () ->
                new SerializationException(
                    "Node could not be serialized = "
                        + node
                        + "\nNo serializer declared for type : "
                        + node.getClass().getName()));
  }

  public Node deserialize(SerializedNode serializedNode, List<Node> parents) {
    return getSerializer(serializedNode).deSerialize(serializedNode, parents);
  }

  private NodeSerializer<?> getSerializer(SerializedNode serializedNode) {
    return Optional.ofNullable(deserializerMap.get(serializedNode.serializerId()))
        .orElseThrow(
            () ->
                new SerializationException(
                    "No serializer matching identifier = "
                        + serializedNode.serializerId()
                        + "\nEnsure all serializers are registered before deserializing the network"));
  }
}
