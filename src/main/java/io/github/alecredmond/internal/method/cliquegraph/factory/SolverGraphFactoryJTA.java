package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractSolverGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactoryJTASolver;

public class SolverGraphFactoryJTA extends AbstractSolverGraphFactory {

  @Override
  protected CliqueFactory getCliqueFactory() {
    return new CliqueFactoryJTASolver();
  }
}
