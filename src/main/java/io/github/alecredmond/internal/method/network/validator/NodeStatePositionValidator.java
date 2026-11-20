package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import java.util.Arrays;
import java.util.List;

public class NodeStatePositionValidator implements NetworkValidator {
  @Override
  public void validateData(BayesianNetworkData networkData) {
    Arrays.stream(networkData.getNodes()).forEach(this::checkPositionsCorrect);
  }

  private void checkPositionsCorrect(Node node) {
    List<NodeState> states = node.getStates();
    if (states.isEmpty()) {
      throw new NetworkStructureException("Node with no states = " + node);
    }
    int position = 0;
    for (NodeState state : states) {
      if (state.getPosition() == position++) continue;
      throw new NetworkStructureException(
          "NodeState %s was in position %d, expected position %d"
              .formatted(state, state.getPosition(), --position));
    }
  }
}
