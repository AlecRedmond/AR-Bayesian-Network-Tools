package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.inference.JunctionTreeAlgorithm;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BayesianNetworkImpl implements BayesianNetwork {
  @Getter private final BayesianNetworkData networkData;

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetworkImpl(BayesianNetworkData networkData) {
    this.networkData = networkData;
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FILE IO---------------------------------------------
  // ----------------------------------------------------------------------------------------------

  @Override
  public boolean saveNetworkToFile(File file) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this, file);
  }

  @Override
  public boolean saveNetworkToFile(String filePath) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this, filePath);
  }

  @Override
  public boolean saveNetworkToFile() {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this);
  }

  @Override
  public SerializedBayesianNetwork serializeNetwork() {
    return new BayesianNetworkSerializer().serialize(this);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE/STATE GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  @Override
  public <T extends Serializable> int getNodeOrdinalById(T nodeId) {
    return networkData.getNodeIndexFromId().get(nodeId);
  }

  @Override
  public int getNodeOrdinalByName(String name) {
    return networkData.getNodeIndexFromName().get(name);
  }

  @Override
  public int getNodeOrdinal(Node node) {
    return getNodeOrdinalById(node.getId());
  }

  @Override
  public <T extends Serializable> Node getNodeById(T nodeID) {
    return networkData.getNodes()[getNodeOrdinalById(nodeID)];
  }

  @Override
  public <T extends Serializable> Set<Node> getNodesByIds(Collection<T> nodeIDs) {
    return nodeIDs.stream()
        .distinct()
        .map(this::getNodeById)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public Node getNodeByName(String name) {
    return networkData.getNodes()[getNodeOrdinalByName(name)];
  }

  @Override
  public Set<Node> getNodesByName(Collection<String> nodeNames) {
    return nodeNames.stream()
        .map(this::getNodeByName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
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
  public <T extends Serializable> NetworkTable getNetworkTableById(T nodeID) {
    return networkData.getNetworkTables()[getNodeOrdinalById(nodeID)];
  }

  @Override
  public NetworkTable getNetworkTable(Node node) {
    return getNetworkTableById(node.getId());
  }

  @Override
  public NetworkTable[] getAllNetworkTables() {
    NetworkTable[] tables = networkData.getNetworkTables();
    return Arrays.copyOf(tables, tables.length);
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
