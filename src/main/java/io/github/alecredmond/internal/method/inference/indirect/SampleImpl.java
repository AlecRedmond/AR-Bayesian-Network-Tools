package io.github.alecredmond.internal.method.inference.indirect;

import io.github.alecredmond.export.inference.indirect.Sample;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.inference.indirect.SampleData;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;
import java.util.function.Supplier;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SampleImpl implements Sample {
  private final SampleData sampleData;

  public SampleImpl(NodeState[] states, int count) {
    sampleData = new SampleData(states);
    sampleData.setCount(count);
  }

  public int count() {
    return sampleData.getCount();
  }

  public NodeState[] getAllStates() {
    return sampleData.getRawStateArray();
  }

  public NodeState[] getDisplayedStates() {
    return sampleData.getExportStateArray();
  }

  public <T extends Collection<NodeState>, S extends T> T getDisplayedStates(Supplier<S> supplier) {
    return SampleUtils.stateArrayToCollection(sampleData.getExportStateArray(), supplier);
  }

  @Override
  public String toString() {
    return "%s : %d"
        .formatted(CollectionToString.apply(sampleData.getRawStateSet()), sampleData.getCount());
  }

  public boolean containsAll(Collection<NodeState> states) {
    return sampleData.getRawStateSet().containsAll(states);
  }

  public void displayAllNodes() {
    sampleData.setExportStateArray(
        Arrays.stream(sampleData.getRawStateArray()).toArray(NodeState[]::new));
  }

  public void setDisplayedNodes(Collection<Node> nodes) {
    if (nodes.isEmpty()) {
      displayAllNodes();
      return;
    }
    Set<Node> nodeSet = new HashSet<>(nodes);
    sampleData.setExportStateArray(
        Arrays.stream(sampleData.getRawStateArray())
            .filter(nodeState -> nodeSet.contains(nodeState.getNode()))
            .toArray(NodeState[]::new));
  }
}
