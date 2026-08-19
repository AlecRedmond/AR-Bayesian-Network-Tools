package io.github.alecredmond.internal.method.sampler;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.Sample;
import io.github.alecredmond.export.sampler.SampleCollection;
import io.github.alecredmond.internal.application.sampler.SampleCollectionData;
import io.github.alecredmond.internal.method.network.NetworkDataUtils;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleCollectionImpl implements SampleCollection {
  private final SampleCollectionData collectionData;
  private final BayesianNetworkData networkData;

  public SampleCollectionImpl(
      SampleCollectionData collectionData, BayesianNetworkData networkData) {
    this.collectionData = collectionData;
    this.networkData = networkData;
  }

  public Map<Node, NodeObservation> getObservations() {
    return collectionData.getNetworkObservations();
  }

  public Node[] getNodes() {
    return collectionData.getNodes();
  }

  public boolean isEmpty() {
    return countSamples() == 0;
  }

  public int countSamples() {
    return collectionData.getTotalSamples();
  }

  @Override
  public String toString() {
    return getSamples().stream().map(Sample::toString).collect(Collectors.joining("\n"));
  }

  public List<Sample> getSamples() {
    return collectionData.getSamples();
  }

  public <T extends Serializable> void setDisplayedNodesById(Collection<T> nodeIds) {
    Set<Node> nodes = NetworkDataUtils.getNodesByID(nodeIds, networkData);
    setDisplayedNodes(nodes);
  }

  public <T extends Serializable> void setDisplayedNodesById(T nodeId) {
    setDisplayedNodesById(List.of(nodeId));
  }

  public void setDisplayedNodes(Collection<Node> nodes) {
    SampleUtils.applyToSamples(this, s -> s.setDisplayedNodes(nodes));
  }

  public void setDisplayedNodes(Node node) {
    setDisplayedNodes(List.of(node));
  }

  public void displayAllNodes() {
    SampleUtils.applyToSamples(this, Sample::displayAllNodes);
  }

  public <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds) {
    return countSamplesIncludingStates(NetworkDataUtils.getStatesByID(stateIds, networkData));
  }

  public <T extends Serializable> int countSamplesIncludingStateIds(T stateId) {
    return countSamplesIncludingStateIds(List.of(stateId));
  }

  public int countSamplesIncludingStates(Collection<NodeState> states) {
    if (states.isEmpty()) {
      return countSamples();
    }
    return SampleUtils.countSamplesIncludingStates(this, states);
  }

  public int countSamplesIncludingStates(NodeState state) {
    return countSamplesIncludingStates(List.of(state));
  }

  public <T extends Serializable> List<Sample> getSamplesIncludingStatesById(
      Collection<T> includedStateIds) {
    return SampleUtils.listSamplesIncludingStates(
        this, NetworkDataUtils.getStatesByID(includedStateIds, networkData));
  }

  public <T extends Serializable> List<Sample> getSamplesIncludingStatesById(T stateId) {
    return getSamplesIncludingStatesById(List.of(stateId));
  }

  public List<Sample> getSamplesIncludingStates(Collection<NodeState> includedStates) {
    return SampleUtils.listSamplesIncludingStates(this, includedStates);
  }

  public List<Sample> getSamplesIncludingStates(NodeState state) {
    return getSamplesIncludingStates(List.of(state));
  }
}
