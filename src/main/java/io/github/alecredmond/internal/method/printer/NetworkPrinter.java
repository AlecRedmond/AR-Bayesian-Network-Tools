package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import java.util.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPrinter {
  private final BayesianNetwork network;
  private final PrinterPropertyConfigs configs;
  private final PrinterFileExporter printerFileExporter;

  public NetworkPrinter(BayesianNetwork network) {
    this.network = network;
    this.configs = new PrinterPropertyConfigs();
    this.printerFileExporter = new PrinterFileExporter(configs);
  }

  public NetworkPrinter(InferenceEngine engine) {
    this.network = engine.getNetwork();
    this.configs = new PrinterPropertyConfigs();
    this.printerFileExporter = new PrinterFileExporter(configs);
  }

  public <T extends ProbabilityTable> void printTable(T table) {
    List<String> outputLines = new ArrayList<>(List.of("TABLE", ""));
    outputLines.addAll(generateTableLines(table));
    printLines(outputLines, "TABLE");
  }

  private <T extends ProbabilityTable> List<String> generateTableLines(T table) {
    if (table instanceof PrintableTable printableTable) {
      return printableTable.generateLines(configs);
    }
    throw new NetworkPrinterException("table " + table + " was not of a printable type!");
  }

  public void printLines(List<String> outputLines, String documentTitle) {
    if (configs.isPrintToConsole()) {
      outputLines.forEach(log::info);
    }
    if (configs.isPrintToTextFile()) {
      printerFileExporter.exportLinesToFile(outputLines, documentTitle, network.getNetworkName());
    }
  }

  public void printNetwork() {
    printTables(network.getNetworkTables(), "NETWORK TABLES");
  }

  public <T extends ProbabilityTable> void printTables(T[] tables, String tableType) {
    List<String> outputLines = new ArrayList<>(List.of(tableType, ""));
    Arrays.stream(tables).map(this::generateTableLines).forEach(outputLines::addAll);
    try {
      printLines(outputLines, tableType);
    } catch (NetworkPrinterException e) {
      log.error(e.getMessage());
    }
  }
}
