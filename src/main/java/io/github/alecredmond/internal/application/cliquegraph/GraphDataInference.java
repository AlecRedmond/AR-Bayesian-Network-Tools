package io.github.alecredmond.internal.application.cliquegraph;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GraphDataInference extends GraphData {
  protected ObservedTable[] observedTables;
  protected Map<Node, NodeObservation> observedEvidence;
  protected Map<Node, NodeObservation> unobservedBackup;
  protected double jointProbability;
}
