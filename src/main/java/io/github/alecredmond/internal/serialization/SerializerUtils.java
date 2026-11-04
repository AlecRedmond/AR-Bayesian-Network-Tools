package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SerializerUtils {
  private SerializerUtils() {}

  public static <T extends Collection<NodeState>, R extends T> T deSerializeNodeStates(
      Collection<Serializable> stateIds, Supplier<R> collectionSupplier, SerializationData data) {
    return deSerializeCollection(stateIds, data.getNodeStateIdMap()::get, collectionSupplier);
  }

  public static <T extends Collection<S>, R extends T, S> T deSerializeCollection(
      Collection<Serializable> toDeserialize,
      Function<Serializable, S> deSerializationFunction,
      Supplier<R> collectionSupplier) {
    return toDeserialize.stream()
        .map(deSerializationFunction)
        .collect(Collectors.toCollection(collectionSupplier));
  }

  public static List<Serializable> serializeNodeStates(Collection<NodeState> states) {
    return states.stream().map(NodeState::getId).toList();
  }
}
