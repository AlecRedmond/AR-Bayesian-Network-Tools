package io.github.alecredmond.internal.method.printer;

import io.github.alecredmond.exceptions.NetworkPrinterException;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.printer.PrinterPropertyConfigs;
import java.util.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkPrinter {
  private final BayesianNetwork network;
  private final PrinterPropertyConfigs configs;
  private final TableFormatter tableFormatter;
  private final PrinterFileExporter printerFileExporter;

  public NetworkPrinter(BayesianNetwork network) {
    this.network = network;
    this.configs = new PrinterPropertyConfigs();
    this.printerFileExporter = new PrinterFileExporter(configs);
    this.tableFormatter = new TableFormatter(configs);
  }

  public NetworkPrinter(InferenceEngine engine) {
    this.network = engine.getNetwork();
    this.configs = new PrinterPropertyConfigs();
    this.printerFileExporter = new PrinterFileExporter(configs);
    this.tableFormatter = new TableFormatter(configs);
  }

  public <T extends ProbabilityTable> void printTable(T table) {
    List<String> outputLines = new ArrayList<>(List.of("TABLE", ""));
    outputLines.addAll(tableFormatter.generateTableLines(table));
    printLines(outputLines, "TABLE");
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
    printTables(network.getNetworkTablesMap(), "NETWORK TABLES");
  }

  public <T extends ProbabilityTable> void printTables(
      Map<Node, T> associatedMap, String tableType) {
    List<String> outputLines = new ArrayList<>(List.of(tableType, ""));

    Arrays.stream(network.getNodes())
        .filter(associatedMap::containsKey)
        .map(associatedMap::get)
        .map(tableFormatter::generateTableLines)
        .forEach(outputLines::addAll);

    try {
      printLines(outputLines, tableType);
    } catch (NetworkPrinterException e) {
      log.error(e.getMessage());
    }
  }
}
