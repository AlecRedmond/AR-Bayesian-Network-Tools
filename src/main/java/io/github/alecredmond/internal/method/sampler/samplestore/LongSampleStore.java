package io.github.alecredmond.internal.method.sampler.samplestore;

import io.github.alecredmond.export.node.NodeState;

public class LongSampleStore extends AbstractSampleStore<Long> {

  public LongSampleStore(NodeState[][] nodeStates, Long[] numberOfStates, Long[] strideLengths) {
    super(nodeStates, numberOfStates, strideLengths);
  }

  @Override
  public Long convertToVirtualIndex(int[] statePositions) {
    long idx = 0;
    for (int i = 0; i < length; i++) {
      idx += strideLengths[i] * statePositions[i];
    }
    return idx;
  }

  @Override
  protected NodeState[] convertFromVirtualIndex(Long virtualIndex) {
    long remainder = virtualIndex;
    NodeState[] states = new NodeState[length];
    for (int i = 0; i < length; i++) {
      long stride = strideLengths[i];
      long stateIdx = remainder / stride;
      states[i] = nodeStates[i][(int) stateIdx];
      remainder %= stride;
    }
    return states;
  }
}
