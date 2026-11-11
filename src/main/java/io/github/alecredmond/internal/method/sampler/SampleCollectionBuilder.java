package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.sampler.Sample;
import io.github.alecredmond.export.sampler.SampleCollection;
import io.github.alecredmond.internal.application.sampler.SampleCollectionData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class SampleCollectionBuilder {

  public SampleCollection build(
      int numberOfSamples,
      List<Sample> samples,
      Map<Node, NodeObservation> observations,
      BayesianNetwork network) {
    Node[] nodeArray = network.getNodes();
    if (samples.isEmpty()) numberOfSamples = 0;

    SampleCollectionData collectionData =
        SampleCollectionData.builder()
            .totalSamples(numberOfSamples)
            .samples(radixSort(samples, observations, nodeArray))
            .networkObservations(Collections.unmodifiableMap(observations))
            .nodes(nodeArray)
            .nodeIndexMap(NodeUtils.buildNodeIndexMap(nodeArray))
            .build();

    return new SampleCollectionImpl(collectionData, network);
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
        .mapToObj(SampleCollectionBuilder::compareStatesByPosition)
        .reduce(Comparator::thenComparing)
        .orElseThrow(() -> new SampleValidationException("SAMPLES COULD NOT BE ORDERED"));
  }

  private boolean validObservationForSorting(Node node, Map<Node, NodeObservation> observations) {
    return switch (observations.get(node).status()) {
      case UNOBSERVED, PARTIALLY_OBSERVED -> true;
      case OBSERVED -> false;
      case NEGATED -> throw new IllegalStateException("Negated Observation found for node " + node);
    };
  }

  private static Comparator<Sample> compareStatesByPosition(int nodeIndex) {
    return Comparator.comparing(s -> s.getAllStates()[nodeIndex].getPosition());
  }
}
