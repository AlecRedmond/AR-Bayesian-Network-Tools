package io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory;

import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;

public class SeparatorFactorySolver extends AbstractSeparatorFactory {

  public SeparatorFactorySolver(GraphData graphData) {
    super(graphData);
  }

  @Override
  protected TableTransfer buildTableTransfer(
      JunctionTreeTable read, JunctionTreeTable write, JunctionTreeTable separator) {
    return iteratorFactory.buildMarginalTransfer(read, write);
  }
}
