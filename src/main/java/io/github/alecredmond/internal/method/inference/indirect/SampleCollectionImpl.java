package io.github.alecredmond.internal.method.inference.indirect;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.indirect.Sample;
import io.github.alecredmond.export.inference.indirect.SampleCollection;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.inference.indirect.SampleCollectionData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.io.Serializable;
import java.util.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SampleCollectionImpl implements SampleCollection {
  private final SampleCollectionData collectionData;
  private final BayesianNetwork network;

  public SampleCollectionImpl(SampleCollectionData collectionData, BayesianNetwork network) {
    this.collectionData = collectionData;
    this.network = network;
  }

  public Map<Node, NodeObservation> getObservations() {
    return collectionData.getNetworkObservations();
  }

  public Node[] getNodes() {
    return collectionData.getNodes();
  }

  @Delegate
  private List<Sample> getSamples() {
    return collectionData.getSamples();
  }

  public int countSamples() {
    return collectionData.getTotalSamples();
  }

  public <T extends Serializable> void setDisplayedNodesById(Collection<T> nodeIds) {
    Set<Node> nodes = network.getNodesByIds(nodeIds);
    setDisplayedNodes(nodes);
  }

  public void setDisplayedNodes(Collection<Node> nodes) {
    for (Sample sample : this) {
      sample.setDisplayedNodes(nodes);
    }
  }

  public void displayAllNodes() {
    for (Sample sample : this) {
      sample.displayAllNodes();
    }
  }

  public <T extends Serializable> int countSamplesIncludingStateIds(Collection<T> stateIds) {
    return countSamplesIncludingStates(network.getNodeStatesById(stateIds));
  }

  public int countSamplesIncludingStates(Collection<NodeState> states) {
    if (states.isEmpty()) {
      return countSamples();
    }
    return getSamplesIncludingStates(states).stream().mapToInt(Sample::count).sum();
  }

  private static boolean checkContains(Sample sample, IntObjectMap<Set<NodeState>> setsPerIndex) {
    NodeState[] states = sample.getAllStates();
    for (IntObjectCursor<Set<NodeState>> entry : setsPerIndex) {
      if (!entry.value.contains(states[entry.key])) return false;
    }
    return true;
  }

  public <T extends Serializable> List<Sample> getSamplesIncludingStatesById(
      Collection<T> includedStateIds) {
    return getSamplesIncludingStates(network.getNodeStatesById(includedStateIds));
  }

  public List<Sample> getSamplesIncludingStates(Collection<NodeState> includedStates) {
    if (includedStates.isEmpty()) return new ArrayList<>(this);
    List<Sample> samples = new ArrayList<>();
    IntObjectMap<Set<NodeState>> idxRequest =
        NodeUtils.generateIndexMap(includedStates, collectionData.getNodeIndexMap());
    for (Sample sample : this) {
      if (checkContains(sample, idxRequest)) samples.add(sample);
    }
    return samples;
  }
}
