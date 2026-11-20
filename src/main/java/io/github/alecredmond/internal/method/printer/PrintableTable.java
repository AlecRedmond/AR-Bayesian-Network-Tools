package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import io.github.alecredmond.internal.application.printer.TableCellMatrix;
import java.util.List;

public interface PrintableTable {
  List<String> generateLines(PrinterPropertyConfigs configs);

  TableCellMatrix buildCellMatrix(PrinterPropertyConfigs configs);
}
