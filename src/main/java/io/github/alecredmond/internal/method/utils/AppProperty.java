package io.github.alecredmond.internal.method.utils;

import lombok.Getter;

@Getter
public enum AppProperty {
  // Directory Properties
  DIRECTORY_ROOT("app.bayes.directory.rootDirectory"),
  DIRECTORY_PRINTER("app.bayes.directory.printerDirectory"),
  DIRECTORY_SAVE("app.bayes.directory.saveDirectory"),

  // Solver Properties
  SOLVER_CYCLES_LIMIT("app.bayes.solver.cyclesLimit"),
  SOLVER_TIME_LIMIT_SECONDS("app.bayes.solver.timeLimitSeconds"),
  SOLVER_LOG_PROGRESS("app.bayes.solver.logSolverProgress"),
  SOLVER_LOG_INTERVAL_SECONDS("app.bayes.solver.logIntervalSeconds"),
  SOLVER_CONVERGE_THRESHOLD("app.bayes.solver.convergeThreshold"),

  // Internal Properties
  INTERNAL_DOUBLE_EQUALITY("app.bayes.internal.doubleEqualityPrecision"),

  // Printer Properties
  PRINTER_OPEN_FILE_ON_CREATION("app.bayes.printer.openFileOnCreation"),
  PRINTER_OPEN_FOLDER_ON_CREATION("app.bayes.printer.openFolderOnCreation"),
  PRINTER_PRINT_TO_CONSOLE("app.bayes.printer.printToConsole"),
  PRINTER_PRINT_TO_TEXT_FILE("app.bayes.printer.printToTextFile"),
  PRINTER_PROB_DECIMAL_PLACES("app.bayes.printer.probDecimalPlaces"),

  // Extension Properties
  EXTENSION_FILE_TYPE("app.bayes.extension.fileExtension");

  private final String key;

  AppProperty(String key) {
    this.key = key;
  }
}
