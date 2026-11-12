package io.github.alecredmond.internal.method.sampler.samplestore;

import com.carrotsearch.hppc.LongDoubleHashMap;
import com.carrotsearch.hppc.LongDoubleMap;
import com.carrotsearch.hppc.procedures.LongDoubleProcedure;
import io.github.alecredmond.export.node.NodeState;
import java.util.LinkedHashMap;
import java.util.Map;

public class LongSampleStore implements SampleStore {
  private final long[] strideLengths;
  private final LongDoubleMap weightMap;
  private final NodeState[][] nodeStates;
  private final int numberOfNodes;

  public LongSampleStore(NodeState[][] nodeStates, long[] strideLengths) {
    this.nodeStates = nodeStates;
    this.numberOfNodes = nodeStates.length;
    this.strideLengths = strideLengths;
    this.weightMap = new LongDoubleHashMap();
  }

  @Override
  public void clear() {
    weightMap.clear();
  }

  @Override
  public void storeSample(int[] statePositions, double weight) {
    long virtualIndex = convertToVirtualIndex(statePositions);
    weightMap.addTo(virtualIndex, weight);
  }

  @Override
  public Map<NodeState[], Double> returnWeightsAndClear() {
    Map<NodeState[], Double> map = new LinkedHashMap<>();
    LongDoubleProcedure procedure =
        (virtualIndex, weight) -> map.put(convertFromVirtualIndex(virtualIndex), weight);
    weightMap.forEach(procedure);
    weightMap.clear();
    return map;
  }

  private NodeState[] convertFromVirtualIndex(long virtualIndex) {
    long remainder = virtualIndex;
    NodeState[] states = new NodeState[numberOfNodes];
    for (int i = 0; i < numberOfNodes; i++) {
      long stride = strideLengths[i];
      long stateIdx = remainder / stride;
      states[i] = nodeStates[i][(int) stateIdx];
      remainder %= stride;
    }
    return states;
  }

  private long convertToVirtualIndex(int[] statePositions) {
    long idx = 0;
    for (int i = 0; i < numberOfNodes; i++) {
      idx += strideLengths[i] * statePositions[i];
    }
    return idx;
  }
}
