package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractInferenceGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactorySingleTable;

public class InferenceGraphFactorySTA extends AbstractInferenceGraphFactory {

  @Override
  protected CliqueFactory getCliqueFactory() {
    return new CliqueFactorySingleTable();
  }
}
