package io.github.alecredmond.internal.method.sampler.samplestore;

import io.github.alecredmond.export.node.NodeState;
import java.math.BigInteger;

public class BigIntegerSampleStore extends AbstractSampleStore<BigInteger> {

  protected BigIntegerSampleStore(
      NodeState[][] nodeStates, BigInteger[] numberOfStates, BigInteger[] strideLengths) {
    super(nodeStates, numberOfStates, strideLengths);
  }

  @Override
  protected BigInteger convertToVirtualIndex(int[] statePositions) {
    BigInteger idx = BigInteger.ZERO;
    for (int i = 0; i < length; i++) {
      BigInteger delta = BigInteger.valueOf(statePositions[i]).multiply(strideLengths[i]);
      idx = idx.add(delta);
    }
    return idx;
  }

  @Override
  protected NodeState[] convertFromVirtualIndex(BigInteger virtualIndex) {
    BigInteger remainder = virtualIndex;
    NodeState[] states = new NodeState[length];
    for (int i = 0; i < length; i++) {
      BigInteger stride = strideLengths[i];
      BigInteger stateIdx = remainder.divide(stride);
      states[i] = nodeStates[i][stateIdx.intValue()];
      remainder = remainder.mod(stride);
    }
    return states;
  }
}
