package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.application.sampler.SamplePickerFactoryData;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class SamplePickerFactory {

  public SamplePicker create(
      NodeObservation nodeObservation,
      NetworkTable networkTable,
      Map<Node, Integer> nodeArrayPosition) {
    SamplePickerFactoryData factoryData =
        new SamplePickerFactoryData(nodeObservation, networkTable, nodeArrayPosition);
    return switch (nodeObservation.status()) {
      case UNOBSERVED -> buildUnobservedSamplePicker(factoryData);
      case OBSERVED -> buildObservedSamplePicker(factoryData);
      case PARTIALLY_OBSERVED -> buildPartiallyObservedSamplePicker(factoryData);
      case NEGATED -> buildNegatedSamplePicker(nodeObservation.node());
    };
  }

  private SamplePicker buildUnobservedSamplePicker(SamplePickerFactoryData factoryData) {
    List<NodeState> states = factoryData.getNode().getNodeStates();

    factoryData.setEventCptSteps(IntStream.range(0, states.size()).toArray());
    factoryData.setEventStates(states.toArray(NodeState[]::new));
    factoryData.setSampleWeighting(new double[states.size()]);

    return new UnobservedSamplePicker(factoryData);
  }

  private SamplePicker buildObservedSamplePicker(SamplePickerFactoryData factoryData) {
    NodeState observed = factoryData.getNodeObservation().observedStates().iterator().next();
    return new ObservedSamplePicker(factoryData, observed);
  }

  private SamplePicker buildPartiallyObservedSamplePicker(SamplePickerFactoryData factoryData) {
    NodeState[] partiallyObserved =
        factoryData.getNodeObservation().observedStates().stream()
            .sorted(Comparator.comparingInt(NodeState::getPosition))
            .toArray(NodeState[]::new);
    factoryData.setEventStates(partiallyObserved);

    int[] eventCptSteps = Arrays.stream(partiallyObserved).mapToInt(NodeState::getPosition).toArray();
    factoryData.setEventCptSteps(eventCptSteps);

    double[] sampleWeighting = new double[partiallyObserved.length];
    factoryData.setSampleWeighting(sampleWeighting);

    return null;
  }

  private SamplePicker buildNegatedSamplePicker(Node node) {
      return new NegatedSamplePicker(node);
  }
}
