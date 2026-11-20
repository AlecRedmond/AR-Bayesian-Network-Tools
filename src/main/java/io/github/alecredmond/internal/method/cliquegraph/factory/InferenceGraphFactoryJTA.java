package io.github.alecredmond.internal.method.cliquegraph.factory;

import io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory.AbstractInferenceGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory.CliqueFactoryJTAInference;

public class InferenceGraphFactoryJTA extends AbstractInferenceGraphFactory {

  @Override
  protected CliqueFactory getCliqueFactory() {
    return new CliqueFactoryJTAInference();
  }
}
