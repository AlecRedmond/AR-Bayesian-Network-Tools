package io.github.alecredmond.export.serializer;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import java.io.Serializable;
import java.util.List;

public interface NodeSerializer<T extends Node> {

  boolean canSerialize(Node node);

  boolean canDeSerialize(SerializedNode serializedNode);

  Serializable serializerId();

  default SerializedNode serialize(Node node, double[] cptProbabilities) {
    return serializeGeneric(safeCastNode(node), cptProbabilities);
  }

  SerializedNode serializeGeneric(T node, double[] cptProbabilities);

  T safeCastNode(Node node);

  T deSerialize(SerializedNode serializedNode, List<Node> parents);
}
