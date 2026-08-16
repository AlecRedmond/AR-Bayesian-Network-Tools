package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.application.sampler.LikelihoodWeightingSamplerData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.sampler.picker.SamplePicker;
import io.github.alecredmond.internal.method.sampler.picker.SamplePickerFactory;
import io.github.alecredmond.internal.method.utils.WeightedAllocator;
import java.util.*;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LikelihoodWeightingSampler extends MonteCarloSamplerImpl {
  private final LikelihoodWeightingSamplerData samplerData;

  public LikelihoodWeightingSampler(BayesianNetwork network) {
    super(network);
    this.samplerData = buildSamplerData();
  }

  private LikelihoodWeightingSamplerData buildSamplerData() {
    BayesianNetworkData networkData = network.getNetworkData();
    Node[] nodes = networkData.getNodes().toArray(Node[]::new);
    NetworkTable[] tables = new NetworkTable[nodes.length];
    Map<Node, NetworkTable> networkTables = networkData.getNetworkTablesMap();
    IntStream.range(0, nodes.length).forEach(i -> tables[i] = networkTables.get(nodes[i]));
    return new LikelihoodWeightingSamplerData(nodes, tables);
  }

  @Override
  public SampleCollectionImpl generateSamplesInternal(
      Map<Node, NodeObservation> observations, int numberOfSamples) {
    if (numberOfSamples < 0) {
      log.error("Attempted to generate less than zero samples!");
      return null;
    }
    if (anyNegatedObservations(observations)) {
      return null;
    }
    initSamplerData(observations, numberOfSamples);
    generateWeightedStateSets();
    convertSetsToSamples();
    distributeSamples();
    return new SampleBuilder()
        .build(
            numberOfSamples,
            samplerData.getDistributedSamples(),
            observations,
            samplerData.getNodes(),
            network.getNetworkData());
  }

  private boolean anyNegatedObservations(Map<Node, NodeObservation> observations) {
    List<Node> negated =
        observations.values().stream()
            .filter(obs -> obs.status().equals(ObservationStatus.NEGATED))
            .map(NodeObservation::node)
            .toList();
    if (negated.isEmpty()) return false;
    log.warn("Cannot run sampler, Nodes were negated: {}", NodeUtils.formatNodesToString(negated));
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

  private void generateWeightedStateSets() {
    NodeState[] defaultSample = new NodeState[samplerData.getNodes().length];
    SamplePicker[] samplePickers = samplerData.getSamplePickers();
    Map<Set<NodeState>, Double> weightedStateSets = samplerData.getWeightedStateSets();

    for (int s = 0; s < samplerData.getNumberOfSamples(); s++) {
      double weight = 1.0;
      for (int i = 0; i < samplePickers.length && weight > 0.0; i++) {
        weight *= samplePickers[i].pickAndReturnWeight(defaultSample, weight);
      }
      addWeightedSet(weightedStateSets, defaultSample, weight);
    }
  }

  private void convertSetsToSamples() {
    Map<SampleImpl, Double> weightedSamples = samplerData.getWeightedSamples();
    Map<Set<NodeState>, Double> weightedStateSets = samplerData.getWeightedStateSets();
    weightedStateSets.forEach(
        (set, weight) ->
            weightedSamples.put(new SampleImpl(set.toArray(NodeState[]::new)), weight));
  }

  private void distributeSamples() {
    samplerData.setDistributedSamples(
        WeightedAllocator.allocate(
            samplerData.getWeightedSamples(), samplerData.getNumberOfSamples()));
  }

  private void addWeightedSet(
      Map<Set<NodeState>, Double> weightedStateSets, NodeState[] sample, double weight) {
    if (weight <= 0.0) return;
    Set<NodeState> set = new LinkedHashSet<>(Arrays.asList(sample));
    weightedStateSets.compute(set, (s, oldVal) -> oldVal == null ? weight : oldVal + weight);
  }
}
