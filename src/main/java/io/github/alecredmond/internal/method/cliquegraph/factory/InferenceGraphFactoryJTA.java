package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractInferenceGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactoryJTAInference;

public class InferenceGraphFactoryJTA extends AbstractInferenceGraphFactory {

  @Override
  protected void buildCliques(GraphDataInference cliqueGraphData) {
    new CliqueFactoryJTAInference().buildCliques(cliqueGraphData);
  }
}
