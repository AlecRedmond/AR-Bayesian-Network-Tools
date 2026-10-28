package io.github.alecredmond.internal.method.cliquegraph.externalwriters;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.method.probabilitytables.ObservedTableImpl;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class InferenceExternalWriter extends AbstractExternalTableWriter<GraphDataInference> {
  public InferenceExternalWriter(GraphDataInference cliqueGraphData) {
    super(cliqueGraphData);
  }

  @Override
  public void writeToExternal() {
    Arrays.stream(cliqueGraphData.getCliques())
        .map(clique -> clique.getWriteToExternal())
        .flatMap(Collection::stream)
        .parallel()
        .forEach(TableTransfer::transfer);

    ObservedTable[] observedTables = cliqueGraphData.getObservedTables();

    Arrays.stream(observedTables).parallel().forEach(ProbabilityTable::normalizeTable);

    Map<Node, NodeObservation> observationMap =
        Collections.unmodifiableMap(cliqueGraphData.getObservedEvidence());

    Arrays.stream(observedTables)
        .filter(ObservedTableImpl.class::isInstance)
        .map(ObservedTableImpl.class::cast)
        .forEach(oti -> oti.setObservations(observationMap));
  }
}
