package io.github.alecredmond.internal.method.network.prevalidationlogic;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.nodedef.NodeDefinition;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintHandler;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import java.util.Optional;

public class CptConstraintAssigner implements PreValidationLogic {

  @Override
  public void runLogic(BayesianNetworkImpl bayesianNetwork) {
    NetworkConstraintHandler handler = bayesianNetwork.getNetworkConstraintHandler();
    for (Node node : bayesianNetwork.getNodes()) {
      Optional.ofNullable(node.getNodeDef())
          .map(NodeDefinition::getAttachedCpt)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .ifPresent(
              cptDef -> {
                handler.removeCptConstraints(node);
                handler.addConstraints(cptDef.supplyConstraints());
              });
    }
  }
}
