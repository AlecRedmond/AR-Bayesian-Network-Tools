package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class TransferWriterMultiplyIn extends TransferIterator implements TransferWriter {

  @NonNull
  public TransferWriterMultiplyIn(
      double[] probabilities, double[] transferArray, int[][] cachedIndexesPerCondition) {
    super(probabilities, transferArray, cachedIndexesPerCondition);
  }

  @Override
  public void writeTable() {
    int transferIndex = 0;
    for (int[] indexesToAdjust : cachedIndexesPerCondition) {
      double ratio = transferArray[transferIndex++];
      for (int index : indexesToAdjust) {
        probabilities[index] *= ratio;
      }
    }
  }
}
