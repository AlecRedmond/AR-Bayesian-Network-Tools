package io.github.alecredmond.internal.method.cliquegraph.externalwriters;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.method.cliquegraph.Clique;
import io.github.alecredmond.internal.method.probabilitytables.ObservedTableImpl;
import java.util.*;

public class InferenceExternalWriter extends AbstractExternalTableWriter<GraphDataInference> {
  public InferenceExternalWriter(GraphDataInference cliqueGraphData) {
    super(cliqueGraphData);
  }

  @Override
  public void writeToExternal() {
    ObservedTable[] observedTables = cliqueGraphData.getObservedTables();

    for (ObservedTable observedTable : observedTables) {
      Arrays.fill(observedTable.getProbabilities(), 1.0);
    }

    for (Clique clique : cliqueGraphData.getCliques()) {
      clique.exportPotentialsToTables();
    }

    Map<Node, NodeObservation> observationMap = cliqueGraphData.getObservedEvidence();

    for (ObservedTable observedTable : observedTables) {
      observedTable.normalizeTable();
      if (!(observedTable instanceof ObservedTableImpl impl)) {
        throw new IllegalStateException("Unexpected instance :" + observedTable.getClass());
      }
      impl.setObservations(observationMap);
    }
  }
}
