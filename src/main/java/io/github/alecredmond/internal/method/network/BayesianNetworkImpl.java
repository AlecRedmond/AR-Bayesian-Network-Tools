package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.direct.JunctionTreeAlgorithm;
import io.github.alecredmond.export.inference.indirect.LikelihoodWeightingSampler;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialization.NodeSerializer;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode
@ToString
public class BayesianNetworkImpl implements BayesianNetwork {
  private final BayesianNetworkData networkData;
  @EqualsAndHashCode.Exclude @ToString.Exclude private final BayesNetIdConverter converter;

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetworkImpl(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.converter = new BayesNetIdConverter(this);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FILE IO---------------------------------------------
  // ----------------------------------------------------------------------------------------------

  @Override
  public boolean saveNetworkToFile(File file, NodeSerializer<?>... additionalSerializers) {
    return new NetworkFileIO().saveNetwork(serializeNetwork(additionalSerializers), file);
  }

  @Override
  public boolean saveNetworkToFile(String filePath, NodeSerializer<?>... additionalSerializers) {
    return new NetworkFileIO().saveNetwork(serializeNetwork(additionalSerializers), filePath);
  }

  @Override
  public SerializedBayesianNetwork serializeNetwork(NodeSerializer<?>... additionalSerializers) {
    return new BayesianNetworkSerializer(additionalSerializers).serializeNetwork(this);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE/STATE GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  @Override
  public <T extends Serializable> Integer getNodeOrdinalById(T nodeId) {
    return networkData.getNodeIndexFromId().get(nodeId);
  }

  @Override
  public Integer getNodeOrdinalByName(String name) {
    return networkData.getNodeIndexFromName().get(name);
  }

  @Override
  public Integer getNodeOrdinal(Node node) {
    return getNodeOrdinalById(node.getId());
  }

  @Override
  public <T extends Serializable> Node getNodeById(T nodeID) {
    return converter.accessByNodeId(nodeID, networkData.getNodes());
  }

  @Override
  public <T extends Serializable> Set<Node> getNodesByIds(Collection<T> nodeIDs) {
    return converter.collectByNodeIds(nodeIDs, networkData.getNodes(), LinkedHashSet::new);
  }

  @Override
  public Node getNodeByName(String name) {
    return converter.accessByNodeName(name, networkData.getNodes());
  }

  @Override
  public Set<Node> getNodesByName(Collection<String> nodeNames) {
    return converter.collectByNodeName(nodeNames, networkData.getNodes(), LinkedHashSet::new);
  }

  @Override
  public Node[] getNodes() {
    Node[] nodes = networkData.getNodes();
    return Arrays.copyOf(nodes, nodes.length);
  }

  @Override
  public Stream<Node> streamNodes() {
    return Arrays.stream(networkData.getNodes());
  }

  @Override
  public <T extends Collection<Node>, U extends T> U getNodes(Supplier<U> collectionSupplier) {
    return streamNodes().collect(Collectors.toCollection(collectionSupplier));
  }

  @Override
  public <E extends Serializable> NodeState getNodeStateById(E nodeStateID) {
    return networkData.getNodeStateIDsMap().get(nodeStateID);
  }

  @Override
  public <E extends Serializable> Set<NodeState> getNodeStatesById(Collection<E> nodeStateIDs) {
    return nodeStateIDs.stream()
        .distinct()
        .map(this::getNodeStateById)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public Set<ProbabilityConstraint> getConstraints() {
    return new LinkedHashSet<>(networkData.getConstraints());
  }

  @Override
  public String getNetworkName() {
    return networkData.getNetworkName();
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetworkImpl printNetwork() {
    new NetworkPrinter(this).printNetwork();
    return this;
  }

  @Override
  public <T extends Serializable> NetworkTable getNetworkTableByNodeId(T nodeID) {
    return converter.accessByNodeId(nodeID, networkData.getNetworkTables());
  }

  @Override
  public NetworkTable getNetworkTableByNodeName(String nodeName) {
    return converter.accessByNodeName(nodeName, networkData.getNetworkTables());
  }

  @Override
  public NetworkTable getNetworkTable(Node node) {
    return converter.accessByNode(node, networkData.getNetworkTables());
  }

  @Override
  public NetworkTable[] getNetworkTables() {
    NetworkTable[] tables = networkData.getNetworkTables();
    return Arrays.copyOf(tables, tables.length);
  }

  @Override
  public <T extends Collection<NetworkTable>, U extends T> T getNetworkTables(
      Supplier<U> collectionSupplier) {
    return Arrays.stream(networkData.getNetworkTables())
        .collect(Collectors.toCollection(collectionSupplier));
  }

  public Map<Node, NetworkTable> getNetworkTablesMap() {
    return Arrays.stream(networkData.getNetworkTables())
        .map(table -> Map.entry(table.getNetworkNode(), table))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
  }

  public InferenceEngine buildInferenceEngine() {
    return new JunctionTreeAlgorithm(this);
  }

  public MonteCarloSampler buildSampler() {
    return new LikelihoodWeightingSampler(this);
  }
}
