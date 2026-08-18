package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

public class NodeStatePositionValidator implements NetworkValidator {
  @Override
  public void validateData(BayesianNetworkData networkData) {
    networkData.getNodeIDsMap().values().parallelStream().forEach(this::checkPositionsCorrect);
  }

  private void checkPositionsCorrect(Node node) {
    int position = 0;
    for (NodeState state : node.getNodeStates()) {
      if (state.getPosition() == position++) continue;
      throw new IllegalStateException(
          "NodeState %s was in position %d, expected position %d"
              .formatted(state, state.getPosition(), --position));
    }
  }
}
