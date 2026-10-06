package io.github.alecredmond.internal.method.probabilitytables.tabletransfer;

import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferReader;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferWriter;

public class TableTransfer {
  private final TransferReader reader;
  private final TransferWriter writer;

  public TableTransfer(TransferReader reader, TransferWriter writer) {
    this.reader = reader;
    this.writer = writer;
  }

  public void transfer() {
    reader.readTable();
    writer.writeTable();
  }
}
