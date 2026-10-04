package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.internal.application.vectoriterator.IteratorFactoryData;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMultiplyIn extends VectorIterator implements TransferIterator {
  private final double[] transferArray;
  private final int[] tIndex = {0};

  public TransferWriterMultiplyIn(IteratorFactoryData data, double[] transferArray) {
    super(data);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    tIndex[0] = 0;
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        () -> {
          double ratio = transferArray[tIndex[0]];
          iterateInner((o, i) -> probabilities[i] *= ratio);
          tIndex[0]++;
        });
  }
}
