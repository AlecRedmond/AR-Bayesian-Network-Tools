package io.github.alecredmond.internal.method.sampler.samplestore;

import io.github.alecredmond.export.node.NodeState;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractSampleStore<N extends Number> implements SampleStore {
  protected final NodeState[][] nodeStates;
  protected final int length;
  protected final N[] numberOfStates;
  protected final N[] strideLengths;
  protected final Map<N, Double> weights;

  protected AbstractSampleStore(NodeState[][] nodeStates, N[] numberOfStates, N[] strideLengths) {
    this.nodeStates = nodeStates;
    this.length = nodeStates.length;
    this.numberOfStates = numberOfStates;
    this.strideLengths = strideLengths;
    this.weights = new LinkedHashMap<>();
  }

  @Override
  public void storeSample(int[] statePositions, double weight) {
    N virtualIndex = convertToVirtualIndex(statePositions);
    weights.merge(virtualIndex, weight, Double::sum);
  }

  protected abstract N convertToVirtualIndex(int[] statePositions);

  @Override
  public Map<NodeState[], Double> getWeightedSamples() {
    Map<NodeState[], Double> map = new LinkedHashMap<>();
    weights.forEach(
        (virtualIndex, weight) -> map.put(convertFromVirtualIndex(virtualIndex), weight));
    return map;
  }

  protected abstract NodeState[] convertFromVirtualIndex(N virtualIndex);
}
