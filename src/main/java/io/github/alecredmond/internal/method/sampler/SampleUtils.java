package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.Sample;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SampleUtils {
  private SampleUtils() {}

  public static void applyToSamples(
      SampleCollectionImpl collection, Consumer<Sample> sampleConsumer) {
    collection.getSamples().forEach(sampleConsumer);
  }

  public static int countSamplesIncludingStates(
      SampleCollectionImpl sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).mapToInt(Sample::count).sum();
  }

  private static Stream<Sample> streamAllContaining(
      SampleCollectionImpl sampleCollection, Collection<NodeState> states) {
    Map<Integer, Set<NodeState>> indexRequestMap =
        buildIndexRequestMap(
            NodeUtils.generateMultiRequest(states), sampleCollection.getIndexMap());
    return sampleCollection.getSamples().stream()
        .filter(sample -> containsRequest(sample, indexRequestMap));
  }

  private static Map<Integer, Set<NodeState>> buildIndexRequestMap(
      Map<Node, Set<NodeState>> multirequest, Map<Node, Integer> indexMap) {
    return multirequest.entrySet().stream()
        .map(e -> Map.entry(indexMap.get(e.getKey()), e.getValue()))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
  }

  private static boolean containsRequest(
      Sample sample, Map<Integer, Set<NodeState>> indexRequestMap) {
    NodeState[] states = sample.getAllStates();
    for (Map.Entry<Integer, Set<NodeState>> entry : indexRequestMap.entrySet()) {
      Integer index = entry.getKey();
      Set<NodeState> request = entry.getValue();
      if (!request.contains(states[index])) return false;
    }
    return true;
  }

  public static List<Sample> listSamplesIncludingStates(
      SampleCollectionImpl sampleCollection, Collection<NodeState> states) {
    return streamAllContaining(sampleCollection, states).toList();
  }

  public static <F extends E, E extends Collection<NodeState>> E stateArrayToCollection(
      NodeState[] stateArray, Supplier<F> supplier) {
    return Arrays.stream(stateArray).collect(Collectors.toCollection(supplier));
  }
}
