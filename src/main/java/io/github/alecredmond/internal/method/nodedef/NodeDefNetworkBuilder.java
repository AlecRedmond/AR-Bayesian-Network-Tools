package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.nodedef.NodeDefinition;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import java.util.Collection;

public class NodeDefNetworkBuilder {
  private final DiscreteNodeDefMapper discreteMapper = new DiscreteNodeDefMapper();

  public <T extends NodeDefinition> BayesianNetwork createNetwork(
      Collection<T> nodeDefs, String networkName) {
    BayesianNetwork network = new BayesianNetworkImpl(networkName);
    nodeDefs.forEach(def -> discreteMapper.createNode(network, def));
    nodeDefs.forEach(def -> discreteMapper.addParents(network, def));
    return network.buildNetworkData();
  }

}
