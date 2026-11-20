package io.github.alecredmond.internal.method.inference.indirect.samplestore;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.TreewidthValidator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SampleStoreFactory {
  private static final double LOG_INT_MAX = Math.log(Integer.MAX_VALUE);
  private static final double LOG_LONG_MAX = Math.log(Long.MAX_VALUE);

  public SampleStore buildSampleStore(BayesianNetwork network) {
    List<Node> nodes = network.getNodes(ArrayList::new);
    NodeState[][] nodeStates =
        nodes.stream()
            .map(node -> node.getStates().toArray(NodeState[]::new))
            .toArray(NodeState[][]::new);

    double logCardinality = TreewidthValidator.getLogCardinality(nodes);
    if (logCardinality < LOG_INT_MAX) return createIntSampleStore(nodeStates);
    if (logCardinality < LOG_LONG_MAX) return createLongSampleStore(nodeStates);
    return createBigIntegerSampleStore(nodeStates);
  }

  private IntegerSampleStore createIntSampleStore(NodeState[][] nodeStates) {
    int[] strideLengths = new int[nodeStates.length];
    int strideLength = 1;
    for (int i = nodeStates.length - 1; i >= 0; i--) {
      int states = nodeStates[i].length;
      strideLengths[i] = strideLength;
      strideLength *= states;
    }
    return new IntegerSampleStore(nodeStates, strideLengths);
  }

  private LongSampleStore createLongSampleStore(NodeState[][] nodeStates) {
    long[] strideLengths = new long[nodeStates.length];
    long strideLength = 1;
    for (int i = nodeStates.length - 1; i >= 0; i--) {
      long states = nodeStates[i].length;
      strideLengths[i] = strideLength;
      strideLength *= states;
    }
    return new LongSampleStore(nodeStates, strideLengths);
  }

  private BigIntegerSampleStore createBigIntegerSampleStore(NodeState[][] nodeStates) {
    BigInteger[] strideLengths = new BigInteger[nodeStates.length];
    BigInteger strideLength = BigInteger.ONE;
    for (int i = nodeStates.length - 1; i >= 0; i--) {
      BigInteger states = BigInteger.valueOf(nodeStates[i].length);
      strideLengths[i] = strideLength;
      strideLength = strideLength.multiply(states);
    }
    return new BigIntegerSampleStore(nodeStates, strideLengths);
  }
}
