package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferWriterMultiplyIn;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;

public class TransferWriterMultiplyInFactory
    extends TransferReadWriteFactory<TransferWriterMultiplyIn> {
  protected TransferWriterMultiplyInFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    super(readTable, writeTable, transferArray);
  }

  @Override
  public TransferWriterMultiplyIn build() {
    return new TransferWriterMultiplyIn(
        StandardIteratorFactory.createFactoryData(this, writeTable.getVector()), transferArray);
  }
}
