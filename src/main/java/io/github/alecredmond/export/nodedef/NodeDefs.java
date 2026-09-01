package io.github.alecredmond.export.nodedef;

import io.github.alecredmond.export.nodedef.base.DiscreteNodeDefImpl;
import io.github.alecredmond.export.nodedef.base.StateDefImpl;
import java.util.Collection;

public class NodeDefs {
  private NodeDefs() {}

  public static DiscreteNodeDef create(String nodeName, Collection<String> stateNames) {
    DiscreteNodeDefImpl nodeDef = new DiscreteNodeDefImpl();
    nodeDef.setName(nodeName);
    nodeDef.setStateDefs(stateNames.stream().map(name -> createNewState(name, nodeDef)).toList());
    return nodeDef;
  }

  private static StateDef createNewState(String name, DiscreteNodeDef discreteNodeDef) {
    StateDefImpl stateDef = new StateDefImpl(discreteNodeDef);
    stateDef.setName(name);
    return stateDef;
  }
}
