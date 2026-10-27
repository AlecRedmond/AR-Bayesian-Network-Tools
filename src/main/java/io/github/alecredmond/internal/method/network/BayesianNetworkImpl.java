package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.internal.application.network.NetworkErrorPolicy;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintHandler;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import io.github.alecredmond.internal.method.solver.BayesSolverImpl;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BayesianNetworkImpl implements BayesianNetwork {
  private final BayesianNetworkData networkData;
  private final NetworkErrorPolicy policy = new NetworkErrorPolicy();
  private final NetworkConstraintHandler networkConstraintHandler;

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetworkImpl(String networkName) {
    this.networkData = new BayesianNetworkData();
    this.networkData.setNetworkName(networkName);
    this.networkData.setBayesianNetwork(this);
    this.networkConstraintHandler = new NetworkConstraintHandler(networkData);
  }

  public BayesianNetworkImpl() {
    this.networkData = new BayesianNetworkData();
    this.networkData.setBayesianNetwork(this);
    this.networkConstraintHandler = new NetworkConstraintHandler(networkData);
  }

  public BayesianNetworkImpl(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.networkData.setBayesianNetwork(this);
    this.networkConstraintHandler = new NetworkConstraintHandler(networkData);
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
  public <T extends Serializable> Node getNodeById(T nodeID) {
    return networkData.getNodeIDsMap().get(nodeID);
  }

  public <T extends Serializable> Set<Node> getNodesByIds(Collection<T> nodeIDs) {
    return nodeIDs.stream()
        .distinct()
        .map(this::getNodeById)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public Node getNodeByName(String name) {
    return networkData.getNodeNameMap().get(name);
  }

  @Override
  public Set<Node> getNodesByName(Collection<String> nodeNames) {
    return nodeNames.stream()
        .map(this::getNodeByName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public Set<Node> getAllNodes() {
    return new LinkedHashSet<>(networkData.getNodes());
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

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  @Override
  public BayesianNetwork solveNetwork() {
    new BayesSolverImpl(networkData).solve();
    return this;
  }

  public boolean isSolved() {
    return networkData.isSolved();
  }

  public BayesianNetworkImpl printNetwork() {
    if (!networkData.isSolved()) solveNetwork();
    new NetworkPrinter(networkData).printNetwork();
    return this;
  }

  public <T extends Serializable> NetworkTable getNetworkTableById(T nodeID) {
    return getNetworkTables().get(getNodeById(nodeID));
  }

  public Map<Node, NetworkTable> getNetworkTables() {
    return Map.copyOf(networkData.getNetworkTablesMap());
  }

  public InferenceEngine buildInferenceEngine() {
    return InferenceEngine.create(this);
  }

  public MonteCarloSampler buildSampler() {
    return new LikelihoodWeightingSampler(this);
  }
}
