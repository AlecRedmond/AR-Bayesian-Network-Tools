package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.sampler.Sample;
import io.github.alecredmond.internal.application.sampler.SampleCollectionData;
import java.util.*;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class SampleBuilder {

  public SampleCollectionImpl build(
      int numberOfSamples,
      Map<SampleImpl, Integer> sampleMap,
      Map<Node, NodeObservation> observations,
      Node[] nodeArray,
      BayesianNetworkData networkData) {

    if (sampleMap.isEmpty()) numberOfSamples = 0;
    else setCounts(sampleMap);

    SampleCollectionData collectionData =
        SampleCollectionData.builder()
            .totalSamples(numberOfSamples)
            .samples(radixSort(sampleMap.keySet(), observations, nodeArray))
            .networkObservations(Collections.unmodifiableMap(observations))
            .nodes(nodeArray)
            .build();

    return new SampleCollectionImpl(collectionData, networkData);
  }

  private void setCounts(Map<SampleImpl, Integer> sampleMap) {
    sampleMap.forEach((sample, integer) -> sample.getSampleData().setCount(integer));
  }

  private <T extends Sample> List<Sample> radixSort(
      Collection<T> samples, Map<Node, NodeObservation> observations, Node[] nodeArray) {
    try {
      return samples.stream()
          .map(Sample.class::cast)
          .sorted(radixComparator(nodeArray, observations))
          .toList();
    } catch (SampleValidationException e) {
      log.warn("{}, USING RANDOM ORDER...", e.getMessage());
      return samples.stream().map(Sample.class::cast).toList();
    }
  }

  private Comparator<Sample> radixComparator(
      Node[] nodeArray, Map<Node, NodeObservation> observations) {
    return IntStream.range(0, nodeArray.length)
        .filter(i -> validObservationForSorting(nodeArray[i], observations))
        .mapToObj(SampleBuilder::compareStatesByPosition)
        .reduce(Comparator::thenComparing)
        .orElseThrow(() -> new SampleValidationException("SAMPLES COULD NOT BE ORDERED"));
  }

  private boolean validObservationForSorting(Node node, Map<Node, NodeObservation> observations) {
    return switch (observations.get(node).status()) {
      case UNOBSERVED, PARTIALLY_OBSERVED -> true;
      case OBSERVED -> false;
      case NEGATED ->
          throw new IllegalStateException("Negated Observation found for node %s".formatted(node));
    };
  }

  private static Comparator<Sample> compareStatesByPosition(int nodeIndex) {
    return Comparator.comparing(s -> s.getAllStates()[nodeIndex].getPosition());
  }
}
