package io.github.alecredmond.internal.application.sampler;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.util.Map;
import lombok.Data;

@Data
public class SamplePickerFactoryData {
  private final NodeObservation nodeObservation;
  private final NetworkTable networkTable;
  private final Map<Node, Integer> nodeArrayPosition;
  private final ProbabilityVector vector;
  private final Node node;
  private final int[] strideLengths;
  private final double[] cptProbabilities;
  private final int[] conditionIndexesInSampleArray;
  private final int eventNodeIndexInSampleArray;

  private int[] eventCptSteps = null;
  private NodeState[] eventStates = null;
  private double[] sampleWeighting = null;

  public SamplePickerFactoryData(
      NodeObservation nodeObservation,
      NetworkTable networkTable,
      Map<Node, Integer> nodeArrayPosition) {
    this.nodeObservation = nodeObservation;
    this.networkTable = networkTable;
    this.nodeArrayPosition = nodeArrayPosition;
    this.vector = networkTable.getVector();
    this.node = networkTable.getNetworkNode();
    this.cptProbabilities = vector.getProbabilities();
    this.strideLengths = vector.getStrideLengths();
    this.conditionIndexesInSampleArray =
        networkTable.getConditions().stream().mapToInt(nodeArrayPosition::get).sorted().toArray();
    this.eventNodeIndexInSampleArray = nodeArrayPosition.get(node);
  }
}
