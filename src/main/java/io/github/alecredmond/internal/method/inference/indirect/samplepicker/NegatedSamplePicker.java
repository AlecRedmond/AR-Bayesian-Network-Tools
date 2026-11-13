package io.github.alecredmond.internal.method.inference.indirect.samplepicker;

import io.github.alecredmond.exceptions.SampleValidationException;
import io.github.alecredmond.export.node.Node;

public class NegatedSamplePicker implements SamplePicker {
  private final Node node;

  public NegatedSamplePicker(Node node) {
    this.node = node;
  }

  @Override
  public double selectStateAndReturnWeight(int[] statePositions) {
    throw new SampleValidationException(
        "Attempted to sample a network with negated node : %s!".formatted(node));
  }
}
