package io.github.alecredmond.internal.method.nodedef;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.nodedef.DiscreteNodeDef;
import io.github.alecredmond.export.nodedef.NodeDef;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import java.util.Collection;

public class NodeDefNetworkBuilder {
  private final DiscreteNodeDefMapper discreteMapper = new DiscreteNodeDefMapper();

  public <T extends NodeDef> BayesianNetwork createNetwork(
      Collection<T> nodeDefs, String networkName) {
    BayesianNetwork network = new BayesianNetworkImpl(networkName);
    nodeDefs.forEach(def -> getDefMapper(def).createNode(network, def));
    nodeDefs.forEach(def -> getDefMapper(def).addParents(network, def));
    return network.buildNetworkData();
  }

  @SuppressWarnings("unchecked")
  private <T extends NodeDef> NodeDefMapper<? super T> getDefMapper(T nodeDef) {
    return (NodeDefMapper<? super T>)
        switch (nodeDef) {
          case DiscreteNodeDef ignored -> discreteMapper;
          default -> throw new IllegalStateException("Unexpected value: " + nodeDef);
        };
  }


}
