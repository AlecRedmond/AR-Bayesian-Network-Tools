package io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory;

import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;

public class SeparatorFactorySolver extends SeparatorFactory {

  public SeparatorFactorySolver(GraphData graphData) {
    super(graphData);
  }

  @Override
  protected TableTransfer buildTableTransfer(Clique read, Clique write, Separator separator) {
    return iteratorFactory.buildMarginalTransfer(read.getTable(), write.getTable());
  }
}
