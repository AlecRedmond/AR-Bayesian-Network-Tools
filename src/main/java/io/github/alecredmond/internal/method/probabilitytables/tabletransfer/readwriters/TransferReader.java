package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TransferReader extends TransferIterator {

  public TransferReader(
      double[] probabilities, double[] transferArray, int[][] cachedIndexesPerCondition) {
    super(probabilities, transferArray, cachedIndexesPerCondition);
  }

  public void readTable() {
    int transferIndex = 0;
    for (int[] indexesToSum : cachedIndexesPerCondition) {
      double sum = 0;
      for (int index : indexesToSum) {
        sum += probabilities[index];
      }
      transferArray[transferIndex++] = sum;
    }
  }
}
