package io.github.alecredmond.internal.method.sampler.samplestore;

import com.carrotsearch.hppc.ObjectDoubleHashMap;
import com.carrotsearch.hppc.ObjectDoubleMap;
import com.carrotsearch.hppc.procedures.ObjectDoubleProcedure;
import io.github.alecredmond.export.node.NodeState;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

public class BigIntegerSampleStore implements SampleStore {
  private final BigInteger[] strideLengths;
  private final ObjectDoubleMap<BigInteger> weightMap;
  private final NodeState[][] nodeStates;
  private final int numberOfNodes;

  protected BigIntegerSampleStore(NodeState[][] nodeStates, BigInteger[] strideLengths) {
    this.nodeStates = nodeStates;
    this.numberOfNodes = nodeStates.length;
    this.strideLengths = strideLengths;
    this.weightMap = new ObjectDoubleHashMap<>();
  }

  @Override
  public void clear() {
    weightMap.clear();
  }

  @Override
  public void storeSample(int[] statePositions, double weight) {
    BigInteger virtualIndex = convertToVirtualIndex(statePositions);
    weightMap.addTo(virtualIndex, weight);
  }

  @Override
  public Map<NodeState[], Double> returnWeightsAndClear() {
    Map<NodeState[], Double> map = new LinkedHashMap<>();
    ObjectDoubleProcedure<BigInteger> procedure =
        (virtualIndex, weight) -> map.put(convertFromVirtualIndex(virtualIndex), weight);
    weightMap.forEach(procedure);
    weightMap.clear();
    return map;
  }

  protected NodeState[] convertFromVirtualIndex(BigInteger virtualIndex) {
    BigInteger remainder = virtualIndex;
    NodeState[] states = new NodeState[numberOfNodes];
    for (int i = 0; i < numberOfNodes; i++) {
      BigInteger stride = strideLengths[i];
      BigInteger stateIdx = remainder.divide(stride);
      states[i] = nodeStates[i][stateIdx.intValue()];
      remainder = remainder.mod(stride);
    }
    return states;
  }

  protected BigInteger convertToVirtualIndex(int[] statePositions) {
    BigInteger idx = BigInteger.ZERO;
    for (int i = 0; i < numberOfNodes; i++) {
      BigInteger delta = BigInteger.valueOf(statePositions[i]).multiply(strideLengths[i]);
      idx = idx.add(delta);
    }
    return idx;
  }
}
