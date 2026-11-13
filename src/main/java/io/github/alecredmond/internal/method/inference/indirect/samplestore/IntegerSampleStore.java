package io.github.alecredmond.internal.method.inference.indirect.samplestore;

import com.carrotsearch.hppc.IntDoubleHashMap;
import com.carrotsearch.hppc.IntDoubleMap;
import com.carrotsearch.hppc.procedures.IntDoubleProcedure;
import io.github.alecredmond.export.node.NodeState;
import java.util.LinkedHashMap;
import java.util.Map;

public class IntegerSampleStore implements SampleStore {
  private final int[] strideLengths;
  private final IntDoubleMap weightMap;
  private final NodeState[][] nodeStates;
  private final int numberOfNodes;

  public IntegerSampleStore(NodeState[][] nodeStates, int[] strideLengths) {
    this.nodeStates = nodeStates;
    this.numberOfNodes = nodeStates.length;
    this.strideLengths = strideLengths;
    this.weightMap = new IntDoubleHashMap();
  }

  @Override
  public void clear() {
    weightMap.clear();
  }

  @Override
  public void storeSample(int[] statePositions, double weight) {
    int virtualIndex = convertToVirtualIndex(statePositions);
    weightMap.addTo(virtualIndex, weight);
  }

  @Override
  public Map<NodeState[], Double> returnWeightsAndClear() {
    Map<NodeState[], Double> map = new LinkedHashMap<>();
    IntDoubleProcedure procedure =
        (virtualIndex, weight) -> map.put(convertFromVirtualIndex(virtualIndex), weight);
    weightMap.forEach(procedure);
    weightMap.clear();
    return map;
  }

  protected NodeState[] convertFromVirtualIndex(int virtualIndex) {
    int remainder = virtualIndex;
    NodeState[] states = new NodeState[numberOfNodes];
    for (int i = 0; i < numberOfNodes; i++) {
      int stride = strideLengths[i];
      int stateIdx = remainder / stride;
      states[i] = nodeStates[i][stateIdx];
      remainder %= stride;
    }
    return states;
  }

  private int convertToVirtualIndex(int[] statePositions) {
    int idx = 0;
    for (int i = 0; i < numberOfNodes; i++) {
      idx += strideLengths[i] * statePositions[i];
    }
    return idx;
  }
}
