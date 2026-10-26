package io.github.alecredmond.export.network.newbuilder;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.network.cptdefinition.CptDefinition;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.constraint.ConstraintFactoryOutput;
import io.github.alecredmond.internal.method.constraints.ConstraintFactory;
import io.github.alecredmond.internal.method.network.BaseNetworkFactory;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.NetworkTableFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;
import java.util.*;
import java.util.stream.Stream;

public class NetworkBuilderImpl extends BaseNetworkFactory {
  private final ConstraintFactory constraintFactory;

  public NetworkBuilderImpl() {
    super(new BayesianNetworkData(), new NetworkTableFactory());
    this.constraintFactory = new ConstraintFactory(networkData);
  }

  @Override
  public NetworkBuilder setNetworkName(String networkName) {
    networkData.setNetworkName(networkName);
    return this;
  }

  @Override
  public NetworkBuilder setNodes(Node... nodes) {
    setNodesCommon(Arrays.stream(nodes));
    return this;
  }

  private void setNodesCommon(Stream<Node> nodeStream) {
    networkData.getNodeIDsMap().clear();
    networkData.getNodeStateIDsMap().clear();
    networkData.getNetworkTablesMap().clear();
    networkData.getConstraints().clear();
    networkData.setSolved(false);
    List<Node> nodes = networkData.getNodes();
    nodes.clear();
    nodeStream.forEach(
        node -> {
          node.setChildren(new ArrayList<>());
          nodes.add(node);
        });
  }

  @Override
  public NetworkBuilder setNodes(Collection<Node> nodes) {
    setNodesCommon(nodes.stream());
    return this;
  }

  @Override
  public NetworkBuilder defineCPT(CptDefinition cptDefinition) {
    new ConstraintBuilderIterator(cptDefinition)
        .buildConstraints().stream()
            .map(constraintFactory::verifyConstraint)
            .forEach(this::addConstraintOrThrow);
    return this;
  }

  private void addConstraintOrThrow(ConstraintFactoryOutput factoryOutput) {
    if (factoryOutput.optException() != null) {
      throw factoryOutput.optException();
    }
    networkData.getConstraints().add(factoryOutput.validated().getConstraint());
  }

  @Override
  public NetworkBuilder addConstraint(NodeState eventState, double probability) {
    addConstraintCommon(Set.of(eventState), Set.of(), probability);
    return this;
  }

  private void addConstraintCommon(
      Set<NodeState> eventStates, Set<NodeState> conditionStates, double probability) {
    addConstraintOrThrow(
        constraintFactory.buildConstraint(eventStates, conditionStates, probability));
  }

  @Override
  public NetworkBuilder addConstraint(
      NodeState eventState, NodeState conditionState, double probability) {
    addConstraintCommon(Set.of(eventState), Set.of(conditionState), probability);
    return this;
  }

  @Override
  public NetworkBuilder addConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    addConstraintCommon(Set.of(eventState), new HashSet<>(conditionStates), probability);
    return this;
  }

  @Override
  public NetworkBuilder addConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    addConstraintCommon(new HashSet<>(eventStates), new HashSet<>(conditionStates), probability);
    return this;
  }

  @Override
  public NetworkBuilder addConstraint(ProbabilityConstraint probabilityConstraint) {
    addConstraintOrThrow(constraintFactory.verifyConstraint(probabilityConstraint));
    return this;
  }

  @Override
  public NetworkBuilder addConstraints(Collection<ProbabilityConstraint> probabilityConstraints) {
    probabilityConstraints.forEach(this::addConstraint);
    return this;
  }
}
