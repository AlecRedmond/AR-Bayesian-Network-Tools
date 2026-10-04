package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import io.github.alecredmond.internal.application.vectoriterator.IteratorFactoryData;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.stream.IntStream;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMessagePass extends VectorIterator implements TransferIterator {
  private final double[] transferArray;
  private final double[] ratioArray;
  private final double[] separatorProbs;
  private final int[] tIndex = {0};

  public TransferWriterMessagePass(
      IteratorFactoryData data,
      double[] transferArray,
      double[] ratioArray,
      double[] separatorProbs) {
    super(data);
    this.transferArray = transferArray;
    this.ratioArray = ratioArray;
    this.separatorProbs = separatorProbs;
  }

  @Override
  public void performRun() {
    fillRatioArray();
    tIndex[0] = 0;
    double[] probabilities = controller.getOdometer().getProbabilities();
    iterateOuter(
        (() -> {
          double ratio = ratioArray[tIndex[0]];
          iterateInner((o, i) -> probabilities[i] *= ratio);
          tIndex[0]++;
        }));
    setNewSeparators();
  }

  private void fillRatioArray() {
    IntStream.range(0, separatorProbs.length)
        .forEach(i -> ratioArray[i] = ratioOrZero(transferArray[i], separatorProbs[i]));
  }

  private void setNewSeparators() {
    System.arraycopy(transferArray, 0, separatorProbs, 0, separatorProbs.length);
  }

  private double ratioOrZero(double numerator, double divisor) {
    return divisor == 0.0 ? 0.0 : numerator / divisor;
  }
}
