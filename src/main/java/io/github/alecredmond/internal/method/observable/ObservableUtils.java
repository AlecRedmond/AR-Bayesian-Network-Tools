package io.github.alecredmond.internal.method.observable;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.observable.Observable;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObservableUtils {
  private ObservableUtils() {}

  public static <T extends Observable> List<NodeState> convertToEliminations(
      T observable, BayesianNetwork network, Map<Node, NodeObservation> newObservations) {
    observable.resetObservations();
    Set<Node> networkNodes = network.getNodes(LinkedHashSet::new);
    return newObservations.entrySet().stream()
        .filter(e -> networkNodes.contains(e.getKey()))
        .map(Map.Entry::getValue)
        .map(NodeObservation::eliminatedStates)
        .flatMap(Collection::stream)
        .toList();
  }
}
