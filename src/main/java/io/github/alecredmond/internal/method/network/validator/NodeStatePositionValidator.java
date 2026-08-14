package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.NodeState;
import java.util.List;
import java.util.stream.IntStream;

public class NodeStatePositionValidator implements NetworkValidator {
  @Override
  public void validateData(BayesianNetworkData networkData) {
    networkData.getNodeIDsMap().values().parallelStream()
        .forEach(
            node -> {
              List<NodeState> states = node.getNodeStates();
              IntStream.range(0, states.size()).forEach(i -> states.get(i).setPosition(i));
            });
  }
}
