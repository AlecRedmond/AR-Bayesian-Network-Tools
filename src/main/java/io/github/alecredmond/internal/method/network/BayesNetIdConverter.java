package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BayesNetIdConverter {
  private final BayesianNetwork network;

  public BayesNetIdConverter(BayesianNetwork network) {
    this.network = network;
  }

  public <R> R accessByNode(Node node, R[] array) {
    return accessByNodeId(node.getId(), array);
  }

  public <T extends Serializable, R> R accessByNodeId(T nodeId, R[] array) {
    return accessByNodeOrdinal(network::getNodeOrdinalById, nodeId, array);
  }

  private <T, R> R accessByNodeOrdinal(Function<T, Integer> getOrdinal, T input, R[] array) {
    Integer ordinal = getOrdinal.apply(input);
    if (ordinal == null) return null;
    return array[ordinal];
  }

  public <R> R accessByNodeName(String nodeName, R[] array) {
    return accessByNodeOrdinal(network::getNodeOrdinalByName, nodeName, array);
  }

  public <R, U extends Collection<R>, V extends U> U collectByNodeName(
      Collection<String> nodeNames, R[] array, Supplier<V> collector) {
    return toCollection(network::getNodeOrdinalByName, nodeNames, array, collector);
  }

  private <R, T, U extends Collection<R>, V extends U> U toCollection(
      Function<T, Integer> ordinalFunc, Collection<T> test, R[] array, Supplier<V> collector) {
    return test.stream()
        .distinct()
        .map(t -> accessByNodeOrdinal(ordinalFunc, t, array))
        .collect(Collectors.toCollection(collector));
  }

  public <R, T extends Serializable, U extends Collection<R>, V extends U> U collectByNodeIds(
      Collection<T> nodeIds, R[] array, Supplier<V> collector) {
    return toCollection(network::getNodeOrdinalById, nodeIds, array, collector);
  }
}
