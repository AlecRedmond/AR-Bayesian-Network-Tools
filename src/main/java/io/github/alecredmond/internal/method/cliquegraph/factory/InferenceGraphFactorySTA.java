package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractInferenceGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactorySingleTable;

public class InferenceGraphFactorySTA extends AbstractInferenceGraphFactory {

  @Override
  protected void buildCliques(GraphDataInference cliqueGraphData) {
    new CliqueFactorySingleTable().buildCliques(cliqueGraphData);
  }
}
