package io.github.alecredmond.internal.method.cliquegraph.externalwriters;

import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.cliquegraph.Clique;
import io.github.alecredmond.internal.method.cliquegraph.Separator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExternalTableWriter<T extends GraphData>
    implements ExternalTableWriter {
  protected final T cliqueGraphData;

  protected AbstractExternalTableWriter(T cliqueGraphData) {
    this.cliqueGraphData = cliqueGraphData;
    initializeJunctionTreeFromNetwork();
  }

  @Override
  public void initializeJunctionTreeFromNetwork() {
    for (Clique clique : cliqueGraphData.getCliques()) {
      clique.writePotentialsFromNetwork();
    }
    for (Separator separator : cliqueGraphData.getSeparators()) {
      separator.resetPotentials();
    }
  }
}
