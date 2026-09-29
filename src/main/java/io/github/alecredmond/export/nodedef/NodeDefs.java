package io.github.alecredmond.export.nodedef;

import io.github.alecredmond.export.nodedef.base.NodeDefImpl;
import io.github.alecredmond.export.nodedef.base.StateDefImpl;

import java.util.Collection;

public class NodeDefs {
  private NodeDefs() {}

  public static NodeDefinition discrete(String nodeName, Collection<String> stateNames) {
    NodeDefImpl nodeDef = new NodeDefImpl();
    nodeDef.setName(nodeName);
    nodeDef.setStateDefs(stateNames.stream().map(name -> createNewState(name, nodeDef)).toList());
    return nodeDef;
  }

  private static StateDef createNewState(String name, NodeDefinition nodeDef) {
    StateDefImpl stateDef = new StateDefImpl(nodeDef);
    stateDef.setName(name);
    return stateDef;
  }
}
