package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractSolverGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactorySingleTable;

public class SolverGraphFactorySTA extends AbstractSolverGraphFactory {
  @Override
  protected CliqueFactory getCliqueFactory() {
    return new CliqueFactorySingleTable();
  }
}
