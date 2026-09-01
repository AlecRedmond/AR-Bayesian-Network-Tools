package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import java.util.Optional;

public class NodeLinkedToNetworkValidator implements NetworkValidator {

  @Override
  public void validateData(BayesianNetworkData networkData) {
    BayesianNetwork bayesianNetwork = networkData.getBayesianNetwork();
    networkData.getNodeIDsMap().values().stream()
        .filter(node -> networkNotMapped(node, bayesianNetwork))
        .forEach(node -> node.setNetwork(bayesianNetwork));
  }

  private static boolean networkNotMapped(Node node, BayesianNetwork bayesianNetwork) {
    return Optional.ofNullable(node.getNetwork())
        .map(bayesianNetwork::equals)
        .map(b -> !b)
        .orElse(true);
  }
}
