package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.application.cliquegraph.GraphDataSolver;
import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractSolverGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactorySingleTable;

public class SolverGraphFactorySTA extends AbstractSolverGraphFactory {
  @Override
  protected void buildCliques(GraphDataSolver cliqueGraphData) {
    new CliqueFactorySingleTable().buildCliques(cliqueGraphData);
  }
}
