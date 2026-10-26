package io.github.alecredmond.networkscenario.templates;

import io.github.alecredmond.export.network.cptdefinition.*;
import io.github.alecredmond.export.network.newbuilder.NetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class CptDefinedScenario extends AbstractNetworkScenario {
  protected final Map<Serializable, double[]> probabilitiesMap = new HashMap<>();
  protected final Map<Node, CptDefinition> cptDefinitionMap = new HashMap<>();

  @Override
  public NetworkBuilder preBuiltScenario() {
    NetworkBuilder nb = super.preBuiltScenario();
    cptDefinitionMap.values().forEach(nb::defineCPT);
    return nb;
  }
  

  protected void addCpt(Node node, double... probabilities) {
    probabilitiesMap.put(node.getId(), probabilities);
    cptDefinitionMap.put(node, commonCptDef(node));
  }

  protected CptDefinition commonCptDef(Node node) {
    return new OrderedCptDefinition(
        node, probabilitiesMap.get(node.getId()), getCptNodeOrder(), getCptStrideOrder());
  }

  protected CptNodeOrder getCptNodeOrder() {
    return CptNodeOrder.CONDITIONS_BEFORE_EVENT;
  }

  protected CptStrideOrder getCptStrideOrder() {
    return CptStrideOrder.DESCENDING_STRIDE_LENGTH;
  }

  protected void addHalfCpt(BinaryNode node, double... probabilities) {
    probabilitiesMap.put(node.getId(), probabilities);
    cptDefinitionMap.put(node, halfCptDef(node));
  }

  protected CptDefinition halfCptDef(BinaryNode binaryNode) {
    return new HalfCptDefinition(
        binaryNode,
        probabilitiesMap.get(binaryNode.getId()),
        getCptNodeOrder(),
        getCptStrideOrder());
  }
}
