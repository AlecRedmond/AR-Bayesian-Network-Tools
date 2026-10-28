package io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory;

import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;

public class SeparatorFactoryInference extends SeparatorFactory {

  public SeparatorFactoryInference(GraphData graphData) {
    super(graphData);
  }

  @Override
  protected TableTransfer buildTableTransfer(Clique read, Clique write, Separator separator) {
    return iteratorFactory.buildMessagePassTransfer(
        read.getTable(), write.getTable(), separator.getTable());
  }
}
