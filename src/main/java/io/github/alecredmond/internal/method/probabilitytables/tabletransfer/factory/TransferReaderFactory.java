package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferReader;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;

public class TransferReaderFactory extends TransferReadWriteFactory<TransferReader> {
  protected TransferReaderFactory(ProbabilityTable readTable, ProbabilityTable writeTable) {
    super(readTable, writeTable);
  }

  @Override
  public TransferReader build() {
    return new TransferReader(
        StandardIteratorFactory.createFactoryData(this, readTable.getVector()), transferArray);
  }
}
