package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

public class NegatedSamplePicker implements SamplePicker {
  private final Node node;

  public NegatedSamplePicker(Node node) {
    this.node = node;
  }

  @Override
  public double selectStateAndReturnWeight(NodeState[] sampleArray, double currentWeight) {
    throw new SampleValidationException(
        "Attempted to sample a network with negated node : %s!".formatted(node));
  }
}
