package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.method.sampler.SampleImpl;
import java.util.Map;
import java.util.Set;

import io.github.alecredmond.internal.method.sampler.picker.SamplePicker;
import lombok.Data;

@Data
public class LikelihoodWeightingSamplerData {
  private final Node[] nodes;
  private final NetworkTable[] tables;
  private Map<Node, NodeObservation> observations;
  private SamplePicker[] samplePickers;
  private int numberOfSamples;
  private Map<Set<NodeState>, Double> weightedStateSets;
  private Map<SampleImpl, Double> weightedSamples;
  private Map<SampleImpl, Integer> distributedSamples;

  public LikelihoodWeightingSamplerData(Node[] nodes, NetworkTable[] tables) {
    this.nodes = nodes;
    this.tables = tables;
  }
}
