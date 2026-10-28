package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.network.cptdefinition.CptDefinition;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.constraints.ConstraintFactory;
import io.github.alecredmond.internal.method.network.validator.ValidatorType;
import io.github.alecredmond.internal.method.node.NodeComparator;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.NetworkTableFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesianNetworkBuilderImpl implements BayesianNetworkBuilder {
  private final Set<ProbabilityConstraint> constraints = new LinkedHashSet<>();
  private ConstraintFactory constraintFactory;
  private Node[] nodes;
  private String networkName = "UNNAMED NETWORK";

  @Override
  public BayesianNetworkBuilder setNetworkName(String networkName) {
    this.networkName = networkName;
    return this;
  }

  @Override
  public Optional<BayesianNetwork> build(BayesSolver solver) {
    setNodeChildren();
    BayesianNetworkData networkData = new BayesianNetworkData(networkName, nodes, constraints);
    validateData(networkData);
    NodeComparator comparator = orderNodes(networkData);
    buildArraysAndMaps(networkData, comparator);
    return solver.solveNetwork(new BayesianNetworkImpl(networkData));
  }

  private void setNodeChildren() {
    for (Node node : nodes) {
      if (node.getChildren() == null) {
        node.setChildren(new ArrayList<>());
      }
      for (Node parent : node.getParents()) {
        if (parent.getChildren() == null) {
          parent.setChildren(new ArrayList<>());
        }
        parent.getChildren().add(node);
      }
    }
  }

  private void validateData(BayesianNetworkData networkData) {
    Arrays.stream(ValidatorType.values())
        .map(ValidatorType::getValidatorSupplier)
        .map(Supplier::get)
        .forEach(networkValidator -> networkValidator.validateData(networkData));
  }

  private NodeComparator orderNodes(BayesianNetworkData networkData) {
    NodeComparator comparator = new NodeComparator(networkData);
    Arrays.sort(networkData.getNodes(), comparator);
    return comparator;
  }

  private void buildArraysAndMaps(BayesianNetworkData networkData, NodeComparator comparator) {
    Node[] nodeArray = networkData.getNodes();
    NodeState[][] states = networkData.getStates();
    NetworkTable[] networkTables = networkData.getNetworkTables();
    Map<Serializable, Integer> nodeIndexFromId = networkData.getNodeIndexFromId();
    Map<String, Integer> nodeIndexFromName = networkData.getNodeIndexFromName();
    Map<Serializable, NodeState> nodeStateIDsMap = networkData.getNodeStateIDsMap();
    NetworkTableFactory tableFactory = new NetworkTableFactory();

    final int bound = nodeArray.length;
    for (int i = 0; i < bound; i++) {
      Node node = nodeArray[i];
      nodeIndexFromId.put(node.getId(), i);
      nodeIndexFromName.put(node.getName(), i);
      states[i] = buildStateArrayAndAddToIdMap(node, nodeStateIDsMap);
      networkTables[i] = buildNetworkTable(node, tableFactory, comparator);
    }
  }

  private NodeState[] buildStateArrayAndAddToIdMap(
      Node node, Map<Serializable, NodeState> nodeStateIDsMap) {
    return node.getStates().stream()
        .map(state -> nodeStateIDsMap.put(state.getId(), state))
        .toArray(NodeState[]::new);
  }

  private NetworkTable buildNetworkTable(
      Node node, NetworkTableFactory tableFactory, NodeComparator comparator) {
    List<Node> events = List.of(node);
    List<Node> conditions = node.getParents().stream().sorted(comparator).toList();
    NetworkTable table = tableFactory.buildTable(events, conditions);
    table.normalizeTable();
    node.setCpt(table);
    return table;
  }

  private void addConstraintCommon(Supplier<ProbabilityConstraint> supplier) {
    try {
      getConstraints().add(supplier.get());
    } catch (ConstraintValidationException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public Set<ProbabilityConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public void clearConstraints() {
    constraints.clear();
  }

  @Override
  public BayesianNetworkBuilder setNodes(Node... nodes) {
    this.nodes = nodes;
    Set<ProbabilityConstraint> carriedOver = new LinkedHashSet<>(constraints);
    constraints.clear();
    this.constraintFactory = new ConstraintFactory(nodes, constraints);
    addConstraints(carriedOver);
    return this;
  }

  @Override
  public BayesianNetworkBuilder setNodes(Collection<Node> nodes) {
    setNodes(nodes.toArray(Node[]::new));
    return this;
  }

  @Override
  public BayesianNetworkBuilder defineCPT(CptDefinition cptDefinition) {
    new ConstraintBuilderIterator(cptDefinition)
        .buildConstraints().stream()
            .map(constraintFactory::validateConstraint)
            .forEach(constraints::add);
    return this;
  }

  @Override
  public BayesianNetworkBuilder addConstraint(NodeState eventState, double probability) {
    Set<NodeState> eSet = Set.of(eventState);
    Set<NodeState> cSet = Set.of();
    addConstraintCommon(() -> constraintFactory.buildConstraint(eSet, cSet, probability));
    return this;
  }

  @Override
  public BayesianNetworkBuilder addConstraint(
      NodeState eventState, NodeState conditionState, double probability) {
    Set<NodeState> eSet = Set.of(eventState);
    Set<NodeState> cSet = Set.of(conditionState);
    addConstraintCommon(() -> constraintFactory.buildConstraint(eSet, cSet, probability));
    return this;
  }

  @Override
  public BayesianNetworkBuilder addConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability) {
    Set<NodeState> eSet = Set.of(eventState);
    Set<NodeState> cSet = new HashSet<>(conditionStates);
    addConstraintCommon(() -> constraintFactory.buildConstraint(eSet, cSet, probability));
    return this;
  }

  @Override
  public BayesianNetworkBuilder addConstraint(
      Collection<NodeState> eventStates,
      Collection<NodeState> conditionStates,
      double probability) {
    Set<NodeState> eSet = new HashSet<>(eventStates);
    Set<NodeState> cSet = new HashSet<>(conditionStates);
    addConstraintCommon(() -> constraintFactory.buildConstraint(eSet, cSet, probability));
    return this;
  }

  @Override
  public BayesianNetworkBuilder addConstraint(ProbabilityConstraint probabilityConstraint) {
    addConstraintCommon(() -> constraintFactory.validateConstraint(probabilityConstraint));
    return this;
  }

  @Override
  public BayesianNetworkBuilder addConstraints(
      Collection<ProbabilityConstraint> probabilityConstraints) {
    probabilityConstraints.forEach(this::addConstraint);
    return this;
  }
}
