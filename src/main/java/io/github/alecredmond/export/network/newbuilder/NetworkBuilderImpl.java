package io.github.alecredmond.export.network.newbuilder;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.network.cptdefinition.CptDefinition;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.application.constraint.ConstraintFactoryOutput;
import io.github.alecredmond.internal.method.constraints.ConstraintFactory;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.method.network.validator.ValidatorType;
import io.github.alecredmond.internal.method.node.NodeComparator;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.NetworkTableFactory;
import io.github.alecredmond.internal.method.solver.BayesSolverImpl;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NetworkBuilderImpl implements NetworkBuilder {
  private final BayesianNetworkData networkData;
  private final NetworkTableFactory tableBuilder;
  private final ConstraintFactory constraintFactory;

  public NetworkBuilderImpl() {
    this.networkData = new BayesianNetworkData();
    this.tableBuilder = new NetworkTableFactory();
    this.constraintFactory = new ConstraintFactory(networkData);
  }

  @Override
  public NetworkBuilder setNetworkName(String networkName) {
    networkData.setNetworkName(networkName);
    return this;
  }

  @Override
  public BayesianNetwork build() {
    if (!networkData.isSolved()) buildSolver().solve();
    return new BayesianNetworkImpl(networkData);
  }

  @Override
  public BayesSolver buildSolver() {
    clearData();
    setChildren();
    validateData();
    buildNameAndIdMaps();
    buildNetworkTablesMap(orderNodes());
    return new BayesSolverImpl(networkData);
  }

  @Override
  public NetworkBuilder setNodes(Node... nodes) {
    setNodesCommon(Arrays.stream(nodes));
    return this;
  }

  private void setNodesCommon(Stream<Node> nodeStream) {
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
    if (factoryOutput.optException() != null) throw factoryOutput.optException();
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

  private void clearData() {
    networkData.getNodeIDsMap().clear();
    networkData.getNodeStateIDsMap().clear();
    networkData.getNetworkTablesMap().clear();
    networkData.getConstraints().clear();
    networkData.getNodeNameMap().clear();
    networkData.setSolved(false);
  }

  private void setChildren() {
    for (Node node : networkData.getNodes()) {
      for (Node parent : node.getParents()) {
        parent.getChildren().add(node);
      }
    }
  }

  private void validateData() {
    Arrays.stream(ValidatorType.values())
        .map(ValidatorType::getValidatorSupplier)
        .map(Supplier::get)
        .forEach(networkValidator -> networkValidator.validateData(networkData));
  }

  private void buildNameAndIdMaps() {
    Map<Serializable, Node> nodeIdMap = networkData.getNodeIDsMap();
    Map<Serializable, NodeState> stateIdMap = networkData.getNodeStateIDsMap();
    Map<String, Node> nodeNameMap = networkData.getNodeNameMap();

    for (Node node : networkData.getNodes()) {
      nodeIdMap.put(node.getId(), node);
      nodeNameMap.put(node.getName(), node);
      for (NodeState state : node.getStates()) {
        stateIdMap.put(state.getId(), state);
      }
    }
  }

  private void buildNetworkTablesMap(NodeComparator comparator) {
    networkData.getNetworkTablesMap().clear();
    networkData
        .getNodes()
        .forEach(
            node -> {
              List<Node> events = List.of(node);
              List<Node> conditions = node.getParents().stream().sorted(comparator).toList();
              NetworkTable table = tableBuilder.buildTable(events, conditions);
              table.normalizeTable();
              networkData.getNetworkTablesMap().put(node, table);
              node.setCpt(table);
            });
  }

  private NodeComparator orderNodes() {
    NodeComparator comparator = new NodeComparator(networkData);
    List<Node> nodes = networkData.getNodes();
    nodes.clear();
    nodes.addAll(comparator.getOrderedNodes());
    return comparator;
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
