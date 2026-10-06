package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters;

public abstract class TransferIterator {
  protected final double[] probabilities;
  protected final double[] transferArray;
  protected final int[][] cachedIndexesPerCondition;

  protected TransferIterator(
      double[] probabilities, double[] transferArray, int[][] cachedIndexesPerCondition) {
    this.probabilities = probabilities;
    this.transferArray = transferArray;
    this.cachedIndexesPerCondition = cachedIndexesPerCondition;
  }

  protected final double ratioOrZero(double numerator, double divisor) {
      return divisor == 0.0 ? 0.0 : numerator / divisor;
  }
}
