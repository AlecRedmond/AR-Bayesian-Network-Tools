package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.observable.ObservationStatus;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.SampleCollection;
import io.github.alecredmond.internal.application.sampler.LikelihoodWeightingSamplerData;
import io.github.alecredmond.internal.method.sampler.picker.SamplePicker;
import io.github.alecredmond.internal.method.sampler.picker.SamplePickerFactory;
import io.github.alecredmond.internal.method.sampler.samplestore.SampleStore;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import io.github.alecredmond.internal.method.utils.WeightedAllocator;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LikelihoodWeightingSampler extends MonteCarloSamplerImpl {
  private final LikelihoodWeightingSamplerData samplerData;
  private final SampleStore sampleStore;

  public LikelihoodWeightingSampler(BayesianNetwork network) {
    super(network);
    this.samplerData = buildSamplerData();
    this.sampleStore = SampleStore.create(network);
  }

  private LikelihoodWeightingSamplerData buildSamplerData() {
    Node[] nodes = network.getNodes();
    NetworkTable[] tables = network.getNetworkTables();
    return new LikelihoodWeightingSamplerData(nodes, tables);
  }

  @Override
  public SampleCollection generateSamplesInternal(
      Map<Node, NodeObservation> observations, int numberOfSamples) {
    if (numberOfSamples < 0) {
      log.error("Attempted to generate less than zero samples!");
      return null;
    }
    if (anyNegatedObservations(observations)) {
      return null;
    }
    initSamplerData(observations, numberOfSamples);
    generateWeightedStateArrays();
    convertArraysToSamples();
    distributeSamples();
    return new SampleCollectionBuilder()
        .build(
            numberOfSamples,
            samplerData.getDistributedSamples(),
            observations,
            samplerData.getNodes(),
            network);
  }

  private boolean anyNegatedObservations(Map<Node, NodeObservation> observations) {
    List<Node> negated =
        observations.values().stream()
            .filter(obs -> obs.status().equals(ObservationStatus.NEGATED))
            .map(NodeObservation::node)
            .toList();
    if (negated.isEmpty()) return false;
    log.warn("Cannot run sampler, Nodes were negated: {}", CollectionToString.apply(negated));
    return true;
  }

  private void initSamplerData(Map<Node, NodeObservation> observations, int numberOfSamples) {
    samplerData.setObservations(observations);
    samplerData.setNumberOfSamples(numberOfSamples);
    samplerData.setSamplePickers(new SamplePickerFactory().create(samplerData));
    samplerData.setWeightedStateSets(new HashMap<>());
    samplerData.setWeightedSamples(new HashMap<>());
    samplerData.setDistributedSamples(new HashMap<>());
  }

  private void generateWeightedStateArrays() {
    int[] selectedStatePositions = new int[samplerData.getNodes().length];
    SamplePicker[] samplePickers = samplerData.getSamplePickers();
    int numberOfSamples = samplerData.getNumberOfSamples();
    for (int s = 0; s < numberOfSamples; s++) {
      performWeightedRandomWalk(samplePickers, selectedStatePositions);
    }
  }

  private void convertArraysToSamples() {
    Map<SampleImpl, Double> weightedSamples = samplerData.getWeightedSamples();
    Map<NodeState[], Double> weightedStateSets = sampleStore.getWeightedSamples();
    weightedStateSets.forEach((set, weight) -> weightedSamples.put(new SampleImpl(set), weight));
  }

  private void distributeSamples() {
    samplerData.setDistributedSamples(
        WeightedAllocator.allocate(
            samplerData.getWeightedSamples(), samplerData.getNumberOfSamples()));
  }

  private void performWeightedRandomWalk(
      SamplePicker[] samplePickers, int[] selectedStatePositions) {
    double weight = 1.0;
    for (SamplePicker samplePicker : samplePickers) {
      weight *= samplePicker.selectStateAndReturnWeight(selectedStatePositions);
      if (weight <= 0) return;
    }
    sampleStore.storeSample(selectedStatePositions, weight);
  }
}
