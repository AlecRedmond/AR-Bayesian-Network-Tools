package io.github.alecredmond.networkscenario.templates;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.newbuilder.NetworkBuilder;
import io.github.alecredmond.export.network.newbuilder.NetworkBuilderImpl;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.newnode.DefaultNode;
import io.github.alecredmond.networkscenario.ConstraintData;
import io.github.alecredmond.networkscenario.NetworkScenario;
import java.util.*;

public abstract class AbstractNetworkScenario implements NetworkScenario {
  protected final Set<ConstraintData> constraintData = new LinkedHashSet<>();
  protected final Map<String, Node> nodeNameMap = new HashMap<>();

  public NodeState state(Node node, String stateName) {
    return node.getStateByName(stateName);
  }

  protected Node defaultNode(String name, List<String> stateNames, Node... parents) {
    return new DefaultNode(name, stateNames, Arrays.asList(parents));
  }

  protected final void addConstraint(
      NodeState event, Collection<NodeState> conditions, double probability) {
    addConstraint(List.of(event), conditions, probability);
  }

  protected final void addConstraint(
      Collection<NodeState> events, Collection<NodeState> conditions, double probability) {
    constraintData.add(new ConstraintData(events, conditions, probability));
  }

  protected final void addConstraint(NodeState event, double probability) {
    addConstraint(List.of(event), probability);
  }

  protected final void addConstraint(List<NodeState> events, double probability) {
    addConstraint(events, List.of(), probability);
  }

  protected final void addNodesToNameMap(Node... nodes) {
    for (Node node : nodes) {
      nodeNameMap.put(node.getName(), node);
    }
  }

  @Override
  public BayesianNetwork buildNetwork() {
    addNodesToNameMap(definedNodes());
    return getBuilder().build();
  }

  @Override
  public NetworkBuilder getBuilder() {
    NetworkBuilder nb =
        new NetworkBuilderImpl().setNetworkName(getNetworkName()).setNodes(definedNodes());
    constraintData.forEach(cd -> nb.addConstraint(cd.events(), cd.conditions(), cd.probability()));
    return nb;
  }

  protected abstract String getNetworkName();

  protected abstract Node[] definedNodes();

  @Override
  public Node getNodeByName(String name) {
    return nodeNameMap.get(name);
  }

  @Override
  public NodeState getStateByName(String nodeName, String stateName) {
    return getNodeByName(nodeName).getStateByName(stateName);
  }

  @Override
  public Set<ConstraintData> getConstraintData() {
    return constraintData;
  }
}
