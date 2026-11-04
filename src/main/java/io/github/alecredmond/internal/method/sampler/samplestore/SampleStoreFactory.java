package io.github.alecredmond.internal.method.sampler.samplestore;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils.TreewidthValidator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class SampleStoreFactory {

  public SampleStore buildSampleStore(BayesianNetwork network) {
    List<Node> nodes = network.getNodes(ArrayList::new);
    NodeState[][] nodeStates =
        nodes.stream()
            .map(node -> node.getStates().toArray(NodeState[]::new))
            .toArray(NodeState[][]::new);

    return TreewidthValidator.validateIndexableByLong(nodes)
        ? createLongSampleStore(nodeStates)
        : createBigIntegerSampleStore(nodeStates);
  }

  private LongSampleStore createLongSampleStore(NodeState[][] nodeStates) {
    Long[] numberOfStates = new Long[nodeStates.length];
    Long[] strideLengths = new Long[nodeStates.length];
    long strideLength = 1;
    for (int i = nodeStates.length - 1; i >= 0; i--) {
      long states = nodeStates[i].length;
      numberOfStates[i] = states;
      strideLengths[i] = strideLength;
      strideLength *= states;
    }
    return new LongSampleStore(nodeStates, numberOfStates, strideLengths);
  }

  private BigIntegerSampleStore createBigIntegerSampleStore(NodeState[][] nodeStates) {
    BigInteger[] numberOfStates = new BigInteger[nodeStates.length];
    BigInteger[] strideLengths = new BigInteger[nodeStates.length];
    BigInteger strideLength = BigInteger.ONE;
    for (int i = nodeStates.length - 1; i >= 0; i--) {
      BigInteger states = BigInteger.valueOf(nodeStates[i].length);
      numberOfStates[i] = states;
      strideLengths[i] = strideLength;
      strideLength = strideLength.multiply(states);
    }
    return new BigIntegerSampleStore(nodeStates, numberOfStates, strideLengths);
  }
}
