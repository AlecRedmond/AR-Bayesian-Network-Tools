package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.internal.application.network.NetworkErrorPolicy;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintHandler;
import io.github.alecredmond.internal.method.network.changehandlers.NetworkPropertyChangeEvent;
import io.github.alecredmond.internal.method.network.validator.NetworkIdValidator;
import io.github.alecredmond.internal.method.printer.NetworkPrinter;
import io.github.alecredmond.internal.method.sampler.LikelihoodWeightingSampler;
import io.github.alecredmond.internal.serialization.BayesianNetworkSerializer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class BayesianNetworkImpl implements BayesianNetwork, PropertyChangeListener {
  private final BayesianNetworkData networkData;
  private final NetworkErrorPolicy policy = new NetworkErrorPolicy();
  private final NetworkConstraintHandler networkConstraintHandler;

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRUCTORS------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetworkImpl(String networkName) {
    this.networkData = new BayesianNetworkData();
    networkData.setNetworkName(networkName);
    this.networkConstraintHandler = new NetworkConstraintHandler(networkData);
  }

  public BayesianNetworkImpl() {
    this.networkData = new BayesianNetworkData();
    this.networkConstraintHandler = new NetworkConstraintHandler(networkData);
  }

  public BayesianNetworkImpl(BayesianNetworkData networkData) {
    this.networkData = networkData;
    this.networkConstraintHandler = new NetworkConstraintHandler(networkData);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------LISTENERS---------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    NetworkPropertyChangeEvent.valueOf(evt.getPropertyName())
        .getSupplier()
        .get()
        .applyChange(evt, networkData);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FILE IO---------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public boolean saveNetworkToFile(File file) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this, file);
  }

  public boolean saveNetworkToFile(String filePath) {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this, filePath);
  }

  public boolean saveNetworkToFile() {
    return new NetworkFileIO(new BayesianNetworkSerializer()).saveNetwork(this);
  }

  public SerializedBayesianNetwork serializeNetwork() {
    return new BayesianNetworkSerializer().serialize(this);
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE IN/OUT-------------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetwork addNode(Node node) throws BayesNetIDException {
    new NetworkIdValidator().validateNewNode(node, networkData);
    node.addPropertyChangeListener(this);
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl addNewNode(T nodeID)
      throws BayesNetIDException {
    addNode(new Node(nodeID));
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addNewNode(
      T nodeID, Collection<E> nodeStateIDs) throws BayesNetIDException {
    addNode(new Node(nodeID, nodeStateIDs));
    return this;
  }

  public boolean removeNode(Node node) {
    if (node == null) {
      return false;
    }
    return removeNodeByID(node.getId());
  }

  public <T extends Serializable> boolean removeNodeByID(T nodeID) {
    if (nodeID == null) {
      return false;
    }
    Node node = getNode(nodeID);
    if (node == null) {
      return false;
    }
    node.removePropertyChangeListener(this);
    return true;
  }

  public boolean removeAllNodes() {
    Set<Serializable> ids = new HashSet<>(networkData.getNodeIDsMap().keySet());
    ids.forEach(this::removeNodeByID);
    return !ids.isEmpty();
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NODE/STATE GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public <T extends Serializable> Node getNode(T nodeID) {
    return NetworkDataUtils.getNodeById(nodeID, networkData);
  }

  public <T extends Serializable> Set<Node> getNodes(Collection<T> nodeIDs) {
    return NetworkDataUtils.getNodesByID(nodeIDs, networkData);
  }

  public Set<Node> getNodes() {
    if (networkData.isSolved()) {
      return Collections.unmodifiableSet(new LinkedHashSet<>(networkData.getNodes()));
    }
    return Set.copyOf(networkData.getNodeIDsMap().values());
  }

  public <E extends Serializable> NodeState getNodeState(E nodeStateID) {
    return NetworkDataUtils.getStateById(nodeStateID, networkData);
  }

  public <E extends Serializable> Set<NodeState> getNodeStates(Collection<E> nodeStateIDs) {
    return nodeStateIDs.stream()
        .map(id -> Optional.ofNullable(getNodeState(id)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  // ----------------------------------------------------------------------------------------------
  // ---------------------------------NODE PARENT/CHILD RELATIONS----------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetwork addParents(Node child, Collection<Node> parents)
      throws NetworkStructureException {
    child.setParents(Stream.concat(child.getParents().stream(), parents.stream()).toList());
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addParents(
      T childID, Collection<E> parentIDs) {
    addParents(getNode(childID), getNodes(parentIDs));
    return this;
  }

  public BayesianNetwork addParents(Node child, Node parent) throws NetworkStructureException {
    child.addParent(parent);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addParents(
      T childID, E parentID) throws NetworkStructureException {
    getNode(childID).addParent(getNode(parentID));
    return this;
  }

  public BayesianNetwork removeParent(Node child, Node parent) {
    child.removeParent(parent);
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl removeParent(
      T childID, E parentID) {
    removeParent(getNode(childID), getNode(parentID));
    return this;
  }

  public BayesianNetwork removeParents(Node child) {
    child.setParents(List.of());
    return this;
  }

  public <T extends Serializable> BayesianNetworkImpl removeParents(T childID) {
    getNode(childID).setParents(List.of());
    return this;
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT ADDERS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addConstraint(
      Collection<T> eventStateIDs, Collection<E> conditionStateIDs, double probability) {
    networkData.setSolved(false);
    networkConstraintHandler
        .addConstraints(
            NetworkDataUtils.getStatesByIdOrThrow(eventStateIDs, networkData),
            NetworkDataUtils.getStatesByIdOrThrow(conditionStateIDs, networkData),
            probability)
        .ifPresent(policy.getConstraintValidationExceptionPolicy());
    return this;
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, Collection<E> conditionStateIDs, double probability) {
    return addConstraint(List.of(eventStateID), conditionStateIDs, probability);
  }

  public <T extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, double probability) {
    return addConstraint(List.of(eventStateID), List.of(), probability);
  }

  public <T extends Serializable, E extends Serializable> BayesianNetworkImpl addConstraint(
      T eventStateID, E conditionStateId, double probability) {
    return addConstraint(List.of(eventStateID), List.of(conditionStateId), probability);
  }

  public BayesianNetwork addConstraint(ProbabilityConstraint probabilityConstraint) {
    networkData.setSolved(false);
    networkConstraintHandler
        .addConstraint(probabilityConstraint)
        .ifPresent(policy.getConstraintValidationExceptionPolicy());
    return this;
  }

  public BayesianNetwork addConstraints(Collection<ProbabilityConstraint> probabilityConstraints) {
    networkData.setSolved(false);
    networkConstraintHandler
        .addConstraints(probabilityConstraints)
        .forEach(policy.getConstraintValidationExceptionPolicy());
    return this;
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT GETTERS------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public <T extends Serializable> MarginalConstraint getConstraint(@NonNull T eventStateId) {
    ProbabilityConstraint constraint = getConstraint(List.of(eventStateId), List.of());
    if (constraint instanceof MarginalConstraint mc) return mc;
    return null;
  }

  public <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      @NonNull T eventStateId, @NonNull Collection<E> conditionStateIds) {
    return getConstraint(List.of(eventStateId), conditionStateIds);
  }

  public <T extends Serializable, E extends Serializable> ProbabilityConstraint getConstraint(
      @NonNull Collection<T> eventStateIds, @NonNull Collection<E> conditionStateIds) {
    return networkConstraintHandler.getConstraint(
        getNodeStates(eventStateIds), getNodeStates(conditionStateIds));
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT REMOVERS-----------------------------------------
  // ----------------------------------------------------------------------------------------------

  public boolean removeConstraint(ProbabilityConstraint probabilityConstraint) {
    networkData.setSolved(false);
    return networkConstraintHandler.removeConstraint(probabilityConstraint);
  }

  public <T extends Serializable> boolean removeConstraint(T eventStateId) {
    return removeConstraint(List.of(eventStateId), List.of());
  }

  public <T extends Serializable, E extends Serializable> boolean removeConstraint(
      T eventStateId, Collection<E> conditionStateIds) {
    return removeConstraint(List.of(eventStateId), conditionStateIds);
  }

  public <T extends Serializable, E extends Serializable> boolean removeConstraint(
      Collection<T> eventStateIds, Collection<E> conditionStateIds) {
    networkData.setSolved(false);
    return networkConstraintHandler.removeConstraint(
        getNodeStates(eventStateIds), getNodeStates(conditionStateIds));
  }

  public boolean removeAllConstraints() {
    networkData.setSolved(false);
    return networkConstraintHandler.removeAllConstraints();
  }

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------NETWORK FUNCTIONS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  public BayesianNetworkImpl solveNetwork() {
    if (networkData.isSolved()) {
      return this;
    }
    BayesSolver.create(this).solve();
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

  public BayesianNetwork buildNetworkData() {
    new NetworkDataBuilder(networkData).build();
    return this;
  }

  public <T extends Serializable> NetworkTable getNetworkTable(T nodeID) {
    return networkData.getNetworkTableById(nodeID);
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

  public void resetAllData() {
    removeAllNodes();
    NetworkDataUtils.resetAll(networkData);
  }
}
