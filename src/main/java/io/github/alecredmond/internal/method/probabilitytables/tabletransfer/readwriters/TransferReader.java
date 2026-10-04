package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.internal.application.vectoriterator.IteratorFactoryData;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TransferReader extends VectorIterator implements TransferIterator {
  private final double[] transferArray;
  private final double[] adder = {0.0};
  private final int[] tIndex = {0};

  public TransferReader(IteratorFactoryData data, double[] transferArray) {
    super(data);
    this.transferArray = transferArray;
  }

  @Override
  public void performRun() {
    tIndex[0] = 0;
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        () -> {
          adder[0] = 0.0;
          iterateInner((o, i) -> adder[0] += probabilities[i]);
          transferArray[tIndex[0]] = adder[0];
          tIndex[0]++;
        });
  }
}
