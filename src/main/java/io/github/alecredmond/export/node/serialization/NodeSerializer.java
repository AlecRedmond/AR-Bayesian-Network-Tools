package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.serialization.NodeSerializers;
import java.io.Serializable;
import java.util.List;

public interface NodeSerializer<T extends Node> {

  static void register(NodeSerializer<?>... serializers) {
    NodeSerializers.getInstance().registerSerializers(serializers);
  }

  default SerializedNode serialize(Node node, double[] cptProbabilities) {
    return serializeGeneric(safeCastNode(node), cptProbabilities);
  }

  SerializedNode serializeGeneric(T node, double[] cptProbabilities);

  T safeCastNode(Node node);

  Class<T> nodeClass();

  Serializable serializerId();

  T deSerialize(SerializedNode serializedNode, List<Node> parents);
}
