package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMessagePass extends TransferIterator implements TransferWriter {
  private final double[] ratioArray;
  private final double[] separatorProbabilities;

  public TransferWriterMessagePass(
      double[] probabilities,
      double[] transferArray,
      int[][] cachedIndexesPerCondition,
      double[] ratioArray,
      double[] separatorProbabilities) {
    super(probabilities, transferArray, cachedIndexesPerCondition);
    this.ratioArray = ratioArray;
    this.separatorProbabilities = separatorProbabilities;
  }

  @Override
  public void writeTable() {
    final int bound = cachedIndexesPerCondition.length;
    for (int i = 0; i < bound; i++) {
      double ratio = ratioOrZero(transferArray[i], separatorProbabilities[i]);
      for (int index : cachedIndexesPerCondition[i]) {
        probabilities[index] *= ratio;
      }
      separatorProbabilities[i] = transferArray[i];
    }
  }
}
