package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import static io.github.alecredmond.internal.method.utils.DoublePrecision.fuzzyEquals;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMarginal extends TransferIterator implements TransferWriter {

  public TransferWriterMarginal(
      double[] probabilities, double[] transferArray, int[][] cachedIndexesPerCondition) {
    super(probabilities, transferArray, cachedIndexesPerCondition);
  }

  @Override
  public void writeTable() {
    int transferIndex = 0;
    for (int[] indexesToSum : cachedIndexesPerCondition) {
      double actual = 0;
      for (int index : indexesToSum) {
        actual += probabilities[index];
      }
      double expected = transferArray[transferIndex++];
      if (fuzzyEquals(expected, actual)) continue;
      double ratio = ratioOrZero(expected, actual);
      for (int index : indexesToSum) {
        probabilities[index] *= ratio;
      }
    }
  }
}
