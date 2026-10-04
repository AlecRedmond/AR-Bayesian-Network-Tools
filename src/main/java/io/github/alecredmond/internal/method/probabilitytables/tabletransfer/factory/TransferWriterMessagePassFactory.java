package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferWriterMessagePass;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;

public class TransferWriterMessagePassFactory
    extends TransferReadWriteFactory<TransferWriterMessagePass> {
  private final ProbabilityVector separatorVector;

  protected TransferWriterMessagePassFactory(
      ProbabilityTable readTable,
      ProbabilityTable writeTable,
      ProbabilityTable separatorTable,
      double[] transferArray) {
    super(readTable, writeTable, transferArray);
    this.separatorVector = separatorTable.getVector();
  }

  @Override
  public TransferWriterMessagePass build() {
    double[] ratioArray = new double[transferArray.length];
    double[] separatorProbs = separatorVector.getProbabilities();
    return new TransferWriterMessagePass(
        StandardIteratorFactory.createFactoryData(this, writeTable.getVector()),
        transferArray,
        ratioArray,
        separatorProbs);
  }
}
